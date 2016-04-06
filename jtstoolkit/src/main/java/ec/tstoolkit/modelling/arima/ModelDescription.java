/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.DeterministicComponent;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.SarimaFixedMapping;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.*;
import ec.tstoolkit.timeseries.simplets.ITsDataTransformation.LogJacobian;
import ec.tstoolkit.timeseries.simplets.*;
import ec.tstoolkit.utilities.IntList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ModelDescription implements Cloneable {

    private final TsData original_;
    private final TsDomain estimationDomain_;
    private double[] y0_, y_;
    private int[] missings_;
    private SarimaComponent arima_ = new SarimaComponent();
    private List<Variable> users_ = new ArrayList<>();
    private List<Variable> calendars_ = new ArrayList<>();
    private List<Variable> holidays_ = new ArrayList<>();
    private List<IOutlierVariable> outliers_ = new ArrayList<>();
    private List<IOutlierVariable> poutliers_ = new ArrayList<>();
    private double units_ = 1;
    private PreadjustmentType adjust_ = PreadjustmentType.None;
    private volatile LengthOfPeriodType lp_ = LengthOfPeriodType.None;
    private volatile int diff_;
    private DefaultTransformationType function_ = DefaultTransformationType.None;
    private double logtransform0_, logtransform_;
    // caching of the regression variables
    private final HashMap<ITsVariable, DataBlock[]> xmap_
            = new HashMap<>();

    public ModelDescription(TsData originalTs, TsDomain eDomain) {
        this.original_ = originalTs;
        if (eDomain == null) {
            estimationDomain_ = original_.getDomain();
        } else {
            estimationDomain_ = original_.getDomain().intersection(eDomain);
        }
        y0_ = original_.fittoDomain(estimationDomain_).internalStorage();
    }

    @Override
    public ModelDescription clone() {
        try {
            ModelDescription model = (ModelDescription) super.clone();
            model.users_ = new ArrayList<>();
            for (Variable var : users_) {
                model.users_.add(var.clone());
            }
            model.calendars_ = new ArrayList<>();
            for (Variable var : calendars_) {
                model.calendars_.add(var.clone());
            }
            model.holidays_ = new ArrayList<>();
            for (Variable var : holidays_) {
                model.holidays_.add(var.clone());
            }
            model.poutliers_ = new ArrayList<>(poutliers_);
            model.outliers_ = new ArrayList<>(outliers_);
            model.arima_ = arima_.clone();
            return model;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    void invalidateData() {
        logtransform_ = 0;
        y_ = null;
        lp_ = LengthOfPeriodType.None;
    }

    // the regression variables are organized as follows:
    // [0. Mean correction]
    // 1. additive outliers_ for missing values
    // 2 users
    // 3 calendars
    // 4 moving holidays
    // 5 outliers, 5.1 pre-specified, 5.2 detected 
    private List<DataBlock> createX() {
        checkVariables();
        ArrayList<DataBlock> xdata = new ArrayList<>();
        // users...
        if (users_ != null) {
            for (Variable var : users_) {
                if (var.status.isSelected()) {
                    DataBlock[] cur = getX(var.getVariable());
                    Collections.addAll(xdata, cur);
                }
            }
        }
        // calendars...
        if (calendars_ != null) {
            for (Variable var : calendars_) {
                if (var.status.isSelected()) {
                    DataBlock[] cur = getX(var.getVariable());
                    Collections.addAll(xdata, cur);
                }
            }
        }
        // moving holidays...
        if (holidays_ != null) {
            for (Variable var : holidays_) {
                if (var.status.isSelected()) {
                    DataBlock[] cur = getX(var.getVariable());
                    Collections.addAll(xdata, cur);
                }
            }
        }

        if (poutliers_ != null) {
            for (IOutlierVariable var : poutliers_) {
                if (var.isSignificant(estimationDomain_)) {
                    DataBlock[] cur = getX(var);
                    Collections.addAll(xdata, cur);
                }
            }
        }

        if (outliers_ != null) {
            for (IOutlierVariable var : outliers_) {
                if (var.isSignificant(estimationDomain_)) {
                    DataBlock[] cur = getX(var);
                    Collections.addAll(xdata, cur);
                }
            }
        }
        return xdata;
    }

    public void checkVariables() {
        for (Variable var : users_) {
            if (var.status.isSelected() && !var.getVariable().isSignificant(estimationDomain_)) {
                var.status = RegStatus.Excluded;
            }
        }
        for (Variable var : calendars_) {
            if (var.status.isSelected() && !var.getVariable().isSignificant(estimationDomain_)) {
                var.status = RegStatus.Excluded;
            }
        }
        for (Variable var : holidays_) {
            if (var.status.isSelected() && !var.getVariable().isSignificant(estimationDomain_)) {
                var.status = RegStatus.Excluded;
            }
        }
    }

    public IParametricMapping<SarimaModel> defaultMapping() {
        if (arima_.getFixedParametersCount() == 0) {
            return new SarimaMapping(arima_.getSpecification(), false);
        } else {
            return new SarimaFixedMapping(arima_.getSpecification(), arima_.getParameters(), arima_.getFixedConstraints());
        }
    }

    /**
     * Build the regression variables list. The regression variables are organized as follows:
     * 1 users-defined variables (user variables - intervention variables - ramps)
     * 2 calendars
     * 3 moving holidays
     * 4 pre-specified outliers
     * 5 detected outliers
     * @return The variables list. May be empty
     */
    public TsVariableList buildRegressionVariables() {
        checkVariables();
        TsVariableList x = new TsVariableList();
        // users...
        for (Variable var : users_) {
            if (var.status.isSelected() && var.getVariable().isSignificant(estimationDomain_)) {
                x.add(var.getVariable());
            }
        }
        // calendars...
        for (Variable var : calendars_) {
            if (var.status.isSelected()) {
                x.add(var.getVariable());
            }
        }
        // moving holidays...
        for (Variable var : holidays_) {
            if (var.status.isSelected()) {
                x.add(var.getVariable());
            }
        }

        for (IOutlierVariable var : poutliers_) {
            if (var.isSignificant(estimationDomain_)) {
                x.add(var);

            }
        }
        for (IOutlierVariable var : outliers_) {
            if (var.isSignificant(estimationDomain_)) {
                x.add(var);

            }
        }
        return x;
    }

    public ComponentType getType(IOutlierVariable var) {
        return DeterministicComponent.getType(var);
    }

    public ComponentType getType(ITsVariable var) {
        // outliers
        if (var instanceof IOutlierVariable) {
            return getType((IOutlierVariable) var);
        }
        if (Variable.search(calendars_, var) != null) {
            return ComponentType.CalendarEffect;
        }
        if (Variable.search(holidays_, var) != null) {
            return ComponentType.CalendarEffect;
        }
        Variable user = Variable.search(users_, var);
        if (user != null) {
            return user.type;
        }

        return ComponentType.Undefined;

    }

    private DataBlock[] getX(ITsVariable variable) {
        DataBlock[] x = xmap_.get(variable);
        if (x != null) {
            return x;
        } else {
            int n = estimationDomain_.getLength();
            x = new DataBlock[variable.getDim()];
            ArrayList<DataBlock> tmp = new ArrayList<>();
            for (int i = 0; i < x.length; ++i) {
                x[i] = new DataBlock(n);
                tmp.add(x[i]);
            }
            variable.data(estimationDomain_, tmp);
            xmap_.put(variable, x);
            return x;
        }
    }

    public int getRegressionVariablesStartingPosition() {
        int start = 0;
        if (arima_.isMean()) {
            ++start;
        }
        if (missings_ != null) {
            start += missings_.length;
        }
        return start;
    }

    public int[] getRegressionVariablePositions(ComponentType type) {
        checkVariables();
        IntList x = new IntList();
        // users...
        int curpos = 0;
        for (Variable var : users_) {
            if (var.status.isSelected() && var.getVariable().isSignificant(estimationDomain_)) {
                int n = var.getVariable().getDim();
                if (var.type == type) {
                    for (int i = 0; i < n; ++i) {
                        x.add(curpos++);
                    }
                } else {
                    curpos += n;
                }
            }
        }
        // calendars...
        for (Variable var : calendars_) {
            if (var.status.isSelected()) {
                int n = var.getVariable().getDim();
                if (type == ComponentType.CalendarEffect) {
                    for (int i = 0; i < n; ++i) {
                        x.add(curpos++);
                    }
                } else {
                    curpos += n;
                }
            }
        }
        // moving holidays...
        for (Variable var : holidays_) {
            if (var.status.isSelected()) {
                int n = var.getVariable().getDim();
                if (type == ComponentType.CalendarEffect) {
                    for (int i = 0; i < n; ++i) {
                        x.add(curpos++);
                    }
                } else {
                    curpos += n;
                }
            }
        }

        for (IOutlierVariable var : poutliers_) {
            if (var.isSignificant(estimationDomain_)) {
                if (getType(var) == type) {
                    x.add(curpos);
                }
                ++curpos;
            }
        }
        for (IOutlierVariable var : outliers_) {
            if (var.isSignificant(estimationDomain_)) {
                if (getType(var) == type) {
                    x.add(curpos);
                }
                ++curpos;
            }
        }
        return x.toArray();
    }

    public RegArimaModel<SarimaModel> buildRegArima() {
        double[] y = getY();
        DataBlock ydata = new DataBlock(y);

        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>(arima_.getModel(), ydata);
        regarima.setMeanCorrection(arima_.isMean());
        regarima.setMissings(missings_);
        List<DataBlock> xdata = createX();
        for (DataBlock var : xdata) {
            regarima.addX(var);
        }
        return regarima;
    }

    /**
     * @return the original_
     */
    public TsData getOriginal() {
        return original_.clone();
    }

    /**
     * @return the estimationDomain_
     */
    public TsDomain getSeriesDomain() {
        return original_.getDomain();
    }

    /**
     * @return the estimationDomain_
     */
    public TsDomain getEstimationDomain() {
        return estimationDomain_;
    }

    public int getFrequency() {
        return estimationDomain_.getFrequency().intValue();
    }

    /**
     * @return the y_
     */
    public double[] getY() {
        if (!checkY()) {
            computeY();
        }
        return y_;
    }

    /**
     * Gets the transformed original series. The original may be transformed for leap year 
     * correction and for log-transformation. It contains missing values
     * @return 
     */
    public TsData transformedOriginal() {
        TsData tmp = original_.clone();
        if (lp_ != LengthOfPeriodType.None) {
            new LengthOfPeriodTransformation(lp_).transform(tmp, null);
        }
        if (function_ == DefaultTransformationType.Log) {
            new LogTransformation().transform(tmp, null);
        }
        return tmp;
    }

    /**
     * @return the missings
     */
    public int[] getMissingValues() {
        return missings_;
    }

    /**
     * @return the arima
     */
    public SarimaComponent getArimaComponent() {
        return arima_;
    }

    public SarimaSpecification getSpecification() {
        return arima_.getSpecification();
    }

    /**
     * @return the mean_
     */
    public boolean isMean() {
        return arima_.isMean();
    }

    /**
     * @return the users_
     */
    public List<Variable> getUserVariables() {
        return users_;
    }

    /**
     * @return the calendars_
     */
    public List<Variable> getCalendars() {
        return calendars_;
    }

    /**
     * @return the holidays
     */
    public List<Variable> getMovingHolidays() {
        return holidays_;
    }

    /**
     * @return the outliers
     */
    public List<IOutlierVariable> getOutliers() {
        return outliers_;
    }

    /**
     * @return the pre-specified outliers
     */
    public List<IOutlierVariable> getPrespecifiedOutliers() {
        return poutliers_;
    }

    public int[] getOutliersPosition(boolean prespecified) {
        List<IOutlierVariable> vars = prespecified ? poutliers_ : outliers_;
        if (vars.isEmpty()) {
            return null;
        }
        int[] pos = new int[vars.size()];
        TsPeriod start = estimationDomain_.getStart();
        for (int i = 0; i < pos.length; ++i) {
            pos[i] = vars.get(i).getPosition().minus(start);
        }
        return pos;
    }

    /**
     * @return the units_
     */
    public double getUnits() {
        return units_;
    }

    /**
     * @return the adjust_
     */
    public PreadjustmentType getPreadjustmentType() {
        return adjust_;
    }

    public LengthOfPeriodType getLengthOfPeriodType() {
        return adjust_.convert(Variable.isUsed(calendars_), function_ == DefaultTransformationType.Log);
    }

    /**
     * @return the log_
     */
    public DefaultTransformationType getTransformation() {
        return function_;
    }

    private void computeY() {
        TsData tmp = new TsData(this.estimationDomain_.getStart(), y0_, true);
        int len = y0_.length;
        diff_ = arima_.getDifferencingOrder();
        LogJacobian lj = new LogJacobian(diff_, len);
        lp_ = adjust_.convert(Variable.isUsed(calendars_), function_ == DefaultTransformationType.Log);
        if (lp_ != LengthOfPeriodType.None) {
            new LengthOfPeriodTransformation(lp_).transform(tmp, lj);
        }
        if (function_ == DefaultTransformationType.Log) {
            TsDataBlock lts = TsDataBlock.all(tmp);
            LogTransformation tlog = new LogTransformation();
            if (tlog.canTransform(tmp)) {
                tlog.transform(tmp, lj);
            } else {
                throw new TsException("Series contains values lower or equal to zero. Logs not allowed");
            }

        }

        logtransform_ = lj.value + logtransform0_;
        y_ = tmp.internalStorage();
    }

    public void setUnit(double unit) {
        if (units_ != unit) {
            TsData tmp = original_.fittoDomain(estimationDomain_);
            int len = tmp.getLength();
            int diff = arima_.getDifferencingOrder();
            LogJacobian lj = new LogJacobian(diff, len);
            ConstTransformation.unit(units_).transform(tmp, lj);
            logtransform0_ = lj.value;
            y0_ = tmp.internalStorage();
            invalidateData();
        }
    }

    public boolean updateMissing(ITsDataInterpolator interpolator) {
        if (missings_ != null) {
            return false;
        }
        double[] y = y0_.clone();
        TsData tmp = new TsData(estimationDomain_.getStart(), y, false);
        IntList missings = new IntList(tmp.getObsCount());
        if (!interpolator.interpolate(tmp, missings)) {
            return false;
        }
        if (missings.isEmpty()) {
            return true;
        }
        missings_ = new int[missings.size()];
        for (int i = 0; i < missings_.length; ++i) {
            missings_[i] = missings.get(i);
        }
        y0_ = y;
        invalidateData();
        return true;
    }

    public void setInterpolatedSeries(double[] y, int[] missing) {
        y0_ = y;
        missings_ = missing;
        invalidateData();
    }

    public void setTransformation(DefaultTransformationType fn,
            PreadjustmentType adjust) {
        function_ = fn;
        adjust_ = adjust;
        checkPreadjustment();
        invalidateData();
    }

    public void setTransformation(PreadjustmentType lengthOfPeriodType) {
        if (adjust_ != lengthOfPeriodType) {
            adjust_ = lengthOfPeriodType;
            invalidateData();
        }
    }

    public void setTransformation(DefaultTransformationType fn) {
        if (function_ != fn) {
            function_ = fn;
            checkPreadjustment();
            invalidateData();
        }
    }

    public void setArimaComponent(SarimaComponent arima) {
        arima_ = arima;

    }

    public void setMean(boolean mean) {
        arima_.setMean(mean);
    }

    public void setSpecification(SarimaSpecification spec) {
        arima_.setSpecification(spec);
    }

    public void setAirline(boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(getFrequency());
        spec.airline(seas);
        arima_.setSpecification(spec);
    }

    public boolean replace(ITsVariable oldVar, ITsVariable newVar) {
        if (Variable.replace(calendars_, oldVar, newVar)) {
            return true;
        } else if (Variable.replace(holidays_, oldVar, newVar)) {
            return true;
        }
        if (Variable.replace(users_, oldVar, newVar)) {
            return true;
        } else {
            return false;
        }
    }

    public void setOutliers(List<IOutlierVariable> outliers) {
        outliers_.clear();
        if (outliers != null) {
            for (IOutlierVariable xvar : outliers) {
                xvar.setPrespecified(false);
            }
            outliers_.addAll(outliers);
        }
    }

    public void addOutliers(List<IOutlierVariable> outliers) {
        if (outliers == null || outliers.isEmpty()) {
            return;
        }
        for (IOutlierVariable xvar : outliers) {
            xvar.setPrespecified(false);
        }
        outliers_.addAll(outliers);
    }

    public void setPrespecifiedOutliers(List<IOutlierVariable> outliers) {
        poutliers_.clear();
        if (outliers != null) {
            for (IOutlierVariable xvar : outliers) {
                xvar.setPrespecified(true);
            }
            poutliers_.addAll(outliers);
        }
    }

    public void addPrespecifiedOutliers(List<IOutlierVariable> outliers) {
        if (outliers == null || outliers.isEmpty()) {
            return;
        }
        for (IOutlierVariable xvar : outliers) {
            xvar.setPrespecified(true);
        }
        poutliers_.addAll(outliers);
    }

    public DataBlock getOlsResiduals() {
        RegModel dmodel = buildRegArima().getDModel();
        DataBlock yc = dmodel.getY();
        if (dmodel.getVarsCount() > 0) {
            Ols ols = new Ols();
            if (ols.process(dmodel)) {
                yc = dmodel.calcRes(new ReadDataBlock(ols.getLikelihood().getB()));
            }
        }
        return yc;
    }

    public double getLikelihoodCorrection() {
        if (!checkY()) {
            computeY();
        }
        return logtransform0_ + logtransform_;
    }

    public List<ITsDataTransformation> transformations() {
        if (!checkY()) {
            computeY();
        }
        ArrayList<ITsDataTransformation> tr = new ArrayList<>();

        if (units_ != 1) {
            tr.add(ConstTransformation.unit(units_));
        }
        if (lp_ != LengthOfPeriodType.None) {
            tr.add(new LengthOfPeriodTransformation(lp_));
        }
        if (function_ == DefaultTransformationType.Log) {
            tr.add(new LogTransformation());
        }
        return tr;
    }

    /**
     * Back transformation
     *
     * @param T The series that will be back transformed contains the trend
     * component
     * @param S The series that will be back transformed contains the seasonal
     * component
     * @return The list of the transformation
     */
    public List<ITsDataTransformation> backTransformations(boolean T, boolean S) {
        if (!checkY()) {
            computeY();
        }
        ArrayList<ITsDataTransformation> tr = new ArrayList<>();

        if (function_ == DefaultTransformationType.Log) {
            tr.add(new ExpTransformation());
        }
        if (S && lp_ != LengthOfPeriodType.None) {
            tr.add(new LengthOfPeriodTransformation(lp_).converse());
        }
        if (units_ != 1 && (function_ == DefaultTransformationType.Log || T)) {
            tr.add(ConstTransformation.unit(1 / units_));
        }
        return tr;
    }

    private void checkPreadjustment() {
        if (adjust_ == PreadjustmentType.Auto && function_ == DefaultTransformationType.Log) {
            Variable.setStatus(calendars_, ILengthOfPeriodVariable.class, RegStatus.Excluded);
        }
    }

    private boolean checkY() {
        if (y_ == null) {
            return false;
        }
        if (lp_ != this.getLengthOfPeriodType()) {
            this.invalidateData();
            return false;
        }
        if (diff_ != this.arima_.getDifferencingOrder()) {
            this.invalidateData();
            return false;
        }
        return true;
    }

    public boolean isFullySpecified() {
        if (!this.arima_.isDefined()) {
            return false;
        }
        return isRegressionDefined();
    }

    public boolean isPartiallySpecified() {
        if (!isRegressionDefined()) {
            return false;
        }
        return !arima_.isUndefined();
    }

    public boolean isRegressionPrespecified() {
        if (this.function_ == DefaultTransformationType.Auto) {
            return false;
        }
        return !Variable.needTesting(calendars_)
                && !Variable.needTesting(users_)
                && !Variable.needTesting(holidays_);
    }

    public boolean isRegressionDefined() {
        if (this.function_ == DefaultTransformationType.Auto) {
            return false;
        }
        return Variable.isUsageDefined(calendars_)
                && Variable.isUsageDefined(users_)
                && Variable.isUsageDefined(holidays_);
    }
}
