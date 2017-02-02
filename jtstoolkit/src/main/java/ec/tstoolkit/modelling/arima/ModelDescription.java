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

import ec.tstoolkit.OperationType;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.LogSign;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.DeterministicComponent;
import ec.tstoolkit.modelling.PreadjustmentVariable;
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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private List<PreadjustmentVariable> preadjustment = new ArrayList<>();
    private List<Variable> variables = new ArrayList<>();
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
            model.preadjustment = new ArrayList<>();
            for (PreadjustmentVariable var : preadjustment) {
                model.preadjustment.add(var);
            }
            model.variables = new ArrayList<>();
            for (Variable var : variables) {
                model.variables.add(var.clone());
            }
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
        variables.stream().filter(var
                -> var.isUser() && var.status.isSelected())
                .forEach(
                        var -> {
                            DataBlock[] cur = getX(var.getVariable());
                            Collections.addAll(xdata, cur);
                        });
        // calendars
        variables.stream().filter(var
                -> var.isCalendar() && var.status.isSelected())
                .forEach(
                        var -> {
                            DataBlock[] cur = getX(var.getVariable());
                            Collections.addAll(xdata, cur);
                        });
        // moving holidays
        variables.stream().filter(var
                -> var.isMovingHoliday() && var.status.isSelected())
                .forEach(
                        var -> {
                            DataBlock[] cur = getX(var.getVariable());
                            Collections.addAll(xdata, cur);
                        });
        // prespecified outliers
        variables.stream().filter(var
                -> var.isOutlier() && var.status == RegStatus.Prespecified)
                .forEach(
                        var -> {
                            DataBlock[] cur = getX(var.getVariable());
                            Collections.addAll(xdata, cur);
                        });
        // other outliers
        variables.stream().filter(var
                -> var.isOutlier() && var.status != RegStatus.Prespecified)
                .forEach(
                        var -> {
                            DataBlock[] cur = getX(var.getVariable());
                            Collections.addAll(xdata, cur);
                        });
        return xdata;
    }

    public void checkVariables() {
        for (Variable var : variables) {
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

    public List<Variable> getOrderedVariables() {
        checkVariables();
        List<Variable> x = new ArrayList<>();
        // users
        variables.stream().filter(var
                -> var.isUser() && var.status.isSelected())
                .forEach(var -> x.add(var));
        // calendars
        variables.stream().filter(var
                -> var.isCalendar() && var.status.isSelected())
                .forEach(var -> x.add(var));
        // moving holidays
        variables.stream().filter(var
                -> var.isMovingHoliday() && var.status.isSelected())
                .forEach(var -> x.add(var));
        // prespecified outliers
        variables.stream().filter(var
                -> var.isOutlier() && var.status == RegStatus.Prespecified)
                .forEach(var -> x.add(var));
        // other outliers
        variables.stream().filter(var
                -> var.isOutlier() && var.status != RegStatus.Prespecified)
                .forEach(var -> x.add(var));
        return x;
    }

    /**
     * Build the regression variables list. The regression variables should be
     * organized as follows: 1 users-defined variables (user variables -
     * intervention variables - ramps), 2 calendars, 3 moving holidays, 4
     * pre-specified outliers, 5 detected outliers
     *
     * @return The variables list. May be empty
     */
    public TsVariableList buildRegressionVariables() {
        TsVariableList x = new TsVariableList();
        List<Variable> vars = getOrderedVariables();
        for (Variable var : vars) {
            x.add(var.getVariable());
        }
        return x;
    }

    public TsVariableSelection buildRegressionVariables(Predicate<Variable> pred) {
        checkVariables();
        TsVariableSelection x = new TsVariableSelection();
        int cur = 0;
        // users
        for (Variable var : variables) {
            if (var.isUser() && var.status.isSelected()) {
                if (pred.test(var)) {
                    x.add(var.getVariable(), cur);
                }
                cur += var.getVariable().getDim();
            }
        }
        // calendars
        for (Variable var : variables) {
            if (var.isCalendar() && var.status.isSelected()) {
                if (pred.test(var)) {
                    x.add(var.getVariable(), cur);
                }
                cur += var.getVariable().getDim();
            }
        }
        // moving holidays
        for (Variable var : variables) {
            if (var.isMovingHoliday() && var.status.isSelected()) {
                if (pred.test(var)) {
                    x.add(var.getVariable(), cur);
                }
                cur += var.getVariable().getDim();
            }
        }
        // prespecified outliers
        for (Variable var : variables) {
            if (var.isOutlier() && var.status == RegStatus.Prespecified && var.status.isSelected()) {
                if (pred.test(var)) {
                    x.add(var.getVariable(), cur);
                }
                cur += var.getVariable().getDim();
            }
        }
        // other outliers
        for (Variable var : variables) {
            if (var.isOutlier() && var.status != RegStatus.Prespecified && var.status.isSelected()) {
                if (pred.test(var)) {
                    x.add(var.getVariable(), cur);
                }
                cur += var.getVariable().getDim();
            }
        }
        return x;
    }

    public boolean isPrespecified(final IOutlierVariable ovar) {
        Variable var = searchVariable(ovar);
        return var == null ? false : var.status == RegStatus.Prespecified;
    }

    public ComponentType getType(ITsVariable tsvar) {
        // outliers
        Variable var = searchVariable(tsvar);
        return var == null ? ComponentType.Undefined : var.type;
    }

    public Variable searchVariable(ITsVariable tsvar) {
        Optional<Variable> found = variables.stream().filter(var -> var.getVariable() == tsvar).findAny();
        return found.isPresent() ? found.get() : null;
    }

    public PreadjustmentVariable searchPreadjustmentVariable(ITsVariable tsvar) {
        Optional<PreadjustmentVariable> found = preadjustment.stream().filter(var -> var.getVariable() == tsvar).findAny();
        return found.isPresent() ? found.get() : null;
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
        if (arima_.isEstimatedMean()) {
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
        for (Variable var : variables) {
            if (var.status.isSelected()) {
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
        return x.toArray();
    }

    public <S extends ITsVariable> int[] getRegressionVariablePositions(List<S> slist) {
        checkVariables();
        IntList x = new IntList();
        // users...
        for (S var : slist) {
            int pos = getRegressionVariablePosition(var);
            int n = var.getDim();
            if (pos >= 0) {
                for (int i = 0; i < n; ++i) {
                    x.add(pos++);
                }
            } else {
                for (int i = 0; i < n; ++i) {
                    x.add(-1);
                }
            }
        }
        return x.toArray();
    }

    public <S extends ITsVariable> int getRegressionVariablePosition(S s) {
        checkVariables();
        // users...
        int curpos = 0;
        for (Variable var : variables) {
            if (var.status.isSelected()) {
                if (s == var.getVariable()) {
                    return curpos;
                } else {
                    int n = var.getVariable().getDim();
                    curpos += n;
                }
            }
        }
        return -1;
    }

    public RegArimaModel<SarimaModel> buildRegArima() {
        double[] y = getY();
        DataBlock ydata = new DataBlock(y);

        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>(arima_.getModel(), ydata);
        if (arima_.isEstimatedMean()) {
            regarima.setMeanCorrection(true);
        } else if (arima_.isMean()) {
            regarima.setMeanCorrection(arima_.getMu().getValue());
        }
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
     * Gets the transformed original series. The original may be transformed for
     * leap year correction or log-transformation and for fixed effects. The fixed
     * effects are always applied additively after the log-transformation. The
     * transformed original may contain missing values
     *
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
        tmp.applyOnFinite(PreadjustmentVariable.regressionEffect(preadjustment.stream(), tmp.getDomain()), (x, y) -> x - y);
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
     * @return the mean_
     */
    public boolean isEstimatedMean() {
        return arima_.isEstimatedMean();
    }

    /**
     * @return the variables
     */
    public Stream<PreadjustmentVariable> preadjustmentVariables() {
        return preadjustment.stream();
    }

    public boolean hasFixedEffects() {
        return !preadjustment.isEmpty();
    }

    public Stream<Variable> variables() {
        return variables.stream();
    }

    /**
     * @return the variables
     */
    public List<Variable> getUserVariables() {
        return variables.stream()
                .filter(var -> var.getVariable() instanceof IUserTsVariable)
                .collect(Collectors.toList());
    }

    /**
     * @return the calendars_
     */
    public List<Variable> getCalendars() {
        return selectVariables(var -> var instanceof ICalendarVariable);
    }

    /**
     * all the variables
     *
     * @return
     */
    protected List<Variable> getVariables() {
        return variables;
    }

    /**
     * all the pre-adjustment variables
     *
     * @return
     */
    protected List<PreadjustmentVariable> getPreadjustmentVariables() {
        return preadjustment;
    }

    public List<Variable> getMovingHolidays() {
        return selectVariables(var -> var instanceof IMovingHolidayVariable);
    }

    public List<Variable> selectVariables(Predicate<Variable> pred) {
        return variables.stream()
                .filter(pred)
                .collect(Collectors.toList());
    }

    public List<PreadjustmentVariable> selectPreadjustmentVariables(Predicate<PreadjustmentVariable> pred) {
        return preadjustment.stream()
                .filter(pred)
                .collect(Collectors.toList());
    }

    public boolean contains(Predicate<Variable> pred) {
        return variables.stream().anyMatch(pred);
    }

    public int countVariables(Predicate<Variable> pred) {
        return (int) variables.stream()
                .filter(pred)
                .count();
    }

    public int countRegressors(Predicate<Variable> pred) {
        return variables.stream()
                .filter(pred)
                .mapToInt(var -> var.getVariable().getDim()).sum();
    }

    /**
     * @return the outliers
     */
    public List<IOutlierVariable> getOutliers() {
        return variables.stream()
                .filter(var -> var.getVariable() instanceof IOutlierVariable && var.status != RegStatus.Prespecified)
                .map(var -> (IOutlierVariable) var.getVariable())
                .collect(Collectors.toList());
    }

    /**
     * @return the pre-specified outliers
     */
    public List<IOutlierVariable> getPrespecifiedOutliers() {
        return variables.stream()
                .filter(var -> var.getVariable() instanceof IOutlierVariable && var.status == RegStatus.Prespecified)
                .map(var -> (IOutlierVariable) var.getVariable())
                .collect(Collectors.toList());
    }

    /**
     * @return the pre-specified outliers
     */
    public List<IOutlierVariable> getFixedOutliers() {
        return preadjustment.stream()
                .filter(var -> var.isOutlier() )
                .map(var -> (IOutlierVariable) var.getVariable())
                .collect(Collectors.toList());
    }
    
    public int[] getOutliersPosition(boolean prespecified) {
        List<IOutlierVariable> vars = prespecified ? getPrespecifiedOutliers() : getOutliers();

        int[] pos = new int[vars.size()];
        TsPeriod start = estimationDomain_.getStart();
        for (int i = 0; i < pos.length; ++i) {
            TsPeriod ostart = new TsPeriod(estimationDomain_.getFrequency(), vars.get(i).getPosition());
            pos[i] = ostart.minus(start);
        }
        return pos;
    }

    public int[] getFixedOutliersPosition() {
        List<IOutlierVariable> vars = getFixedOutliers();

        int[] pos = new int[vars.size()];
        TsPeriod start = estimationDomain_.getStart();
        for (int i = 0; i < pos.length; ++i) {
            TsPeriod ostart = new TsPeriod(estimationDomain_.getFrequency(), vars.get(i).getPosition());
            pos[i] = ostart.minus(start);
        }
        return pos;
    }

    public <T extends ITsVariable> boolean isUsed(Class<T> tclass) {
        return variables.stream().anyMatch(var -> tclass.isInstance(var.getVariable()) && var.status.isSelected());
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
        return adjust_.convert(isUsed(ICalendarVariable.class), function_ == DefaultTransformationType.Log);
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
        lp_ = adjust_.convert(isUsed(ICalendarVariable.class), function_ == DefaultTransformationType.Log);
        if (lp_ != LengthOfPeriodType.None) {
            new LengthOfPeriodTransformation(lp_).transform(tmp, lj);
        }
        if (function_ == DefaultTransformationType.Log) {
            LogTransformation tlog = new LogTransformation();
            if (tlog.canTransform(tmp)) {
                tlog.transform(tmp, lj);
            } else {
                throw new TsException("Series contains values lower or equal to zero. Logs not allowed");
            }
        }
        if (!preadjustment.isEmpty()) {
            DataBlock all = PreadjustmentVariable.regressionEffect(preadjustment.stream(), estimationDomain_);
            tmp.apply(all, (x, y) -> x - y);
            // we don't need to modify the adjustment factor, which is computed on the initial figures
            // TODO: check for missing values
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

    @Deprecated
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

    public void setPreadjustments(List<PreadjustmentVariable> var) {
        preadjustment.clear();
        preadjustment.addAll(var);
        invalidateData();
    }

    public void setVariables(List<Variable> var) {
        variables.clear();
        variables.addAll(var);
        invalidateData();
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

//    public boolean replace(ITsVariable oldVar, ITsVariable newVar) {
//        return Variable.replace(variables, oldVar, newVar);
//    }
    public void setOutliers(List<IOutlierVariable> outliers) {
        variables.removeIf(var -> var.isOutlier() && var.status != RegStatus.Prespecified);
        if (outliers != null) {
            for (IOutlierVariable o : outliers) {
                variables.add(Variable.outlier(o));
            }
        }
    }

    public void addOutliers(List<IOutlierVariable> outliers) {
        if (outliers != null) {
            for (IOutlierVariable o : outliers) {
                variables.add(Variable.outlier(o));
            }
        }
    }

    public void setPrespecifiedOutliers(List<IOutlierVariable> outliers) {
        variables.removeIf(var -> var.isOutlier() && var.status == RegStatus.Prespecified);
        if (outliers != null) {
            for (IOutlierVariable o : outliers) {
                variables.add(Variable.prespecifiedOutlier(o));
            }
        }
    }

    public void addPrespecifiedOutliers(List<IOutlierVariable> outliers) {
        if (outliers != null) {
            for (IOutlierVariable o : outliers) {
                variables.add(Variable.prespecifiedOutlier(o));
            }
        }
    }

    public void addVariable(Variable... var) {
        for (Variable v : var) {
            variables.add(v);
        }
    }

    public void addPreadjustment(PreadjustmentVariable... var) {
        for (PreadjustmentVariable v : var) {
            preadjustment.add(v);
        }
    }

    public void removeVariable(Predicate<Variable> pred) {
        variables.removeIf(pred);
    }

    public void removePreadjustment(Predicate<PreadjustmentVariable> pred) {
        preadjustment.removeIf(pred);
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
            variables.stream().filter(var -> var.getVariable() instanceof ILengthOfPeriodVariable).forEach(var -> var.status = RegStatus.Rejected);
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
        return !Variable.needTesting(variables);
    }

    public boolean isRegressionDefined() {
        if (this.function_ == DefaultTransformationType.Auto) {
            return false;
        }
        return Variable.isUsageDefined(variables);
    }
}
