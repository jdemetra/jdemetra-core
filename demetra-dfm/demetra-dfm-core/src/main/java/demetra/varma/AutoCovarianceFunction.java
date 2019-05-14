/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.varma;

import demetra.maths.matrices.CanonicalMatrix;
import java.util.ArrayList;

/**
 *
 * @author palatej
 */
public class AutoCovarianceFunction {

    private final ArrayList<CanonicalMatrix> covs_ = new ArrayList<>();
    private final ArrayList<CanonicalMatrix> g_ = new ArrayList<>();
    private final VarmaModel varma_;

    public AutoCovarianceFunction(VarmaModel varma) {
        varma_ = varma;
        g_.add(varma.sig());
        computeInitialCov();

    }
    
    public CanonicalMatrix cov(int lag) {
        
        if (lag >= covs_.size()) {
            compute(lag);
        }
        return covs_.get(lag);
    }
    
    private void computeInitialCov(){
        
    }

    private void compute(int lag) {
        computeg(lag);
        for (int i=covs_.size(); i<=lag; ++i){
            calcCov(i);
        }
    }

    private void computeg(int lag) {
        int n=varma_.getDim(), p=varma_.getP(), q=varma_.getQ();
        if (g_.size() > lag) {
            return;
        }
        for (int i = g_.size(); i <= lag; ++i) {
            CanonicalMatrix g = CanonicalMatrix.square(n);
            if (i <= q) {
                g = varma_.th(i).times(varma_.sig());
            }
            for (int j = 1; j <= Math.min(i, p); ++j) {
                CanonicalMatrix tmp = varma_.phi(j).times(g_.get(i-j));
                g.sub(tmp);
            }
            g_.add(g);
        }
    }

    private void calcCov(int k) {
        int n=varma_.getDim(), p=varma_.getP(), q=varma_.getQ();
        CanonicalMatrix cov=g_.get(k).deepClone();
        for (int i=1; i<=q; ++i){
            cov.add(g_.get(k-i).times(varma_.th(k-i)));
        }
    }

}
