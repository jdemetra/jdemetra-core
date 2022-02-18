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

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Parameter;
import demetra.arima.SarimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.Variable;
import demetra.util.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.interpolation.DataInterpolator;
import jdplus.data.transformation.LogJacobian;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.stats.likelihood.LogLikelihoodFunction;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.ami.ModellingUtility;
import jdplus.sarima.SarimaModel;
import jdplus.sarima.estimation.SarimaFixedMapping;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.timeseries.simplets.Transformations;
import jdplus.timeseries.simplets.TsDataTransformation;
import nbbrd.design.Development;
import org.checkerframework.checker.nullness.qual.NonNull;
import jdplus.regarima.IRegArimaComputer;

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

    private boolean mean;
    /**
     * Regression variables
     */
    private final List<Variable> variables = new ArrayList<>();

    /**
     * Arima component
     */
    private SarimaSpec arima = SarimaSpec.whiteNoise();

    private boolean sortedVariables; //optimization

    public static ModelDescription dummyModel() {
        return new ModelDescription();
    }

    public static ModelDescription copyOf(@NonNull ModelDescription model) {
        return copyOf(model, null);
    }

    public static ModelDescription copyOf(@NonNull ModelDescription model, TsDomain estimationDomain) {
        ModelDescription nmodel = new ModelDescription(model.series, estimationDomain);
        nmodel.arima = model.arima;
        nmodel.mean = model.mean;
        nmodel.logTransformation = model.logTransformation;
        nmodel.lpTransformation = model.lpTransformation;
        nmodel.interpolatedData = model.interpolatedData;
        nmodel.transformedData = model.transformedData;
        nmodel.missing = model.missing;
        nmodel.llCorrection = model.llCorrection;
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
    private void sortVariables() {
        if (sortedVariables) {
            return;
        }
        List<Variable> vars = new ArrayList<>();
        variables.stream()
                .filter(v -> ModellingUtility.isUser(v))
                .forEachOrdered(v -> vars.add(v));
        variables.stream()
                .filter(v -> ModellingUtility.isDaysRelated(v))
                .forEachOrdered(v -> vars.add(v));
        variables.stream()
                .filter(v -> ModellingUtility.isMovingHoliday(v))
                .forEachOrdered(v -> vars.add(v));
        variables.stream()
                .filter(v -> ModellingUtility.isOutlier(v))
                .filter(v -> !ModellingUtility.isAutomaticallyIdentified(v))
                .forEachOrdered(v -> vars.add(v));
        variables.stream()
                .filter(v -> ModellingUtility.isOutlier(v))
                .filter(v -> ModellingUtility.isAutomaticallyIdentified(v))
                .forEachOrdered(v -> vars.add(v));
        variables.clear();
        variables.addAll(vars);
        sortedVariables = true;
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

            // remove preadjustment
            if (hasFixedEffects()) {
                final DataBlock ndata = DataBlock.of(tmp.getValues());
                final TsDomain domain = tmp.getDomain();
                variables.forEach(v -> {
                    if (!v.isFree()) {
                        FastMatrix m = Regression.matrix(domain, v.getCore());
                        DataBlockIterator columns = m.columnsIterator();
                        int cur = 0;
                        while (columns.hasNext()) {
                            DataBlock col = columns.next();
                            Parameter c = v.getCoefficient(cur++);
                            if (c.isFixed()) {
                                ndata.addAY(-c.getValue(), col);
                            }
                        }
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

    public int[] getMissingInEstimationDomain() {
        if (estimationDomain == null || missing == null || missing.length == 0) {
            return missing;
        }
        int start = series.getStart().until(estimationDomain.getStartPeriod());
        if (start == 0) {
            return missing;
        }
        int[] nmissing = missing.clone();
        for (int i = 0; i < nmissing.length; ++i) {
            nmissing[i] -= start;
        }
        return nmissing;
    }

    /**
     * Gets the regarima model corresponding to the estimation domain
     *
     * @return
     */
    public RegArimaModel<SarimaModel> regarima() {
        buildTransformation();
        sortVariables();
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
                .arima(SarimaModel.builder(arima).build());
        List<Variable> excluded = new ArrayList<>();
        for (Variable v : variables) {
            if (!v.isPreadjustment()) {
                FastMatrix x = Regression.matrix(domain, v.getCore());
                if (x == null) {
                    excluded.add(v);
                } else {
                    DataBlockIterator columns = x.columnsIterator();
                    int ic = 0;
                    while (columns.hasNext()) {
                        DataBlock col = columns.next();
                        if (v.getCoefficient(ic++).isFree()) {
                            builder.addX(col.unmodifiable());
                        }
                    }
                }
            }
        }
        if (! excluded.isEmpty()){
            variables.replaceAll(v->v.exclude(true));
        }
        return builder.build();
    }

    private void invalidateTransformation() {
        this.transformedData = null;
        this.llCorrection = 0;
    }

    public Variable variable(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getName().equals(name))
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public Variable variable(ITsVariable v) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getCore() == v)
                .findFirst();
        return search.isPresent() ? search.get() : null;
    }

    public boolean remove(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getName().equals(name))
                .findFirst();
        if (search.isPresent()) {
            variables.remove(search.get());
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(ITsVariable v) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getCore() == v)
                .findFirst();
        if (search.isPresent()) {
            variables.remove(search.get());
            return true;
        } else {
            return false;
        }
    }

    public Variable addVariable(Variable var) {
        String name = var.getName();
        while (contains(name)) {
            name = ITsVariable.nextName(name);
        }
        Variable nvar = var.rename(name);
        variables.add(nvar);
        sortedVariables = false;
        return nvar;
    }

    public boolean contains(String name) {
        return variables.stream()
                .anyMatch(var -> var.getName().equals(name));
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
    public SarimaSpec getArimaSpec() {
        return arima;
    }

    public void setArimaSpec(SarimaSpec spec) {
        arima = spec;
        if (transformedData != null && (arima.getD() != spec.getD() || arima.getBd() != spec.getBd())) {
            transformedData = null;
            buildTransformation();
        }
    }

    public SarimaOrders specification() {
        return arima.orders();
    }

    public SarimaModel arima() {
        return SarimaModel.builder(arima).build();
    }

    public void setSpecification(SarimaOrders spec) {
        if (transformedData != null && (arima.getD() != spec.getD() || arima.getBd() != spec.getBd())) {
            transformedData = null;
        }
        arima = SarimaSpec.builder()
                .period(spec.getPeriod())
                .d(spec.getD())
                .bd(spec.getBd())
                .p(spec.getP())
                .q(spec.getQ())
                .bp(spec.getBp())
                .bq(spec.getBq())
                .buildWithoutValidation();
        buildTransformation();
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
        return variables.stream().anyMatch(v -> !v.isFree());
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
                .mapToInt(var -> var.getCore().dim()).sum();
    }

    public void setPreadjustment(LengthOfPeriodType lengthOfPeriodType) {
        if (lpTransformation != lengthOfPeriodType) {
            lpTransformation = lengthOfPeriodType;
            invalidateTransformation();
        }
    }

    public LengthOfPeriodType getPreadjustment() {
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

    public boolean removeVariable(Predicate<Variable> pred) {
        if (variables.removeIf(pred.and(var -> ModellingUtility.isAutomaticallyIdentified(var)))) {
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
     * @return -1 if not found, otherwise the position of the first
     * free coefficient of the considered variable, corrected for the presence
     * of a mean correction (which is always the first one).
     */
    public int findPosition(ITsVariable variable) {
        sortVariables();
        int pos = 0;
        boolean found = false;
        for (Variable var : variables) {
            if (!var.isPreadjustment()) {
                if (var.getCore() == variable) {
                    found = true;
                    break;
                } else {
                    pos += var.freeCoefficientsCount();
                }
            }
        }
        if (!found) {
            return -1;
        }
        return mean ? pos + 1 : pos;
    }

    public IArimaMapping<SarimaModel> mapping() {
        if (arima.hasFixedParameters()) {
            int n = arima.getP() + arima.getBp() + arima.getQ() + arima.getBq();
            double[] p = new double[n];
            boolean[] b = new boolean[n];
            int j = 0;
            Parameter[] P = arima.getPhi();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            P = arima.getTheta();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            P = arima.getBtheta();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            return new SarimaFixedMapping(specification(), DoubleSeq.of(p), b);
        } else {
            return SarimaMapping.of(specification());
        }
    }

    public RegArimaEstimation<SarimaModel> estimate(IRegArimaComputer<SarimaModel> processor) {

        RegArimaModel<SarimaModel> model = regarima();
        RegArimaEstimation<SarimaModel> rslt;
        if (!arima.hasFixedParameters()) {
            rslt = processor.process(model, mapping());
        } else {
            rslt = processor.optimize(model, mapping());
        }
        // update current description
        int p = this.getAnnualFrequency();
        LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> max = rslt.getMax();
        if (max != null) {
            setFreeParameters(max.getParameters());
        }
        return RegArimaEstimation.<SarimaModel>builder()
                .model(rslt.getModel())
                .concentratedLikelihood(rslt.getConcentratedLikelihood())
                .max(max)
                .llAdjustment(llCorrection)
                .build();
    }

    public void freeArimaParameters() {
        arima = arima.resetParameters(null);
    }

    public void setFreeParameters(DoubleSeq p) {
        SarimaSpec.Builder builder = arima.toBuilder();
        DoubleSeqCursor pcur = p.cursor();
        Parameter[] P = arima.getPhi();
        for (int i = 0; i < P.length; ++i) {
            if (!P[i].isFixed()) {
                P[i] = Parameter.estimated(pcur.getAndNext());
            }
        }
        builder.phi(P);
        P = arima.getBphi();
        for (int i = 0; i < P.length; ++i) {
            if (!P[i].isFixed()) {
                P[i] = Parameter.estimated(pcur.getAndNext());
            }
        }
        builder.bphi(P);
        P = arima.getTheta();
        for (int i = 0; i < P.length; ++i) {
            if (!P[i].isFixed()) {
                P[i] = Parameter.estimated(pcur.getAndNext());
            }
        }
        builder.theta(P);
        P = arima.getBtheta();
        for (int i = 0; i < P.length; ++i) {
            if (!P[i].isFixed()) {
                P[i] = Parameter.estimated(pcur.getAndNext());
            }
        }
        builder.btheta(P);
        arima = builder.buildWithoutValidation();
    }

}
