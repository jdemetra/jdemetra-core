/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.regsarima.regular;

import demetra.timeseries.regression.Variable;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import jdplus.data.transformation.LogJacobian;
import demetra.data.ParameterType;
import jdplus.data.interpolation.DataInterpolator;
import demetra.design.Development;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.matrices.Matrix;
import demetra.timeseries.regression.PreadjustmentVariable;
import demetra.timeseries.regression.ITsVariable;
import jdplus.modelling.regression.Regression;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regarima.ami.TransformedSeries;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.LengthOfPeriodType;
import jdplus.timeseries.simplets.Transformations;
import jdplus.timeseries.simplets.TsDataTransformation;
import demetra.util.IntList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;
import demetra.timeseries.TsException;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class ModelDescription {

    /**
     * Original series
     */
    private final TsData series;

    private final TsDomain estimationDomain;
    /**
     * Interpolated data (before transformation) and transformed data. Their
     * domain correspond to the domain of the series
     */
    private double[] interpolatedData, transformedData;
    private double llCorrection;
    /**
     * Position in the original series of the missing values (if interpolated)
     */
    private int[] missing;

    private boolean logTransformation;
    private LengthOfPeriodType lpTransformation = LengthOfPeriodType.None;

    /**
     * Preadjustment variables (with their coefficients
     */
    private final List<PreadjustmentVariable> preadjustmentVariables = new ArrayList<>();

    private boolean mean;
    /**
     * Regression variables
     */
    private final List<Variable> variables = new ArrayList<>();

    /**
     * Arima component
     */
    private final SarimaComponent arima = new SarimaComponent();

    // Caching
    private ITsVariable[] regressionVariables;

    public static ModelDescription dummyModel() {
        return new ModelDescription();
    }

    public static ModelDescription copyOf(@NonNull ModelDescription model) {
        return copyOf(model, null);
    }
    
    public static ModelDescription copyOf(@NonNull ModelDescription model, TsDomain estimationDomain) {
        ModelDescription nmodel = new ModelDescription(model.series, estimationDomain);
        nmodel.arima.copy(model.arima);
        nmodel.mean = model.mean;
        nmodel.logTransformation = model.logTransformation;
        nmodel.lpTransformation = model.lpTransformation;
        nmodel.interpolatedData = model.interpolatedData;
        nmodel.transformedData = model.transformedData;
        nmodel.missing=model.missing;
        nmodel.llCorrection = model.llCorrection;
        nmodel.regressionVariables = model.regressionVariables;
        model.preadjustmentVariables.forEach(nmodel.preadjustmentVariables::add);
        model.variables.forEach(nmodel.variables::add);
        return nmodel;
    }

    private ModelDescription() {
        this.series = null;
        this.estimationDomain = null;
    }

    /**
     * Creates a new Model Description The series should not contain missing
     * values outside the estimation domain when it is specified
     *
     * @param series The given series
     * @param estimationDomain Estimation domain. Can be null. In that case, the
     * estimation will be performed on the whole series
     */
    public ModelDescription(@NonNull TsData series, TsDomain estimationDomain) {
        this.series = series;
        if (estimationDomain != null) {
            estimationDomain = estimationDomain.intersection(series.getDomain());
            // check possible missing values
            int beg = series.getStart().until(estimationDomain.getStartPeriod());
            if (series.getValues().range(0, beg).anyMatch(z -> !Double.isFinite(z))) {
                throw new TsException("Missing values outside the estimation domain");
            }
            int end = series.getStart().until(estimationDomain.getEndPeriod());
            if (series.getValues().range(end, series.length()).anyMatch(z -> !Double.isFinite(z))) {
                throw new TsException("Missing values outside the estimation domain");
            }
            this.estimationDomain = estimationDomain;
        } else {
            this.estimationDomain = null;
        }
    }

    // the regression variables are organized as follows:
    // [0. additive outliers_ for missing values]
    // [1. Mean correction]
    // 2 users
    // 3 calendars
    // 4 moving holidays
    // 5 outliers, 5.1 pre-specified, 5.2 detected 
    private void buildRegressionVariables() {
        if (regressionVariables == null) {
            List<ITsVariable> vars = new ArrayList<>();
            variables.stream().filter(v -> v.isUser()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isCalendar()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isMovingHolidays()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isOutlier(true)).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isOutlier(false)).forEachOrdered(v -> vars.add(v.getVariable()));
            regressionVariables = vars.toArray(new ITsVariable[vars.size()]);
        }
    }

    private void buildTransformation() {
        if (transformedData == null) {
            int diff = arima.getDifferencingOrder();
            LogJacobian lj;
            TsData tmp;
            lj = new LogJacobian(diff, series.length(), missing);
            tmp = interpolatedData == null ? series : TsData.ofInternal(series.getStart(), interpolatedData);
            if (logTransformation) {
                if (lpTransformation != LengthOfPeriodType.None) {
                    tmp = Transformations.lengthOfPeriod(lpTransformation).transform(tmp, lj);
                }
                tmp = Transformations.log().transform(tmp, lj);
            }
            llCorrection = lj.value;
            if (!preadjustmentVariables.isEmpty()) {
                final DataBlock ndata = DataBlock.of(tmp.getValues());
                final TsDomain domain = tmp.getDomain();
                preadjustmentVariables.forEach(v -> {
                    Matrix m = Regression.matrix(domain, v.getVariable());
                    DoubleSeqCursor reader = v.getCoefficients().cursor();
                    DataBlockIterator columns = m.columnsIterator();
                    while (columns.hasNext()) {
                        ndata.addAY(reader.getAndNext(), columns.next());
                    }
                });
                tmp = TsData.ofInternal(domain.getStartPeriod(), ndata.getStorage());
            }
            transformedData = tmp.getValues().toArray();
        }
    }

    /**
     * Gets the estimation domain of the model
     *
     * @return
     */
    public TsDomain getEstimationDomain() {
        return estimationDomain == null ? series.getDomain() : estimationDomain;
    }
    
    public int[] getMissingInEstimationDomain(){
        if (estimationDomain == null || missing == null || missing.length == 0)
            return missing;
        int start=series.getStart().until(estimationDomain.getStartPeriod());
        if (start == 0)
            return missing;
        int[] nmissing=missing.clone();
        for (int i=0; i<nmissing.length; ++i)
            nmissing[i]-=start;
        return nmissing;
    }

    /**
     * Gets the regarimamodel corresponding to the estimation domain
     *
     * @return
     */
    public RegArimaModel<SarimaModel> regarima() {
        buildTransformation();
        buildRegressionVariables();
        TsDomain domain = getEstimationDomain();
        double[] y = transformedData;
        DoubleSeq yc;
        int n = domain.getLength();
        int[] missingc = missing;
        if (y.length > n) {
            int pos = series.getStart().until(domain.getStartPeriod());
            yc = DoubleSeq.of(y, pos, n);
            if (missing != null && missing.length > 0) {
                missingc = missing.clone();
                for (int i = 0; i < missingc.length; ++i) {
                    missingc[i] -= pos;
                }
            }
        } else {
            yc = DoubleSeq.of(y);
        }
        RegArimaModel.Builder builder = RegArimaModel.<SarimaModel>builder()
                .y(yc)
                .missing(missingc)
                .meanCorrection(mean)
                .arima(arima.getModel());
        for (ITsVariable v : regressionVariables) {
            builder.addX(getX(v, domain));
        }
        return builder.build();
    }

    private void invalidateRegarima() {
        this.regressionVariables = null;
    }

    private void invalidateTransformation() {
        this.transformedData = null;
        this.llCorrection = 0;
    }

    public ITsVariable[] regressionVariables() {
        buildRegressionVariables();
        return regressionVariables;
    }

    public Variable variable(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getName().equals(name))
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public Variable variable(ITsVariable v) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getVariable() == v)
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public boolean remove(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getName().equals(name))
                .findFirst();
        if (search.isPresent()) {
            variables.remove(search.get());
            invalidateRegarima();
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(ITsVariable v) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getVariable() == v)
                .findFirst();
        if (search.isPresent()) {
            variables.remove(search.get());
            invalidateRegarima();
            return true;
        } else {
            return false;
        }
    }

    public PreadjustmentVariable preadjustmentVariable(String name) {
        Optional<PreadjustmentVariable> search = preadjustmentVariables.stream()
                .filter(var -> var.getName().equals(name))
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public Variable addVariable(Variable var) {
        String name = var.getName();
        while (contains(name)) {
            name = ITsVariable.nextName(name);
        }
        Variable nvar = var.rename(name);
        variables.add(nvar);
        invalidateRegarima();
        return nvar;
    }

    public PreadjustmentVariable addPreadjustmentVariable(PreadjustmentVariable var) {
        String name = var.getName();
        while (contains(name)) {
            name = ITsVariable.nextName(name);
        }
        PreadjustmentVariable nvar = var.rename(name);
        preadjustmentVariables.add(nvar);
        invalidateTransformation();
        return nvar;
    }

    public boolean contains(String name) {
        return variables.stream()
                .anyMatch(var -> var.getName().equals(name))
                || preadjustmentVariables.stream()
                        .anyMatch(var -> var.getVariable().equals(name));
    }

    public void setLogTransformation(boolean log) {
        if (this.logTransformation == log) {
            return;
        }
        this.logTransformation = log;
        invalidateTransformation();
    }

    public boolean isLogTransformation() {
        return this.logTransformation;
    }

    public boolean isAdjusted() {
        return logTransformation && lpTransformation != LengthOfPeriodType.None;
    }

    private Matrix getX(ITsVariable variable, TsDomain domain) {
        return Regression.matrix(domain, variable);
    }

    /**
     * @return the original_
     */
    public TsData getSeries() {
        return series;
    }

    /**
     * @return the estimationDomain_
     */
    public TsDomain getDomain() {
        return series.getDomain();
    }

    /**
     * Gets the transformed original series. The original may be transformed for
     * leap year correction or log-transformation and for fixed effects. The
     * fixed effects are always applied additively after the log-transformation.
     * The transformed original may also be corrected for missing values when an
     * interpolator is provided
     *
     * @return
     */
    public TsData getTransformedSeries() {
        buildTransformation();
        return TsData.ofInternal(series.getStart(), transformedData);
    }

    public TsData getInterpolatedSeries() {
        if (interpolatedData == null) {
            return series;
        } else {
            return TsData.ofInternal(series.getStart(), interpolatedData);
        }
    }

    /**
     * @return the arima
     */
    public SarimaComponent getArimaComponent() {
        return arima;
    }

    public SarimaOrders specification() {
        return arima.specification();
    }

    public SarimaModel arima() {
        return arima.getModel();
    }

    public IArimaMapping<SarimaModel> mapping() {
        return arima.defaultMapping();
    }

    public void setSpecification(SarimaOrders spec) {
        SarimaOrders oldSpec = arima.specification();
        arima.setSpecification(spec);
        if (transformedData != null && (oldSpec.getD() != spec.getD() || oldSpec.getBd() != spec.getBd())) {
            transformedData = null;
            buildTransformation();
        }
    }

    public void setAirline(boolean seas) {
        int period = getAnnualFrequency();
        SarimaOrders s = seas ? SarimaOrders.airline(period)
                : SarimaOrders.m011(period);
        setSpecification(s);
    }

    /**
     * @return the mean_
     */
    public boolean isMean() {
        return mean;
    }

    public boolean hasFixedEffects() {
        return !preadjustmentVariables.isEmpty();
    }

    /**
     * @return the pre-adjustment variables
     */
    public Stream<PreadjustmentVariable> preadjustmentVariables() {
        return preadjustmentVariables.stream();
    }

    /**
     * @return the variables
     */
    public Stream<Variable> variables() {
        return variables.stream();
    }

    /**
     * Counts all the (non fixed) regression variables, which statisfy a given
     * condition
     *
     * @param pred The condition
     * @return The number of regressors (>= # variables)
     */
    public int countRegressors(Predicate<Variable> pred) {
        return variables()
                .filter(pred)
                .mapToInt(var -> var.getVariable().dim()).sum();
    }

    public void setTransformation(LengthOfPeriodType lengthOfPeriodType) {
        if (lpTransformation != lengthOfPeriodType) {
            lpTransformation = lengthOfPeriodType;
            invalidateTransformation();
        }
    }

    public LengthOfPeriodType getTransformation() {
        return lpTransformation;
    }

    /**
     * Interpolates missing values. The interpolation is processed on the
     * complete series
     *
     * @param interpolator
     */
    public void interpolate(@NonNull DataInterpolator interpolator) {
        TsData y = series;
        if (series.getValues().anyMatch(z -> Double.isNaN(z))) {
            IntList lmissing = new IntList();
            interpolatedData = interpolator.interpolate(series.getValues(), lmissing);
            missing = lmissing.isEmpty() ? null : lmissing.toArray();
            invalidateTransformation();
        } else {
            interpolatedData = null;
            missing = IntList.EMPTY;
        }
    }

    public int[] getMissing() {
        return this.missing;
    }

    public void setMean(boolean mean) {
        this.mean = mean;
    }

    public void addPreadjustmentVariable(PreadjustmentVariable... var) {
        if (var != null) {
            for (PreadjustmentVariable v : var) {
                preadjustmentVariables.add(v);
            }
        }
    }

    public boolean removeVariable(Predicate<Variable> pred) {
        if (variables.removeIf(pred.and(var -> !var.isPrespecified()))) {
            regressionVariables = null;
            return true;
        } else {
            return false;
        }
    }

    public int getAnnualFrequency() {
        return series.getAnnualFrequency();
    }

    public List<TsDataTransformation> transformations() {
        ArrayList<TsDataTransformation> tr = new ArrayList<>();

        if (lpTransformation != LengthOfPeriodType.None) {
            tr.add(Transformations.lengthOfPeriod(lpTransformation));
        }
        if (logTransformation) {
            tr.add(Transformations.log());
        }
        return tr;
    }

    /**
     * Back transformation
     *
     * @return The list of the transformation
     */
    public List<TsDataTransformation> backTransformations() {
        ArrayList<TsDataTransformation> tr = new ArrayList<>();

        if (logTransformation) {
            tr.add(Transformations.exp());
        }
        if (lpTransformation != LengthOfPeriodType.None) {
            tr.add(Transformations.lengthOfPeriod(lpTransformation).converse());
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
    public List<TsDataTransformation> backTransformations(boolean T, boolean S) {
        ArrayList<TsDataTransformation> tr = new ArrayList<>();

        if (logTransformation) {
            tr.add(Transformations.exp());
        }
        if (S && lpTransformation != LengthOfPeriodType.None) {
            tr.add(Transformations.lengthOfPeriod(lpTransformation).converse());
        }
        return tr;
    }

    /**
     * Position of the variable in the generated regarima model, The returned
     * position take into account an eventual mean correction.
     *
     * @param variable
     * @return
     */
    public int findPosition(ITsVariable variable) {
        buildRegressionVariables();
        int pos = 0;
        int cur = 0;
        while (cur < regressionVariables.length && regressionVariables[cur] != variable) {
            pos += regressionVariables[cur++].dim();
        }
        if (cur >= regressionVariables.length) {
            return -1; // not found
        }
        return mean ? pos + 1 : pos;
    }

    public RegArimaEstimation<SarimaModel> estimate(IRegArimaProcessor<SarimaModel> processor) {

        RegArimaModel<SarimaModel> model = regarima();
        RegArimaEstimation<SarimaModel> rslt;
        if (arima.isDefined()) {
            rslt = processor.optimize(model, arima.defaultMapping());
        } else {
            rslt = processor.process(model, arima.defaultMapping());
        }
        // update current description
        int p = this.getAnnualFrequency();
        LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> max = rslt.getMax();
        if (max != null) {
            arima.setFreeParameters(DoubleSeq.of(max.getParameters()), ParameterType.Estimated);
        }
        return RegArimaEstimation.<SarimaModel>builder()
                .model(rslt.getModel())
                .concentratedLikelihood(rslt.getConcentratedLikelihood())
                .max(max)
                .llAdjustment(llCorrection)
                .build();
    }

    public static boolean sameArimaSpecification(ModelDescription desc1, ModelDescription desc2) {
        if (!desc1.specification().equals(desc2.specification())) {
            return false;
        }
        return desc1.isMean() == desc2.isMean();
    }

    public static boolean sameVariables(ModelDescription desc1, ModelDescription desc2) {
        desc1.buildRegressionVariables();
        desc2.buildRegressionVariables();
        return Arrays.deepEquals(desc1.regressionVariables, desc2.regressionVariables);
    }

}
