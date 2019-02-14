/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.tramo;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.modelling.regression.ModellingContext;
import demetra.regarima.regular.PreprocessingModel;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.timeseries.Month;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class TramoProcessorTest {

    private final double[] data, datamissing;
    public static final Calendar france;
    public static final ec.tstoolkit.timeseries.calendars.NationalCalendar ofrance;

    static {
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new Holiday(new FixedDay(7, 14)));
        holidays.add(new Holiday(new FixedDay(5, 8)));
        holidays.add(new Holiday(FixedDay.ALLSAINTSDAY));
        holidays.add(new Holiday(FixedDay.ARMISTICE));
        holidays.add(new Holiday(FixedDay.ASSUMPTION));
        holidays.add(new Holiday(FixedDay.CHRISTMAS));
        holidays.add(new Holiday(FixedDay.MAYDAY));
        holidays.add(new Holiday(FixedDay.NEWYEAR));
        holidays.add(new Holiday(EasterRelatedDay.ASCENSION));
        holidays.add(new Holiday(EasterRelatedDay.EASTERMONDAY));
        holidays.add(new Holiday(EasterRelatedDay.WHITMONDAY));

        france = new Calendar(holidays.toArray(new Holiday[holidays.size()]), true);

        ofrance = new ec.tstoolkit.timeseries.calendars.NationalCalendar();
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(13, Month.July));
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(7, Month.May));
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(10, Month.November));
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.AllSaintsDay);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.Assumption);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.Christmas);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.MayDay);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.NewYear);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.Ascension);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.EasterMonday);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.PentecostMonday);
    }

    public TramoProcessorTest() {
        data = Data.PROD.clone();
        datamissing = Data.PROD.clone();
        datamissing[2] = Double.NaN;
        datamissing[100] = Double.NaN;
        datamissing[101] = Double.NaN;
        datamissing[102] = Double.NaN;
    }

//    @Test
    public void testProdMissing() {
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TR5, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.of(start, DoubleSequence.ofInternal(datamissing));
        demetra.regarima.regular.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("JD3 with missing");
        System.out.println(rslt.getEstimation().getStatistics().getLogLikelihood());
    }

//    @Test
    public void testProdLegacyMissing() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR5.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, datamissing, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy with missing");
        System.out.println(rslt.estimation.getStatistics().logLikelihood);
    }

//    @Test
    public void testProd() {
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TRfull, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.of(start, DoubleSequence.ofInternal(data));
        PreprocessingModel rslt = processor.process(s, null);
        System.out.println("JD3");
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
    }

    @Test
    public void testInseeFull() {
        TsData[] all = Data.insee();
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TRfull, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            PreprocessingModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TRfull");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInseeFullc() {
        TsData[] all = Data.insee();
        TramoSpec spec = TramoSpec.TRfull;
        ModellingContext context = new ModellingContext();
        context.getCalendars().set("france", france);

        RegressionSpec regSpec = spec.getRegression();
        CalendarSpec calSpec = regSpec.getCalendar();
        TradingDaysSpec tdSpec = calSpec.getTradingDays()
                .toBuilder()
                .holidays("france")
                .build();
        spec = spec.toBuilder()
                .regression(regSpec.toBuilder()
                        .calendar(calSpec.toBuilder()
                                .tradingDays(tdSpec)
                                .build())
                        .build())
                .build();

        TramoProcessor processor = TramoProcessor.of(spec, context);

        ec.tstoolkit.algorithm.ProcessingContext ocontext = new ec.tstoolkit.algorithm.ProcessingContext();
        ocontext.getGregorianCalendars().set("france", new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(ofrance));
        ec.tstoolkit.modelling.arima.tramo.TramoSpecification ospec = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.clone();
        ospec.getRegression().getCalendar().getTradingDays().setHolidays("france");
        IPreprocessor oprocessor = ospec.build(ocontext);
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            PreprocessingModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TRfullc");
        System.out.println(n);

// The old implementation was bugged. 
        assertTrue(n > .6 * all.length);
    }

    @Test
    public void testInsee0() {
        TsData[] all = Data.insee();
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TR0, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR0.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            PreprocessingModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR0");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee1() {
        TsData[] all = Data.insee();
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TR1, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR1.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            PreprocessingModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR1");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee2() {
        TsData[] all = Data.insee();
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TR2, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR2.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            PreprocessingModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR2");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee3() {
        TsData[] all = Data.insee();
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TR3, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR3.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            PreprocessingModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR3");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee4() {
        TsData[] all = Data.insee();
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TR4, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR4.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            PreprocessingModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR4");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee5() {
        TsData[] all = Data.insee();
        TramoProcessor processor = TramoProcessor.of(TramoSpec.TR5, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR5.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            PreprocessingModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR5");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

//    @Test
    public void testProdLegacy() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy");
        System.out.println(rslt.estimation.getStatistics().adjustedLogLikelihood);
    }

//    @Test
    public void testProdWald() {
        TramoSpec nspec = TramoSpec.TRfull;

        RegressionSpec regSpec = nspec.getRegression();
        CalendarSpec calSpec = regSpec.getCalendar();
        TradingDaysSpec tdSpec = calSpec.getTradingDays()
                .toBuilder()
                .automaticMethod(TradingDaysSpec.AutoMethod.WaldTest)
                .build();
        nspec = nspec.toBuilder()
                .regression(regSpec.toBuilder()
                        .calendar(calSpec.toBuilder()
                                .tradingDays(tdSpec)
                                .build())
                        .build())
                .build();

        TramoProcessor processor = TramoProcessor.of(nspec, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.of(start, DoubleSequence.ofInternal(data));
        PreprocessingModel rslt = processor.process(s, null);
        System.out.println("JD3 wald");
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
    }

//    @Test
    public void testProdWaldLegacy() {
        ec.tstoolkit.modelling.arima.tramo.TramoSpecification nspec = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.clone();
        nspec.getRegression().getCalendar().getTradingDays().setAutomaticMethod(ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec.AutoMethod.WaldTest);
        IPreprocessor processor = nspec.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy wald");
        System.out.println(rslt.estimation.getStatistics().adjustedLogLikelihood);
    }

    //@Test
    public void stressTestProd() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            TramoProcessor processor = TramoProcessor.of(TramoSpec.TRfull, null);
            TsPeriod start = TsPeriod.monthly(1967, 1);
            TsData s = TsData.of(start, DoubleSequence.ofInternal(data));
            PreprocessingModel rslt = processor.process(s, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("JD3");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
            ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        }
        t1 = System.currentTimeMillis();
        System.out.println("Legacy");
        System.out.println(t1 - t0);
    }

}
