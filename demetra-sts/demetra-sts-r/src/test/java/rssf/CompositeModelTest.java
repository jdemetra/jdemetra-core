/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.msts.AtomicModels;
import jdplus.msts.ModelEquation;
import demetra.data.Data;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.MatrixSerializer;
import demetra.math.functions.Optimizer;
import jdplus.math.matrices.Matrix;
import jdplus.msts.CompositeModel;
import jdplus.msts.CompositeModelEstimation;
import jdplus.ssf.implementations.Loading;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import demetra.math.matrices.MatrixType;
import demetra.ssf.SsfInitialization;
import demetra.timeseries.TsData;
import jdplus.msts.MstsMapping;
import jdplus.sts.LocalLevel;
import jdplus.sts.LocalLinearTrend;

/**
 *
 * @author palatej
 */
public class CompositeModelTest {

    static final MatrixType data;

    static {
        MatrixType tmp = null;
        try {
            URI uri = CompositeModels.class.getResource("/mssf1").toURI();
            tmp = MatrixSerializer.read(new File(uri), "\t|,");
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(CompositeModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        data = tmp;
    }

    public CompositeModelTest() {
    }

    @Test
    public void testAirline() {
        TsData P = Data.TS_PROD;
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.sarima("air", 12, new int[]{0, 1, 1}, new int[]{0, 1, 1}, null, false, 1, false));
        model.add(AtomicModels.tdRegression("td", P.getDomain(), new int[]{1, 2, 3, 4, 5, 6, 0}, true, 0, false));
        ModelEquation eq = new ModelEquation("eq1", 0, true);
        eq.add("air", 1, true, null);
        eq.add("td", 1, true, null);
        model.add(eq);
        int len = Data.PROD.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.PROD, 0);
        CompositeModelEstimation rslt = model.estimate(M, false, true, SsfInitialization.Augmented_NoCollapsing, Optimizer.LevenbergMarquardt, 1e-15, null);
        System.out.println(DataBlock.of(rslt.getFullParameters()));
        System.out.println(rslt.getSmoothedStates().getComponent(19));
        System.out.println(rslt.getSmoothedStates().getComponentVariance(1));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    @Test
    public void testBsm() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", 1, .01, true, false));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, 1, true));
        model.add(AtomicModels.noise("n", 1, true));
        ModelEquation eq = new ModelEquation("eq1", 0, true);
        eq.add("l", 1, true, null);
        eq.add("s", .1, false, null);
        eq.add("n", .1, false, null);
        model.add(eq);
        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModelEstimation rslt = model.estimate(M, false, true, SsfInitialization.Augmented_NoCollapsing, Optimizer.LevenbergMarquardt, 1e-15, null);
        System.out.println(DataBlock.of(rslt.getFullParameters()));
        System.out.println(rslt.getSmoothedStates().getComponent(0));
        System.out.println(rslt.getSmoothedStates().getComponentVariance(0));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    @Test
    public void testX() {
        Matrix x = Matrix.make(data.getRowsCount(), 6);
        x.column(0).copy(data.column(0));
        x.column(1).copy(data.column(9));
        x.column(2).copy(data.column(2));
        x.column(3).copy(data.column(3));
        x.column(4).copy(data.column(5));
        x.column(5).copy(data.column(6));

        DataBlockIterator cols = x.columnsIterator();
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            col.normalize();
        }

        CompositeModel model = new CompositeModel();
// create the components and add them to the model
        model.add(AtomicModels.localLinearTrend("tu", 0, 0.01, true, false));
        model.add(AtomicModels.localLinearTrend("ty", 0, 0.01, true, false));
        model.add(AtomicModels.localLevel("tpicore", 0.01, false, Double.NaN));
        model.add(AtomicModels.localLevel("tpi", 0.01, false, Double.NaN));
        model.add(AtomicModels.ar("cycle", new double[]{1, -.5}, false, 1, true, 4, 4));
        model.add(AtomicModels.localLevel("tb", 0, true, Double.NaN));
        model.add(AtomicModels.localLevel("tc", 0, true, Double.NaN));
// create the equations 

        ModelEquation eq1 = new ModelEquation("eq1", 1, true);
        eq1.add("tu", LocalLinearTrend.defaultLoading());
        eq1.add("cycle", .1, false, Loading.fromPosition(4));
        model.add(eq1);
        ModelEquation eq2 = new ModelEquation("eq2", 0.01, false);
        eq2.add("ty", LocalLinearTrend.defaultLoading());
        eq2.add("cycle", .1, false, Loading.fromPosition(4));
        model.add(eq2);
        ModelEquation eq3 = new ModelEquation("eq3", .01, false);
        eq3.add("tpicore", LocalLevel.defaultLoading());
        eq3.add("cycle", .1, false, Loading.fromPosition(0));
        model.add(eq3);
        ModelEquation eq4 = new ModelEquation("eq4", .01, false);
        eq4.add("tpi", LocalLevel.defaultLoading());
        eq4.add("cycle", .1, false, Loading.fromPosition(4));
        model.add(eq4);
        ModelEquation eq5 = new ModelEquation("eq5", .01, false);
        eq5.add("tb", LocalLevel.defaultLoading());
        eq5.add("cycle", .1, false, Loading.fromPosition(5));
        model.add(eq5);
        ModelEquation eq6 = new ModelEquation("eq6", .01, false);
        eq6.add("tc", LocalLevel.defaultLoading());
        eq6.add("cycle", .1, false, Loading.from(new int[]{5, 6, 7, 8}, new double[]{1, 1, 1, 1}));
        model.add(eq6);
        //estimate the model
        MstsMapping mapping = model.mapping();
        double[] dp = mapping.getDefaultParameters().toArray();
        CompositeModelEstimation rslt = model.estimate(x, false, true, SsfInitialization.Diffuse, Optimizer.BFGS, 1e-15, null);
//        System.out.println(rslt.getLikelihood().logLikelihood());
//        System.out.println(DataBlock.of(rslt.getFullParameters()));
////        System.out.println(rslt.getLikelihood().sigma2());
////        System.out.println(rslt.getFilteringStates().getComponent(0));
////        System.out.println(rslt.getFilteredStates().getComponent(0));
////        System.out.println(rslt.getSmoothedStates().getComponentVariance(0));
////        System.out.println(rslt.getSmoothedStates().getComponentVariance(1));
//        double[] parameters = rslt.getFullParameters().clone();
//        for (int i = 0; i <= 1000; ++i) {
//            double j=(i-500)*.001;
//            parameters[5] =  j*j;
////        for (int i = 0; i <= 500; ++i) {
////            double j=i*.0005;
////            parameters[5] =  j;
//            double ll = model.compute(x, parameters, false, true).getLikelihood().logLikelihood();
//            System.out.println(ll);
//        }
    }
}
