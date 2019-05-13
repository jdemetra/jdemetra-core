/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.estimation;

import demetra.arima.IArimaModel;
import demetra.arima.StationaryTransformation;
import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.sarima.SarimaUtility;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ArmaFilterTest {

    private static final SarimaModel airline, arima;
    private static final DoubleSeq data;

    static {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        airline = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.6).build();
        spec.setP(3);
        arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).phi(-.2, -.5, -.2).build();
        DataBlock s = DataBlock.copyOf(Data.PROD);
        DataBlock ds = DataBlock.make(s.length() - spec.getDifferenceOrder());
        SarimaUtility.getDifferencingFilter(spec).apply(s, ds);
        data = ds;
    }

    public ArmaFilterTest() {

    }

    @Test
    public void testAirline() {
        ArmaFilter filter = ArmaFilter.ansley();
        int m = filter.prepare((IArimaModel) airline.stationaryTransformation().getStationaryModel(), data.length());
        DataBlock s = DataBlock.make(m);
        filter.apply(data, s);
        double ldet = filter.getLogDeterminant(), ssq = s.ssq();
        filter = ArmaFilter.kalman();
        m = filter.prepare((IArimaModel) airline.stationaryTransformation().getStationaryModel(), data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-8);
        assertEquals(ssq, s.ssq(), 1e-6);
        filter = ArmaFilter.ljungBox();
        m = filter.prepare((IArimaModel) airline.stationaryTransformation().getStationaryModel(), data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-8);
        assertEquals(ssq, s.ssq(), 1e-6);
        filter = ArmaFilter.modifiedLjungBox();
        m = filter.prepare((IArimaModel) airline.stationaryTransformation().getStationaryModel(), data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-8);
        assertEquals(ssq, s.ssq(), 1e-6);
    }

    @Test
    public void testArima() {
        ArmaFilter filter = ArmaFilter.ansley();
        int m = filter.prepare((IArimaModel) arima.stationaryTransformation().getStationaryModel(), data.length());
        DataBlock s = DataBlock.make(m);
        filter.apply(data, s);
        double ldet = filter.getLogDeterminant(), ssq = s.ssq();
        filter = ArmaFilter.kalman();
        m = filter.prepare((IArimaModel) arima.stationaryTransformation().getStationaryModel(), data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-8);
        assertEquals(ssq, s.ssq(), 1e-6);
        filter = ArmaFilter.ljungBox();
        m = filter.prepare((IArimaModel) arima.stationaryTransformation().getStationaryModel(), data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-8);
        assertEquals(ssq, s.ssq(), 1e-6);
        filter = ArmaFilter.modifiedLjungBox();
        m = filter.prepare((IArimaModel) arima.stationaryTransformation().getStationaryModel(), data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-8);
        assertEquals(ssq, s.ssq(), 1e-6);
    }

    public static void stressTest() {
        System.out.println("Stress Test 1");
        int K = 100000;
        IArimaModel[] models = new IArimaModel[]{airline, arima};
        for (IArimaModel model : models) {
            long t0 = System.currentTimeMillis();
            ArmaFilter filter = ArmaFilter.ansley();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                DataBlock s = DataBlock.make(m);
                filter.apply(data, s);
            }
            long t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            filter = ArmaFilter.kalman();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                DataBlock s = DataBlock.make(m);
                filter.apply(data, s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            filter = ArmaFilter.ljungBox();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                DataBlock s = DataBlock.make(m);
                filter.apply(data, s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            filter = ArmaFilter.modifiedLjungBox();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                DataBlock s = DataBlock.make(m);
                filter.apply(data, s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);

            ec.tstoolkit.arima.IArimaModel lmodel = toLegacy(model);

            t0 = System.currentTimeMillis();
            ec.tstoolkit.arima.estimation.AnsleyFilter a = new ec.tstoolkit.arima.estimation.AnsleyFilter();
            for (int i = 0; i < K; ++i) {
                int m = a.initialize((ec.tstoolkit.arima.IArimaModel) lmodel.stationaryTransformation().stationaryModel, data.length());
                ec.tstoolkit.data.DataBlock s = new ec.tstoolkit.data.DataBlock(m);
                a.filter(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(a.getClass().getName());
            System.out.println(t1 - t0);

            t0 = System.currentTimeMillis();
            ec.tstoolkit.arima.estimation.KalmanFilter kf = new ec.tstoolkit.arima.estimation.KalmanFilter();
            for (int i = 0; i < K; ++i) {
                int m = kf.initialize((ec.tstoolkit.arima.IArimaModel) lmodel.stationaryTransformation().stationaryModel, data.length());
                ec.tstoolkit.data.DataBlock s = new ec.tstoolkit.data.DataBlock(m);
                kf.filter(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(kf.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            ec.tstoolkit.arima.estimation.LjungBoxFilter lb = new ec.tstoolkit.arima.estimation.LjungBoxFilter();
            for (int i = 0; i < K; ++i) {
                int m = lb.initialize((ec.tstoolkit.arima.IArimaModel) lmodel.stationaryTransformation().stationaryModel, data.length());
                ec.tstoolkit.data.DataBlock s = new ec.tstoolkit.data.DataBlock(m);
                lb.filter(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(lb.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter mlb = new ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter();
            for (int i = 0; i < K; ++i) {
                int m = mlb.initialize((ec.tstoolkit.arima.IArimaModel) lmodel.stationaryTransformation().stationaryModel, data.length());
                ec.tstoolkit.data.DataBlock s = new ec.tstoolkit.data.DataBlock(m);
                mlb.filter(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(mlb.getClass().getName());
            System.out.println(t1 - t0);
        }
    }

    public static void stressTest2() {
        System.out.println("Stress Test 2");
        int K = 10000, L = 10;
        long t0 = System.currentTimeMillis();
        IArimaModel[] models = new IArimaModel[]{airline, arima};
        for (IArimaModel model : models) {
            ArmaFilter filter = ArmaFilter.ansley();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                for (int j = 0; j < L; ++j) {
                    DataBlock s = DataBlock.make(m);
                    filter.apply(data, s);
                }
            }
            long t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            filter = ArmaFilter.kalman();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                for (int j = 0; j < L; ++j) {
                    DataBlock s = DataBlock.make(m);
                    filter.apply(data, s);
                }
            }
            t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            filter = ArmaFilter.ljungBox();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                for (int j = 0; j < L; ++j) {
                    DataBlock s = DataBlock.make(m);
                    filter.apply(data, s);
                }
            }
            t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            filter = ArmaFilter.modifiedLjungBox();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                for (int j = 0; j < L; ++j) {
                    DataBlock s = DataBlock.make(m);
                    filter.apply(data, s);
                }
            }
            t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);

        }
    }

    private static ec.tstoolkit.arima.IArimaModel toLegacy(IArimaModel model) {
        if (model instanceof SarimaModel) {
            SarimaModel sarima = (SarimaModel) model;
            SarimaSpecification spec = sarima.specification();
            ec.tstoolkit.sarima.SarimaSpecification lspec = new ec.tstoolkit.sarima.SarimaSpecification(spec.getPeriod());
            lspec.setP(spec.getP());
            lspec.setD(spec.getD());
            lspec.setQ(spec.getQ());
            lspec.setBP(spec.getBp());
            lspec.setBD(spec.getBd());
            lspec.setBQ(spec.getBq());
            ec.tstoolkit.sarima.SarimaModel lmodel = new ec.tstoolkit.sarima.SarimaModel(lspec);
            lmodel.setParameters(new ec.tstoolkit.data.ReadDataBlock(sarima.parameters().toArray()));
            return lmodel;
        } else {
            return null;
        }
    }

    @Test
    @Ignore
    public void testAO() {
        int POS=35;
        double[] ao = new double[120];
        ao[POS] = 1;
        ArmaFilter filter = ArmaFilter.ansley();
        StationaryTransformation<SarimaModel> st = airline.stationaryTransformation();
        double[] dao = new double[ao.length - st.getUnitRoots().getDegree()];
        st.getUnitRoots().apply(DataBlock.of(ao), DataBlock.of(dao));
        int m = filter.prepare((IArimaModel) st.getStationaryModel(), dao.length);
        DataBlock s = DataBlock.make(m);
        filter.apply(DoubleSeq.of(dao), s);
        System.out.println(s);
        double[] coefficients = st.getStationaryModel().getPiWeights().getRationalFunction().coefficients(120);
        double[] q=coefficients.clone();
        int start=POS-st.getUnitRoots().getDegree();
        for (int j=start+1; j<dao.length; ++j){
            double a=dao[j];
            if (a != 0){
                for (int k=j; k<q.length; ++k){
                    q[k-start]+=a*coefficients[k-j];
                }
            }
        }
        System.out.println(DoubleSeq.of(q));
    }

    @Test
    @Ignore
    public void testLS() {
        int POS=35;
        double[] ls = new double[120];
        for (int i = 0; i < POS; ++i) {
            ls[i] = -1;
        }
        ArmaFilter filter = ArmaFilter.ansley();
        StationaryTransformation<SarimaModel> st = airline.stationaryTransformation();
        double[] dls = new double[ls.length - st.getUnitRoots().getDegree()];
        st.getUnitRoots().apply(DataBlock.of(ls), DataBlock.of(dls));
        int m = filter.prepare((IArimaModel) st.getStationaryModel(), dls.length);
        DataBlock s = DataBlock.make(m);
        filter.apply(DoubleSeq.of(dls), s);
        System.out.println(s);
        double[] coefficients = st.getStationaryModel().getPiWeights().getRationalFunction().coefficients(120);
        double[] q=coefficients.clone();
        int start=POS-st.getUnitRoots().getDegree();
        for (int j=start+1; j<dls.length; ++j){
            double a=dls[j];
            if (a != 0){
                for (int k=j; k<q.length; ++k){
                    q[k-start]+=a*coefficients[k-j];
                }
            }
        }
        System.out.println(DoubleSeq.of(q));
    }
    
    public static void main(String[] arg){
        stressTest();
        stressTest2();
    }
}
