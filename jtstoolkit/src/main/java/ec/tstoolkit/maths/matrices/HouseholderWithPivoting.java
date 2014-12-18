/*
* Copyright 2013 National Bank of Belgium
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

package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;

/**
 * NOT FINISHED !!!
 * @author Jean Palate
 */
public class HouseholderWithPivoting {

    private static double EPS = 2.220446e-16;
    private int[] pivot_;
    private Matrix m_;
    private double[] norm_, rdiag_;
    
    public HouseholderWithPivoting(Matrix m){
        m_=m;
        qrDecomposition();
    }
    
    public double norm(int i){
        return norm_[i];
    }
    
    public int[] getPivots(){
        return pivot_;
    }

    public IReadDataBlock getRDiag(){
        return new ReadDataBlock(rdiag_);
    }
    
    public void applyQty(double[] y){
        int n = m_.getColumnsCount(), m = m_.getRowsCount();
        double[] a = m_.internalStorage();
      
    }
        
    private void qrDecomposition() {
        int n = m_.getColumnsCount(), m = m_.getRowsCount();
        double[] a = m_.internalStorage();
        double[] wa = new double[n];
        // initializations
        for (int k = 0; k < n; ++k) {
            pivot_[k] = k;
            double u = m_.column(k).nrm2();
            norm_[k] = u;
            rdiag_[k] = u;
            wa[k] = u;
        }

        int mn = Math.min(m, n);

        // transform the matrix column after column
        for (int j = 0, jj = 0, mj = m; j < mn; ++j, jj += m + 1, mj += m) {

            // select the column with the greatest norm on active components
            int kmax = j;
            for (int k = j + 1; k < n; ++k) {
                if (rdiag_[k] > rdiag_[kmax]) {
                    kmax = k;
                }
            }
            if (kmax != j) {
                m_.permuteColumns(j, kmax);
                rdiag_[kmax] = rdiag_[j];
                wa[kmax] = wa[j];
                int itmp = pivot_[j];
                pivot_[j] = pivot_[kmax];
                pivot_[kmax] = itmp;
            }

//        COMPUTE THE HOUSEHOLDER TRANSFORMATION TO REDUCE THE
//        J-TH COLUMN OF A TO A MULTIPLE OF THE J-TH UNIT VECTOR.
//
            double ajnorm = m_.column(j).drop(j, 0).nrm2();
            if (ajnorm > EPS) {
                if (a[jj] < 0) {
                    ajnorm = -ajnorm;
                }
                for (int ij = jj; ij < mj; ++ij) {
                    a[ij] += ajnorm;
                }
                a[jj] += 1;
//        APPLY THE TRANSFORMATION TO THE REMAINING COLUMNS
//        AND UPDATE THE NORMS.
                for (int k = j + 1, jk = jj + m; k < n; ++k, jk += m) {
                    double sum = 0;
                    for (int ij = jj, ik = jk; ij < mj; ++ij, ++ik) {
                        sum += a[ij] * a[ik];
                    }
                    double tmp = sum / a[jj];
                    for (int ij = jj, ik = jk; ij < mj; ++ij, ++ik) {
                        a[ik] -= tmp * a[ij];
                    }
                    if (Math.abs(rdiag_[k]) > EPS) {
                        tmp = a[jk] / rdiag_[k];
                        rdiag_[k] *= Math.sqrt(Math.max(1 - tmp * tmp, 0));
                        tmp = rdiag_[k] / wa[k];
                        if (.05 * tmp * tmp <= EPS) {
                            rdiag_[k] = m_.column(k).drop(j + 1, 0).nrm2();
                            wa[k] = rdiag_[k];
                        }
                    }
                }
            }
            rdiag_[j] = -ajnorm;
        }
    }
}
