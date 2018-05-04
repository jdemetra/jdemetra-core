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
package demetra.regarima.regular;

import demetra.data.AverageInterpolator;
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
import demetra.regarima.ami.SarimaComponent;
import demetra.regarima.ami.TransformedSeries;
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
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class ModelDescription {

    private final TsData series;
    private boolean logTransformation;
    private LengthOfPeriodType lpTransformation = LengthOfPeriodType.None;
    private IDataInterpolator interpolator = new AverageInterpolator();
    private final List<PreadjustmentVariable> preadjustmentVariables = new ArrayList<>();
    private final List<Variable> variables = new ArrayList<>();
    private SarimaComponent arima = new SarimaComponent();

    // Caching
    private ITsVariable<TsDomain>[] regressionVariables;
    private TransformedSeries transformedSeries;
    private RegArimaModel<SarimaModel> regarima;

    public static ModelDescription dummyModel() {
        return new ModelDescription();
    }

    public static ModelDescription of(@Nonnull TsData series, @Nonnull ModelDescription model) {
        ModelDescription nmodel = new ModelDescription(series);
        model.preadjustmentVariables.forEach(nmodel.preadjustmentVariables::add);
        model.variables.forEach(nmodel.variables::add);
        nmodel.arima = model.arima.clone();
        nmodel.logTransformation = model.logTransformation;
        nmodel.lpTransformation = model.lpTransformation;
        return nmodel;
    }

    private ModelDescription() {
        this.series = null;
    }

    public ModelDescription(@Nonnull TsData series) {
        this.series = series;
    }

    public ModelDescription(@Nonnull ModelDescription desc) {
        this.series = desc.series;
        desc.preadjustmentVariables.forEach(preadjustmentVariables::add);
        desc.variables.forEach(variables::add);
        this.arima = desc.arima.clone();
        this.logTransformation = desc.logTransformation;
        this.lpTransformation = desc.lpTransformation;
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
    private void buildRegressionVariables() {
        if (regressionVariables == null) {
            List<ITsVariable<TsDomain>> vars = new ArrayList<>();
            variables.stream().filter(v -> v.isUser()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isCalendar()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isMovingHolidays()).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isOutlier(true)).forEachOrdered(v -> vars.add(v.getVariable()));
            variables.stream().filter(v -> v.isOutlier(false)).forEachOrdered(v -> vars.add(v.getVariable()));
            regressionVariables = vars.toArray(new ITsVariable[vars.size()]);
        }
    }

    private void buildTransformation() {
        if (transformedSeries == null) {
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

            transformedSeries = TransformedSeries.builder()
                    .transformationCorrection(lj.value)
                    .data(data)
                    .missing(m)
                    .build();
        }
    }

    private void buildRegarima() {
        if (regarima == null) {
            buildTransformation();
            buildRegressionVariables();
            RegArimaModel.Builder builder = RegArimaModel.builder(SarimaModel.class)
                    .y(DoubleSequence.ofInternal(transformedSeries.data))
                    .missing(transformedSeries.missing)
                    .arima(arima.getModel())
                    .meanCorrection(arima.isMean());
            for (ITsVariable<TsDomain> v : regressionVariables) {
                builder.addX(getX(v));
            }
            regarima = builder.build();
        }
    }

    private void invalidateRegarima() {
        this.regressionVariables = null;
        this.regarima = null;
    }

    private void invalidateTransformation() {
        this.transformedSeries = null;
        this.regarima = null;
    }

    public Stream<ITsVariable<TsDomain>> regressionVariables() {
        buildRegressionVariables();
        return Arrays.stream(regressionVariables);
    }

    public RegArimaModel<SarimaModel> regarima() {
        buildRegarima();
        return regarima;
    }

    public Variable variable(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getVariable().getName().equals(name))
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public Variable variable(ITsVariable<TsDomain> v) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getVariable()==v)
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public boolean remove(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getVariable().getName().equals(name))
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
                .filter(var -> var.getVariable()==v)
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
                .filter(var -> var.getVariable().getName().equals(name))
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public Variable addVariable(Variable var) {
        String name = var.getVariable().getName();
        while (contains(name)) {
            name = ITsVariable.nextName(name);
        }
        Variable nvar = var.rename(name);
        variables.add(nvar);
        invalidateRegarima();
        return nvar;
    }

    public PreadjustmentVariable addPreadjustmentVariable(PreadjustmentVariable var) {
        String name = var.getVariable().getName();
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
                .anyMatch(var -> var.getVariable().getName().equals(name))
                || preadjustmentVariables.stream()
                .anyMatch(var -> var.getVariable().getName().equals(name));
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

    private DoubleSequence[] getX(ITsVariable variable) {
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
     * @return
     */
    public TransformedSeries transformation() {
        buildTransformation();
        return transformedSeries;
    }

    public TsData getTransformedSeries() {
        transformation();
        return TsData.ofInternal(series.getStart(), transformedSeries.data);
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
        if (regarima != null) {
            regarima = RegArimaModel.of(regarima, arima.getModel());
        }

    }

    public void setAirline(boolean seas) {
        SarimaSpecification s = new SarimaSpecification(this.getAnnualFrequency());
        s.airline(seas);
        arima.setSpecification(s);
        if (regarima != null) {
            regarima = RegArimaModel.of(regarima, arima.getModel());
        }
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

    public void setTransformation(LengthOfPeriodType lengthOfPeriodType) {
        if (lpTransformation != lengthOfPeriodType) {
            lpTransformation = lengthOfPeriodType;
            invalidateTransformation();
        }
    }

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
    /**
     * @return the interpolator
     */
    public IDataInterpolator getInterpolator() {
        return interpolator;
    }

    /**
     * @param interpolator the interpolator to set
     */
    public void setInterpolator(IDataInterpolator interpolator) {
        this.interpolator = interpolator;
        invalidateTransformation();
    }

    public int findPosition(ITsVariable<TsDomain> variable) {
        buildRegressionVariables();
        int pos = 0;
        int cur = 0;
        while (cur < regressionVariables.length && regressionVariables[cur] != variable) {
            pos += regressionVariables[cur++].getDim();
        }
        return cur < regressionVariables.length ? pos : -1;
    }
}
