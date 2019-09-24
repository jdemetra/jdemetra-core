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
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.dfa.MSEDecomposition;
import jdplus.filters.AsymmetricCriterion;
import jdplus.filters.FSTFilter;
import jdplus.filters.FSTFilterFactory;
import jdplus.filters.FSTFilterSpec;
import jdplus.filters.IFiltering;
import jdplus.filters.LocalPolynomialFilterFactory;
import jdplus.filters.LocalPolynomialFilterSpec;
import jdplus.filters.SpectralDensity;
import jdplus.maths.linearfilters.AsymmetricFilters;
import jdplus.maths.linearfilters.IFiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.rkhs.RKHSFilterFactory;
import jdplus.rkhs.RKHSFilterSpec;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11KernelTest {

    private static final DoubleSeq INPUT = DoubleSeq.of(Data.RETAIL_BOOKSTORES);

    private static final int H = 6;
    private static final DecompositionMode mode = DecompositionMode.Multiplicative;

    public X11KernelTest() {
    }

    public static void main(String[] cmds) {

        TsData[] surveys = Data.indprod_de();
        List<DoubleSeq> lseq = new ArrayList<>();
        for (int i = 0; i < surveys.length; ++i) {
            if (surveys[i].getValues().allMatch(x -> Double.isFinite(x))) {
                IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
                TsPeriod start = surveys[i].getStart();
                ec.tstoolkit.timeseries.simplets.TsData s
                        = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(start.getUnit().getAnnualFrequency()),
                                start.year(), start.annualPosition(), surveys[i].getValues().toArray(), false);
                PreprocessingModel model = processor.process(s, null);
                ec.tstoolkit.timeseries.simplets.TsData lin = model.linearizedSeries();
                if (model.isMultiplicative()) {
                    lin = lin.exp();
                }
                lseq.add(DoubleSeq.of(lin.internalStorage()));

            }
        }
        DoubleSeq[] seq = lseq.toArray(new DoubleSeq[lseq.size()]);
        int lag = 1; //12;
        for (int l = 0; l < 3; ++l) {
            System.out.println(l);
            //System.out.println(test_FST_ap_Prod(seq, l));
            System.out.println(test_LP_c_0(seq, 1 + l * lag));
            System.out.println(test_LP_c_1(seq, 1 + l * lag));
            System.out.println(test_Musgrave(seq, 1 + l * lag));
            System.out.println(test_LP_quad(seq, 1 + l * lag));
            System.out.println(test_LP_DAF(seq, 1 + l * lag));
            System.out.println(test_LP_cut(seq, 1 + l * lag));
            System.out.println(test_RKHS_frf(seq, 1 + l * lag));
            System.out.println(test_RKHS_acc(seq, 1 + l * lag));
            System.out.println(test_RKHS_timeliness(seq, 1 + l * lag));
        }
    }

    @Test
    public void testWeekly() {
        X11Kernel kernel = new X11Kernel();
//        System.out.println("Exact");
        X11Context context1 = X11Context.builder()
                .period(365.25 / 7)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(365.25 / 7, SeasonalFilterOption.S3X1))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(365.25 / 7, SeasonalFilterOption.S3X9))
                .build();
        kernel.process(DoubleSeq.of(WeeklyData.US_CLAIMS2), context1);
//        System.out.println(kernel.getDstep().getD11());
//        System.out.println("Rounded");
        X11Context context2 = X11Context.builder()
                .period(52)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(52, SeasonalFilterOption.S3X1))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(52, SeasonalFilterOption.S3X9))
                .build();
        kernel.process(DoubleSeq.of(WeeklyData.US_CLAIMS2), context2);
//        System.out.println(kernel.getDstep().getD11());
    }

    @Test
    public void testDaily() {
        Random rnd=new Random();
        DataBlock s=DataBlock.make(5000);
        s.set(rnd::nextDouble);
        X11Kernel kernel = new X11Kernel();
//        System.out.println("Exact");
        X11Context context1 = X11Context.builder()
                .period(365.25)
                .trendFiltering(daf(367))
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(365.25, SeasonalFilterOption.S3X1))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(365.25, SeasonalFilterOption.S3X9))
                .build();
        long t0=System.currentTimeMillis();
        kernel.process(s, context1);
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
    }

    @Test
    public void testMonthly() {
        X11Kernel kernel = new X11Kernel();
        X11Context context1 = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
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

    public ec.tstoolkit.modelling.arima.PreprocessingModel sa(TsData ts) {
        ec.tstoolkit.modelling.arima.IPreprocessor p = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        TsPeriod start = ts.getStart();
        ec.tstoolkit.timeseries.simplets.TsData s
                = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(start.getUnit().getAnnualFrequency()),
                        start.year(), start.annualPosition(), ts.getValues().toArray(), false);
        PreprocessingModel model = p.process(s, null);
        return model;
    }

    @Ignore
    @Test
    public void testLP() {
        TsData test = Data.surveys()[9];
//        PreprocessingModel sa = sa(test);
        DoubleSeq input = test.getValues();//DoubleSeq.of(sa.linearizedSeries().internalStorage());
        X11Kernel kernel = new X11Kernel();
        RKHSFilterSpec rspec = new RKHSFilterSpec();
        rspec.setAsymmetricBandWith(AsymmetricCriterion.FrequencyResponse);
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(6);
//        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
//        fspec.setAsymmetricPolynomialDegree(0);
//        fspec.setLinearModelCoefficients(null);
//        fspec.setTimelinessWeight(100);
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 3, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                //                .trendFiltering(LocalPolynomialFilterFactory.of(fspec))
                .trendFiltering(RKHSFilterFactory.of(rspec))
                .mode(DecompositionMode.Additive)
                .build();
        for (int i = 200; i < 300; ++i) {
            kernel.process(input.range(0, i), context);
            System.out.println(kernel.getDstep().getD12().drop(200, 0));
        }
    }

    public static double test(X11Context context, DoubleSeq[] input, int k) {
        X11Kernel kernel = new X11Kernel();
        double se = 0, se2 = 0;
        int n = 0;
        for (int j = 0; j < input.length; ++j) {
            if (!mode.isMultiplicative() || input[j].allMatch(x -> x > 0)) {
                kernel.process(input[j], context);
                DoubleSeq target = kernel.getDstep().getD12();
//                DoubleSeq target = kernel.getDstep().getD11();
                int start = 84;
                for (int i = start; i < target.length() - 24; ++i) {
                    try {
                        kernel.process(input[j].range(0, i), context);
                        DoubleSeq rt = kernel.getDstep().getD12();
//                        DoubleSeq d11 = kernel.getDstep().getD11();
                        double e = !mode.isMultiplicative() ? rt.get(i - k) - target.get(i - k)
                                : (rt.get(i - k) - target.get(i - k)) / target.get(i - k);
                        ++n;
//                se += Math.abs(e);
                        se2 += e * e;
                    } catch (Exception e) {
                    }
                }
            }
        }
        return Math.sqrt(se2 * n - se * se) / n;
//        return se/n;
    }

    public static double test_FST_ap_Prod(DoubleSeq[] input, int k) {
        FSTFilterSpec fspec = new FSTFilterSpec();
        fspec.setAntiphase(true);
        fspec.setSmoothnessWeight(.5);
        fspec.setTimelinessWeight(0.5);
        fspec.setLags(7);
        fspec.setLeads(5);
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(FSTFilterFactory.of(fspec))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_LP_c_0(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(lp_c0(H))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_LP_c_1(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(lp_c1(H, 10))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_Musgrave(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(musgrave(H))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_LP_quad(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(lp_quad(H, 1, 10))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_LP_DAF(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(daf(H))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_LP_cut(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(cut(H))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_RKHS_frf(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(rkhs_frf(H))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_RKHS_acc(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(rkhs_acc(H))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static double test_RKHS_timeliness(DoubleSeq[] input, int k) {
        X11Context context = X11Context.builder()
                .period(12)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X3))
                //                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, 2, DiscreteKernel.trapezoidal(3)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(12, SeasonalFilterOption.S3X5))
                .trendFiltering(rkhs_timeliness(H))
                .mode(mode)
                .build();
        return test(context, input, k);
    }

    public static IFiltering lp_c0(int h) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(h);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        fspec.setLinearModelCoefficients(new double[0]);
        return LocalPolynomialFilterFactory.of(fspec);
    }

    public static IFiltering lp_c1(int h, int tw) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(h);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        fspec.setLinearModelCoefficients(new double[0]);
        fspec.setTimelinessWeight(tw);
        return LocalPolynomialFilterFactory.of(fspec);
    }

    public static IFiltering lp_quad(int h, double c, double tw) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(H);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        fspec.setAsymmetricPolynomialDegree(1);
        fspec.setLinearModelCoefficients(new double[]{c});
        fspec.setTimelinessWeight(tw);
        return LocalPolynomialFilterFactory.of(fspec);
    }

    public static IFiltering musgrave(int h) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(h);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.MMSRE);
        return LocalPolynomialFilterFactory.of(fspec);
    }

    public static IFiltering daf(int h) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(h);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.Direct);
        return LocalPolynomialFilterFactory.of(fspec);
    }

    public static IFiltering cut(int h) {
        LocalPolynomialFilterSpec fspec = new LocalPolynomialFilterSpec();
        fspec.setFilterLength(h);
        fspec.setAsymmetricFilters(AsymmetricFilters.Option.CutAndNormalize);
        return LocalPolynomialFilterFactory.of(fspec);
    }

    public static IFiltering rkhs_frf(int h) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.WhiteNoise);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.FrequencyResponse);
        tspec.setFilterLength(H);
        return RKHSFilterFactory.of(tspec);
    }

    public static IFiltering rkhs_acc(int h) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.WhiteNoise);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.Accuracy);
        tspec.setFilterLength(H);
        return RKHSFilterFactory.of(tspec);
    }

    public static IFiltering rkhs_timeliness(int h) {
        RKHSFilterSpec tspec = new RKHSFilterSpec();
        tspec.setDensity(SpectralDensity.WhiteNoise);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.Timeliness);
        tspec.setFilterLength(H);
        return RKHSFilterFactory.of(tspec);
    }

    private static final double[] X = new double[]{
        -32.6788264, -58.0473876, -66.22635948, -49.27173938, -53.14157742, -61.09071485, -85.76962979
    };

    @Test
    @Ignore
    public void testFilters() {
        int h = 6;
        IFiltering[] ff = new IFiltering[]{lp_c0(h), lp_c1(h, 10), musgrave(h), lp_quad(h, 1, 10),
            daf(h), cut(h), rkhs_frf(h), rkhs_acc(h), rkhs_timeliness(h)
        };
        for (int i = 0; i < ff.length; ++i) {
            IFiniteFilter cf = ff[i].centralFilter();
            IFiniteFilter[] af = ff[i].leftEndPointsFilters();
            for (int j = 0; j < af.length; ++j) {
                fst(af[j]);
            }
            System.out.println();
        }
        System.out.println();
        for (int i = 0; i < ff.length; ++i) {
            IFiniteFilter cf = ff[i].centralFilter();
            IFiniteFilter[] af = ff[i].leftEndPointsFilters();
            for (int j = 0; j < af.length; ++j) {
                ast(cf, af[j]);
            }
            System.out.println();
        }
        System.out.println();
        System.out.println(DoubleSeq.of(X));
        for (int i = 0; i < ff.length; ++i) {
            IFiniteFilter cf = ff[i].centralFilter();
            IFiniteFilter[] af = ff[i].rightEndPointsFilters();
            double[] f = AsymmetricFilters.implicitForecasts(cf, af, DoubleSeq.of(X));System.out.println(DoubleSeq.of(f));
        }

    }

    public static void fst(IFiniteFilter f) {
        System.out.print(FSTFilter.FidelityCriterion.fidelity(f));
        System.out.print('\t');
        System.out.print(FSTFilter.SmoothnessCriterion.smoothness(f));
        System.out.print('\t');
        System.out.print(FSTFilter.TimelinessCriterion.timeliness(f, Math.PI / 8));
        System.out.print('\t');
    }

    public static void ast(IFiniteFilter t, IFiniteFilter r) {
        MSEDecomposition d = MSEDecomposition.of(x -> 1, t.frequencyResponseFunction(), r.frequencyResponseFunction(), Math.PI / 8);
        System.out.print(d.getTotal());
        System.out.print('\t');
        System.out.print(d.getAccuracy());
        System.out.print('\t');
        System.out.print(d.getSmoothness());
        System.out.print('\t');
        System.out.print(d.getTimeliness());
        System.out.print('\t');
    }
}
