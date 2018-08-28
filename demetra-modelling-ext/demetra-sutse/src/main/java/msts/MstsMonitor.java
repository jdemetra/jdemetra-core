/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msts;

import demetra.data.DoubleSequence;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.minpack.MinPackMinimizer;
import demetra.maths.functions.riso.LbfgsMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.ssf.akf.AkfFunction;
import demetra.ssf.akf.AkfFunctionPoint;
import demetra.ssf.akf.MarginalLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.dk.SsfFunctionPoint;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author palatej
 */
public class MstsMonitor {

    private static final int MAXITER=20;
    
    private Matrix data;
    private MstsMapping model;
    private MultivariateCompositeSsf ssf;
    private DoubleSequence prslts, fullp;
    private DefaultSmoothingResults srslts;
    private MarginalLikelihood ll;
    private int[] cpos;
    private int maxiter=MAXITER;

    private final List<VarianceParameter> smallVariances = new ArrayList<>();

    public void process(Matrix data, MstsMapping model) {
        
        this.data = data;
        this.model = model;
        smallVariances.clear();
        SsfMatrix mdata = new SsfMatrix(data);
        ISsfData udata = M2uAdapter.of(mdata);
        prslts = model.getDefaultParameters();
        int niter=0;
        do {
            AkfFunction<MultivariateCompositeSsf, ISsf> fn = AkfFunction.builder(udata, model, m -> M2uAdapter.of(m))
                    .useParallelProcessing(true)
                    .useScalingFactor(true)
                    .useMaximumLikelihood(true)
                    .build();

//            MinPackMinimizer lm = new MinPackMinimizer();
//            LbfgsMinimizer lm = new LbfgsMinimizer();
            LevenbergMarquardtMinimizer lm = new LevenbergMarquardtMinimizer();
//            lm.setMaxIter(500);

//            boolean ok = lm.minimize(fn.evaluate(model.getDefaultParameters()));
            boolean ok = lm.minimize(fn.evaluate(prslts));
            AkfFunctionPoint rslt = (AkfFunctionPoint) lm.getResult();
            ll = rslt.getLikelihood();
            prslts = rslt.getParameters();
            if (!fixSmallVariance(fn, 1e-3) && !freeSmallVariance(fn)) {
                break;
            }
        } while (niter++<maxiter);
        ssf = model.map(prslts);
        cpos = ssf.componentsPosition();
        srslts = DkToolkit.sqrtSmooth(M2uAdapter.of(ssf), udata, true);
    }
    
    private boolean freeSmallVariance(AkfFunction fn){
        if (smallVariances.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceParameter cur = null;
        for (VarianceParameter small : smallVariances) {
            fullp = model.trueParameters(prslts);
            small.fix(1e-5);
            DoubleSequence nprslts = model.functionParameters(fullp);
            MarginalLikelihood nll = fn.evaluate(nprslts).getLikelihood();
            double d = nll.logLikelihood() - ll.logLikelihood();
            if (d > 0 && d > dll) {
                dll = d;
                cur = small;
            }
            small.fix(0);
        }
        if (cur != null) {
            cur.free();
            prslts = model.functionParameters(fullp);
            smallVariances.remove(cur);
            return true;
        } else {
            return false;
        }
        
    }

    private boolean fixSmallVariance(AkfFunction fn, double eps) {
        List<VarianceParameter> svar = model.smallVariances(prslts, 1e-3);
        if (svar.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceParameter cur = null;
        for (VarianceParameter small : svar) {
            fullp = model.trueParameters(prslts);
            small.fix(0);
            DoubleSequence nprslts = model.functionParameters(fullp);
            MarginalLikelihood nll = fn.evaluate(nprslts).getLikelihood();
            double d = nll.logLikelihood() - ll.logLikelihood();
            if (d > 0 && d > dll) {
                dll = d;
                cur = small;
            }
            small.free();
        }
        if (cur != null) {
            cur.fix(0);
            prslts = model.functionParameters(fullp);
            smallVariances.add(cur);
            return true;
        } else {
            return false;
        }

    }

    /**
     * @return the data
     */
    public Matrix getData() {
        return data;
    }

    /**
     * @return the model
     */
    public MstsMapping getModel() {
        return model;
    }

    /**
     * @return the ssf
     */
    public MultivariateCompositeSsf getSsf() {
        return ssf;
    }

    /**
     * @return the prslts
     */
    public DoubleSequence getPrslts() {
        return prslts;
    }

    public DoubleSequence smoothedComponent(int pos) {
        ssf.componentsPosition();
        return srslts.getComponent(cpos[pos]).extract(0, data.getRowsCount(), data.getColumnsCount());
    }

    public DoubleSequence varianceOfSmoothedComponent(int pos) {
        ssf.componentsPosition();
        return srslts.getComponentVariance(cpos[pos]).extract(0, data.getRowsCount(), data.getColumnsCount());
    }

    /**
     * @return the ll
     */
    public MarginalLikelihood getLogLikelihood() {
        return ll;
    }

    /**
     * @return the maxiter
     */
    public int getMaxiter() {
        return maxiter;
    }

    /**
     * @param maxiter the maxiter to set
     */
    public void setMaxiter(int maxiter) {
        this.maxiter = maxiter;
    }
}
