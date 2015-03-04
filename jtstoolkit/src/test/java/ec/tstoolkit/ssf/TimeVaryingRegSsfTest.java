/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.eco.DiffuseLikelihood;
import ec.tstoolkit.maths.matrices.HouseholderC;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author PCUser
 */
public class TimeVaryingRegSsfTest {

    private static double[] g_pitex = new double[]{
        96.2, 94, 100, 91.9, 86.9, 99.8, 58.4, 82.6, 98.6, 103.8, 100.3, 102.2, 99.8, 97.5,
        102.4, 101.3, 100.5, 101.8, 68.2, 86.2, 105.9, 117.2, 104.2, 108.1, 108.8, 105.1,
        111.5, 113.6, 105.6, 117.6, 76.5, 88.5, 114.1, 122.3, 106.7, 114.6, 108.1, 109.4,
        113.9, 118.8, 100, 114.8, 74.1, 87.2, 111.5, 116.1, 105.6, 116.3, 105, 109.3, 124.9,
        116.5, 106, 118.8, 77.2, 98.2, 120.7, 121.2, 116.5, 126.5, 112.8, 118.8, 129.8,
        112.5, 109.7, 127.9, 68.8, 92.2, 117.3, 123.5, 118.8, 118.5, 114.3, 115.4, 126.2,
        112.7, 119.1, 118.4, 72.8, 101.3, 115.5, 127.5, 117.1, 110.2, 122.2, 116.6, 123.5,
        122.3, 117.1, 117.1, 73.2, 86.6, 108.1, 113.9, 94.7, 91.3, 95.2, 95.2, 97, 102.9,
        84.2, 84.8, 65.2, 72.2, 100.1, 109, 93.2, 97.4, 102.9, 108.4, 124.2, 114, 103.7,
        115.6, 62.6, 83.6, 112.4, 106.3, 96.4, 102.1, 98.5, 97, 112.1, 94, 85.1, 100.3, 48,
        73.1, 96.8, 89.8, 89.5, 94.5, 89.1, 86, 95.1, 86.9, 79.2, 96.2, 49.4, 73.5, 95.2,
        98.8, 93.4, 87.5, 91.3, 93, 102.3, 90, 97.4, 95.4, 62.8, 81.1, 97.4, 110.4, 101.3,
        93.4, 106.4, 107.1, 107.2, 103.4, 85.6, 102.2, 66.5, 76.1, 105.7, 110.3, 89, 92.7,
        96.2, 99.3, 107.5, 99.4, 89.3, 104.8, 55.6, 75.2, 103.7, 103, 92.8, 92.9, 93.2, 93.5,
        106.5, 100.5, 86.8, 107.5, 53.1, 75.8, 105.9, 102.4, 93.3, 91.4, 102.5, 104.2, 120.6,
        101.8, 97.1, 114.3, 50.2, 80.8, 112.7, 105, 106.3, 97.8, 106.5, 111.3, 114.6, 97.6,
        109.2, 102.8, 59.3, 81.4, 106, 119.1, 107.4, 86.9, 105.2, 104.6, 110.2, 107.1, 99.3,
        106, 67.7, 76.1, 109.5, 115.6, 106.3, 91.3, 110.5, 106.8, 110.4, 115.5, 96.6, 109.3,
        64.2, 80.3, 114.1, 117.2, 98.6, 90.8, 99.2, 102.7, 112.2, 106.9, 96.7, 109.7, 56.4,
        82.9, 118.7, 120, 108.1, 95.9, 103.4, 110.4, 120.2, 103.4, 100.8, 115.5, 52.5, 85.3,
        115.8, 112, 110.9, 93.6, 111.2, 110.3, 118.1, 111.9, 104.3, 121.5, 60.6, 85.7, 116.7,
        121.1, 118.2, 94.5, 124.6, 118.7, 129.1, 110.3, 118.3, 116.3, 66.9, 90.5, 120.7,
        135.5, 122.8, 93.4, 114.3, 109.8, 117.7, 109.8, 101.4, 112.9, 61.9, 81, 116.3, 127.9,
        110.8, 87.6, 118.4, 118.9, 122, 112.5, 100.6, 114.9, 61.6, 85.3, 128.6, 115.6, 105.1,
        93.9, 105.4, 107.4, 118.5, 102.8, 89.9, 111, 55.5, 79.3, 117.8, 113.3, 103.5, 94.9,
        111.1, 108.8, 122.8, 103.1, 100.3, 116.4, 50.7, 83.6, 113.2, 105.6, 106.3, 86.7
    };
    private static double[] g_pisuc = new double[]{
        34.8, 21, 32.2, 22.4, 18.2, 15.9, 13.3, 16.7, 19, 91.5, 246.5, 258.3, 126.5, 19.7, 24.8,
        16.7, 12.2, 14.3, 10.9, 14.9, 17.6, 211.7, 358.3, 201, 4, 34.6, 31.3, 17, 16.1, 16.8,
        9.6, 18.1, 18.5, 215.4, 329.6, 244.3, 21.4, 16.9, 37.5, 16.8, 14.4, 30, 12, 14.3, 17.4,
        242.3, 323.4, 257.3, 16, 13.6, 18.9, 28.8, 20.4, 43.1, 7.8, 17.2, 23.3, 240.2, 371.5,
        272.5, 24.5, 22.1, 25.6, 29.8, 47.3, 16.4, 10, 17.8, 24.3, 322.1, 380, 318.9, 31, 30.8,
        31.5, 42.3, 22.6, 18.2, 9.3, 21, 27.3, 305.6, 352.2, 251.2, 17.2, 17.9, 34.4, 31.7,
        28.7, 21.2, 9, 16.1, 57.5, 351.1, 379.1, 358.5, 108.2, 20, 34.2, 28.2, 27.1, 17.5, 9.2,
        16.3, 107.8, 373.5, 399.3, 381, 26.7, 15.4, 18.8, 39.7, 21.6, 21.1, 6.6, 19.1, 18.8,
        257.9, 419.3, 234, 16.9, 19.1, 19.5, 28.6, 23.5, 21.5, 17.6, 19, 10, 279.3, 406.3, 291.2,
        19, 16.1, 24.6, 30.1, 18.9, 19.2, 5, 12.3, 34.6, 367.8, 423.5, 229.5, 17, 12.6, 14.7,
        48.4, 24.9, 23.2, 18.6, 17.1, 33.9, 310.1, 365.1, 177.1, 64.1, 74.1, 82.5, 75.7, 70.7,
        78.2, 25.6, 78.3, 96.5, 138.1, 137.2, 93.6, 65.9, 71.6, 79.4, 67.8, 64, 75.4, 16.1, 74.2,
        90.7, 134.3, 132.1, 82, 56.7, 61, 63.7, 63.2, 57.7, 68.5, 16.1, 64, 80.9, 115.7, 115.8,
        91.3, 56, 60.3, 65.6, 53.5, 59.5, 61.4, 17.3, 64.1, 82.8, 112.1, 108.9, 79.6, 60.1, 56.8,
        62.2, 59.1, 54.4, 59.1, 17.2, 56.6, 70.7, 111.2, 107.1, 74.2, 53.8, 53.1, 55.3, 50.1,
        47.3, 53.1, 19.5, 47.8, 72.6, 99.6, 95.5, 64.2, 48.6, 50.3, 57.6, 46.1, 43.2, 52.8, 13.9,
        48.2, 71.1, 91.9, 89.3, 73, 37.1, 42.9, 51.1, 39.8, 37, 49.1, 17.3, 45.2, 52.5, 72.6,
        73.5, 61.3
    };

    private static double[] g_piali = new double[]{
        48.2, 49.6, 57.5, 51.1, 53.8, 61.2, 54.8, 61.3, 58.8, 67.1, 67.4, 55.3, 50.2, 49.9, 56.1,
        55.9, 60.2, 58.5, 59.4, 60.2, 57.8, 69.2, 63.7, 58.3, 56.8, 50.7, 54.9, 59, 57.6, 63.7,
        64.6, 63.7, 62.7, 76.1, 67, 62.9, 54.9, 56.6, 61.5, 65.6, 58.7, 70.5, 66.9, 66.7, 68.9,
        74.3, 68.6, 67.4, 55.5, 58.9, 68.1, 64.7, 59.7, 68.6, 66.6, 70.6, 72, 81, 76, 73.8, 59.7,
        62.3, 72.4, 65.1, 69, 74.6, 61.5, 73.5, 72.8, 78.7, 79, 69.6, 67.1, 64.9, 75.4, 69.3,
        76.7, 76.2, 75.1, 79.3, 78.3, 90.9, 86.9, 71.5, 75.6, 70.5, 75.9, 80.1, 83.1, 79.9,
        78.9, 81.2, 82, 84.2, 78.8, 74.2, 79.8, 67.9, 69, 79.8, 71.1, 78.6, 74.7, 79.2, 80.4,
        90.6, 85.1, 76, 66.8, 70.5, 81.7, 78.9, 76.1, 84.9, 75.5, 77, 80.4, 87.3, 87.1, 84.6,
        69.9, 71, 88.3, 76.5, 75.3, 86.6, 69.1, 84.4, 85.2, 89.9, 90.3, 86.8, 69.9, 69.3, 83.5,
        78.4, 80, 89.1, 69.1, 83.2, 85.4, 96.9, 99, 82.8, 73.2, 71.7, 84.9, 80.6, 85.2, 87.7,
        75.4, 90.7, 84.3, 104, 97.2, 80.9, 81.2, 81.1, 83.1, 87.9, 82.2, 89.1, 77.5, 82.5, 92.2,
        105.8, 87.9, 83.1, 82.5, 79.2, 89.1, 89.7, 82.7, 93, 83.1, 87.5, 92.5, 105.7, 96, 92.4,
        84, 79.8, 95.6, 93, 86.6, 99.3, 84.7, 94, 101.8, 109.6, 103.7, 98.4, 86.8, 83.4, 94.6,
        92, 91, 101.3, 81.2, 104, 103.1, 104.7, 106.7, 94.7, 90.7, 88.1, 96.5, 90.7, 99.8, 99.5,
        86.8, 100.8, 93.8, 118.9, 114, 97.9, 87.6, 86.3, 97.1, 100.9, 97.9, 100.3, 93.2, 99,
        100.7, 128.6, 112.7, 94.9, 97.7, 89.2, 93.2, 105.2, 96, 105.7, 97.7, 99.2, 106.9, 127,
        111.1, 99, 101.4, 96.8, 101.7, 108.5, 99.2, 112.1, 98, 106.4, 117.9, 118.8, 105.3,
        96.9, 98.3, 99.5, 111.4, 103.8, 105, 122.1, 90.3, 113.2, 121.3, 114.7, 112.8, 97.9,
        108.2, 102.2, 115.5, 109.6, 113.5, 126.1, 95.9, 118.4, 122.8, 124.5, 119.5, 99.4,
        117.4, 107, 118.8, 110.3, 117.9, 123.6, 104.1, 123.6, 124.2, 135, 125.6, 102.2, 121.3,
        104.9, 115.8, 120.9, 120.3, 120.5, 112.8, 123.1, 129.5, 137, 120.6, 104.9, 121.5,
        108.9, 119.9, 122.2, 114.2, 128, 120.4, 120.1, 131.9, 132.2, 121.3, 111.8, 112.8,
        111.6, 126.1, 117.8, 109.8, 129, 107.6, 120.8, 132.2, 133.5, 122.1, 116.9, 115.5,
        114.8, 129.4, 114.7, 112.5, 127.1, 96.8, 118.2, 122.9, 122.2, 120, 109.7
    };
    private final TsData s;

    public TimeVaryingRegSsfTest() {
        s = data.Data.P.log();
        //s = new TsData(TsFrequency.Monthly, 1967, 0, g_pitex, true);//.log();
    }

    //@Test
    public void demoTD() {
        DataBlock p = new DataBlock(new double[]{-.2, -.2, 1});
        TDvarMapping mapping = new TDvarMapping(s.getDomain());
        TimeVaryingRegSsf ssf = mapping.map(p);
        SsfModel<TimeVaryingRegSsf> model = new SsfModel(ssf, new SsfData(s.getValues().internalStorage(), null), null, null);
        SsfFunction<TimeVaryingRegSsf> fn = new SsfFunction<>(
                model,
                mapping,
                new SsfAlgorithm());

        LevenbergMarquardtMethod lm = new LevenbergMarquardtMethod();
        lm.minimize(fn, p);
        SsfFunctionInstance<TimeVaryingRegSsf> xfn = (SsfFunctionInstance<TimeVaryingRegSsf>) lm.getResult();
        DiffuseConcentratedLikelihood ll = xfn.getLikelihood();
        System.out.println("td variables");
        System.out.println(ll.getLogLikelihood());
        Smoother smoother = new Smoother();
        ssf = xfn.ssf;
        smoother.setSsf(ssf);
        SmoothingResults rslts = new SmoothingResults();
        smoother.process(new SsfData(s, null), rslts);
        Matrix q = new Matrix(s.getLength(), 6);
        for (int i = ssf.getStateDim() - 6, j = 0; i < ssf.getStateDim(); ++i) {
            q.column(j++).copyFrom(rslts.component(i), 0);
        }
        SubMatrix x = xfn.ssf.getX();
        DataBlock z=new DataBlock(x.getRowsCount());
        for (int i=0; i<z.getLength(); ++i){
            z.set(i, x.row(i).dot(q.row(i)));
        }
        System.out.println(q);
        System.out.println(z);
    }

    //@Test
    public void demoTDc() {
        DataBlock p = new DataBlock(new double[]{-.2, -.2, 1});
        TDvarMapping mapping = new TDvarMapping(s.getDomain(), 0);
        TimeVaryingRegSsf ssf = mapping.map(p);
        SsfModel<TimeVaryingRegSsf> model = new SsfModel(ssf, new SsfData(s.getValues().internalStorage(), null), null, null);
        SsfFunction<TimeVaryingRegSsf> fn = new SsfFunction<>(
                model,
                mapping,
                new SsfAlgorithm());

        LevenbergMarquardtMethod lm = new LevenbergMarquardtMethod();
        lm.minimize(fn, p);
        SsfFunctionInstance<TimeVaryingRegSsf> xfn = (SsfFunctionInstance<TimeVaryingRegSsf>) lm.getResult();
        DiffuseConcentratedLikelihood ll = xfn.getLikelihood();
        System.out.println("td variables");
        System.out.println(ll.getLogLikelihood());
        Smoother smoother = new Smoother();
        ssf = xfn.ssf;
        smoother.setSsf(ssf);
        SmoothingResults rslts = new SmoothingResults();
        smoother.process(new SsfData(s, null), rslts);
        Matrix q = new Matrix(s.getLength(), 6);
        for (int i = ssf.getStateDim() - 6, j = 0; i < ssf.getStateDim(); ++i) {
            q.column(j++).copyFrom(rslts.component(i), 0);
        }
        SubMatrix x = xfn.ssf.getX();
        DataBlock z=new DataBlock(x.getRowsCount());
        for (int i=0; i<z.getLength(); ++i){
            z.set(i, x.row(i).dot(q.row(i)));
        }
        System.out.println(q);
        System.out.println(z);
    }

    //@Test
    public void demoTD2() {
        DataBlock p = new DataBlock(new double[]{-.2, -.2, 1, 1, 1, 1, 1, 1});
        TDvar2Mapping mapping = new TDvar2Mapping(s.getDomain());
        TimeVaryingRegSsf ssf = mapping.map(p);
        SsfModel<TimeVaryingRegSsf> model = new SsfModel(ssf, new SsfData(s.getValues().internalStorage(), null), null, null);
        SsfFunction<TimeVaryingRegSsf> fn = new SsfFunction<>(
                model,
                mapping,
                new SsfAlgorithm());

        LevenbergMarquardtMethod lm = new LevenbergMarquardtMethod();
        lm.minimize(fn, p);
        SsfFunctionInstance<TimeVaryingRegSsf> xfn = (SsfFunctionInstance<TimeVaryingRegSsf>) lm.getResult();
        DiffuseConcentratedLikelihood ll = xfn.getLikelihood();
        System.out.println("td variables -2");
        System.out.println(ll.getLogLikelihood());
        Smoother smoother = new Smoother();
        ssf = xfn.ssf;
        smoother.setSsf(ssf);
        SmoothingResults rslts = new SmoothingResults();
        smoother.process(new SsfData(s, null), rslts);
        Matrix q = new Matrix(s.getLength(), 6);
        for (int i = ssf.getStateDim() - 6, j = 0; i < ssf.getStateDim(); ++i) {
            q.column(j++).copyFrom(rslts.component(i), 0);
        }
        System.out.println(q);
    }

    @Test
    public void dTD2Mapping() {
        DataBlock p = new DataBlock(new double[]{-.2, -.2, 1, 1, 1, 1, 1, 1});
        TDvar2Mapping mapping = new TDvar2Mapping(s.getDomain());
        TimeVaryingRegSsf ssf = mapping.map(p);
        DataBlock p2 = new DataBlock(mapping.map(ssf));
        assertTrue(p.distance(p2) < 1e-9);
    }

    @Test
    public void testTD3Mapping() {
        DataBlock p = new DataBlock(new double[]{-.2, -.2, 1, 1, 1, 1, 1, 1});
        TDvar3Mapping mapping = new TDvar3Mapping(s.getDomain());
        TimeVaryingRegSsf ssf = mapping.map(p);
        DataBlock p2 = new DataBlock(mapping.map(ssf));
        assertTrue(p.distance(p2) < 1e-9);
    }

    //@Test
    public void demoTD3() {
        DataBlock p = new DataBlock(new double[]{-.2, -.2, 1, 1, 1, 1, 1, 1});
        TDvar3Mapping mapping = new TDvar3Mapping(s.getDomain());
        TimeVaryingRegSsf ssf = mapping.map(p);
        SsfModel<TimeVaryingRegSsf> model = new SsfModel(ssf, new SsfData(s.getValues().internalStorage(), null), null, null);
        SsfFunction<TimeVaryingRegSsf> fn = new SsfFunction<>(
                model,
                mapping,
                new SsfAlgorithm());

        LevenbergMarquardtMethod lm = new LevenbergMarquardtMethod();
        lm.minimize(fn, p);
        SsfFunctionInstance<TimeVaryingRegSsf> xfn = (SsfFunctionInstance<TimeVaryingRegSsf>) lm.getResult();
        DiffuseConcentratedLikelihood ll = xfn.getLikelihood();
        System.out.println("td variables -3");
        System.out.println(ll.getLogLikelihood());
        Smoother smoother = new Smoother();
        ssf = xfn.ssf;
        smoother.setSsf(ssf);
        SmoothingResults rslts = new SmoothingResults();
        smoother.process(new SsfData(s, null), rslts);
        
        Matrix q = new Matrix(s.getLength(), 6);
        for (int i = ssf.getStateDim() - 6, j = 0; i < ssf.getStateDim(); ++i) {
            q.column(j++).copyFrom(rslts.component(i), 0);
        }
        SubMatrix x = xfn.ssf.getX();
        DataBlock z=new DataBlock(x.getRowsCount());
        for (int i=0; i<z.getLength(); ++i){
            z.set(i, x.row(i).dot(q.row(i)));
        }
        System.out.println(q);
        System.out.println(z);
    }

    //@Test
    public void demoFixedTD() {
        DataBlock p = new DataBlock(new double[]{-.2, -.2, 1});
        TDfixedMapping mapping = new TDfixedMapping(s.getDomain());
        RegSsf ssf = mapping.map(p);
        SsfModel<RegSsf> model = new SsfModel(ssf, new SsfData(s.getValues().internalStorage(), null), null, null);
        SsfFunction<RegSsf> fn = new SsfFunction<>(
                model,
                mapping,
                new SsfAlgorithm());

        LevenbergMarquardtMethod lm = new LevenbergMarquardtMethod();
        lm.minimize(fn, p);
        SsfFunctionInstance<RegSsf> xfn = (SsfFunctionInstance<RegSsf>) lm.getResult();
        DiffuseConcentratedLikelihood ll = xfn.getLikelihood();
        System.out.println("td variables");
        System.out.println(ll.getLogLikelihood());
    }
}

class TDvarMapping implements IParametricMapping<TimeVaryingRegSsf> {

    private final TsDomain domain;
    private final Matrix X;
    private static final double SCALE = 100;
    private final SarimaMapping sm;
    private final double var;

    TDvarMapping(TsDomain domain) {
        this(domain, 0);
    }

    TDvarMapping(TsDomain domain, double var) {
        this.domain = domain;
        GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        X = RegressionUtilities.matrix(td, domain);
        SarimaSpecification spec = new SarimaSpecification(domain.getFrequency().intValue());
        spec.airline();
        sm = new SarimaMapping(spec, true);
        this.var = var;
    }

    @Override
    public TimeVaryingRegSsf map(IReadDataBlock p) {
        SarimaSpecification spec = new SarimaSpecification(domain.getFrequency().intValue());
        spec.airline();
        SarimaModel arima = new SarimaModel(spec);
        arima.setTheta(1, p.get(0));
        arima.setBTheta(1, p.get(1));
        SsfArima ssf = new SsfArima(arima);
        int n = X.getColumnsCount();
        double nc = n + 1;
        Matrix v = new Matrix(n, n);
        v.set(-1 / nc);
        v.diagonal().set(n / nc);
        if (var == 0) {
            double c = p.get(2) / SCALE;
            v.mul(c * c);
        } else {
            v.mul(var);
        }
        return new TimeVaryingRegSsf(ssf, X.subMatrix(), v);
    }

    @Override
    public IReadDataBlock map(TimeVaryingRegSsf t) {
        SsfArima ssf = (SsfArima) t.getCoreSsf();
        double[] p = new double[var == 0 ? 3 : 2];
        SarimaModel airline = (SarimaModel) ssf.getModel();
        p[0] = airline.theta(1);
        p[1] = airline.btheta(1);
        if (var == 0) {
            double v = t.getNoiseVar().get(0, 0);
            int n = X.getColumnsCount();
            p[2] = Math.sqrt(v / n) * SCALE;
        }
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return sm.checkBoundaries(inparams.rextract(0, 2));
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        if (idx < 2) {
            return sm.epsilon(inparams, idx);
        }
        return inparams.get(2) * .01;
    }

    @Override
    public int getDim() {
        return var == 0 ? 3 : 2;
    }

    @Override
    public double lbound(int idx) {
        if (idx < 2) {
            return sm.lbound(idx);
        } else {
            return -10;
        }
    }

    @Override
    public double ubound(int idx) {
        if (idx < 2) {
            return sm.ubound(idx);
        } else {
            return 10;
        }
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        if (var == 0) {
            ParamValidation pv = ParamValidation.Valid;
            if (ioparams.get(2) < -10) {
                pv = ParamValidation.Changed;
                ioparams.set(2, -10);
            }
            if (ioparams.get(2) > 10) {
                pv = ParamValidation.Changed;
                ioparams.set(2, 10);
            }
            ParamValidation pv2 = sm.validate(ioparams.extract(0, 2));
            if (pv == ParamValidation.Valid && pv2 == ParamValidation.Valid) {
                return ParamValidation.Valid;
            }
            if (pv == ParamValidation.Invalid || pv2 == ParamValidation.Invalid) {
                return ParamValidation.Invalid;
            }
            return ParamValidation.Changed;
        } else {
            return sm.validate(ioparams);
        }
    }

    @Override
    public String getDescription(int idx) {
        if (idx < 2) {
            return sm.getDescription(idx);
        } else {
            return "noise stdev";
        }
    }
}

class TDvar2Mapping implements IParametricMapping<TimeVaryingRegSsf> {

    private final TsDomain domain;
    private final Matrix X;
    private static final double SCALE = 100;
    private final SarimaMapping sm;

    TDvar2Mapping(TsDomain domain) {
        this.domain = domain;
        GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        X = RegressionUtilities.matrix(td, domain);
        SarimaSpecification spec = new SarimaSpecification(domain.getFrequency().intValue());
        spec.airline();
        sm = new SarimaMapping(spec, true);
    }

    @Override
    public TimeVaryingRegSsf map(IReadDataBlock p) {
        SarimaSpecification spec = new SarimaSpecification(domain.getFrequency().intValue());
        spec.airline();
        SarimaModel arima = new SarimaModel(spec);
        arima.setTheta(1, p.get(0));
        arima.setBTheta(1, p.get(1));
        SsfArima ssf = new SsfArima(arima);
        int n = X.getColumnsCount();
        Matrix var = new Matrix(n, n);
        for (int i = 0; i < n; ++i) {
            double v = p.get(2 + i) / SCALE;
            var.set(i, i, v * v);
        }
        return new TimeVaryingRegSsf(ssf, X.subMatrix(), var);
    }

    @Override
    public IReadDataBlock map(TimeVaryingRegSsf t) {
        SsfArima ssf = (SsfArima) t.getCoreSsf();
        int n = X.getColumnsCount();
        double[] p = new double[2 + n];
        SarimaModel airline = (SarimaModel) ssf.getModel();
        p[0] = airline.theta(1);
        p[1] = airline.btheta(1);
        DataBlock diag = t.getNoiseVar().diagonal();
        for (int i = 0; i < n; ++i) {
            p[2 + i] = Math.sqrt(diag.get(i)) * SCALE;
        }
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return sm.checkBoundaries(inparams.rextract(0, 2));
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        if (idx < 2) {
            return sm.epsilon(inparams, idx);
        }
        return inparams.get(idx) * .01;
    }

    @Override
    public int getDim() {
        return 2 + X.getColumnsCount();
    }

    @Override
    public double lbound(int idx) {
        if (idx < 2) {
            return sm.lbound(idx);
        } else {
            return -10;
        }
    }

    @Override
    public double ubound(int idx) {
        if (idx < 2) {
            return sm.ubound(idx);
        } else {
            return 10;
        }
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        ParamValidation pv = ParamValidation.Valid;
        for (int i = 0; i < X.getColumnsCount(); ++i) {
            if (ioparams.get(2 + i) < -10) {
                pv = ParamValidation.Changed;
                ioparams.set(2 + i, -10);
            } else if (ioparams.get(2 + i) > 10) {
                pv = ParamValidation.Changed;
                ioparams.set(2 + i, 10);
            }
        }
        ParamValidation pv2 = sm.validate(ioparams.extract(0, 2));
        if (pv == ParamValidation.Valid && pv2 == ParamValidation.Valid) {
            return ParamValidation.Valid;
        }
        if (pv == ParamValidation.Invalid || pv2 == ParamValidation.Invalid) {
            return ParamValidation.Invalid;
        }
        return ParamValidation.Changed;
    }

    @Override
    public String getDescription(int idx) {
        if (idx < 2) {
            return sm.getDescription(idx);
        } else {
            return "noise stdev " + (idx - 1);
        }
    }

}

class TDvar3Mapping implements IParametricMapping<TimeVaryingRegSsf> {

    private final TsDomain domain;
    private final Matrix X;
    private static final double SCALE = 100;
    private final SarimaMapping sm;
    private final Matrix A, B;

    TDvar3Mapping(TsDomain domain) {
        this.domain = domain;
        GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        X = RegressionUtilities.matrix(td, domain);
        SarimaSpecification spec = new SarimaSpecification(domain.getFrequency().intValue());
        spec.airline();
        sm = new SarimaMapping(spec, true);
        int n = X.getColumnsCount();
        A = new Matrix(n, n);
        double z = n + 1;
        A.set(-1 / z);
        A.diagonal().add(1);
        B = new Matrix(n, n);
        B.set(1 / (z * z));
        B.diagonal().add(1 - 2 / z);
        SymmetricMatrix.lcholesky(B);
    }

    @Override
    public TimeVaryingRegSsf map(IReadDataBlock p) {
        SarimaSpecification spec = new SarimaSpecification(domain.getFrequency().intValue());
        spec.airline();
        SarimaModel arima = new SarimaModel(spec);
        arima.setTheta(1, p.get(0));
        arima.setBTheta(1, p.get(1));
        SsfArima ssf = new SsfArima(arima);
        int n = X.getColumnsCount();
        Matrix var = new Matrix(n, n);
        for (int i = 0; i < n; ++i) {
            double v = p.get(2 + i) / SCALE;
            var.set(i, i, v * v);
        }
        var = A.times(var);
        var = var.times(A);
        return new TimeVaryingRegSsf(ssf, X.subMatrix(), var);
    }

    @Override
    public IReadDataBlock map(TimeVaryingRegSsf t) {
        SsfArima ssf = (SsfArima) t.getCoreSsf();
        int n = X.getColumnsCount();
        double[] p = new double[2 + n];
        SarimaModel airline = (SarimaModel) ssf.getModel();
        p[0] = airline.theta(1);
        p[1] = airline.btheta(1);
        DataBlock d = t.getNoiseVar().diagonal().deepClone();
        LowerTriangularMatrix.rsolve(B, d);
        LowerTriangularMatrix.lsolve(B, d);
        for (int i = 0; i < n; ++i) {
            p[2 + i] = Math.sqrt(d.get(i)) * SCALE;
        }
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return sm.checkBoundaries(inparams.rextract(0, 2));
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        if (idx < 2) {
            return sm.epsilon(inparams, idx);
        }
        return inparams.get(idx) * .01;
    }

    @Override
    public int getDim() {
        return 2 + X.getColumnsCount();
    }

    @Override
    public double lbound(int idx) {
        if (idx < 2) {
            return sm.lbound(idx);
        } else {
            return -10;
        }
    }

    @Override
    public double ubound(int idx) {
        if (idx < 2) {
            return sm.ubound(idx);
        } else {
            return 10;
        }
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        ParamValidation pv = ParamValidation.Valid;
        for (int i = 0; i < X.getColumnsCount(); ++i) {
            if (ioparams.get(2 + i) < -10) {
                pv = ParamValidation.Changed;
                ioparams.set(2 + i, -10);
            }
            if (ioparams.get(2 + i) > 10) {
                pv = ParamValidation.Changed;
                ioparams.set(2 + i, 10);
            }
        }
        ParamValidation pv2 = sm.validate(ioparams.extract(0, 2));
        if (pv == ParamValidation.Valid && pv2 == ParamValidation.Valid) {
            return ParamValidation.Valid;
        }
        if (pv == ParamValidation.Invalid || pv2 == ParamValidation.Invalid) {
            return ParamValidation.Invalid;
        }
        return ParamValidation.Changed;
    }

    @Override
    public String getDescription(int idx) {
        if (idx < 2) {
            return sm.getDescription(idx);
        } else {
            return "noise stdev " + (idx - 1);
        }
    }

}

class TDfixedMapping implements IParametricMapping<RegSsf> {

    private final TsDomain domain;
    private final Matrix X;
    private final SarimaMapping sm;

    TDfixedMapping(TsDomain domain) {
        this.domain = domain;
        GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        X = RegressionUtilities.matrix(td, domain);
        SarimaSpecification spec = new SarimaSpecification(domain.getFrequency().intValue());
        spec.airline();
        sm = new SarimaMapping(spec, true);
    }

    @Override
    public RegSsf map(IReadDataBlock p) {
        SarimaSpecification spec = new SarimaSpecification(domain.getFrequency().intValue());
        spec.airline();
        SarimaModel arima = new SarimaModel(spec);
        arima.setTheta(1, p.get(0));
        arima.setBTheta(1, p.get(1));
        SsfArima ssf = new SsfArima(arima);
        return new RegSsf(ssf, X.subMatrix());
    }

    @Override
    public IReadDataBlock map(RegSsf t) {
        SsfArima ssf = (SsfArima) t.getCoreSsf();
        double[] p = new double[2];
        SarimaModel airline = (SarimaModel) ssf.getModel();
        p[0] = airline.theta(1);
        p[1] = airline.btheta(1);
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return sm.checkBoundaries(inparams);
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return sm.epsilon(inparams, idx);
    }

    @Override
    public int getDim() {
        return 2;
    }

    @Override
    public double lbound(int idx) {
        return sm.lbound(idx);
    }

    @Override
    public double ubound(int idx) {
        return sm.ubound(idx);
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        return sm.validate(ioparams.extract(0, 2));
    }

    @Override
    public String getDescription(int idx) {
        return sm.getDescription(idx);
    }

}
