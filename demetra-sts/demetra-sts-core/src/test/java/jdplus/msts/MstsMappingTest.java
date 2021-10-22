/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts;

import demetra.data.Data;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.MatrixSerializer;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.ParamValidation;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.ssf.akf.AkfToolkit;
import jdplus.ssf.dk.SsfFunction;
import jdplus.ssf.dk.SsfFunctionPoint;
import jdplus.ssf.implementations.Loading;
import jdplus.ssf.implementations.MultivariateCompositeSsf;
import jdplus.arima.ssf.SsfAr;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.sts.LocalLevel;
import jdplus.sts.LocalLinearTrend;
import jdplus.ssf.multivariate.M2uAdapter;
import jdplus.ssf.multivariate.SsfMatrix;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;
import demetra.data.DoubleSeq;
import demetra.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class MstsMappingTest {

    public MstsMappingTest() {
    }

    @Test
    public void testSimple() throws URISyntaxException, IOException {

        File file = Data.copyToTempFile(MultivariateCompositeSsf.class.getResource("/mssf1"));
        Matrix data = MatrixSerializer.read(file, "\t|,");

        FastMatrix D = FastMatrix.make(data.getRowsCount(), 4);
        D.column(0).copy(data.column(0));
        D.column(1).copy(data.column(9));
        D.column(2).copy(data.column(2));
        D.column(3).copy(data.column(3));

        DataBlockIterator cols = D.columnsIterator();
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            col.normalize();
        }

        SsfMatrix mdata = new SsfMatrix(D);
        ISsfData udata = M2uAdapter.of(mdata);

        MstsMapping mapping = new MstsMapping();

        // add the parameters
        // 0=tuvar, 1=tyvar, 2=tpivar, 3=tpicorevar, 4=eq2var, 5=eq3var, 6=eq4var
        for (int i = 0; i < 7; ++i) {
            mapping.add(new VarianceInterpreter("", true));
        }
        // loading
        // 7=l-eq1, 8=l-eq2, 9=l-eq3, 10=l-eq4
        for (int i = 0; i < 4; ++i) {
            mapping.add(new LoadingInterpreter(""));
        }

        // AR 11 - 12
        mapping.add(new GenericParameters("", new ARDomain(), new double[]{-.1, -.1}, false));

        // fixed parameters var cycle and var eq1
        VarianceInterpreter vc = new VarianceInterpreter("", 1, true, false);
        mapping.add(vc);
        VarianceInterpreter v1 = new VarianceInterpreter("", 1, true, false);
        mapping.add(v1);

        // Builder
        mapping.add((p, builder) -> {
            builder.add("tu", LocalLinearTrend.of(0, p.get(0)), null);
            builder.add("ty", LocalLinearTrend.of(0, p.get(1)), null);
            builder.add("tpi", LocalLevel.of(p.get(2)), null);
            builder.add("tpicore", LocalLevel.of(p.get(3)), null);
            builder.add("cycle", SsfAr.of(p.extract(11, 2).toArray(), p.get(13), 5), null);
            double v = p.get(14);
            double l = p.get(7);
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("tu", 1, LocalLinearTrend.defaultLoading()));
            eq.add(new MultivariateCompositeSsf.Item("cycle", 1, SsfAr.defaultLoading()));
            builder.add(eq);
            v = p.get(4);
            l = p.get(8);
            eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("ty", 1, LocalLinearTrend.defaultLoading()));
            eq.add(new MultivariateCompositeSsf.Item("cycle", l, SsfAr.defaultLoading()));
            builder.add(eq);
            v = p.get(5);
            l = p.get(9);
            eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("tpicore", 1, LocalLevel.defaultLoading()));
            eq.add(new MultivariateCompositeSsf.Item("cycle", l, Loading.fromPosition(4)));
            builder.add(eq);
            v = p.get(6);
            l = p.get(10);
            eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("tpi", 1, LocalLevel.defaultLoading()));
            eq.add(new MultivariateCompositeSsf.Item("cycle", l, SsfAr.defaultLoading()));
            builder.add(eq);
            return 15;
        });

        SsfFunction<MultivariateCompositeSsf, ISsf> fn = SsfFunction.builder(udata, mapping, m -> M2uAdapter.of(m))
                .useParallelProcessing(true)
                .useScalingFactor(true)
                .useMaximumLikelihood(true)
                .build();

//        MinPackMinimizer lm = new MinPackMinimizer();
        LevenbergMarquardtMinimizer lm = LevenbergMarquardtMinimizer
                .builder()
                .maxIter(1000)
                .build();
        boolean ok = lm.minimize(fn.evaluate(mapping.getDefaultParameters()));
        SsfFunctionPoint rslt = (SsfFunctionPoint) lm.getResult();
        System.out.println(rslt.getLikelihood().logLikelihood());
        System.out.println(rslt.getLikelihood().ser());
        System.out.println(rslt.getParameters());

//        LbfgsMinimizer bfgs = new LbfgsMinimizer();
//        bfgs.setMaxIter(1000);
//        ok = bfgs.minimize(fn.evaluate(mapping.getDefaultParameters()));
//        AkfFunctionPoint rslt2 = (AkfFunctionPoint) bfgs.getResult();
//        System.out.println(rslt2.getLikelihood().logLikelihood());
        MultivariateCompositeSsf mssf = mapping.map(rslt.getParameters());
//        DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(M2uAdapter.of(mssf), udata, true);
        DefaultSmoothingResults srslts = AkfToolkit.smooth(M2uAdapter.of(mssf), udata, true, false, true);
        int[] pos = mssf.componentsPosition();
        for (int k = 0; k < pos.length; ++k) {
            System.out.println(srslts.getComponent(pos[k]).extract(0, D.getRowsCount(), D.getColumnsCount()));
        }
        for (int k = 0; k < pos.length; ++k) {
            System.out.println(srslts.getComponentVariance(pos[k]).extract(0, D.getRowsCount(), D.getColumnsCount()));
        }
    }
}

class ARDomain implements IParametersDomain {

    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
        // only for ar parameters at pos 8,9
        return SarimaMapping.checkStability(-inparams.get(1), -inparams.get(0));
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
        return 1e-6;
    }

    @Override
    public int getDim() {
        return 2;
    }

    @Override
    public double lbound(int idx) {
        return -Double.MAX_VALUE;
    }

    @Override
    public double ubound(int idx) {
        return Double.MAX_VALUE;
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        Polynomial p = Polynomial.of(new double[]{1, -ioparams.get(0), -ioparams.get(1)});
        Polynomial np = SarimaMapping.stabilize(p);
        if (np.equals(p)) {
            return ParamValidation.Valid;
        } else {
            ioparams.set(0, -np.get(1));
            ioparams.set(1, -np.get(2));
            return ParamValidation.Changed;
        }
    }

}
