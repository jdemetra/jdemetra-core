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

import demetra.data.DataBlock;
import demetra.data.IDataInterpolator;
import demetra.data.LogTransformation;
import demetra.design.Development;
import demetra.modelling.PreadjustmentType;
import demetra.modelling.TransformationType;
import demetra.modelling.regression.ICalendarVariable;
import demetra.modelling.regression.IMovingHolidayVariable;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.ITsTransformation;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.LengthOfPeriodTransformation;
import demetra.regarima.RegArimaModel;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.simplets.TsDataToolkit;
import demetra.utilities.IntList;
import java.util.ArrayList;
import static java.util.Arrays.stream;
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
public final class ModelDescription {

    private final TsData originalSeries;
    private final TsDomain estimationDomain;
    private SarimaComponent arima = new SarimaComponent();
    private final List<Variable> vars = new ArrayList<>();

    private boolean logTransformation, seasonality;

    private final double[] y0; // original data, restricted to the estimation domain
    private double[] ycur;
    private int[] missings;
    private PreadjustmentType preadjustment = PreadjustmentType.None;
    private LengthOfPeriodType lp = LengthOfPeriodType.None;
    private int diff;
    private double logtransform0, logtransform;
    // caching of the regression variables
    private final HashMap<ITsVariable, DataBlock[]> xmap
            = new HashMap<>();

    public ModelDescription(TsData originalTs, TsDomain eDomain) {
        this.originalSeries = originalTs;
        if (eDomain == null) {
            estimationDomain = originalSeries.getDomain();
        } else {
            estimationDomain = originalSeries.getDomain().intersection(eDomain);
        }
        y0 = TsDataToolkit.fitToDomain(originalSeries, estimationDomain).getValues().toArray();
    }

    public ModelDescription(ModelDescription desc) {
        this.originalSeries = desc.originalSeries;
        this.estimationDomain = desc.estimationDomain;
        this.y0 = desc.y0.clone();
        desc.vars.forEach(vars::add);
        desc.xmap.forEach((v, x) -> xmap.put(v, x));
        this.arima = desc.arima.clone();
        this.ycur = desc.ycur;
        this.logTransformation = desc.logTransformation;
        this.seasonality = desc.seasonality;
    }

    private void invalidateData() {
        logtransform = 0;
        ycur = null;
        lp = LengthOfPeriodType.None;
    }

    // the regression variables are organized as follows:
    // [0. Mean correction]
    // 1. additive outliers_ for missing values
    // 2 users
    // 3 calendars
    // 4 moving holidays
    // 5 outliers, 5.1 pre-specified, 5.2 detected 
    private List<DataBlock> createX() {
        return vars.stream()
                .filter(var -> !var.isFixed())
                .map(var -> getX(var.getVariable()))
                .collect(ArrayList::new,
                        (l, array) -> {
                            for (int i = 0; i < array.length; ++i) {
                                l.add(array[i]);
                            }
                        },
                        ArrayList::addAll);
    }

    public Stream<Variable> selectVariables(Predicate<Variable> pred) {
        return vars.stream().filter(v -> pred.test(v));
    }

    public Optional<Variable> searchVariable(Predicate<Variable> pred) {
        return vars.stream().filter(pred).findFirst();
    }

    public Optional<Variable> searchVariable(ITsVariable<TsDomain> var) {
        return searchVariable(v -> v.getVariable().equals(var));
    }

    public boolean findAny(Predicate<Variable> pred) {
        return vars.stream().anyMatch(pred);
    }

    public boolean isPrespecified(final ITsVariable<TsDomain> ovar) {
        Optional<Variable> var = searchVariable(ovar);
        return var.isPresent() ? var.get().isPrespecified() : false;
    }

    private DataBlock[] getX(ITsVariable variable) {
        DataBlock[] x = xmap.get(variable);
        if (x != null) {
            return x;
        } else {
            int n = estimationDomain.getLength();
            x = new DataBlock[variable.getDim()];
            ArrayList<DataBlock> tmp = new ArrayList<>();
            for (int i = 0; i < x.length; ++i) {
                x[i] = DataBlock.make(n);
                tmp.add(x[i]);
            }
            variable.data(estimationDomain, tmp);
            xmap.put(variable, x);
            return x;
        }
    }

    public int getRegressionVariablesStartingPosition() {
        int start = 0;
        if (missings != null) {
            start += missings.length;
        }
        if (arima.isEstimatedMean()) {
            ++start;
        }
        return start;
    }

//    public RegArimaModel<SarimaModel> buildRegArima() {
//        double[] y = getY();
//
//        RegArimaModel.Builder<SarimaModel> builder = RegArimaModel.builder(SarimaModel.class)
//                .y(DoubleSequence.ofInternal(y))
//                .arima(arima.getModel())
//                .meanCorrection(arima.isMean())
//                .missing(missings);
//
//        variables.stream().filter(var -> !var.isFixed())
//                .forEachOrdered(var -> builder.addX(xmap.get(var.getVariable())));
//        return builder.build();
//    }
    /**
     * @return the original_
     */
    public TsData getOriginal() {
        return originalSeries;
    }

    /**
     * @return the estimationDomain_
     */
    public TsDomain getSeriesDomain() {
        return originalSeries.getDomain();
    }

    /**
     * @return the estimationDomain_
     */
    public TsDomain getEstimationDomain() {
        return estimationDomain;
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
     * The transformed original may contain missing values
     *
     * @return
     */
    public TsData transformedOriginal() {
        TsData tmp = originalSeries;
        if (lp != LengthOfPeriodType.None) {
            tmp = new LengthOfPeriodTransformation(lp).transform(tmp, null);
        }
        if (logTransformation) {
            tmp = ITsTransformation.of(new LogTransformation()).transform(tmp, null);
        }
        Stream<Variable> pv = preadjustmentVariables();
        if (pv.findAny().isPresent()) {
            final DataBlock ndata = DataBlock.of(tmp.getValues());
            final TsDomain domain = tmp.getDomain();
            pv.forEachOrdered(v->{
                v.removeEffect(domain, ndata);
            });
            tmp = TsData.ofInternal(domain.getStartPeriod(), ndata.getStorage());
        }
        return tmp;
    }

    /**
     * @return the missings
     */
    public int[] getMissingValues() {
        return missings;
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
        return findAny(v -> v.isFixed());
    }

    public Stream<Variable> allVariables() {
        return vars.stream();
    }

    /**
     * @return the variables
     */
    public Stream<Variable> preadjustmentVariables() {
        return selectVariables(v -> v.isFixed());
    }

    /**
     * @return the variables
     */
    public Stream<Variable> variables() {
        return selectVariables(v -> !v.isFixed());
    }
    /**
     * @return the calendars_
     */
    public Stream<Variable> calendars() {
        return variables().filter(var -> var.isCalendar());
    }

    public Stream<Variable> movingHolidays() {
        return variables().filter(var -> var.isMovingHolidays());
    }


    public int countVariables(Predicate<Variable> pred) {
        return (int) variables()
                .filter(pred)
                .count();
    }

    /**
     * Counts all the (non fixed) regression variables, which statisfy a given condition
     * @param pred The condition
     * @return The number of regressors (>= # variables)
     */
    public int countRegressors(Predicate<Variable> pred) {
        return variables()
                .filter(pred)
                .mapToInt(var -> var.getVariable().getDim()).sum();
    }

    /**
     * Gets all outliers (fixed, prespecified and indentified)
     * @return the outliers
     */
    public Stream<IOutlier<TsDomain>> allOutliers() {
        return vars.stream()
                .filter(var->var.getVariable() instanceof IOutlier)
                .map(var->(IOutlier<TsDomain>)var.getVariable());
    }

    /**
     * Gets pre-specified but not fixed outliers, 
     * @return 
     */
    public Stream<IOutlier<TsDomain>> prespecifiedOutliers() {
        return variables()
                .filter(var->var.isOutlier(true))
                .map(var->(IOutlier<TsDomain>)var.getVariable());
    }

    /**
     * Gets identified outliers (not pre-specified)
     * @return 
     */
    public Stream<IOutlier<TsDomain>> outliers() {
        return variables()
                .filter(var->var.isOutlier(false))
                .map(var->(IOutlier<TsDomain>)var.getVariable());
    }

    /**
     * Gets fixed outliers
     * @return 
     */
    public Stream<IOutlier<TsDomain>> getFixedOutliers() {
        return vars.stream()
                .filter(var->var.isFixed() && var.isOutlier(true))
                .map(var->(IOutlier<TsDomain>)var.getVariable());
    }

    /**
     * @return the adjust_
     */
    public PreadjustmentType getPreadjustmentType() {
        return preadjustment;
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
//     public void setTransformation(DefaultTransformationType fn,
//            PreadjustmentType adjust) {
//        transformation = fn;
//        preadjustment = adjust;
//        checkPreadjustment();
//        invalidateData();
//    }
//
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
//    public void setArimaComponent(SarimaComponent arima) {
//        this.arima = arima;
//
//    }
//
//    public void setMean(boolean mean) {
//        arima.setMean(mean);
//    }
//
//    public void setSpecification(SarimaSpecification spec) {
//        arima.setSpecification(spec);
//    }
//
//    public void setAirline(boolean seas) {
//        SarimaSpecification spec = new SarimaSpecification();
//        spec.setPeriod(estimationDomain.getTsUnit().ratioOf(TsUnit.YEAR));
//        spec.airline(seas);
//        arima.setSpecification(spec);
//    }
//
//    public void addVariable(Variable... var) {
//        for (Variable v : var) {
//            variables.add(v);
//        }
//    }
//
//    public void removeVariable(Predicate<Variable> pred) {
//        variables.removeIf(pred);
//    }
//
//    public double getLikelihoodCorrection() {
//        if (!checkY()) {
//            computeY();
//        }
//        return logtransform0_ + logtransform_;
//    }
//
//    private void checkPreadjustment() {
//        if (preadjustment == PreadjustmentType.Auto && transformation == DefaultTransformationType.Log) {
//            variables.stream().filter(var -> var.getVariable() instanceof ILengthOfPeriodVariable).forEach(var -> var.status = RegStatus.Excluded);
//        }
//    }
//
//    private boolean checkY() {
//        if (ycur == null) {
//            return false;
//        }
//        if (lp != this.getLengthOfPeriodType()) {
//            this.invalidateData();
//            return false;
//        }
//        if (diff_ != this.arima.getDifferencingOrder()) {
//            this.invalidateData();
//            return false;
//        }
//        return true;
//    }
    /**
     * @return the seasonality
     */
    public boolean isSeasonality() {
        return seasonality;
    }

    /**
     * @param seasonality the seasonality to set
     */
    public void setSeasonality(boolean seasonality) {
        this.seasonality = seasonality;
    }

    public int getAnnualFrequency() {
        return originalSeries.getAnnualFrequency();
    }
}
