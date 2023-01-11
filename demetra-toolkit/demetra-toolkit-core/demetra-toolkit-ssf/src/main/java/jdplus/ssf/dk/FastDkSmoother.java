/*
 * Copyright 2016 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.dk;

import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.DataBlockResults;
import jdplus.ssf.univariate.DefaultFilteringResults;

/**
 *
 * @author Jean Palate
 */
public class FastDkSmoother {
    
    public static interface VarianceFilterProvider {
        
        int size();
        
        int endDiffusePosition();
        
        boolean isMissing(int pos);
        
        double errorVariance(int pos);
        
        double diffuseNorm2(int pos);
        
        DataBlock M(int pos);
        
        DataBlock Mi(int pos);
        
        FastMatrix P(int pos);
        
        FastMatrix Pi(int pos);
        
        public static VarianceFilterProvider of(final DefaultDiffuseFilteringResults fr) {
            return new VarianceFilterProvider() {
                @Override
                public int size() {
                    return fr.size();
                }
                
                @Override
                public int endDiffusePosition() {
                    return fr.getEndDiffusePosition();
                }
                
                @Override
                public boolean isMissing(int pos) {
                    return fr.isMissing(pos);
                }
                
                @Override
                public double errorVariance(int pos) {
                    return fr.errorVariance(pos);
                }
                
                @Override
                public double diffuseNorm2(int pos) {
                    return fr.diffuseNorm2(pos);
                }
                
                @Override
                public DataBlock M(int pos) {
                    return fr.M(pos);
                }
                
                @Override
                public DataBlock Mi(int pos) {
                    return fr.Mi(pos);
                }
                
                @Override
                public FastMatrix P(int pos) {
                    return fr.P(pos);
                }
                
                @Override
                public FastMatrix Pi(int pos) {
                    return fr.Pi(pos);
                }
                
            };
        }
        
        public static VarianceFilterProvider of(final DefaultFilteringResults fr) {
            return new VarianceFilterProvider() {
                @Override
                public int size() {
                    return fr.size();
                }
                
                @Override
                public int endDiffusePosition() {
                    return 0;
                }
                
                @Override
                public boolean isMissing(int pos) {
                    return fr.isMissing(pos);
                }
                
                @Override
                public double errorVariance(int pos) {
                    return fr.errorVariance(pos);
                }
                
                @Override
                public double diffuseNorm2(int pos) {
                    return 0;
                }
                
                @Override
                public DataBlock M(int pos) {
                    return fr.M(pos);
                }
                
                @Override
                public DataBlock Mi(int pos) {
                    return DataBlock.EMPTY;
                }
                
                @Override
                public FastMatrix P(int pos) {
                    return fr.P(pos);
                }
                
                @Override
                public FastMatrix Pi(int pos) {
                    return FastMatrix.EMPTY;
                }
                
            };
        }
    }
    
    private final VarianceFilterProvider vf;
    private final ISsf ssf;
    private final ISsfLoading loading;
    private final ISsfDynamics dynamics;
    private final int enddiffuse;
    private final DataBlock state;
    private final DataBlockResults A = new DataBlockResults();
    private final DataBlock Rf, Ri, C, Ci;
    
    public FastDkSmoother(ISsf ssf, DefaultDiffuseFilteringResults frslts) {
        this.vf = VarianceFilterProvider.of(frslts);
        this.ssf = ssf;
        loading = ssf.loading();
        dynamics = ssf.dynamics();
        enddiffuse = frslts.getEndDiffusePosition();
        int dim = ssf.getStateDim();
        state = DataBlock.make(dim);
        Rf = DataBlock.make(dim);
        C = DataBlock.make(dim);
        Ri = DataBlock.make(dim);
        Ci = DataBlock.make(dim);
        A.prepare(dim, 0, vf.size());
    }
    
    public void smooth(DoubleSeq x) {
        DoubleSeq fx=forwardFilter(x);
        backwardFilter(fx);
    }
    
    public DataBlockResults smoothedStates(){
        return A;
    }
    
   
    DoubleSeq forwardFilter(DoubleSeq x) {
        int n = vf.size();
        double[] fx=new double[n];
        int pos = 0;
        ssf.initialization().a0(state);
        DoubleSeqCursor cursor = x.cursor();
        while (pos < n) {
            fx[pos]=iterateFilter(pos, cursor.getAndNext());
            pos++;
        }
        return DoubleSeq.of(fx);
    }
    
    private double iterateFilter(int i, double x) {
        // save the current state
        A.save(i, state);
        // retrieve the current information
        boolean missing = vf.isMissing(i);
        double fx = x - loading.ZX(i, state);
        if (!missing) {
            double f;
            DataBlock M;
            if (i < enddiffuse) {
                double fi = vf.diffuseNorm2(i);
                if (fi != 0) {
                    f = fi;
                    M = vf.Mi(i);
                } else {
                    f = vf.errorVariance(i);
                    M = vf.M(i);
                }
            } else {
                f = vf.errorVariance(i);
                M = vf.M(i);
            }
            if (f > 0) {
                state.addAY(fx / f, M);
            }
        }
        dynamics.TX(i, state);
        return fx;
    }
    
    void backwardFilter(DoubleSeq x) {
        int t = x.length();
        while (--t >= 0) {
            if (t >= enddiffuse) {
                iterateSmoother(t, x.get(t));
            } else {
                iterateDiffuseSmoother(t, x.get(t));
            }
        }
    }
    
    private void iterateDiffuseSmoother(int pos, double fx) {
        double f = vf.errorVariance(pos);
        double fi = vf.diffuseNorm2(pos);
        C.copy(vf.M(pos));
        if (fi != 0) {
            Ci.copy(vf.Mi(pos));
            Ci.mul(1 / fi);
            C.addAY(-f, Ci);
            C.mul(1 / fi);
        } else {
            C.mul(1 / f);
            Ci.set(0);
        }
        boolean missing = vf.isMissing(pos);
        dynamics.XT(pos, Rf);
        dynamics.XT(pos, Ri);
        if (!missing) {
            if (fi == 0) {
                double u = fx / f - Rf.dot(C);
                loading.XpZd(pos, Rf, u);
            } else {
                double ci = fx / fi - Ri.dot(Ci) - Rf.dot(C);
                loading.XpZd(pos, Ri, ci);
                double u = -Rf.dot(Ci);
                loading.XpZd(pos, Rf, u);
            }
        }
        DataBlock a = A.datablock(pos);
        a.addProduct(Rf, vf.P(pos).columnsIterator());
        a.addProduct(Ri, vf.Pi(pos).columnsIterator());
    }
    
    private void iterateSmoother(int pos, double fx) {
        double f = vf.errorVariance(pos);
        C.copy(vf.M(pos));
        C.mul(1 / f);
        boolean missing = vf.isMissing(pos);
        dynamics.XT(pos, Rf);
        if (!missing) {
            double u = fx / f - Rf.dot(C);
            loading.XpZd(pos, Rf, u);
        }
        DataBlock a = A.datablock(pos);
        a.addProduct(Rf, vf.P(pos).columnsIterator());
    }
}
