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

import demetra.modelling.regression.Variable;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.data.transformation.LogJacobian;
import demetra.data.ParameterType;
import demetra.data.transformation.DataInterpolator;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.LogLikelihoodFunction;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.modelling.regression.PreadjustmentVariable;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.Regression;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.TransformedSeries;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.stats.tests.NiidTests;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.simplets.Transformations;
import demetra.timeseries.simplets.TsDataTransformation;
import demetra.util.IntList;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Original series
     */
    private final TsData series;

    /**
     * Interpolated series (before transformation
     */
    private TsData interpolatedSeries;
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

    /**
     * Regression variables
     */
    private final List<Variable> variables = new ArrayList<>();

    /**
     * Arima component (including mean correction
     */
    private final SarimaComponent arima = new SarimaComponent();

    // Caching
    private ITsVariable[] regressionVariables;
    private TransformedSeries transformedSeries;
    private RegArimaModel<SarimaModel> regarima;

    public static ModelDescription dummyModel() {
        return new ModelDescription();
    }

    public static ModelDescription of(@Nonnull TsData series, @Nonnull ModelDescription model) {
        ModelDescription nmodel = new ModelDescription(series);
        model.preadjustmentVariables.forEach(nmodel.preadjustmentVariables::add);
        model.variables.forEach(nmodel.variables::add);
        nmodel.arima.copy(model.arima);
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
        this.interpolatedSeries = desc.interpolatedSeries;
        this.missing = desc.missing;
        desc.preadjustmentVariables.forEach(preadjustmentVariables::add);
        desc.variables.forEach(variables::add);
        this.arima.copy(desc.arima);
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
        if (transformedSeries == null) {
            int diff = arima.getDifferencingOrder();
            LogJacobian lj = new LogJacobian(diff, series.length());
            TsData tmp = interpolatedSeries == null ? series : interpolatedSeries;
            if (logTransformation) {
                if (lpTransformation != LengthOfPeriodType.None) {
                    tmp = Transformations.lengthOfPeriod(lpTransformation).transform(tmp, lj);
                }
                tmp = Transformations.log().transform(tmp, lj);
            }

            if (!preadjustmentVariables.isEmpty()) {
                final DataBlock ndata = DataBlock.of(tmp.getValues());
                final TsDomain domain = tmp.getDomain();
                preadjustmentVariables.forEach(v -> {
                    Matrix m = Regression.matrix(domain, v.getVariable());
                    DoubleReader reader = v.getCoefficients().reader();
                    DataBlockIterator columns = m.columnsIterator();
                    while (columns.hasNext()) {
                        ndata.addAY(reader.next(), columns.next());
                    }
                });
                tmp = TsData.ofInternal(domain.getStartPeriod(), ndata.getStorage());
            }

            transformedSeries = TransformedSeries.builder()
                    .transformationCorrection(lj.value)
                    .data(tmp.getValues().toArray())
                    .missing(missing)
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
            for (ITsVariable v : regressionVariables) {
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

    public ITsVariable[] regressionVariables() {
        buildRegressionVariables();
        return regressionVariables;
    }

    public RegArimaModel<SarimaModel> regarima() {
        buildRegarima();
        return regarima;
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

    private Matrix getX(ITsVariable variable) {
        return Regression.matrix(series.getDomain(), variable);
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

    public SarimaModel arima() {
        if (regarima != null) {
            return regarima.arima();
        } else {
            return arima.getModel();
        }
    }

    public void setSpecification(SarimaSpecification spec) {
        arima.setSpecification(spec);
        if (transformedSeries != null) {
            transformedSeries = null;
            buildTransformation();
        }
        if (regarima != null) {
            regarima = RegArimaModel.of(regarima, arima.getModel());
        }
    }

    public void setAirline(boolean seas) {
        SarimaSpecification s = new SarimaSpecification(this.getAnnualFrequency());
        s.airline(seas);
        arima.setSpecification(s);
        if (transformedSeries != null) {
            transformedSeries = null;
            buildTransformation();
        }
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

    public void interpolate(@Nonnull DataInterpolator interpolator) {
        if (series.getValues().anyMatch(z -> Double.isNaN(z))) {
            IntList lmissing = new IntList();
            double[] data = interpolator.interpolate(series.getValues(), lmissing);
            missing = lmissing.isEmpty() ? null : lmissing.toArray();
            interpolatedSeries = TsData.ofInternal(series.getStart(), data);
            invalidateTransformation();
        }
    }

    public int[] getMissing() {
        return this.missing;
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

    public boolean removeVariable(Predicate<Variable> pred) {
        if (variables.removeIf(pred.and(var -> !var.isPrespecified()))) {
            regressionVariables = null;
            regarima = null;
            return true;
        } else {
            return false;
        }
    }

    public int getAnnualFrequency() {
        return series.getAnnualFrequency();
    }

    public List<TsDataTransformation> transformations() {
        buildTransformation();
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
        buildTransformation();
        ArrayList<TsDataTransformation> tr = new ArrayList<>();

        if (logTransformation) {
            tr.add(Transformations.exp());
        }
        if (lpTransformation != LengthOfPeriodType.None) {
            tr.add(Transformations.lengthOfPeriod(lpTransformation).converse());
        }
        return tr;
    }

    public int findPosition(ITsVariable variable) {
        buildRegressionVariables();
        int pos = 0;
        int cur = 0;
        while (cur < regressionVariables.length && regressionVariables[cur] != variable) {
            pos += regressionVariables[cur++].dim();
        }
        return cur < regressionVariables.length ? pos : -1;
    }

    public ModelEstimation estimate(IRegArimaProcessor<SarimaModel> processor) {

        RegArimaModel<SarimaModel> model = regarima();
        int np = arima.getFreeParametersCount();
        int allp = arima.getParametersCount();
        RegArimaEstimation<SarimaModel> rslt;
        if (arima.isDefined()) {
            rslt = processor.optimize(model);
        } else {
            rslt = processor.process(model);
        }
        // update current description
        regarima = rslt.getModel();
        int p = this.getAnnualFrequency();
        LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihood> max = rslt.getMax();
        Matrix J = Matrix.EMPTY;
        DoubleSequence score = DoubleSequence.empty();
        if (max != null) {
            double[] gradient = max.getGradient();
            Matrix hessian = rslt.getMax().getHessian();
            score = DoubleSequence.ofInternal(gradient == null ? DoubleSequence.EMPTYARRAY : gradient);
            J = hessian == null ? null : SymmetricMatrix.inverse(hessian);
            if (np < allp) {
                J = expand(J);
            }
            DataBlock stde = J.diagonal().deepClone();
            stde.apply(a -> a <= 0 ? 0 : Math.sqrt(a));
            arima.setFreeParameters(regarima.arima().parameters(), stde, ParameterType.Estimated);
        }
        NiidTests tests = NiidTests.builder()
                .data(rslt.getConcentratedLikelihood().e())
                .period(p)
                .k(calcLBLength(p))
                .ks(2)
                .seasonal(p > 1)
                .hyperParametersCount(np)
                .build();

        return ModelEstimation.builder()
                .concentratedLikelihood(rslt.getConcentratedLikelihood())
                .statistics(rslt.statistics(transformedSeries.getTransformationCorrection()))
                .score(score)
                .parametersCovariance(J)
                .tests(tests)
                .build();
    }

    private Matrix expand(Matrix cov) {
        boolean[] fixedItems = arima.fixedConstraints();
        int dim = cov.getColumnsCount();
        int[] idx = new int[dim];
        for (int i = 0, j = 0; i < fixedItems.length; ++i) {
            if (!fixedItems[i]) {
                idx[j++] = i;
            }
        }
        Matrix ecov = Matrix.make(fixedItems.length, fixedItems.length);
        for (int i = 0; i < dim; ++i) {
            for (int j = 0; j <= i; ++j) {
                double s = cov.get(i, j);
                ecov.set(idx[i], idx[j], s);
                if (i != j) {
                    ecov.set(idx[j], idx[i], s);
                }
            }
        }
        return ecov;
    }

    public static boolean sameArimaSpecification(ModelDescription desc1, ModelDescription desc2) {
        if (!desc1.getSpecification().equals(desc2.getSpecification())) {
            return false;
        }
        return desc1.isMean() == desc2.isMean();
    }

    public static boolean sameVariables(ModelDescription desc1, ModelDescription desc2) {
        desc1.buildRegressionVariables();
        desc2.buildRegressionVariables();
        return Arrays.deepEquals(desc1.regressionVariables, desc2.regressionVariables);
    }

    private static int calcLBLength(final int freq) {
        int n;
        switch (freq) {
            case 12:
                return 24;
            case 1:
                return 8;
            default:
                return 4 * freq;
        }
    }

}
