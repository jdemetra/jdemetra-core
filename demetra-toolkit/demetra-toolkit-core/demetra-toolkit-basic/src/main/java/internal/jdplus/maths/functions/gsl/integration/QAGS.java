/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.maths.functions.gsl.integration;

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
public class QAGS {

    public static class Builder {

        private double epsabs = Math.pow(Utility.GSL_DBL_EPSILON, .5), epsrel = epsabs;
        private int limit = 100;
        private IntegrationRule rule = QK21.rule();

        public Builder absoluteTolerance(double epsabs) {
            this.epsabs = epsabs;
            return this;
        }

        public Builder relativeTolerance(double epsrel) {
            this.epsrel = epsrel;
            return this;
        }

        public Builder segmentationLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder integrationRule(@lombok.NonNull IntegrationRule rule) {
            this.rule = rule;
            return this;
        }

        public QAGS build() {
            return new QAGS(epsabs, epsrel, limit, rule);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    static class Workspace {

        final int limit;
        int size;
        int nrmax;
        int i;
        int maximum_level;
        final double[] alist, blist, rlist, elist;
        final int[] order;
        final int[] level;

        Workspace(int n) {
            alist = new double[n];
            blist = new double[n];
            rlist = new double[n];
            elist = new double[n];
            order = new int[n];
            level = new int[n];
            limit = n;
        }

        double a() {
            return alist[i];
        }

        double b() {
            return blist[i];
        }

        double r() {
            return rlist[i];
        }

        double e() {
            return elist[i];
        }

        int level() {
            return level[i];
        }

        int order() {
            return order[i];
        }

        void setInitialResult(IntegrationResult rslt0) {
            rlist[0] = rslt0.getResult();
            elist[0] = rslt0.getAbsError();
            size = 1;
        }

        void update(double a1, double b1, double area1, double error1,
                double a2, double b2, double area2, double error2) {

            int i_max = i;
            int i_new = size;

            int new_level = level[i_max] + 1;

            /* append the newly-created intervals to the list */
            if (error2 > error1) {
                alist[i_max] = a2;
                /* blist[maxerr] is already == b2 */
                rlist[i_max] = area2;
                elist[i_max] = error2;
                level[i_max] = new_level;

                alist[i_new] = a1;
                blist[i_new] = b1;
                rlist[i_new] = area1;
                elist[i_new] = error1;
                level[i_new] = new_level;
            } else {
                blist[i_max] = b1;
                /* alist[maxerr] is already == a1 */
                rlist[i_max] = area1;
                elist[i_max] = error1;
                level[i_max] = new_level;

                alist[i_new] = a2;
                blist[i_new] = b2;
                rlist[i_new] = area2;
                elist[i_new] = error2;
                level[i_new] = new_level;
            }

            size++;

            if (new_level > maximum_level) {
                maximum_level = new_level;
            }

            sort();
        }

        void sort() {
            final int last = size - 1;

            double errmax;
            double errmin;
            int i_cur, k, top;

            int i_nrmax = nrmax;
            int i_maxerr = order[i_nrmax];

            /* Check whether the list contains more than two error estimates */
            if (last < 2) {
                order[0] = 0;
                order[1] = 1;
                i = i_maxerr;
                return;
            }

            errmax = elist[i_maxerr];

            /* This part of the routine is only executed if, due to a difficult
     integrand, subdivision increased the error estimate. In the normal
     case the insert procedure should start after the nrmax-th largest
     error estimate. */
            while (i_nrmax > 0 && errmax > elist[order[i_nrmax - 1]]) {
                order[i_nrmax] = order[i_nrmax - 1];
                i_nrmax--;
            }

            /* Compute the number of elements in the list to be maintained in
     descending order. This number depends on the number of
     subdivisions still allowed. */
            if (last < (limit / 2 + 2)) {
                top = last;
            } else {
                top = limit - last + 1;
            }

            /* Insert errmax by traversing the list top-down, starting
     comparison from the element elist(order(i_nrmax+1)). */
            i_cur = i_nrmax + 1;

            /* The order of the tests in the following line is important to
     prevent a segmentation fault */
            while (i_cur < top && errmax < elist[order[i_cur]]) {
                order[i_cur - 1] = order[i_cur];
                i_cur++;
            }

            order[i_cur - 1] = i_maxerr;

            /* Insert errmin by traversing the list bottom-up */
            errmin = elist[last];

            k = top - 1;

            while (k > i_cur - 2 && errmin >= elist[order[k]]) {
                order[k + 1] = order[k];
                k--;
            }

            order[k + 1] = last;

            /* Set i_max and e_max */
            i_maxerr = order[i_nrmax];

            i = i_maxerr;
            nrmax = i_nrmax;
        }

        boolean isLargeInterval() {
            return level[i] < maximum_level;
        }

        private boolean increaseNrmax() {
            int k;
            int id = nrmax;
            int jupbnd;
            int last = size - 1;

            if (last > (1 + limit / 2)) {
                jupbnd = limit + 1 - last;
            } else {
                jupbnd = last;
            }

            for (k = id; k <= jupbnd; k++) {
                int i_max = order[nrmax];
                i = i_max;

                if (level[i_max] < maximum_level) {
                    return true;
                }

                nrmax++;

            }
            return false;
        }

        private void resetNrmax() {
            nrmax = 0;
            i = order[0];
        }

        private double sumResults() {
            double result_sum = 0;

            for (int k = 0; k < size; k++) {
                result_sum += rlist[k];
            }
            return result_sum;
        }

    }

    static class ExtrapolationTable {

        int n;
        final double[] rlist2 = new double[52];
        int nres;
        final double[] res3la = new double[3];

        void initialise() {
            n = 0;
            nres = 0;
        }

        void append(double y) {
            rlist2[n] = y;
            n++;
        }

        private double result, abserr;

        void qelg() {
            final int m = n - 1;

            final double current = rlist2[m];

            double absolute = Utility.GSL_DBL_MAX;
            double relative = 5 * Utility.GSL_DBL_EPSILON * Math.abs(current);

            int newelm = m / 2;
            int n_orig = m;
            int n_final = m;
            int i;

            final int nres_orig = nres;

            result = current;
            abserr = Utility.GSL_DBL_MAX;

            if (m < 2) {
                result = current;
                abserr = Math.max(absolute, relative);
                return;
            }

            rlist2[m + 2] = rlist2[m];
            rlist2[m] = Utility.GSL_DBL_MAX;

            for (i = 0; i < newelm; i++) {
                double res = rlist2[m - 2 * i + 2];
                double e0 = rlist2[m - 2 * i - 2];
                double e1 = rlist2[m - 2 * i - 1];
                double e2 = res;

                double e1abs = Math.abs(e1);
                double delta2 = e2 - e1;
                double err2 = Math.abs(delta2);
                double tol2 = Math.max(Math.abs(e2), e1abs) * Utility.GSL_DBL_EPSILON;
                double delta3 = e1 - e0;
                double err3 = Math.abs(delta3);
                double tol3 = Math.max(e1abs, Math.abs(e0)) * Utility.GSL_DBL_EPSILON;

                double e3, delta1, err1, tol1, ss;

                if (err2 <= tol2 && err3 <= tol3) {
                    /* If e0, e1 and e2 are equal to within machine accuracy,
             convergence is assumed.  */

                    result = res;
                    absolute = err2 + err3;
                    relative = 5 * Utility.GSL_DBL_EPSILON * Math.abs(res);
                    abserr = Math.max(absolute, relative);
                    return;
                }

                e3 = rlist2[m - 2 * i];
                rlist2[m - 2 * i] = e1;
                delta1 = e1 - e3;
                err1 = Math.abs(delta1);
                tol1 = Math.max(e1abs, Math.abs(e3)) * Utility.GSL_DBL_EPSILON;

                /* If two elements are very close to each other, omit a part of
         the table by adjusting the value of n */
                if (err1 <= tol1 || err2 <= tol2 || err3 <= tol3) {
                    n_final = 2 * i;
                    break;
                }

                ss = (1 / delta1 + 1 / delta2) - 1 / delta3;

                /* Test to detect irregular behaviour in the table, and
         eventually omit a part of the table by adjusting the value of
         n. */
                if (Math.abs(ss * e1) <= 0.0001) {
                    n_final = 2 * i;
                    break;
                }

                /* Compute a new element and eventually adjust the value of
         result. */
                res = e1 + 1 / ss;
                rlist2[m - 2 * i] = res;

                {
                    final double error = err2 + Math.abs(res - e2) + err3;

                    if (error <= abserr) {
                        abserr = error;
                        result = res;
                    }
                }
            }

            /* Shift the table */
            {
                final int limexp = 50 - 1;

                if (n_final == limexp) {
                    n_final = 2 * (limexp / 2);
                }
            }

            if (n_orig % 2 == 1) {
                for (i = 0; i <= newelm; i++) {
                    rlist2[1 + i * 2] = rlist2[i * 2 + 3];
                }
            } else {
                for (i = 0; i <= newelm; i++) {
                    rlist2[i * 2] = rlist2[i * 2 + 2];
                }
            }

            if (n_orig != n_final) {
                for (i = 0; i <= n_final; i++) {
                    rlist2[i] = rlist2[n_orig - n_final + i];
                }
            }

            n = n_final + 1;

            if (nres_orig < 3) {
                res3la[nres_orig] = result;
                abserr = Utility.GSL_DBL_MAX;
            } else {
                /* Compute error estimate */
                abserr = (Math.abs(result - res3la[2]) + Math.abs(result - res3la[1])
                        + Math.abs(result - res3la[0]));

                res3la[0] = res3la[1];
                res3la[1] = res3la[2];
                res3la[2] = result;
            }

            /* In QUADPACK the variable table->nres is incremented at the top of
     qelg, so it increases on every call. This leads to the array
     res3la being accessed when its elements are still undefined, so I
     have moved the update to this point so that its value more
     useful. */
            nres = nres_orig + 1;

            abserr = Math.max(abserr, 5 * Utility.GSL_DBL_EPSILON * Math.abs(result));

            return;
        }

    }

    private final double epsabs, epsrel;
    private final int limit;
    private final IntegrationRule q;

    private double result, abserr;
    private int iteration;

    private QAGS(double epsabs, double epsrel, int limit, IntegrationRule rule) {
        this.epsabs = epsabs;
        this.epsrel = epsrel;
        this.limit = limit;
        this.q = rule;
    }

    public void integrate(DoubleUnaryOperator fn, final double a, final double b) {

        Workspace workspace = new Workspace(limit);

        double area, errsum;
        double res_ext, err_ext;
        double tolerance;

        double ertest = 0;
        double error_over_large_intervals = 0;
        double correc = 0;
        int ktmin = 0;
        int roundoff_type1 = 0, roundoff_type2 = 0, roundoff_type3 = 0;
        int error_type = 0;
        boolean error_type2 = false;
        boolean positive_integrand;
        boolean extrapolate = false;
        boolean disallow_extrapolation = false;

        ExtrapolationTable table = new ExtrapolationTable();

        /* Initialize results */
        workspace.alist[0] = a;
        workspace.blist[0] = b;

        result = 0;
        abserr = 0;

        if (limit > workspace.limit) {
            throw new GslIntegrationException("iteration limit exceeds available workspace");
        }

        /* Test on accuracy */
        if (epsabs <= 0 && (epsrel < 50 * Utility.GSL_DBL_EPSILON || epsrel < 0.5e-28)) {
            throw new GslIntegrationException("tolerance cannot be achieved with given epsabs and epsrel");
        }

        /* Perform the first integration */
        IntegrationResult rslt0 = q.integrate(fn, a, b);

        workspace.setInitialResult(rslt0);

        tolerance = Math.max(epsabs, epsrel * Math.abs(rslt0.getResult()));

        if (rslt0.getAbsError() <= 100 * Utility.GSL_DBL_EPSILON * rslt0.getResultAbs()
                && rslt0.getAbsError() > tolerance) {
            result = rslt0.getResult();
            abserr = rslt0.getAbsError();
            throw new GslIntegrationException("cannot reach tolerance because of roundoff error on first attempt");
        } else if ((rslt0.getAbsError() <= tolerance && rslt0.getAbsError() != rslt0.getResultAsc()) || rslt0.getAbsError() == 0.0) {
            result = rslt0.getResult();
            abserr = rslt0.getAbsError();
            return;
        } else if (limit == 1) {
            result = rslt0.getResult();
            abserr = rslt0.getAbsError();
            throw new GslIntegrationException("a maximum of one iteration was insufficient");
        }

        /* Initialization */
        table.initialise();
        table.append(rslt0.getResult());

        area = rslt0.getResult();
        errsum = rslt0.getAbsError();

        res_ext = rslt0.getResult();
        err_ext = Utility.GSL_DBL_MAX;

        positive_integrand = Utility.test_positivity(rslt0.getResult(), rslt0.getResultAbs());
        iteration = 1;

        do {
            int current_level;
            double a1, b1, a2, b2;
            double a_i, b_i, r_i, e_i;
            double last_e_i;

            /* Bisect the subinterval with the largest error estimate */
            a_i = workspace.a();
            b_i = workspace.b();
            r_i = workspace.r();
            e_i = workspace.e();

            current_level = workspace.level() + 1;

            a1 = a_i;
            b1 = 0.5 * (a_i + b_i);
            a2 = b1;
            b2 = b_i;

            iteration++;

            IntegrationResult rslt1 = q.integrate(fn, a1, b1);
            IntegrationResult rslt2 = q.integrate(fn, a2, b2);
            double area1 = rslt1.getResult(), area2 = rslt2.getResult();
            double area12 = area1 + area2;
            double error1 = rslt1.getAbsError(), error2 = rslt2.getAbsError();
            double error12 = error1 + error2;
            last_e_i = e_i;

            errsum = errsum + error12 - e_i;
            area = area + area12 - r_i;

            tolerance = Math.max(epsabs, epsrel * Math.abs(area));

            if (rslt1.getResultAsc() != error1 && rslt2.getResultAsc() != error2) {
                double delta = r_i - area12;

                if (Math.abs(delta) <= 1.0e-5 * Math.abs(area12) && error12 >= 0.99 * e_i) {
                    if (!extrapolate) {
                        roundoff_type1++;
                    } else {
                        roundoff_type2++;
                    }
                }
                if (iteration > 10 && error12 > e_i) {
                    roundoff_type3++;
                }
            }

            /* Test for roundoff and eventually set error flag */
            if (roundoff_type1 + roundoff_type2 >= 10 || roundoff_type3 >= 20) {
                error_type = 2;
                /* round off error */
            }

            if (roundoff_type2 >= 5) {
                error_type2 = true;
            }

            /* set error flag in the case of bad integrand behaviour at
         a point of the integration range */
            if (Utility.subinterval_too_small(a1, a2, b2)) {
                error_type = 4;
            }

            /* append the newly-created intervals to the list */
            workspace.update(a1, b1, area1, error1, a2, b2, area2, error2);

            if (errsum <= tolerance) {
                computeResult(workspace, errsum, error_type);
                return;
            }

            if (error_type != 0) {
                break;
            }

            if (iteration >= limit - 1) {
                error_type = 1;
                break;
            }

            if (iteration == 2) /* set up variables on first iteration */ {
                error_over_large_intervals = errsum;
                ertest = tolerance;
                table.append(area);
                continue;
            }

            if (disallow_extrapolation) {
                continue;
            }

            error_over_large_intervals += -last_e_i;

            if (current_level < workspace.maximum_level) {
                error_over_large_intervals += error12;
            }

            if (!extrapolate) {
                /* test whether the interval to be bisected next is the
             smallest interval. */

                if (workspace.isLargeInterval()) {
                    continue;
                }

                extrapolate = true;
                workspace.nrmax = 1;
            }

            if (!error_type2 && error_over_large_intervals > ertest) {
                if (workspace.increaseNrmax()) {
                    continue;
                }
            }

            /* Perform extrapolation */
            table.append(area);

            table.qelg();

            ktmin++;

            if (ktmin > 5 && err_ext < 0.001 * errsum) {
                error_type = 5;
            }

            if (table.abserr < err_ext) {
                ktmin = 0;
                err_ext = table.abserr;
                res_ext = table.result;
                correc = error_over_large_intervals;
                ertest = Math.max(epsabs, epsrel * Math.abs(table.result));
                if (err_ext <= ertest) {
                    break;
                }
            }

            /* Prepare bisection of the smallest interval. */
            if (table.n == 1) {
                disallow_extrapolation = true;
            }

            if (error_type == 5) {
                break;
            }

            /* work on interval with largest error */
            workspace.resetNrmax();
            extrapolate = false;
            error_over_large_intervals = errsum;

        } while (iteration < limit);

        result = res_ext;
        abserr = err_ext;

        if (err_ext == Utility.GSL_DBL_MAX) {
            computeResult(workspace, errsum, error_type);
            return;
        }

        if (error_type != 0 || error_type2) {
            if (error_type2) {
                err_ext += correc;
            }

            if (error_type == 0) {
                error_type = 3;
            }

            if (res_ext != 0.0 && area != 0.0) {
                if (err_ext / Math.abs(res_ext) > errsum / Math.abs(area)) {
                    computeResult(workspace, errsum, error_type);
                    return;
                }
            } else if (err_ext > errsum) {
                computeResult(workspace, errsum, error_type);
                return;
            } else if (area == 0.0) {
                returnError(error_type);
                return;
            }
        }

        /*  Test on divergence. */
        double max_area = Math.max(Math.abs(res_ext), Math.abs(area));

        if (!positive_integrand && max_area < 0.01 * rslt0.getAbsError()) {
            computeResult(workspace, errsum, error_type);
            return;

        }
        double ratio = res_ext / area;

        if (ratio < 0.01 || ratio > 100.0 || errsum > Math.abs(area)) {
            error_type = 6;
        }
        returnError(error_type);
    }

    private void computeResult(Workspace workspace, double errsum, int errorType) {
        result = workspace.sumResults();
        abserr = errsum;

        returnError(errorType);
    }

    private void returnError(int error_type) {

        if (error_type > 2) {
            error_type--;
        }
        if (error_type == 0) {
            return;
        }

        switch (error_type) {
            case 1:
                throw new GslIntegrationException("number of iterations was insufficient");
            case 2:
                throw new GslIntegrationException("cannot reach tolerance because of roundoff error");
            case 3:
                throw new GslIntegrationException("bad integrand behavior found in the integration interval");
            case 4:
                throw new GslIntegrationException("roundoff error detected in the extrapolation table");
            case 5:
                throw new GslIntegrationException("integral is divergent, or slowly convergent");
            default:
                throw new GslIntegrationException("could not integrate function");
        }
    }

    /**
     * @return the result
     */
    public double getResult() {
        return result;
    }

    /**
     * @return the abserr
     */
    public double getAbserr() {
        return abserr;
    }
}
