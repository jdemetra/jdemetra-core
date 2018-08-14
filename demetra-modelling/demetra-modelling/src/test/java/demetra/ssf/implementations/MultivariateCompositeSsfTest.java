/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import demetra.ar.ArBuilder;
import demetra.arima.ArimaModel;
import demetra.arima.ssf.SsfArima;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.MatrixSerializer;
import demetra.maths.MatrixType;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.minpack.MinPackMinimizer;
import demetra.maths.functions.riso.LbfgsMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.maths.polynomials.Polynomial;
import demetra.sarima.SarimaMapping;
import demetra.ssf.ISsfLoading;
import demetra.ssf.SsfComponent;
import demetra.ssf.dk.DkLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.dk.SsfFunctionPoint;
import demetra.ssf.implementations.MultivariateCompositeSsf.Item;
import demetra.ssf.models.AR;
import demetra.ssf.models.LocalLevel;
import demetra.ssf.models.LocalLinearTrend;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class MultivariateCompositeSsfTest {

    public MultivariateCompositeSsfTest() {
    }

    //@Test
    public void testSimple() throws URISyntaxException, IOException {

        URI uri = MultivariateCompositeSsf.class.getResource("/mssf1").toURI();
        MatrixType data = MatrixSerializer.read(new File(uri), "\t|,");

        Matrix D = Matrix.make(data.getRowsCount(), 4);
        D.column(0).copy(data.column(0));
        D.column(1).copy(data.column(9));
        D.column(2).copy(data.column(2));
        D.column(3).copy(data.column(3));

        SsfMatrix mdata = new SsfMatrix(D);
        ISsfData udata = M2uAdapter.of(mdata);

        Mapping mapping = new Mapping();
        SsfFunction<MultivariateCompositeSsf, ISsf> fn = SsfFunction.builder(udata, mapping, m -> M2uAdapter.of(m))
                .useParallelProcessing(true)
                .useScalingFactor(true)
                .useMaximumLikelihood(true)
                .build();

        MinPackMinimizer lm = new MinPackMinimizer();
//        LevenbergMarquardtMinimizer lm = new LevenbergMarquardtMinimizer();
        lm.setMaxIter(1000);
        boolean ok = lm.minimize(fn.evaluate(mapping.getDefaultParameters()));
        SsfFunctionPoint rslt = (SsfFunctionPoint) lm.getResult();
        System.out.println(rslt.getLikelihood().logLikelihood());
        System.out.println(rslt.getLikelihood().ser());
        System.out.println(rslt.getParameters());

//        LbfgsMinimizer bfgs = new LbfgsMinimizer();
//        bfgs.setMaxIter(1000);
//        ok = bfgs.minimize(fn.evaluate(mapping.getDefaultParameters()));
//        SsfFunctionPoint rslt2 = (SsfFunctionPoint) bfgs.getResult();
//        System.out.println(rslt2.getLikelihood().logLikelihood());

        MultivariateCompositeSsf mssf = mapping.map(rslt.getParameters());
        DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(M2uAdapter.of(mssf), udata, true);
        int[] pos = mssf.componentsPosition();
        for (int k = 0; k < pos.length; ++k) {
            System.out.println(srslts.getComponent(pos[k]).extract(0, D.getRowsCount(), D.getColumnsCount()));
        }
        for (int k = 0; k < pos.length; ++k) {
            System.out.println(srslts.getComponentVariance(pos[k]).extract(0, D.getRowsCount(), D.getColumnsCount()));
        }
    }

    //@Test
    public void testSimpleX() throws URISyntaxException, IOException {

        URI uri = MultivariateCompositeSsf.class.getResource("/mssf1").toURI();
        MatrixType data = MatrixSerializer.read(new File(uri), "\t|,");

        Matrix D = Matrix.make(data.getRowsCount(), 6);
        D.column(0).copy(data.column(0));
        D.column(1).copy(data.column(9));
        D.column(2).copy(data.column(2));
        D.column(3).copy(data.column(3));
        D.column(4).copy(data.column(5));
        D.column(5).copy(data.column(6));
        
        // remove the means:
        D.column(4).sub(D.column(4).average());
        D.column(5).sub(D.column(5).average());
        SsfMatrix mdata = new SsfMatrix(D);
        ISsfData udata = M2uAdapter.of(mdata);

        Mapping2 mapping = new Mapping2();
        SsfFunction<MultivariateCompositeSsf, ISsf> fn = SsfFunction.builder(udata, mapping, m -> M2uAdapter.of(m))
                .useParallelProcessing(true)
                .useScalingFactor(true)
                .useMaximumLikelihood(true)
                .build();

        MinPackMinimizer lm = new MinPackMinimizer();
//        LevenbergMarquardtMinimizer lm = new LevenbergMarquardtMinimizer();
        lm.setMaxIter(1000);
        boolean ok = lm.minimize(fn.evaluate(mapping.getDefaultParameters()));
        SsfFunctionPoint rslt = (SsfFunctionPoint) lm.getResult();
        System.out.println(rslt.getLikelihood().logLikelihood());
        System.out.println(rslt.getLikelihood().ser());
        System.out.println(rslt.getParameters());
        LbfgsMinimizer bfgs = new LbfgsMinimizer();
        bfgs.setMaxIter(1000);
        ok = bfgs.minimize(fn.evaluate(mapping.getDefaultParameters()));
        SsfFunctionPoint rslt2 = (SsfFunctionPoint) bfgs.getResult();
        System.out.println(rslt2.getLikelihood().logLikelihood());
        System.out.println(rslt2.getParameters());

        MultivariateCompositeSsf mssf = mapping.map(rslt2.getParameters());
        DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(M2uAdapter.of(mssf), udata, false);
        int[] pos = mssf.componentsPosition();
        for (int k = 0; k < pos.length; ++k) {
            System.out.println(srslts.getComponent(pos[k]).extract(0, D.getRowsCount(), D.getColumnsCount()));
        }
    }
}

class Mapping implements IParametricMapping<MultivariateCompositeSsf> {

    private static final int ARPOS = 7;

    private double p(DoubleSequence p, int pos) {
        double x = p.get(pos);
        return pos < ARPOS ? x * x : x;
    }

    @Override
    public MultivariateCompositeSsf map(DoubleSequence p) {
        SsfComponent ar = AR.componentOf(new double[]{p.get(ARPOS), p.get(ARPOS + 1)}, 1, 5);

        MultivariateCompositeSsf.Equation eq1 = new MultivariateCompositeSsf.Equation(1);
        MultivariateCompositeSsf.Equation eq2 = new MultivariateCompositeSsf.Equation(p(p, 4));
        MultivariateCompositeSsf.Equation eq3 = new MultivariateCompositeSsf.Equation(p(p, 5));
        MultivariateCompositeSsf.Equation eq4 = new MultivariateCompositeSsf.Equation(p(p, 6));
        eq1.add(new Item("tu"));
        eq1.add(new Item("cycle", p(p, ARPOS + 2)));
        eq2.add(new Item("ty"));
        eq2.add(new Item("cycle", p(p, ARPOS + 3)));
        eq3.add(new Item("tpicore"));
        eq3.add(new Item("cycle", p(p, ARPOS + 4), Loading.create(4)));
        eq4.add(new Item("tpi"));
        eq4.add(new Item("cycle", p(p, ARPOS + 5)));

        return MultivariateCompositeSsf.builder()
                .add("tu", LocalLinearTrend.of(0, p(p, 0)))
                .add("ty", LocalLinearTrend.of(0, p(p, 1)))
                .add("tpicore", LocalLevel.of(p(p, 2)))
                .add("tpi", LocalLevel.of(p(p, 3)))
                .add("cycle", ar)
                .add(eq1)
                .add(eq2)
                .add(eq3)
                .add(eq4)
                .build();
    }

    @Override
    public DoubleSequence getDefaultParameters() {
        double[] p = new double[13];
        for (int i = 0; i < 13; ++i) {
            p[i] = 0.1;
        }
        return DoubleSequence.ofInternal(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSequence inparams) {
        // only for ar parameters at pos 8,9
        return SarimaMapping.checkStability(-inparams.get(ARPOS + 1), -inparams.get(ARPOS));
    }

    @Override
    public double epsilon(DoubleSequence inparams, int idx) {
        if (idx == ARPOS || idx == ARPOS + 1) {
            return 1e-6;
        } else {
            return Math.max(1e-8, Math.abs(inparams.get(idx)) * 1e-4);
        }
    }

    @Override
    public int getDim() {
        return 13;
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
        Polynomial p = Polynomial.of(new double[]{1, -ioparams.get(ARPOS), -ioparams.get(ARPOS + 1)});
        Polynomial np = SarimaMapping.stabilize(p);
        if (np.equals(p)) {
            return ParamValidation.Valid;
        } else {
            ioparams.set(ARPOS, -np.get(1));
            ioparams.set(ARPOS + 1, -np.get(2));
            return ParamValidation.Changed;
        }
    }

}

class Mapping2 implements IParametricMapping<MultivariateCompositeSsf> {

    private static final int ARPOS = 9, DIM=17;

    private double p(DoubleSequence p, int pos) {
        double x = p.get(pos);
        return pos < ARPOS ? x * x : x;
    }

    @Override
    public MultivariateCompositeSsf map(DoubleSequence p) {
        double c1 = p.get(ARPOS), c2 = p.get(ARPOS + 1);
        SsfComponent ar = AR.componentOf(new double[]{c1, c2}, 1, 5);

        MultivariateCompositeSsf.Equation eq1 = new MultivariateCompositeSsf.Equation(1);
        MultivariateCompositeSsf.Equation eq2 = new MultivariateCompositeSsf.Equation(p(p, 4));
        MultivariateCompositeSsf.Equation eq3 = new MultivariateCompositeSsf.Equation(p(p, 5));
        MultivariateCompositeSsf.Equation eq4 = new MultivariateCompositeSsf.Equation(p(p, 6));
        MultivariateCompositeSsf.Equation eq5 = new MultivariateCompositeSsf.Equation(p(p, 7));
        MultivariateCompositeSsf.Equation eq6 = new MultivariateCompositeSsf.Equation(p(p, 8));
        int pcur=ARPOS+2;
        eq1.add(new Item("tu"));
        eq1.add(new Item("cycle", p(p, pcur++)));
        eq2.add(new Item("ty"));
        eq2.add(new Item("cycle", p(p, pcur++)));
        eq3.add(new Item("tpicore"));
        eq3.add(new Item("cycle", p(p, pcur++), Loading.create(4)));
        eq4.add(new Item("tpi"));
        eq4.add(new Item("cycle", p(p, pcur++)));
        double b1 = p(p, pcur++);
        ISsfLoading pl = Loading.create(new int[]{0, 1}, new double[]{b1 * c1, b1 * c2});
        eq5.add(new Item("cycle", 1, pl));
        double b2=p(p, pcur);
        double c12=c1*c1, c13=c12*c1, c14=c13*c1, c22=c2*c2;
        double d1=c1+c12+c13+c14+c2+c22+2*c1*c2+3*c12*c2; 
        double d2=c2+c1*c2+c22+c12*c2+2*c1*c22+c13*c2;
        ISsfLoading p2 = Loading.create(new int[]{0, 1}, new double[]{b2 * d1, b2 * d2});
        eq6.add(new Item("cycle", 1, p2));

        return MultivariateCompositeSsf.builder()
                .add("tu", LocalLinearTrend.of(0, p(p, 0)))
                .add("ty", LocalLinearTrend.of(0, p(p, 1)))
                .add("tpicore", LocalLevel.of(p(p, 2)))
                .add("tpi", LocalLevel.of(p(p, 3)))
                .add("cycle", ar)
                .add(eq1)
                .add(eq2)
                .add(eq3)
                .add(eq4)
                .add(eq5)
                .add(eq6)
                .build();
    }

    @Override
    public DoubleSequence getDefaultParameters() {
        double[] p = new double[DIM];
        for (int i = 0; i < DIM; ++i) {
            p[i] = 0.1;
        }
        return DoubleSequence.ofInternal(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSequence inparams) {
        // only for ar parameters at pos 8,9
        return SarimaMapping.checkStability(-inparams.get(ARPOS + 1), -inparams.get(ARPOS));
    }

    @Override
    public double epsilon(DoubleSequence inparams, int idx) {
        if (idx == ARPOS || idx == ARPOS + 1) {
            return 1e-6;
        } else {
            return Math.max(1e-8, Math.abs(inparams.get(idx)) * 1e-4);
        }
    }

    @Override
    public int getDim() {
        return DIM;
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
        Polynomial p = Polynomial.of(new double[]{1, -ioparams.get(ARPOS), -ioparams.get(ARPOS + 1)});
        Polynomial np = SarimaMapping.stabilize(p);
        if (np.equals(p)) {
            return ParamValidation.Valid;
        } else {
            ioparams.set(ARPOS, -np.get(1));
            ioparams.set(ARPOS + 1, -np.get(2));
            return ParamValidation.Changed;
        }
    }

}
