/*
* Copyright 2013 National Bank copyOf Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy copyOf the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.maths.matrices.decomposition;

import jdplus.data.DataBlock;
import demetra.design.Development;
import demetra.maths.Constants;

/**
 * A Householder reflection is represented by a matrix of the form H = I -
 * 2/(v'v) * vv' v is called the householder vector.
 *
 * This implementation always uses a transformation that projects x on (|x|,
 * 0...0)
 *
 * See Golub. Van Loan, §5.1
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class HouseholderReflection implements IVectorTransformation {

    private double beta, mu;
    private double[] vector;

    public static HouseholderReflection of(DataBlock v) {
        return of(v, true);
    }

    public static HouseholderReflection of(DataBlock v, boolean apply) {
        int n = v.length();
        double mu, beta;

        double[] x = v.toArray();
        double sig = 0;
        double x0 = x[0];
        for (int i = 1; i < n; ++i) {
            sig += x[i] * x[i];
        }
        if (sig < Constants.getEpsilon()) {
            mu = Math.abs(x0);
            beta = 0;
        } else {
            mu = Math.sqrt(sig + x0 * x0);
            double v0;
            // x-|x|e(1)
            if (x0 <= 0) {
                v0 = x0 - mu;
            } else {
                // (x0-mu)*(x0+mu)/(x0+mu)=(x0^2-mu^2)/(x0+mu)=-sig/x0+mu)
                v0 = -sig / (x0 + mu);
            }
            beta = 2 / (sig + v0 * v0);
            for (int i=0; i<x.length; ++i)
                x[i]/=v0;
        }

        HouseholderReflection reflection = new HouseholderReflection(beta, mu, x);
        if (apply) {
            if (n == 1) {
                v.set(0, mu);
            } else {
                v.set(0);
                v.set(0, mu);
            }
        }
        return reflection;
    }

    @Override
    /**
     * Computes y = H(y) = y - beta*v*v'*y = y - v * (beta*v'y)
     */
    public void transform(DataBlock y) {
        if (beta == 0) {
            return;
        }
        double[] py = y.getStorage();
        int p0=y.getStartPosition(), dp=y.getIncrement();
        if (dp == 1){
            int ycur=p0;
            double s=py[ycur++];
            for (int i=1; i<vector.length; ++i){
                s+=py[ycur++]*vector[i];
            }
            s*=beta;
            py[p0++]-=s;
            for (int i=1; i<vector.length; ++i){
                py[p0++]-=vector[i]*s;
            }
        }else{
            int ycur=p0;
            double s=py[ycur];
            for (int i=1; i<vector.length; ++i){
                ycur+=dp;
                s+=py[ycur]*vector[i];
            }
            s*=beta;
            py[p0]-=s;
            p0+=dp;
            for (int i=1; i<vector.length; ++i){
                p0+=dp;
                py[p0]-=vector[i]*s;
            }
        }
    }
}
