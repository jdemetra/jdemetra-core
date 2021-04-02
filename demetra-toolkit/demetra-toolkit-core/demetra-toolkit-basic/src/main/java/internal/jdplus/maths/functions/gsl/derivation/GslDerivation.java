/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.jdplus.maths.functions.gsl.derivation;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import internal.jdplus.maths.functions.gsl.Utility;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class GslDerivation {

    private DerivationResult central_derivation(DoubleUnaryOperator fn, double x, double h) {
        /* Compute the derivative using the 5-point rule (x-h, x-h/2, x,
     x+h/2, x+h). Note that the central point is not used.  

     Compute the error using the difference between the 5-point and
     the 3-point rule (x-h,x,x+h). Again the central point is not
     used. */

        double fm1 = fn.applyAsDouble(x - h);
        double fp1 = fn.applyAsDouble(x + h);

        double fmh = fn.applyAsDouble(x - h / 2);
        double fph = fn.applyAsDouble(x + h / 2);

        double r3 = 0.5 * (fp1 - fm1);
        double r5 = (4.0 / 3.0) * (fph - fmh) - (1.0 / 3.0) * r3;

        double e3 = (Math.abs(fp1) + Math.abs(fm1)) * Utility.GSL_DBL_EPSILON;
        double e5 = 2.0 * (Math.abs(fph) + Math.abs(fmh)) * Utility.GSL_DBL_EPSILON + e3;

        /* The next term is due to finite precision in x+h = O (eps * x) */
        double dy = Math.max(Math.abs(r3 / h), Math.abs(r5 / h)) * (Math.abs(x) / h) * Utility.GSL_DBL_EPSILON;

        /* The truncation error in the r5 approximation itself is O(h^4).
     However, for safety, we estimate the error from r5-r3, which is
     O(h^2).  By scaling h we will minimise this estimated error, not
     the actual truncation error in r5. */
        return new DerivationResult(r5 / h, Math.abs((r5 - r3) / h), Math.abs(e5 / h) + dy);
    }

    public double centralDerivation(DoubleUnaryOperator fn, double x, double h) {
        DerivationResult r0 = central_derivation(fn, x, h);
        double r_0 = r0.getDf();
        double trunc = r0.getTruncationError();
        double round = r0.getRoundingError();
        double error = round + trunc;

        if (round < trunc && (round > 0 && trunc > 0)) {

            /* Compute an optimised stepsize to minimize the total error,
         using the scaling of the truncation error (O(h^2)) and
         rounding error (O(1/h)). */
            double h_opt = h * Math.pow(round / (2.0 * trunc), 1.0 / 3.0);
            DerivationResult opt = central_derivation(fn, x, h_opt);
            double trunc_opt = opt.getTruncationError();
            double round_opt = opt.getRoundingError();
            double error_opt = round_opt + trunc_opt;
            double r_opt = opt.getDf();

            /* Check that the new error is smaller, and that the new derivative 
         is consistent with the error bounds of the original estimate. */
            if (error_opt < error && Math.abs(r_opt - r_0) < 4.0 * error) {
                return r_opt;
            }
        }

        return r_0;

    }

    DerivationResult forward_derivation(DoubleUnaryOperator fn, double x, double h) {
        /* Compute the derivative using the 4-point rule (x+h/4, x+h/2,
     x+3h/4, x+h).

     Compute the error using the difference between the 4-point and
     the 2-point rule (x+h/2,x+h).  */

        double f1 = fn.applyAsDouble(x + h / 4.0);
        double f2 = fn.applyAsDouble(x + h / 2.0);
        double f3 = fn.applyAsDouble(x + (3.0 / 4.0) * h);
        double f4 = fn.applyAsDouble(x + h);

        double r2 = 2.0 * (f4 - f2);
        double r4 = (22.0 / 3.0) * (f4 - f3) - (62.0 / 3.0) * (f3 - f2)
                + (52.0 / 3.0) * (f2 - f1);

        /* Estimate the rounding error for r4 */
        double e4 = 2 * 20.67 * (Math.abs(f4) + Math.abs(f3) + Math.abs(f2) + Math.abs(f1)) * Utility.GSL_DBL_EPSILON;

        /* The next term is due to finite precision in x+h = O (eps * x) */
        double dy = Math.max(Math.abs(r2 / h), Math.abs(r4 / h)) * Math.abs(x / h) * Utility.GSL_DBL_EPSILON;

        /* The truncation error in the r4 approximation itself is O(h^3).
     However, for safety, we estimate the error from r4-r2, which is
     O(h).  By scaling h we will minimise this estimated error, not
     the actual truncation error in r4. */
        return new DerivationResult(r4 / h, Math.abs((r4 - r2) / h), Math.abs(e4 / h) + dy);
    }

    public double forwardDerivation(DoubleUnaryOperator fn, double x, double h) {

        DerivationResult r0 = forward_derivation(fn, x, h);
        double r_0 = r0.getDf();
        double trunc = r0.getTruncationError();
        double round = r0.getRoundingError();
        double error = round + trunc;

        if (round < trunc && (round > 0 && trunc > 0)) {

            /* Compute an optimised stepsize to minimize the total error,
         using the scaling of the estimated truncation error (O(h)) and
         rounding error (O(1/h)). */
            double h_opt = h * Math.pow(round / (trunc), 1.0 / 2.0);
            DerivationResult opt = forward_derivation(fn, x, h_opt);
            double trunc_opt = opt.getTruncationError();
            double round_opt = opt.getRoundingError();
            double error_opt = round_opt + trunc_opt;
            double r_opt = opt.getDf();

            /* Check that the new error is smaller, and that the new derivative 
         is consistent with the error bounds of the original estimate. */
            if (error_opt < error && Math.abs(r_opt - r_0) < 4.0 * error) {
                return r_opt;
            }
        }
        return r_0;
    }

    public double backwardDerivation(DoubleUnaryOperator fn, double x, double h) {
        return forwardDerivation(fn, x, -h);
    }

    private MDerivationResult central_derivation(DoubleFunction<DoubleSeq> fn, double x, double h) {
        /* Compute the derivative using the 5-point rule (x-h, x-h/2, x,
     x+h/2, x+h). Note that the central point is not used.  

     Compute the error using the difference between the 5-point and
     the 3-point rule (x-h,x,x+h). Again the central point is not
     used. */

        DoubleSeq fml = fn.apply(x - h);
        DoubleSeq fpl = fn.apply(x + h);

        DoubleSeq fmh = fn.apply(x - h / 2);
        DoubleSeq fph = fn.apply(x + h / 2);

        int n = fpl.length();
        double[] df = new double[n];
        double terr = 0, rerr = 0;
        DoubleSeqCursor plcur = fpl.cursor();
        DoubleSeqCursor mlcur = fml.cursor();
        DoubleSeqCursor phcur = fph.cursor();
        DoubleSeqCursor mhcur = fmh.cursor();
        for (int i = 0; i < n; ++i) {
            double pl = plcur.getAndNext(), ml = mlcur.getAndNext();
            double ph = phcur.getAndNext(), mh = mhcur.getAndNext();

            double r3 = 0.5 * (pl - ml);
            double r5 = (4.0 / 3.0) * (ph - mh) - (1.0 / 3.0) * r3;

            double e3 = (Math.abs(pl) + Math.abs(ml)) * Utility.GSL_DBL_EPSILON;
            double e5 = 2.0 * (Math.abs(ph) + Math.abs(mh)) * Utility.GSL_DBL_EPSILON + e3;

            /* The next term is due to finite precision in x+h = O (eps * x) */
            double dy = Math.max(Math.abs(r3 / h), Math.abs(r5 / h)) * (Math.abs(x) / h) * Utility.GSL_DBL_EPSILON;
            df[i] = r5 / h;
            double te = Math.abs((r5 - r3) / h), re = Math.abs(e5 / h) + dy;
            if (te > terr) {
                terr = te;
            }
            if (re > rerr) {
                rerr = re;
            }
        }
        return new MDerivationResult(df, terr, rerr);
    }

    public DoubleSeq centralDerivation(DoubleFunction<DoubleSeq> fn, double x, double h) {
        MDerivationResult r0 = central_derivation(fn, x, h);
        double[] r_0 = r0.getDf();
        double trunc = r0.getTruncationError();
        double round = r0.getRoundingError();
        double error = round + trunc;

        if (round < trunc && (round > 0 && trunc > 0)) {

            /* Compute an optimised stepsize to minimize the total error,
         using the scaling of the truncation error (O(h^2)) and
         rounding error (O(1/h)). */
            double h_opt = h * Math.pow(round / (2.0 * trunc), 1.0 / 3.0);
            MDerivationResult opt = central_derivation(fn, x, h_opt);
            double trunc_opt = opt.getTruncationError();
            double round_opt = opt.getRoundingError();
            double error_opt = round_opt + trunc_opt;
            double[] r_opt = opt.getDf();

            /* Check that the new error is smaller, and that the new derivative 
         is consistent with the error bounds of the original estimate. */
            double dmax = 0;
            for (int i = 0; i < r_0.length; ++i) {
                double d = Math.abs(r_opt[i] - r_0[i]);
                if (d > dmax) {
                    dmax = d;
                }
            }
            if (error_opt < error && dmax < 4.0 * error) {
                return DoubleSeq.of(r_opt);
            }
        }
        return DoubleSeq.of(r_0);
    }

    MDerivationResult forward_derivation(DoubleFunction<DoubleSeq> fn, double x, double h) {
        /* Compute the derivative using the 4-point rule (x+h/4, x+h/2,
     x+3h/4, x+h).

     Compute the error using the difference between the 4-point and
     the 2-point rule (x+h/2,x+h).  */

        DoubleSeq f1 = fn.apply(x + h / 4.0);
        DoubleSeq f2 = fn.apply(x + h / 2.0);
        DoubleSeq f3 = fn.apply(x + (3.0 / 4.0) * h);
        DoubleSeq f4 = fn.apply(x + h);

        DoubleSeqCursor c1 = f1.cursor();
        DoubleSeqCursor c2 = f2.cursor();
        DoubleSeqCursor c3 = f3.cursor();
        DoubleSeqCursor c4 = f4.cursor();
        int n = f1.length();
        double[] df = new double[n];
        double terr = 0, rerr = 0;
        for (int i = 0; i < n; ++i) {

            double p1 = c1.getAndNext();
            double p2 = c2.getAndNext();
            double p3 = c3.getAndNext();
            double p4 = c4.getAndNext();
            double r2 = 2.0 * (p4 - p2);
            double r4 = (22.0 / 3.0) * (p4 - p3) - (62.0 / 3.0) * (p3 - p2)
                    + (52.0 / 3.0) * (p2 - p1);

            /* Estimate the rounding error for r4 */
            double e4 = 2 * 20.67 * (Math.abs(p4) + Math.abs(p3) + Math.abs(p2) + Math.abs(p1)) * Utility.GSL_DBL_EPSILON;

            /* The next term is due to finite precision in x+h = O (eps * x) */
            double dy = Math.max(Math.abs(r2 / h), Math.abs(r4 / h)) * Math.abs(x / h) * Utility.GSL_DBL_EPSILON;
            double te = Math.abs((r4 - r2) / h), re = Math.abs(e4 / h) + dy;
            df[i] = r4 / h;
            if (te > terr) {
                terr = te;
            }
            if (re > rerr) {
                rerr = re;
            }
        }
        return new MDerivationResult(df, terr, rerr);
    }

    public DoubleSeq forwardDerivation(DoubleFunction<DoubleSeq> fn, double x, double h) {

        MDerivationResult r0 = forward_derivation(fn, x, h);
        double[] r_0 = r0.getDf();
        double trunc = r0.getTruncationError();
        double round = r0.getRoundingError();
        double error = round + trunc;

        if (round < trunc && (round > 0 && trunc > 0)) {

            /* Compute an optimised stepsize to minimize the total error,
         using the scaling of the truncation error (O(h^2)) and
         rounding error (O(1/h)). */
            double h_opt = h * Math.pow(round / (trunc), 1.0 / 2.0);
            MDerivationResult opt = central_derivation(fn, x, h_opt);
            double trunc_opt = opt.getTruncationError();
            double round_opt = opt.getRoundingError();
            double error_opt = round_opt + trunc_opt;
            double[] r_opt = opt.getDf();

            /* Check that the new error is smaller, and that the new derivative 
         is consistent with the error bounds of the original estimate. */
            double dmax = 0;
            for (int i = 0; i < r_0.length; ++i) {
                double d = Math.abs(r_opt[i] - r_0[i]);
                if (d > dmax) {
                    dmax = d;
                }
            }
            if (error_opt < error && dmax < 4.0 * error) {
                return DoubleSeq.of(r_opt);
            }
        }
        return DoubleSeq.of(r_0);
    }

    public DoubleSeq backwardDerivation(DoubleFunction<DoubleSeq> fn, double x, double h) {
        return forwardDerivation(fn, x, -h);
    }
}
