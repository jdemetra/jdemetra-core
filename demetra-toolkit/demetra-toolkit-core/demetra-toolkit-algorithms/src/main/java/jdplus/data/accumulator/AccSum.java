/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.data.accumulator;

import demetra.data.DoubleSeq;
import nbbrd.design.Development;
import demetra.math.Constants;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Exploratory)
public strictfp class AccSum {

    private static final double EPS = Constants.MACHEP, ETA = Double.MIN_VALUE;

    private static final double LOG2 = Math.log(2);

    private static double log2(double x) {
        return Math.log(x) / LOG2;
    }

    public static class AccurateDouble {

        public double value;
        public double error;
        
        @Override
        public String toString(){
            StringBuilder builder=new StringBuilder();
            builder.append("value=").append(value).append(", error=").append(error);
            return builder.toString();
        }

        /**
         * Error-free split a=x+y into two parts. x+y=a and both x and y need at
         * most 26 bits in the mantissa. Follows T.J. Dekker: A floating-point
         * technique for extending the available precision, Numerische
         * Mathematik 18:224-242, 1971. Requires 4 flops for scalar input.
         */
        public static AccurateDouble split(double a) {
            AccurateDouble s = new AccurateDouble();
            double factor = Math.pow(2, Math.ceil(log2(2 / EPS) / 2) + 1);
            double c = factor * a;
            double ca = c - a;
            s.value = c - ca;
            s.error = a - s.value;
            return s;
        }
    }

    /**
     * Error-free transformation of a+b into x+y with x=fl(a+b)
     *
     * @param a
     * @param b
     * @param sum Out. Array containing x,y
     */
    public void fastTwoSum(double a, double b, AccurateDouble sum) {
        double x = a + b;
        double y = a - x;
        sum.value = x;
        sum.error = y + b;
    }

    /**
     * Error free transformation of a*b into x+y with x=fl(a*b) Follows G.W.
     * Veltkamp, see T.J. Dekker: A floating-point technique for extending the
     * available precision, Numerische Mathematik 18:224-242, 1971. Requires 17
     * flops for scalar input.
     *
     * @param a
     * @param b
     * @param prod
     */
    public void twoProduct(double a, double b, AccurateDouble prod) {
        double x = a * b;
        AccurateDouble sa = AccurateDouble.split(a), sb = AccurateDouble.split(b);
        double y = sa.error * sb.error - (((x - sa.value * sb.value) - sa.error * sb.value) - sa.value * sb.error);
        prod.value = x;
        prod.error = y;
    }

    /**
     * Ultimately fast and accurate summation with faithful rounding. Implements
     * new algorithm in S.M. Rump: Ultimately Fast Accurate Summation, submitted
     * for publication, 2008.
     *
     * @param seq The input
     * @return The sum of the input
     */
    public double fastAccurateSum(DoubleSeq seq) {
        int n = seq.length();
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return seq.get(0);
        }

        // initialize constants, depending on n
        // double c1 = 1 - n * EPS;
        double c2 = 1 - (3 * n + 1) * EPS;
        double c3 = 2 * EPS;
        double c4 = 1 - EPS;
        double c5 = 2 * n * (n + 2) * EPS;
        double c6 = 1 - 5 * EPS;
        double c7 = (1.5 + 4 * EPS) * (n * EPS);
        double c8 = 2 * n * EPS;
        double c9 = ETA / EPS;

        double T = seq.reduce(0, (x, y) -> x + Math.abs(y));
        if (T <= c8) {   // no rouding error
            return T;
        }
        double res = seq.sum();

        double tp = 0;

        double[] p = seq.toArray();

        while (true) {
            double sigma0 = (2 * T) / c2;
            double z = sigma0;
            for (int i = 0; i < p.length; ++i) {
                double z0 = z;
                z += p[i];
                double q = z - z0;
                p[i] -= q;
            }
            double tau = z - sigma0;
            double t = tp;
            tp = t + tau;
            DoubleSeq P = DoubleSeq.of(p);
            if (tp == 0) {
                return fastAccurateSum(P.select(x -> x != 0));
            }
            double nq = sigma0 / c3;
            double u = Math.abs(nq / c4 - nq);
            double phi = (c5 * u) / c6;
            T = Math.min(c7 * sigma0, c8 * u);
            if (Math.abs(tp) >= phi || 4 * T <= c9) {
                double tau2 = (t - tp) + tau;
                res = tp + (tau2 + P.sum());
                break;
            }
        }
        return res;
    }
}

//
//function [x,y] = Split(a)
//%SPLIT       
//%
//%Reference implementation! Slow due to interpretation!
//%
//
//% written  03/03/07     S.M. Rump
//%
//
//  if isa(a,'double'), prec='double'; else prec='single'; end    
//  factor = 2^ceil(log2(2/eps(prec))/2)+1;
//
//  c = factor*a;            % factor('double')=2^27+1, factor('single')=2^12+1
//  if any(~isfinite(c(:)))
//    error('overflow in Split')
//  end
//  x = c - ( c - a );
//  y = a - x;
//function [x,y] = TwoProduct(a,b)
//%TWOPRODUCT   Error free transformation of a+b into x*y with x=fl(a*b)
//%
//%   [x,y] = TwoProduct(a,b)
//%
//%On return, x+y=a*b and x=fl(a*b) provided no over- or underflow occurs .
//%Input a,b may be vectors or matrices as well, in single or double precision.
//%
//%Follows G.W. Veltkamp, see T.J. Dekker: A floating-point technique for 
//%  extending the available precision, Numerische Mathematik 18:224-242, 1971.
//%Requires 17 flops for scalar input.
//%
//%Reference implementation! Slow due to interpretation!
//%
//
//% written  03/03/07     S.M. Rump
//%
//
//  x = a.*b;
//  if any(~isfinite(x(:))) 
//    error('overflow occurred in TwoProduct')
//  end
//  if isa(x,'double'), alpha=realmin('double'); else alpha=realmin('single'); end
//  if any(abs(x(:)))<alpha
//    error('underflow occurred in TwoProduct')
//  end
//  [ah,al] = Split(a);
//  [bh,bl] = Split(b);
//  y = al.*bl - ( ( ( x - ah.*bh ) - al.*bh ) - ah.*bl );
//function res = Dot2(x,y)
//%DOT2         Dot product 'as if' computed in 2-fold (quadruple) precision
//%
//%   res = Dot2(x,y)
//%
//%On return, res approximates x'*y with accuracy as if computed 
//%  in 2-fold precision.
//%
//%Implements algorithm Dot2 from
//%  T. Ogita, S.M. Rump, S. Oishi: Accurate Sum and Dot Product, 
//%    SIAM Journal on Scientific Computing (SISC), 26(6):1955-1988, 2005 .
//%Requires 25n flops.
//%
//%Reference implementation! Slow due to interpretation!
//%
//
//% written  03/03/07     S.M. Rump
//%
//
//  [p,s] = TwoProduct(x(1),y(1));
//  for i=2:length(x)
//    [h,r] = TwoProduct(x(i),y(i));
//    [p,q] = TwoSum(p,h);
//    s = s + ( q + r );
//  end
//  res = p + s;
//function s = DotXBLAS(x,y)
//%DOTXBLAS     Dot product 'as if' computed in 2-fold precision
//%
//%   res = DotXBLAS(x,y)
//%
//%On return, res approximates x'*y with accuracy as if computed 
//%  in 2-fold precision.
//%
//%Implements algorithm BLAS_ddot_x from
//%  X. Li, J. Demmel, D. Bailey, G. Henry, Y. Hida, J. Iskandar, 
//%    W. Kahan, S. Kang, {S.}, A. Kapur, M. Martin, B. Thompson, {B.},
//%    T. Tung, {T.}, D. Yoo: Design, Implementation and Testing of 
//%    Extended and Mixed Precision BLAS, ACM Trans. Math. Software, 
//%    2(28), p. 152-205, 2002.
//%Requires 37n flops.
//%
//%Reference implementation! Slow due to interpretation!
//%
//
//% written  03/03/07     S.M. Rump
//%
//
//  s = 0;
//  t = 0;
//  for i=1:length(x)
//    [h,r] = TwoProduct(x(i),y(i));
//    [s1,s2] = TwoSum(s,h);
//    [t1,t2] = TwoSum(t,r);
//    s2 = s2 + t1;
//    [t1,s2] = FastTwoSum(s1,s2);
//    t2 = t2 + s2;
//    [s,t] = FastTwoSum(t1,t2);
//  end

