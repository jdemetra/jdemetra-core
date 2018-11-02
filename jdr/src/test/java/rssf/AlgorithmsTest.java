/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.msts.AtomicModels;
import demetra.msts.ModelEquation;
import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.msts.CompositeModel;
import demetra.msts.CompositeModelEstimation;
import demetra.ssf.implementations.Loading;
import demetra.timeseries.TsDomain;
import java.util.Random;
import org.junit.Test;

/**
 *
 * @author PALATEJ
 */
public class AlgorithmsTest {
    
    private static final double[] abs = new double[]{
       2114.11662,2090.18066,2115.06327,2111.26898,2113.24714,2117.1955,
        2112.47551,2113.92091,2125.84613,2114.01322,2144.64727,2098.51733,
        2121.19032,2108.46249,2126.3321,2112.13307,2125.07808,2137.70313,
        2140.34204,2141.9897,2164.16367,2182.94235,2200.66269,2153.79533,
        2188.23988,2204.00216,2220.21314,2229.8786,2218.42889,2228.86219,
        2216.12738,2229.88724,2231.26576,2225.38434,2261.44005,2200.03538,
        2237.15778,2240.17553,2254.75943,2247.67538,2258.26175,2256.55627,
        2253.72451,2264.82303,2259.03931,2251.08506,2278.94977,2232.08606,
        2253.81256,2260.53883,2266.39143,2256.84092,2245.97918,2239.24204,
        2237.24577,2220.54089,2218.23178,2209.73837,2195.66427,2146.02507,
        2182.20029,2169.23641,2177.22363,2182.69997,2172.39548,2165.54999,
        2154.04733,2163.98056,2172.76196,2197.79534,2219.33888,2149.75229,
        2182.07544,2220.43785,2231.44303,2239.6234,2241.44355,2252.86857,
        2252.07806,2262.0726,2266.46795,2274.8749,2290.41717,2222.24332,
        2275.13786,2271.46591,2275.1735,2275.91665,2266.95151,2275.41759,
        2282.88003,2302.99348,2312.88897,2348.54072,2352.55834,2304.52897,
        2339.85679,2348.02283,2383.31527,2370.79025,2378.16087,2371.693,
        2350.53454,2344.49161,2368.00339,2371.4657,2401.11709,2324.81912,
        2363.88878,2383.61713,2397.29309,2389.91187,2403.8588,2419.32596,
        2393.48663,2419.22349,2433.96364,2434.51108,2495.24454,2466.98761,
        2488.05337,2516.81766,2525.55097,2496.2064,2504.73583,2511.36106,
        2508.64668,2507.6342,2533.3229,2527.46819,2547.70272,2499.05843,
        2548.63875,2563.04966,2578.86463,2593.38192,2591.06928,2611.93429,
        2600.92395,2611.14022,2622.93232,2644.88888,2666.82081,2573.44741,
        2600.43631,2638.76832,2629.37729,2649.58492,2649.30987,2658.45243,
        2626.48678,2636.88394,2633.204,2640.81944,2684.39591,2577.54965,
        2608.92282,2620.21993,2637.29301,2641.27691,2604.60706,2600.80895,
        2588.18232,2590.08465,2577.04777,2582.69192,2603.76263,2539.13753,
        2571.63003,2608.20312,2606.1991,2597.28249,2590.51756,2601.28871,
        2594.64144,2585.56491,2576.30956,2528.34279,2573.20484,2530.45718,
        2523.6766,2534.52112,2533.84997,2545.30292,2544.11671,2547.11822,
        2547.43101,2559.05303,2588.91418,2598.08258,2618.45464,2554.64495,
        2598.18698,2625.16838,2620.3624,2631.08687,2626.31726,2651.83829,
        2639.1698,2678.0905,2671.83538,2682.95094,2726.03154,2668.164,2688.76947,
        2703.18811,2732.38167,2727.97188,2748.97294,2747.73829,2755.38055,
        2777.13368,2784.00751,2795.38385,2824.35641,2746.95599,2771.34652,
        2791.21836,2786.68938,2783.09237,2797.30756,2789.75648,2770.21833,
        2782.82846,2800.41676,2785.19564,2837.7554,2774.86522,2776.44331,
        2809.04768,2808.47718,2793.03862,2782.13913,2797.00523,2762.6002,
        2823.90236,2789.88954,2837.19607,2861.91519,2791.03653,2786.00519,
        2819.8705,2832.98957,2820.44541,2842.07862,2864.64829,2811.93445,
        2877.81392,2879.79085,2851.69062,2906.255,2830.00355,2850.91325,
        2882.80847,2871.65258,2879.18217,2899.84548,2915.29478,2884.49231,
        2925.55192,2945.02283,2934.6284,2986.79368,2902.52169,2938.7594,
        2976.11305,2995.6582,2990.71285,3001.01387,3030.74031,3007.77972,
        3034.12625,3013.69276,2979.4354,3027.64634,2969.56618,2983.04514,
        3002.96081,3041.35684,3026.69391,3029.92776,3034.89504,3022.68799,
        3036.27877,3037.65605,3031.85765,3071.59675,3019.6375,3038.63518,
        3043.07044,3040.94756,3040.09192,3051.2186,3069.36144,3060.39731,
        3074.67779,3083.30867,3089.08025,3128.47193,3087.14433,3118.49008,
        3120.51242,3117.23835,3106.36825,3097.30555,3080.90569,3100.95618,
        3112.09783,3134.10435,3115.70467,3157.54802,3091.35687,3110.05832,
        3135.95895,3143.61949,3138.06537,3138.77952,3135.14087,3129.92921,
        3151.17892,3138.52525,3145.04227,3182.12541,3136.35433,3155.09948,
        3171.3992,3196.4816,3175.50818,3197.93417,3197.13618,3198.26477,
        3204.59813,3201.55848,3208.38297,3252.22264,3185.90391,3210.78226,
        3229.2076,3208.50492,3242.27384,3242.30942,3246.82086,3242.70581,
        3266.08269,3262.01743,3268.85116,3334.66131,3239.32646,3259.47693,
        3281.91944,3282.29134,3313.19161,3324.87125,3311.48856,3311.97612,
        3346.91518,3337.74098,3351.35974,3395.74947,3344.06701,3385.72483,
        3423.54415,3422.2959,3401.0344,3427.05196,3394.02049,3371.02986,
        3397.93363,3385.91496,3384.72282,3434.77791,3365.40042,3382.3912,
        3383.80787,3405.90953,3391.58849,3401.94938,3418.91433,3383.59335,
        3406.87283,3395.64656,3396.91075,3449.92059,3384.22297,3412.56769,
        3423.14558,3440.07596,3419.64897,3439.06413,3442.87959,3453.07842,
        3484.48232,3492.96002,3510.93074,3559.93446,3484.43684,3526.73467,
        3536.0318,3498.64501,3498.22684,3487.40098,3503.17313,3496.95603,
        3520.8552,3512.95493,3523.20013,3545.52885,3483.34623,3510.07325,
        3553.20523,3525.0825,3550.07528,3532.44496,3534.75798,3525.88286,
        3567.58291,3571.27337,3554.44485,3606.50104,3552.18012,3593.5944,
        3611.28671,3628.40318,3611.54875,3607.05479,3590.66621,3586.31619,
        3587.57732,3583.40483,3579.30527,3604.09912,3550.96827,3589.22227,
        3636.35145,3642.09456,3603.35589,3613.3269,3610.83677,3605.88121,
        3613.28788,3622.51039,3634.37323,3662.14338,3576.35241,3622.98055,
        3677.07445,3691.5439,3687.74558,3700.58309,3723.58629,3719.69238,
        3740.38935,3755.52791,3810.05725,3831.33232,3738.34323,3761.8877,
        3802.11042,3814.71921,3828.30759,3815.54664,3810.90193,3783.30427,
        3804.48246,3796.03622,3808.47592,3833.39374    
    };

    public AlgorithmsTest() {
    }

    //@Test
    public void testBsm() {
        int len = abs.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(abs, 0);
        M.column(0).apply(q->Math.log(q));
        
        
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLevel("l", .01, false, 0));
        model.add(AtomicModels.localLinearTrend("lt", 0, .01, true, false));
        model.add(AtomicModels.seasonalComponent("s", "Dummy", 12, .01, false));
//        model.add(AtomicModels.rawTdRegression("td", Data.TS_ABS_RETAIL.getDomain(), new int[]{1,1,1,1,1,0,0}, new double[]{0.01, 0.01}, false));
        model.add(AtomicModels.sae("sae", new double[]{0.5, 0.3}, false, 1e-5, true, 1, false));
        model.add(AtomicModels.noise("n", 1, true));
        ModelEquation eq = new ModelEquation("eq", 0, true);
        eq.add("l");
        eq.add("lt");
        eq.add("s");
        eq.add("sae");
        eq.add("n");//, 1, true, Loading.rescale(Loading.fromPosition(0), w));
        model.add(eq);
//        ModelEquation eqs = new ModelEquation("eqs", 0, true);
//        eqs.add("td", 1, true, Loading.sum());
//        model.add(eqs);
//        System.out.println(DataBlock.ofInternal(model.defaultParameters()));
//        System.out.println(DataBlock.ofInternal(model.fullDefaultParameters()));

        CompositeModelEstimation rslt = model.estimate(M, 1e-15, true, true, null);

        double[] p = rslt.getFullParameters();
        System.out.println("SAE+TD");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
    
    @Test
    public void testSutse() {
        int len = Data.ABS63.length;
        Matrix M = Matrix.make(len, 2);
        M.column(0).copyFrom(Data.ABS63, 0);
        M.column(1).copyFrom(Data.ABS68, 0);
        
        
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLevel("l1", 1, false, 0));
        model.add(AtomicModels.localLinearTrend("lt1", 0, 1, true, false));
        model.add(AtomicModels.seasonalComponent("s1", "Trigonometric", 12, 1, false));
        model.add(AtomicModels.noise("n1", 1, false));
        model.add(AtomicModels.localLevel("l2", 1, false, 0));
        model.add(AtomicModels.localLinearTrend("lt2", 0, 1, true, false));
        model.add(AtomicModels.seasonalComponent("s2", "Trigonometric", 12, 1, false));
        model.add(AtomicModels.noise("n2", 1, false));
        ModelEquation eq1 = new ModelEquation("eq", 0, true);
        eq1.add("l1");
        eq1.add("lt1");
        eq1.add("s1");
        eq1.add("n1");
        model.add(eq1);
        ModelEquation eq2 = new ModelEquation("eq", 0, true);
        eq2.add("l2");
        eq2.add("l1", 0, false, null);
        eq2.add("lt2");
        eq2.add("lt1", 0, false, null);
        eq2.add("s2");
        eq2.add("s1", 0, false, null);
        eq2.add("n2");
        eq2.add("n1", 0, false, null);
        model.add(eq2);
 //        ModelEquation eqs = new ModelEquation("eqs", 0, true);
//        eqs.add("td", 1, true, Loading.sum());
//        model.add(eqs);
//        System.out.println(DataBlock.ofInternal(model.defaultParameters()));
//        System.out.println(DataBlock.ofInternal(model.fullDefaultParameters()));

        CompositeModelEstimation rslt = model.estimate(M, 1e-15, true, true, null);

        double[] p = rslt.getFullParameters();
        System.out.println("SUTSE");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

//    @Test
    public void testAirline() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.sarima("air", 12, new int[]{0,1,1}, new int[]{0,1,1}, null, false, 1, false));
        model.add(AtomicModels.tdRegression("td", Data.TS_ABS_RETAIL.getDomain(), new int[]{1,1,1,1,2,3,0}, false, 0.01, false));
        ModelEquation eq = new ModelEquation("eq1", 0, true);
        eq.add("air");
        eq.add("td");
        model.add(eq);
//        System.out.println(DataBlock.ofInternal(model.defaultParameters()));
//        System.out.println(DataBlock.ofInternal(model.fullDefaultParameters()));

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        M.column(0).apply(q->Math.log(q));
        CompositeModelEstimation rslt = model.estimate(M, 1e-12, true, true, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Airline+TD");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    //@Test
    public void testBsm2() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", .01, .01, false, false));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, .01, false));
        ModelEquation eq = new ModelEquation("eq1", 1, true);
        eq.add("l");
        eq.add("s");
        model.add(eq);

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModelEstimation rslt = model.estimate(M, 1e-12, false, true, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Crude");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
    
    //@Test
    public void testBsmBis() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", .1, .1, false, false));
        model.add(AtomicModels.seasonalComponent("s", "Dummy", 12, 1, false));
        model.add(AtomicModels.noise("n", .01, false));
        ModelEquation eq = new ModelEquation("eq1", 0, true);
        eq.add("l");
        eq.add("s");
        eq.add("n");
        model.add(eq);
//        System.out.println(DataBlock.ofInternal(model.defaultParameters()));
//        System.out.println(DataBlock.ofInternal(model.fullDefaultParameters()));

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModelEstimation rslt = model.estimate(M, 1e-12, false, false, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Dummy-non concentrated");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    //@Test
    public void testBsm2Bis() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", .01, .01, false, false));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, .01, false));
        ModelEquation eq = new ModelEquation("eq1", 1, false);
        eq.add("l");
        eq.add("s");
        model.add(eq);

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModelEstimation rslt = model.estimate(M, 1e-12, false, false, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Crude-Non concentrated");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
}
