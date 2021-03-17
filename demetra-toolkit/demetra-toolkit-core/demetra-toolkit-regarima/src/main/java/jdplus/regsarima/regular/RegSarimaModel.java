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
import demetra.likelihood.MissingValueEstimation;
import demetra.modelling.implementations.SarimaSpec;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.Variable;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.timeseries.regression.modelling.LightLinearModel;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.Regression;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.ami.ModellingUtility;
import jdplus.sarima.SarimaModel;
import jdplus.stats.tests.NiidTests;
import jdplus.timeseries.simplets.Transformations;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class RegSarimaModel implements GeneralLinearModel<SarimaSpec> {
    
    public static RegSarimaModel of(ModelDescription description, RegArimaEstimation<SarimaModel> estimation, ProcessingLog log){
        
        LightLinearModel.Description.Builder<SarimaSpec> dbuilder = LightLinearModel.Description.<SarimaSpec>builder();

        LightLinearModel.Estimation.Builder ebuilder = LightLinearModel.Estimation.builder();
        
        
        Builder builder = RegSarimaModel.builder()
                .description(dbuilder.build())
                .estimation(ebuilder.build())
                .logs(log.all());
       
        return builder.build();
    }
       
    Description<SarimaSpec> description;
    Estimation estimation;

    @lombok.Singular
    private Map<String, Object> additionalResults;

    @lombok.Singular
    private List<ProcessingLog.Information> logs;
    
    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    static class Details{
        TsDomain estimationDomain;
        TsData interpolatedSeries, transformedSeries;
    }
    
    private boolean isMean(){
        return description.isMean();
    }
    
    Details details;
    
    public int getAnnualFrequency() {
        return description.getSeries().getAnnualFrequency();
    }

    public SarimaOrders specification() {
        return description.getStochasticComponent().orders();
    }

    public TsData interpolatedSeries(boolean bTransformed) {

        TsData data;
        if (bTransformed) {
            data = details.transformedSeries;
        } else {
            data = details.interpolatedSeries;
        }

        // complete for missings
        MissingValueEstimation[] missing = estimation.getMissing();
        int nmissing =missing.length; 
        if (nmissing > 0) {
            double[] datac = data.getValues().toArray();
            if (bTransformed) {
                for (int i = 0; i < nmissing; ++i) {
                    datac[missing[i].getPosition()] = missing[i].getValue();
                }
            } else {
                for (int i = 0; i < nmissing; ++i) {
                    double m = missing[i].getValue();
                    int pos=missing[i].getPosition();
                    if (description.isLogTransformation()) {
                        datac[pos] = Math.exp(m);
                    } else {
                        datac[pos] = m;
                    }
                }
            }
            data = TsData.ofInternal(description.getSeries().getStart(), datac);
        }
        return data;
    }

    public TsData regressionEffect(TsDomain domain, Predicate<Variable> test) {
        DataBlock all = DataBlock.make(domain.getLength());
        Variable[] variables = description.getVariables();
        if (variables.length > 0) {
            DoubleSeqCursor cursor = estimation.getCoefficients().cursor();
            if (description.isMean()) {
                cursor.skip(1);
            }
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                int nfree = cur.freeCoefficientsCount();
                if (nfree > 0) {
                    if (test.test(cur)) {
                        Matrix m = Regression.matrix(domain, cur.getCore());
                        int ic = 0;
                        DataBlockIterator cols = m.columnsIterator();
                        while (cols.hasNext()) {
                            DataBlock col = cols.next();
                            Parameter c = cur.getCoefficient(ic++);
                            if (c.isFree()) {
                                all.addAY(cursor.getAndNext(), col);
                            }
                        }
                    } else {
                        cursor.skip(nfree);
                    }
                }
            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), all.getStorage());
    }

    public TsData preadjustmentEffect(TsDomain domain, Predicate<Variable> test) {
        DataBlock all = DataBlock.make(domain.getLength());
        Variable[] variables = description.getVariables();
        if (variables.length > 0) {
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                int nfree = cur.freeCoefficientsCount();
                if (cur.dim() > nfree) {
                    if (test.test(cur)) {
                        Matrix m = Regression.matrix(domain, cur.getCore());
                        int ic = 0;
                        DataBlockIterator cols = m.columnsIterator();
                        while (cols.hasNext()) {
                            DataBlock col = cols.next();
                            Parameter c = cur.getCoefficient(ic++);
                            if (!c.isFree()) {
                                all.addAY(c.getValue(), col);
                            }
                        }
                    }
                }
            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), all.getStorage());
    }

    public TsData deterministicEffect(TsDomain domain, Predicate<Variable> test) {
        DataBlock all = DataBlock.make(domain.getLength());
        Variable[] variables = description.getVariables();
        if (variables.length > 0) {
            DoubleSeqCursor cursor = estimation.getCoefficients().cursor();
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                if (test.test(cur)) {
                    Matrix m = Regression.matrix(domain, cur.getCore());
                    int ic = 0;
                    DataBlockIterator cols = m.columnsIterator();
                    while (cols.hasNext()) {
                        DataBlock col = cols.next();
                        Parameter c = cur.getCoefficient(ic++);
                        if (c.isFree()) {
                            all.addAY(cursor.getAndNext(), col);
                        } else {
                            all.addAY(c.getValue(), col);

                        }
                    }
                } else {
                    cursor.skip(cur.freeCoefficientsCount());
                }
            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), all.getStorage());
    }

    public TsData linearizedSeries() {
        TsData interp = interpolatedSeries(true);
        Variable[] variables = description.getVariables();
        if (variables.length == 0) {
            return interp;
        }
        DataBlock rslt = DataBlock.of(interp.getValues());
        DoubleSeqCursor c = estimation.getCoefficients().cursor();
        int j = 0;
        if (description.isMean()) {
            c.skip(1);
            ++j;
        }
        TsDomain all = interp.getDomain();
        for (int i = j; i < variables.length; ++i) {
            Matrix xcur = Regression.matrix(all, variables[i].getCore());
            DataBlockIterator xcols = xcur.columnsIterator();
            while (xcols.hasNext()) {
                rslt.addAY(-c.getAndNext(), xcols.next());
            }
        }
        return TsData.ofInternal(interp.getStart(), rslt);
    }

    /**
     * Back-Transforms a series, so that it become comparable to the original
     * one
     *
     * @param s The transformed series (in logs for instance)
     * @param includeLp Specifies if a correction for leap year must be applied
     * @return
     */
    public TsData backTransform(TsData s, boolean includeLp) {
        if (description.isLogTransformation()) {
            s = s.exp();
            if (includeLp && description.getLengthOfPeriodTransformation() != LengthOfPeriodType.None) {
                s = Transformations.lengthOfPeriod(description.getLengthOfPeriodTransformation()).converse().transform(s, null);
            }
        }
        return s;
    }

    /**
     * Transforms a series with the same operations as those applied to the
     * original series
     *
     * @param s The series to be transformed
     * @param includeLp Specifies if a correction for leap year must be
     * applied
     * @return
     */
    public TsData transform(TsData s, boolean includeLp) {
        if (description.isLogTransformation()) {
            if (includeLp && description.getLengthOfPeriodTransformation() != LengthOfPeriodType.None) {
                s = Transformations.lengthOfPeriod(description.getLengthOfPeriodTransformation()).transform(s, null);
            }
            s = s.log();
        }
        return s;
    }
    
    public TsData fullResiduals(){
        DoubleSeq res = estimation.getResiduals();
        TsPeriod start=details.transformedSeries.getEnd().plus(-res.length());
        return TsData.ofInternal(start, res);
    }
    
    public int freeArimaParametersCount(){
        return description.getStochasticComponent().freeParametersCount();
    }
    
    public NiidTests residualsTests(){
        DoubleSeq res = estimation.getResiduals();
        return NiidTests.builder()
                .data(res)
                .period(getAnnualFrequency())
                .hyperParametersCount(freeArimaParametersCount())
                .build();
    }

    /**
     * tde
     *
     * @param domain
     * @return
     */
    public TsData getTradingDaysEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isDaysRelated(v));
        return backTransform(s, true);
    }

    /**
     * ee
     *
     * @param domain
     * @return
     */
    public TsData getEasterEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isEaster(v));
        return backTransform(s, false);
    }

    /**
     * mhe
     *
     * @param domain
     * @return
     */
    public TsData getMovingHolidayEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isMovingHoliday(v));
        return backTransform(s, false);
    }

    /**
     * rmde
     *
     * @param domain
     * @return
     */
    public TsData getRamadanEffect(TsDomain domain) {
        throw new UnsupportedOperationException("Not supported yet.");
//        TsData s = deterministicEffect(domain, v->v instanceof RamadanVariable);
//        return backTransform(s, false);
    }

    /**
     * out
     *
     * @param domain
     * @return
     */
    public TsData getOutliersEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isOutlier(v));
        return backTransform(s, false);
    }

    /**
     *
     * @param domain
     * @param ami
     * @return
     */
    public TsData getOutliersEffect(TsDomain domain, boolean ami) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isOutlier(v, ami));
        return backTransform(s, false);
    }

    /**
     * cal
     *
     * @param domain
     * @return
     */
    public TsData getCalendarEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isCalendar(v));
        return backTransform(s, true);
    }

    /**
     * Gets all the deterministic effects
     *
     * @param domain
     * @return
     */
    public TsData getDeterministicEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ! (v.getCore() instanceof TrendConstant));
        return backTransform(s, true);
    }

}
