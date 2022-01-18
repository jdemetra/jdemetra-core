/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.ssf.akf;

import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.StateStorage;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class QRSmoother {

    public SmoothingOutput process(final ISsf ssf, final ISsfData data, boolean var, boolean rescale) {

        DefaultAugmentedFilteringResults fr = new DefaultAugmentedFilteringResults(true);
        fr.prepare(ssf, 0, data.length());
        AugmentedFilter filter = new AugmentedFilter();
        if (!filter.process(ssf, data, fr)) {
            return null;
        }
        // compute delta, psi
        int len = data.length();
        int n = data.getObsCount();
        int nd = ssf.getDiffuseDim();
        FastMatrix Xl = FastMatrix.make(n, nd);
        DataBlock yl = DataBlock.make(n);

        // fill the matrices
        for (int i = 0, j = 0; i < len; ++i) {
            if (!data.isMissing(i)) {
                double e = Math.sqrt(fr.errorVariance(i));
                yl.set(j, fr.error(i) / e);
                Xl.row(j).setAY(-1 / e, fr.E(i));
                ++j;
            }
        }
        QRSolution ls = QRSolver.robustLeastSquares(yl, Xl);
        FastMatrix psi = ls.RtR();
        SymmetricMatrix.lcholesky(psi);
        DoubleSeq delta = ls.getB();
        double sig2 = ls.getSsqErr() / (n - nd);

        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(var);
        DefaultSmoothingResults rslts = DefaultSmoothingResults.full();
        rslts.prepare(ssf.getStateDim(), 0, len);
        smoother.process(ssf, len, fr, psi, delta, rslts);
        if (rescale) {
            rslts.rescaleVariances(sig2);
        }
        return new SmoothingOutput(fr, sig2, rslts);
    }
}
