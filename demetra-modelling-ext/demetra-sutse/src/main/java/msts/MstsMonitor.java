/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msts;

import demetra.data.DoubleSequence;
import demetra.maths.functions.minpack.MinPackMinimizer;
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

/**
 *
 * @author palatej
 */
public class MstsMonitor {

    private Matrix data;
    private MstsMapping model;
    private MultivariateCompositeSsf ssf;
    private DoubleSequence prslts;
    private DefaultSmoothingResults srslts;
    private MarginalLikelihood ll;
    private int[] cpos;

    public void process(Matrix data, MstsMapping model) {
        this.data=data;
        this.model=model;
        SsfMatrix mdata = new SsfMatrix(data);
        ISsfData udata = M2uAdapter.of(mdata);
        do {

            AkfFunction<MultivariateCompositeSsf, ISsf> fn = AkfFunction.builder(udata, model, m -> M2uAdapter.of(m))
                    .useParallelProcessing(true)
                    .useScalingFactor(true)
                    .useMaximumLikelihood(true)
                    .build();

            MinPackMinimizer lm = new MinPackMinimizer();
            lm.setMaxIter(500);
            boolean ok = lm.minimize(fn.evaluate(model.getDefaultParameters()));
            AkfFunctionPoint rslt = (AkfFunctionPoint) lm.getResult();
            ll = rslt.getLikelihood();
            System.out.println(ll.logLikelihood());
            prslts = rslt.getParameters();
        } while (model.fixSmallVariance(prslts, 1e-5));
        ssf = model.map(prslts);
        cpos = ssf.componentsPosition();
        srslts = DkToolkit.sqrtSmooth(M2uAdapter.of(ssf), udata, true);
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
}
