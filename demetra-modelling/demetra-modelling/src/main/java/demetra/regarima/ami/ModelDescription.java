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
package demetra.regarima.ami;

import demetra.modelling.Variable;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.IDataInterpolator;
import demetra.data.IDataTransformation.LogJacobian;
import demetra.data.LogTransformation;
import demetra.design.Development;
import demetra.modelling.PreadjustmentVariable;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.ITsTransformation;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.LengthOfPeriodTransformation;
import demetra.regarima.RegArimaModel;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.simplets.TsDataToolkit;
import demetra.utilities.IntList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class ModelDescription {

    private final TsData series;
    private boolean logTransformation;
    private LengthOfPeriodType lpTransformation = LengthOfPeriodType.None;
    private final List<PreadjustmentVariable> preadjustmentVariables = new ArrayList<>();
    private final List<Variable> variables = new ArrayList<>();
    private SarimaComponent arima = new SarimaComponent();

    // Caching
    private ITsVariable<TsDomain>[] regressionVariables;
    private TransformedSeries transformedSeries;
    private RegArimaModel<SarimaModel> regarima;

    public ModelDescription(TsData series) {
        this.series = series;
    }

    public ModelDescription(ModelDescription desc) {
        this.series = desc.series;
        desc.preadjustmentVariables.forEach(preadjustmentVariables::add);
        desc.variables.forEach(variables::add);
        this.arima = desc.arima.clone();
        this.logTransformation = desc.logTransformation;
        this.transformedSeries = desc.transformedSeries;
        this.regarima = desc.regarima;
        this.regressionVariables = desc.regressionVariables;
    }

    // the regression variables are organized as follows:
    // [0. additive outliers_ for missing values]
    // [1. Mean correction]
    // 2 users
    // 3 calendars
    // 4 moving holidays
    // 5 outliers, 5.1 pre-specified, 5.2 detected 
    private ITsVariable<TsDomain>[] buildRegressionVariables() {
        if (regressionVariables == null && !variables.isEmpty()) {
            List<ITsVariable<TsDomain>> vars = new ArrayList<>();
            variables.stream().filter(v -> v.isUser()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isCalendar()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isMovingHolidays()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isOutlier(true)).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isOutlier(false)).forEachOrdered(v -> vars.add(v.getVariable()));
            regressionVariables = vars.toArray(new ITsVariable[vars.size()]);
        }
        return regressionVariables;
    }

    public Stream<ITsVariable<TsDomain>> regressionVariables() {
        return Arrays.stream(regressionVariables);
    }

    public Variable variable(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getVariable().getName().equals(name))
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public PreadjustmentVariable preadjustmentVariable(String name) {
        Optional<PreadjustmentVariable> search = preadjustmentVariables.stream()
                .filter(var -> var.getVariable().getName().equals(name))
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }
    
    public Variable addVariable(Variable var){
        String name=var.getVariable().getName();
        while (contains(name)){
            name=ITsVariable.nextName(name);
        }
        Variable nvar=var.rename(name);
            variables.add(nvar);
            return nvar;
     }
    
    public PreadjustmentVariable addPreadjustmentVariable(PreadjustmentVariable var){
        String name=var.getVariable().getName();
        while (contains(name)){
            name=ITsVariable.nextName(name);
        }
        PreadjustmentVariable nvar=var.rename(name);
            preadjustmentVariables.add(nvar);
            return nvar;
     }

    public boolean contains(String name){
        return variables.stream()
                .anyMatch(var->var.getVariable().getName().equals(name))
                || preadjustmentVariables.stream()
                .anyMatch(var->var.getVariable().getName().equals(name));
    }

    /**
     * Selects/deselects the variable identified by its name
     *
     * @param name
     * @param select True to include the variable in the model, false to exclude
     * it
     * @return True if the variable was found, false otherwise
     */
    public boolean select(String name, boolean select) {
        int n = variables.size();
        for (int i = 0; i < n; ++i) {
            Variable cur = variables.get(i);
            if (cur.getVariable().getName().equals(name)) {
                Variable ncur = cur.select(select);
                if (cur != ncur) {
                    variables.set(i, ncur);
                    regressionVariables = null;
                    regarima = null;
                }
                return true;
            }
        }
        return false;
    }

    public void setLogTransformation(boolean log) {
        this.logTransformation = log;
    }

    public boolean isLogTransformation() {
        return this.logTransformation;
    }

    private DataBlock[] getX(ITsVariable variable) {
        int n = series.length();
        DataBlock[] x = new DataBlock[variable.getDim()];
        ArrayList<DataBlock> tmp = new ArrayList<>();
        for (int i = 0; i < x.length; ++i) {
            x[i] = DataBlock.make(n);
            tmp.add(x[i]);
        }
        variable.data(series.getDomain(), tmp);
        return x;
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

//    /**
//     * @return the y_
//     */
//    private double[] getY() {
//        if (!checkY()) {
//            computeY();
//        }
//        return ycur;
//    }
    /**
     * Gets the transformed original series. The original may be transformed for
     * leap year correction or log-transformation and for fixed effects. The
     * fixed effects are always applied additively after the log-transformation.
     * The transformed original may also be corrected for missing values when an
     * interpolator is provided
     *
     * @param interpolator Interpolator for missing values. May be null; in that
     * case, the returned series will contain the original missing values, if
     * any.
     * @return
     */
    public TransformedSeries transformation(IDataInterpolator interpolator) {
        int diff = arima.getDifferencingOrder();
        LogJacobian lj = new LogJacobian(diff, series.length());
        TsData tmp = series;
        if (lpTransformation != LengthOfPeriodType.None) {
            tmp = new LengthOfPeriodTransformation(lpTransformation).transform(tmp, lj);
        }
        if (logTransformation) {
            tmp = ITsTransformation.of(new LogTransformation()).transform(tmp, lj);
        }

        if (!preadjustmentVariables.isEmpty()) {
            final DataBlock ndata = DataBlock.of(tmp.getValues());
            final TsDomain domain = tmp.getDomain();
            preadjustmentVariables.forEach(v -> {
                v.removeFrom(ndata, domain);
            });
            tmp = TsData.ofInternal(domain.getStartPeriod(), ndata.getStorage());
        }
        int[] m;
        double[] data;
        if (interpolator != null) {
            IntList missing = new IntList();
            data = interpolator.interpolate(tmp.getValues(), missing);
            m = missing.isEmpty() ? null : missing.toArray();
        } else {
            data = tmp.getValues().toArray();
            m = null;
        }

        return TransformedSeries.builder()
                .transformationCorrection(lj.value)
                .data(data)
                .missing(m)
                .build();
    }

    public TsData transformedSeries() {
        TsData tmp = series;
        if (lpTransformation != LengthOfPeriodType.None) {
            tmp = new LengthOfPeriodTransformation(lpTransformation).transform(tmp, null);
        }
        if (logTransformation) {
            tmp = ITsTransformation.of(new LogTransformation()).transform(tmp, null);
        }
        return tmp;
    }

    /**
     * @return the arima
     */
    public SarimaComponent getArimaComponent() {
        return arima;
    }

    public SarimaSpecification getSpecification() {
        return arima.getSpecification();
    }

    public void setSpecification(SarimaSpecification spec) {
        arima.setSpecification(spec);
    }

    public void setAirline(boolean seas) {
        SarimaSpecification s = new SarimaSpecification(this.getAnnualFrequency());
        s.airline(seas);
        arima.setSpecification(s);
    }

    /**
     * @return the mean_
     */
    public boolean isMean() {
        return arima.isMean();
    }

    /**
     * @return the mean_
     */
    public boolean isEstimatedMean() {
        return arima.isEstimatedMean();
    }

    public boolean hasFixedEffects() {
        return !preadjustmentVariables.isEmpty();
    }

    /**
     * @return the variables
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
                .mapToInt(var -> var.getVariable().getDim()).sum();
    }

    //    public LengthOfPeriodType getLengthOfPeriodType() {
//        return preadjustment.convert(isUsed(ICalendarVariable.class), transformation == DefaultTransformationType.Log);
//    }
//
//    /**
//     * @return the log_
//     */
//    public TransformationType getTransformation() {
//        return transformation;
//    }
//
//    private void computeY() {
//        TsData tmp = new TsData(this.estimationDomain.getStart(), y0, true);
//        int len = y0.length;
//        diff_ = arima.getDifferencingOrder();
//        LogJacobian lj = new LogJacobian(diff_, len);
//        lp = preadjustment.convert(isUsed(ICalendarVariable.class), transformation == DefaultTransformationType.Log);
//        if (lp != LengthOfPeriodType.None) {
//            new LengthOfPeriodTransformation(lp).transform(tmp, lj);
//        }
//        if (transformation == DefaultTransformationType.Log) {
//            LogTransformation tlog = new LogTransformation();
//            if (tlog.canTransform(tmp)) {
//                tlog.transform(tmp, lj);
//            } else {
//                throw new TsException("Series contains values lower or equal to zero. Logs not allowed");
//            }
//        }
//        if (!preadjustment.isEmpty()) {
//            DataBlock all = PreadjustmentVariable.regressionEffect(preadjustment.stream(), estimationDomain);
//            tmp.apply(all, (x, y) -> x - y);
//            // we don't need to modify the adjustment factor, which is computed on the initial figures
//            // TODO: check for missing values
//        }
//
//        logtransform_ = lj.value + logtransform0_;
//        ycur = tmp.internalStorage();
//    }
//
//    public boolean updateMissing(IDataInterpolator interpolator) {
//        if (missings != null) {
//            return false;
//        }
//        DoubleSequence y = DoubleSequence.of(y0);
//        IntList missings = new IntList(y0.length);
//        double[] tmp = interpolator.interpolate(y, missings);
//        if (tmp == null) {
//            return false;
//        }
//        if (missings.isEmpty()) {
//            return true;
//        }
//        TsData tmp = new TsData(estimationDomain.getStartPeriod(), y, false);
//        this.missings = new int[missings.size()];
//        for (int i = 0; i < this.missings.length; ++i) {
//            this.missings[i] = missings.get(i);
//        }
//        y0 = y;
//        invalidateData();
//        return true;
//    }
//    public void setTransformation(PreadjustmentType lengthOfPeriodType) {
//        if (preadjustment != lengthOfPeriodType) {
//            preadjustment = lengthOfPeriodType;
//            invalidateData();
//        }
//    }
//
//    public void setTransformation(DefaultTransformationType fn) {
//        if (transformation != fn) {
//            transformation = fn;
//            checkPreadjustment();
//            invalidateData();
//        }
//    }
//
//    public void setPreadjustments(List<PreadjustmentVariable> var) {
//        preadjustment.clear();
//        preadjustment.addAll(var);
//        invalidateData();
//    }
//
//    public void setVariables(List<Variable> var) {
//        variables.clear();
//        variables.addAll(var);
//        invalidateData();
//    }
//
    public void setArimaComponent(SarimaComponent arima) {
        this.arima = arima;

    }

    public void setMean(boolean mean) {
        arima.setMean(mean);
        if (regarima != null && mean != regarima.isMean()) {
            regarima = regarima.toBuilder().meanCorrection(mean).build();
        }
    }

    public void addPreadjustmentVariable(PreadjustmentVariable... var) {
        if (var != null) {
            for (PreadjustmentVariable v : var) {
                preadjustmentVariables.add(v);
            }
        }
    }

    public void addVariable(Variable... var) {
        if (var != null) {
            for (Variable v : var) {
                variables.add(v);
            }
        }
        regressionVariables = null;
        regarima = null;
    }

    public void removeVariable(Predicate<Variable> pred) {
        variables.removeIf(pred.and(var -> !var.isPrespecified()));
        regressionVariables = null;
        regarima = null;
    }

    public int getAnnualFrequency() {
        return series.getAnnualFrequency();
    }

//    public RegArimaModel<SarimaModel> buildRegArima(IDataInterpolator interpolator) {
//        TransformedSeries transformation = this.transformation(interpolator);
//        List<DoubleSequence> x = createX();
//        return RegArimaModel.builder(SarimaModel.class)
//                .y(DoubleSequence.ofInternal(transformation.data))
//                .arima(arima.getModel())
//                .meanCorrection(arima.isMean())
//                .addX(x)
//                .build();
//    }
}
