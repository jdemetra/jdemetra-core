/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus;

import demetra.data.Data;
import demetra.data.WeeklyData;
import demetra.sa.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import org.junit.Test;
import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.util.ArrayList;
import java.util.List;
import jdplus.filters.AsymmetricCriterion;
import jdplus.filters.FSTFilterFactory;
import jdplus.filters.FSTFilterSpec;
import jdplus.filters.LocalPolynomialFilterFactory;
import jdplus.filters.LocalPolynomialFilterSpec;
import jdplus.filters.SpectralDensity;
import jdplus.maths.linearfilters.AsymmetricFilters;
import jdplus.rkhs.RKHSFilterFactory;
import jdplus.rkhs.RKHSFilterSpec;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11KernelTest {

    private static DoubleSeq INPUT = DoubleSeq.of(Data.RETAIL_BOOKSTORES);

    public X11KernelTest() {
    }

    public static void main(String[] cmds) {
//        for (int k = 1; k <= 5; ++k) {
//            test_FST_ap_Prod(k);
//            test_LP_c_Prod(k);
//            test_LP_trend_Prod(k);
//            test_LP_DAF_Prod(k);
//            test_LP_cut_Prod(k);
//            test_RKHS_fr_Prod(k);
//            test_RKHS_fr_Prod2(k);
//            test_RKHS_tm_Prod(k);
//        }

        TsData[] surveys = Data.surveys();
        List<DoubleSeq> lseq = new ArrayList<>();
        List<Boolean> llog = new ArrayList<>();
        for (int i = 0; i < surveys.length; ++i) {
            if (surveys[i].getValues().allMatch(x -> Double.isFinite(x))) {
                IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
                TsPeriod start = surveys[i].getStart();
                ec.tstoolkit.timeseries.simplets.TsData s
                        = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(start.getUnit().getAnnualFrequency()),
                                start.year(), start.annualPosition(), surveys[i].getValues().toArray(), false);
                PreprocessingModel model = processor.process(s, null);
                lseq.add(DoubleSeq.of(model.linearizedSeries().internalStorage()));
                llog.add(model.isMultiplicative());
            }
        }
        DoubleSeq[] seq = lseq.toArray(new DoubleSeq[lseq.size()]);
        boolean[] log = new boolean[seq.length];
        for (int i = 0; i < log.length; ++i) {
            log[i] = llog.get(i);
        }
        for (int l = 1; l <= 3; ++l) {
            System.out.println(l);
            //System.out.println(test_FST_ap_Prod(seq, l));
            System.out.println(test_LP_c_Prod0(seq, log, l));
            System.out.println(test_LP_c_Prod1(seq, log, l));
            System.out.println(test_LP_trend_Prod(seq, log, l));
            System.out.println(test_LP_quad_Prod(seq, log, l));
            System.out.println(test_LP_DAF_Prod(seq, log, l));
            System.out.println(test_LP_cut_Prod(seq, log, l));
            System.out.println(test_RKHS_fr_Prod(seq, log, l));
            System.out.println(test_RKHS_fr_Prod2(seq, log, l));
            System.out.println(test_RKHS_acc_Prod(seq, log, l));
        }
    }

    @Test
    public void testWeekly() {
        X11Kernel kernel = new X11Kernel();
//        System.out.println("Exact");
        X11Context context1 = X11Context.builder()
                .period(365.25 / 7)
                .initialSeasonalFilter(SeasonalFilterOption.S3X1)
                .finalSeasonalFilter(SeasonalFilterOption.S3X9)
                .build();
        kernel.process(DoubleSeq.of(WeeklyData.US_CLAIMS2), context1);
//        System.out.println(kernel.getDstep().getD11());
//        System.out.println("Rounded");
        X11Context context2 = X11Context.builder()
                .period(52)
                .initialSeasonalFilter(SeasonalFilterOption.S3X1)
                .finalSeasonalFilter(SeasonalFilterOption.S3X9)
                .build();
        kernel.process(DoubleSeq.of(WeeklyData.US_CLAIMS2), context2);
//        System.out.println(kernel.getDstep().getD11());
    }

    @Test
    public void testMonthly() {
        X11Kernel kernel = new X11Kernel();
        X11Context context1 = X11Context.builder()
                .period(12)
                .build();
        kernel.process(DoubleSeq.of(Data.PROD), context1);
//        System.out.println(kernel.getDstep().getD13());
        ec.satoolkit.x11.X11Specification spec = new ec.satoolkit.x11.X11Specification();
        spec.setMode(ec.satoolkit.DecompositionMode.Additive);
        spec.setForecastHorizon(0);
        spec.setHendersonFilterLength(13);
        ec.satoolkit.x11.X11Toolkit toolkit = ec.satoolkit.x11.X11Toolkit.create(spec);
        ec.satoolkit.x11.X11Kernel okernel = new ec.satoolkit.x11.X11Kernel();
        okernel.setToolkit(toolkit);
        ec.tstoolkit.timeseries.simplets.TsData s
                = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        X11Results x11 = okernel.process(s);
        ec.tstoolkit.timeseries.simplets.TsData b = x11.getData("d-tables.d13", ec.tstoolkit.timeseries.simplets.TsData.class);
//        System.out.println(new ec.tstoolkit.data.DataBlock(b));
    }

    @Test
    public void testLP() {
        TsData[] insee = Data.surveys();
        DoubleSeq input = insee[9].getValues();
        X11Kernel kernel = new X11Kernel();
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        fspec.setLinearModelCoefficients(new double[]{1, 1});
        fspec.setTimelinessWeight(10);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        for (int i = 200; i < 300; ++i) {
            kernel.process(input.range(0, i), context);
            System.out.println(kernel.getDstep().getD12().drop(200, 0));
        }
    }

    @Test
    public void testRKHS() {
        TsData[] insee = Data.surveys();
        DoubleSeq input = insee[9].getValues();
        X11Kernel kernel = new X11Kernel();
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setFilterLength(6);
        tspec.setDensity(SpectralDensity.RandomWalk);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.Undefined);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(RKHSFilterFactory.of(tspec))
                .mode(DecompositionMode.Additive)
                .build();
        for (int i = 300; i < 330; ++i) {
            kernel.process(input.range(0, i), context);
            System.out.println(kernel.getDstep().getD12().drop(200, 0));
        }
    }

    public static void test_RKHS_fr_Prod(int k) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.RandomWalk);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.FrequencyResponse);
        tspec.setFilterLength(6);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(RKHSFilterFactory.of(tspec))
                .mode(DecompositionMode.Additive)
                .build();
        X11Kernel kernel = new X11Kernel();
        kernel.process(INPUT, context);
        DoubleSeq target = kernel.getDstep().getD12();
        int start = 84;
//        System.out.println(target.drop(start, 0));
        double[] e = new double[target.length() - start - 24];
        for (int i = start; i < target.length() - 24; ++i) {
            kernel.process(INPUT.range(0, i), context);
            DoubleSeq d12 = kernel.getDstep().getD12();
            e[i - start] = target.get(i - k) - d12.get(i - k);
        }
        System.out.println(DoubleSeq.of(e));
    }

    public static void test_FST_ap_Prod(int k) {
        FSTFilterSpec fspec = new FSTFilterSpec();
        fspec.setAntiphase(true);
        fspec.setSmoothnessWeight(.5);
        fspec.setTimelinessWeight(0.5);
        fspec.setLags(8);
        fspec.setLeads(4);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(FSTFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        X11Kernel kernel = new X11Kernel();
        kernel.process(INPUT, context);
        DoubleSeq target = kernel.getDstep().getD12();
        int start = 84;
//        System.out.println(target.drop(start, 0));
        double[] e = new double[target.length() - start - 24];
        for (int i = start; i < target.length() - 24; ++i) {
            kernel.process(INPUT.range(0, i), context);
            DoubleSeq d12 = kernel.getDstep().getD12();
            e[i - start] = target.get(i - k) - d12.get(i - k);
        }
        System.out.println(DoubleSeq.of(e));
    }

    public static void test_RKHS_fr_Prod2(int k) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.Undefined);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.FrequencyResponse);
        tspec.setFilterLength(6);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(RKHSFilterFactory.of(tspec))
                .mode(DecompositionMode.Additive)
                .build();
        X11Kernel kernel = new X11Kernel();
        kernel.process(INPUT, context);
        DoubleSeq target = kernel.getDstep().getD12();
        int start = 84;
//        System.out.println(target.drop(start, 0));
        double[] e = new double[target.length() - start - 24];
        for (int i = start; i < target.length() - 24; ++i) {
            kernel.process(INPUT.range(0, i), context);
            DoubleSeq d12 = kernel.getDstep().getD12();
            e[i - start] = target.get(i - k) - d12.get(i - k);
        }
        System.out.println(DoubleSeq.of(e));
    }

    public static void test_RKHS_tm_Prod(int k) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.RandomWalk);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.Timeliness);
        tspec.setFilterLength(6);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(RKHSFilterFactory.of(tspec))
                .mode(DecompositionMode.Additive)
                .build();
        X11Kernel kernel = new X11Kernel();
        kernel.process(INPUT, context);
        DoubleSeq target = kernel.getDstep().getD12();
        int start = 84;
//        System.out.println(target.drop(start, 0));
        double[] e = new double[target.length() - start - 24];
        for (int i = start; i < target.length() - 24; ++i) {
            kernel.process(INPUT.range(0, i), context);
            DoubleSeq d12 = kernel.getDstep().getD12();
            e[i - start] = target.get(i - k) - d12.get(i - k);
        }
        System.out.println(DoubleSeq.of(e));
    }

    public static void test_LP_trend_Prod(int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        X11Kernel kernel = new X11Kernel();
        kernel.process(INPUT, context);
        DoubleSeq target = kernel.getDstep().getD12();
        int start = 84;
        //       System.out.println(target.drop(start, 0));
        double[] e = new double[target.length() - start - 24];
        for (int i = start; i < target.length() - 24; ++i) {
            kernel.process(INPUT.range(0, i), context);
            DoubleSeq d12 = kernel.getDstep().getD12();
            e[i - start] = target.get(i - k) - d12.get(i - k);
        }
        System.out.println(DoubleSeq.of(e));
    }

    public static void test_LP_c_Prod(int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        fspec.setLinearModelCoefficients(new double[0]);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        X11Kernel kernel = new X11Kernel();
        kernel.process(INPUT, context);
        DoubleSeq target = kernel.getDstep().getD12();
        int start = 84;
//        System.out.println(target.drop(start, 0));
        double[] e = new double[target.length() - start - 24];
        for (int i = start; i < target.length() - 24; ++i) {
            kernel.process(INPUT.range(0, i), context);
            DoubleSeq d12 = kernel.getDstep().getD12();
            e[i - start] = (d12.get(i - k) - target.get(i - k)) / target.get(i - k);
        }
        System.out.println(DoubleSeq.of(e));

    }

    public static double test(X11Context context, DoubleSeq[] input, boolean[] log, int k) {
        X11Kernel kernel = new X11Kernel();
        double se = 0, se2 = 0;
        int n = 0;
        for (int j = 0; j < input.length; ++j) {

            kernel.process(input[j], context);
            DoubleSeq target = kernel.getDstep().getD12();
            int start = 84;
            for (int i = start; i < target.length() - 24; ++i) {
                kernel.process(input[j].range(0, i), context);
                DoubleSeq d12 = kernel.getDstep().getD12();
//                double e = log[j] ? d12.get(i - k) - target.get(i - k)
//                        : (d12.get(i - k) - target.get(i - k)) / target.get(i - k);
                double e = d12.get(i - k) - target.get(i - k);
                ++n;
//                se += Math.abs(e);
                se2 += e * e;
            }
        }
        return Math.sqrt(se2 * n - se * se) / n;
//        return se/n;
    }

    public static double test_FST_ap_Prod(DoubleSeq[] input, boolean[] log, int k) {
        FSTFilterSpec fspec = new FSTFilterSpec();
        fspec.setAntiphase(true);
        fspec.setSmoothnessWeight(.5);
        fspec.setTimelinessWeight(0.5);
        fspec.setLags(7);
        fspec.setLeads(5);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(FSTFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_LP_c_Prod0(DoubleSeq[] input, boolean[] log, int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        fspec.setLinearModelCoefficients(new double[0]);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_LP_c_Prod1(DoubleSeq[] input, boolean[] log, int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        fspec.setLinearModelCoefficients(new double[0]);
        fspec.setTimelinessWeight(1);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_LP_trend_Prod(DoubleSeq[] input, boolean[] log, int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
//        fspec.setAsymmetricPolynomialDegree(1);
//        fspec.setLinearModelCoefficients(new double[]{1});
//        fspec.setTimelinessWeight(10);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_LP_quad_Prod(DoubleSeq[] input, boolean[] log, int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        fspec.setAsymmetricPolynomialDegree(1);
        fspec.setLinearModelCoefficients(new double[]{1});
        fspec.setTimelinessWeight(10);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_LP_DAF_Prod(DoubleSeq[] input, boolean[] log, int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.Direct);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_LP_cut_Prod(DoubleSeq[] input, boolean[] log, int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.CutAndNormalize);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_RKHS_fr_Prod(DoubleSeq[] input, boolean[] log, int k) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.RandomWalk);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.FrequencyResponse);
        tspec.setFilterLength(6);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(RKHSFilterFactory.of(tspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_RKHS_fr_Prod2(DoubleSeq[] input, boolean[] log, int k) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.WhiteNoise);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.FrequencyResponse);
        tspec.setFilterLength(6);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(RKHSFilterFactory.of(tspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static double test_RKHS_acc_Prod(DoubleSeq[] input, boolean[] log, int k) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.RandomWalk);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.Accuracy);
        tspec.setFilterLength(6);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(RKHSFilterFactory.of(tspec))
                .mode(DecompositionMode.Additive)
                .build();
        return test(context, input, log, k);
    }

    public static void test_LP_DAF_Prod(int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.Direct);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        X11Kernel kernel = new X11Kernel();
        kernel.process(INPUT, context);
        DoubleSeq target = kernel.getDstep().getD12();
        int start = 84;
//        System.out.println(target.drop(start, 0));
        double[] e = new double[target.length() - start - 24];
        for (int i = start; i < target.length() - 24; ++i) {
            kernel = new X11Kernel();
            kernel.process(INPUT.range(0, i), context);
            DoubleSeq d12 = kernel.getDstep().getD12();
            e[i - start] = target.get(i - k) - d12.get(i - k);
        }
        System.out.println(DoubleSeq.of(e));

    }

    public static void test_LP_cut_Prod(int k) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.CutAndNormalize);
        X11Context context = X11Context.builder()
                .period(12)
                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .mode(DecompositionMode.Additive)
                .build();
        X11Kernel kernel = new X11Kernel();
        kernel.process(INPUT, context);
        DoubleSeq target = kernel.getDstep().getD12();
        int start = 84;
//        System.out.println(target.drop(start, 0));
        double[] e = new double[target.length() - start - 24];
        for (int i = start; i < target.length() - 24; ++i) {
            kernel = new X11Kernel();
            kernel.process(INPUT.range(0, i), context);
            DoubleSeq d12 = kernel.getDstep().getD12();
            e[i - start] = target.get(i - k) - d12.get(i - k);
        }
        System.out.println(DoubleSeq.of(e));

    }

}
