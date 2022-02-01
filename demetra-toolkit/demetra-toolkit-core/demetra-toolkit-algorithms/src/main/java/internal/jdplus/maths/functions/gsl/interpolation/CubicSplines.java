/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.maths.functions.gsl.interpolation;

import demetra.data.DoubleSeq;
import demetra.math.MathException;
import jdplus.data.DataBlock;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class CubicSplines {

    static class State {

        double[] c;
        double[] g;
        double[] diag;
        double[] offdiag;

        State(int size) {
            c = new double[size];
            g = new double[size];
            diag = new double[size];
            offdiag = new double[size];
        }
    }

    public static class Spline {

        private static class Eval {

            double b, c, d;
        }

        final double[] c;
        final double[] x;
        final double[] y;

        Spline(State state, double[] x, double[] y) {
            this.x = x;
            this.y = y;
            int size = state.c.length;
            c = state.c;

        }

        public double eval(double a) {
            int idx = bsearch(x, a, 0, x.length - 1);
            double x_hi = x[idx + 1];
            double x_lo = x[idx];
            double dx = x_hi - x_lo;
            if (dx > 0.0) {
                double y_lo = y[idx];
                double y_hi = y[idx + 1];
                double dy = y_hi - y_lo;
                double delx = a - x_lo;
                Eval eval = new Eval();
                coeff(eval, dy, dx, idx);
                return y_lo + delx * (eval.b + delx * (eval.c + delx * eval.d));
            } else {
                return Double.NaN;
            }
        }

        private void coeff(Eval eval, double dy, double dx, int idx) {
            double c_i = c[idx];
            double c_ip1 = c[idx + 1];
            eval.b = (dy / dx) - dx * (c_ip1 + 2.0 * c_i) / 3.0;
            eval.c = c_i;
            eval.d = (c_ip1 - c_i) / (3.0 * dx);
        }
    }

    public Spline natural(double[] xa, double[] ya) {

        int num_points = xa.length;
        int max_index = num_points - 1;
        /* Engeln-Mullges + Uhlig "n" */
        int sys_size = max_index - 1;
        /* linear system is sys_size x sys_size */

        State state = new State(num_points);

        state.c[0] = 0.0;
        state.c[max_index] = 0.0;

        for (int i = 0; i < sys_size; i++) {
            double h_i = xa[i + 1] - xa[i];
            double h_ip1 = xa[i + 2] - xa[i + 1];
            double ydiff_i = ya[i + 1] - ya[i];
            double ydiff_ip1 = ya[i + 2] - ya[i + 1];
            double g_i = (h_i != 0.0) ? 1.0 / h_i : 0.0;
            double g_ip1 = (h_ip1 != 0.0) ? 1.0 / h_ip1 : 0.0;
            state.offdiag[i] = h_ip1;
            state.diag[i] = 2.0 * (h_ip1 + h_i);
            state.g[i] = 3.0 * (ydiff_ip1 * g_ip1 - ydiff_i * g_i);
        }

        if (sys_size == 1) {
            state.c[1] = state.g[0] / state.diag[0];
            return new Spline(state, xa, ya);
        } else {
            DoubleSeq g_vec = DoubleSeq.of(state.g, 0, sys_size);
            DoubleSeq diag_vec = DoubleSeq.of(state.diag, 0, sys_size);
            DoubleSeq offdiag_vec = DoubleSeq.of(state.offdiag, 0, sys_size - 1);
            DataBlock solution_vec = DataBlock.of(state.c, 1, sys_size + 1);

            solveTriDiag(diag_vec, offdiag_vec, g_vec, solution_vec);
            return new Spline(state, xa, ya);
        }
    }

    public Spline periodic(double[] xa, double[] ya) {

        int num_points = xa.length;
        int max_index = num_points - 1;
        /* Engeln-Mullges + Uhlig "n" */
        int sys_size = max_index;
        /* linear system is sys_size x sys_size */

        State state = new State(num_points);

        if (sys_size == 2) {
            /* solve 2x2 system */

            double h0 = xa[1] - xa[0];
            double h1 = xa[2] - xa[1];

            double A = 2.0 * (h0 + h1);
            double B = h0 + h1;
            double det;

            double g0 = 3.0 * ((ya[2] - ya[1]) / h1 - (ya[1] - ya[0]) / h0);
            double g1 = 3.0 * ((ya[1] - ya[2]) / h0 - (ya[2] - ya[1]) / h1);

            det = 3.0 * (h0 + h1) * (h0 + h1);
            state.c[1] = (A * g0 - B * g1) / det;
            state.c[2] = (-B * g0 + A * g1) / det;
            state.c[0] = state.c[2];

            return new Spline(state, xa, ya);
        } else {

            for (int i = 0; i < sys_size - 1; i++) {
                double h_i = xa[i + 1] - xa[i];
                double h_ip1 = xa[i + 2] - xa[i + 1];
                double ydiff_i = ya[i + 1] - ya[i];
                double ydiff_ip1 = ya[i + 2] - ya[i + 1];
                double g_i = (h_i != 0.0) ? 1.0 / h_i : 0.0;
                double g_ip1 = (h_ip1 != 0.0) ? 1.0 / h_ip1 : 0.0;
                state.offdiag[i] = h_ip1;
                state.diag[i] = 2.0 * (h_ip1 + h_i);
                state.g[i] = 3.0 * (ydiff_ip1 * g_ip1 - ydiff_i * g_i);
            }
            int ilast = sys_size - 1;
            double h_i = xa[ilast + 1] - xa[ilast];
            double h_ip1 = xa[1] - xa[0];
            double ydiff_i = ya[ilast + 1] - ya[ilast];
            double ydiff_ip1 = ya[1] - ya[0];
            double g_i = (h_i != 0.0) ? 1.0 / h_i : 0.0;
            double g_ip1 = (h_ip1 != 0.0) ? 1.0 / h_ip1 : 0.0;
            state.offdiag[ilast] = h_ip1;
            state.diag[ilast] = 2.0 * (h_ip1 + h_i);
            state.g[ilast] = 3.0 * (ydiff_ip1 * g_ip1 - ydiff_i * g_i);
            DoubleSeq g_vec = DoubleSeq.of(state.g, 0, sys_size);
            DoubleSeq diag_vec = DoubleSeq.of(state.diag, 0, sys_size);
            DoubleSeq offdiag_vec = DoubleSeq.of(state.offdiag, 0, sys_size);
            DataBlock solution_vec = DataBlock.of(state.c, 1, sys_size + 1);

            solveCyclicTriDiag(diag_vec, offdiag_vec, g_vec, solution_vec);
            state.c[0] = state.c[max_index];
            return new Spline(state, xa, ya);
        }
    }

    /* for description of method see [Engeln-Mullges + Uhlig, p. 92]
 *
 *     diag[0]  offdiag[0]             0   .....
 *  offdiag[0]     diag[1]    offdiag[1]   .....
 *           0  offdiag[1]       diag[2]
 *           0           0    offdiag[2]   .....
     */
    public void solveTriDiag(DoubleSeq diag, DoubleSeq offdiag, DoubleSeq b, DataBlock x) {
        int N = diag.length();
        double[] gamma = new double[N];
        double[] alpha = new double[N];
        double[] c = new double[N];
        double[] z = new double[N];

        /* Cholesky decomposition
         A = L.D.L^t
         lower_diag(L) = gamma
         diag(D) = alpha
         */
        alpha[0] = diag.get(0);
        gamma[0] = offdiag.get(0) / alpha[0];

        if (alpha[0] == 0) {
            throw new MathException(MathException.DIVBYZERO);
        }

        for (int i = 1; i < N - 1; i++) {
            alpha[i] = diag.get(i) - offdiag.get(i - 1) * gamma[i - 1];
            gamma[i] = offdiag.get(i) / alpha[i];
            if (alpha[i] == 0) {
                throw new MathException(MathException.DIVBYZERO);
            }
        }
        if (N > 1) {
            alpha[N - 1] = diag.get(N - 1) - offdiag.get(N - 2) * gamma[N - 2];
        }
        /* update RHS */
        z[0] = b.get(0);
        for (int i = 1; i < N; i++) {
            z[i] = b.get(i) - gamma[i - 1] * z[i - 1];
        }
        for (int i = 0; i < N; i++) {
            c[i] = z[i] / alpha[i];
        }

        /* backsubstitution */
        x.set(N - 1, c[N - 1]);
        if (N >= 2) {
            for (int i = N - 2, j = 0; j <= N - 2; j++, i--) {
                x.set(i, c[i] - gamma[i] * x.get(i + 1));
            }
        }
    }

    public void solveCyclicTriDiag(DoubleSeq diag, DoubleSeq offdiag, DoubleSeq b, DataBlock x) {
        int N = diag.length();
        double[] delta = new double[N];
        double[] gamma = new double[N];
        double[] alpha = new double[N];
        double[] c = new double[N];
        double[] z = new double[N];
        double sum = 0.0;

        /* factor */
        if (N == 1) {
            x.set(0, b.get(0) / diag.get(0));
            return;
        }

        alpha[0] = diag.get(0);
        if (alpha[0] == 0) {
            throw new MathException(MathException.DIVBYZERO);
        }
        gamma[0] = offdiag.get(0) / alpha[0];
        delta[0] = offdiag.get(N - 1) / alpha[0];

        for (int i = 1; i < N - 2; i++) {
            alpha[i] = diag.get(i) - offdiag.get(i-1) * gamma[i - 1];
            gamma[i] = offdiag.get(i) / alpha[i];
            delta[i] = -delta[i - 1] * offdiag.get(i-1) / alpha[i];
            if (alpha[i] == 0) {
                throw new MathException(MathException.DIVBYZERO);
            }
        }

        for (int i = 0; i < N - 2; i++) {
            sum += alpha[i] * delta[i] * delta[i];
        }

        alpha[N - 2] = diag.get(N - 2) - offdiag.get(N - 3) * gamma[N - 3];
        gamma[N - 2] = (offdiag.get(N-2) - offdiag.get(N - 3) * delta[N - 3]) / alpha[N - 2];
        alpha[N - 1] = diag.get(N - 1) - sum - alpha[(N - 2)] * gamma[N - 2] * gamma[N - 2];

        /* update */
        z[0] = b.get(0);
        for (int i = 1; i < N - 1; i++) {
            z[i] = b.get(i) - z[i - 1] * gamma[i - 1];
        }
        sum = 0.0;
        for (int i = 0; i < N - 2; i++) {
            sum += delta[i] * z[i];
        }
        z[N - 1] = b.get(N - 1) - sum - gamma[N - 2] * z[N - 2];
        for (int i = 0; i < N; i++) {
            c[i] = z[i] / alpha[i];
        }

        /* backsubstitution */
        x.set(N - 1, c[N - 1]);
        x.set(N - 2, c[N - 2] - gamma[N - 2] * x.get(N - 1));
        if (N >= 3) {
            for (int i = N - 3, j = 0; j <= N - 3; j++, i--) {
                x.set(i, c[i] - gamma[i] * x.get(i + 1) - delta[i] * x.get(N - 1));
            }
        }
    }

    /* Perform a binary search of an array of values.
 * 
 * The parameters index_lo and index_hi provide an initial bracket,
 * and it is assumed that index_lo < index_hi. The resulting index
 * is guaranteed to be strictly less than index_hi and greater than
 * or equal to index_lo, so that the implicit bracket [index, index+1]
 * always corresponds to a region within the implicit value range of
 * the value array.
 *
 * Note that this means the relationship of 'x' to x_array[index]
 * and x_array[index+1] depends on the result region, i.e. the
 * behaviour at the boundaries may not correspond to what you
 * expect. We have the following complete specification of the
 * behaviour.
 * Suppose the input is x_array[] = { x0, x1, ..., xN }
 *    if ( x == x0 )           then  index == 0
 *    if ( x > x0 && x <= x1 ) then  index == 0, and sim. for other interior pts
 *    if ( x == xN )           then  index == N-1
 *    if ( x > xN )            then  index == N-1
 *    if ( x < x0 )            then  index == 0 
     */
    int bsearch(double[] x_array, double x, int index_lo, int index_hi) {
        int ilo = index_lo;
        int ihi = index_hi;
        while (ihi > ilo + 1) {
            int i = (ihi + ilo) / 2;
            if (x_array[i] > x) {
                ihi = i;
            } else {
                ilo = i;
            }
        }
        return ilo;
    }

//
///* natural spline calculation
// * see [Engeln-Mullges + Uhlig, p. 254]
// */
//static int
//
//
///* periodic spline calculation
// * see [Engeln-Mullges + Uhlig, p. 256]
// */
//}
//
//
///* function for common coefficient determination
// */
//static inline void
//coeff_calc (const double c_array[], double dy, double dx, size_t index,  
//            double * b, double * c, double * d)
//{
//  const double c_i = c_array[index];
//  const double c_ip1 = c_array[index + 1];
//  *b = (dy / dx) - dx * (c_ip1 + 2.0 * c_i) / 3.0;
//  *c = c_i;
//  *d = (c_ip1 - c_i) / (3.0 * dx);
//}
//
//
//static
//int
//cspline_eval (const void * vstate,
//              const double x_array[], const double y_array[], size_t size,
//              double x,
//              gsl_interp_accel * a,
//              double *y)
//{
//  const cspline_state_t *state = (const cspline_state_t *) vstate;
//
//  double x_lo, x_hi;
//  double dx;
//  size_t index;
//  
//  if (a != 0)
//    {
//      index = gsl_interp_accel_find (a, x_array, size, x);
//    }
//  else
//    {
//      index = gsl_interp_bsearch (x_array, x, 0, size - 1);
//    }
//  
//  /* evaluate */
//  x_hi = x_array[index + 1];
//  x_lo = x_array[index];
//  dx = x_hi - x_lo;
//  if (dx > 0.0)
//    {
//      const double y_lo = y_array[index];
//      const double y_hi = y_array[index + 1];
//      const double dy = y_hi - y_lo;
//      double delx = x - x_lo;
//      double b_i, c_i, d_i; 
//      coeff_calc(state->c, dy, dx, index,  &b_i, &c_i, &d_i);
//      *y = y_lo + delx * (b_i + delx * (c_i + delx * d_i));
//      return GSL_SUCCESS;
//    }
//  else
//    {
//      *y = 0.0;
//      return GSL_EINVAL;
//    }
//}
//
//
//static
//int
//cspline_eval_deriv (const void * vstate,
//                    const double x_array[], const double y_array[], size_t size,
//                    double x,
//                    gsl_interp_accel * a,
//                    double *dydx)
//{
//  const cspline_state_t *state = (const cspline_state_t *) vstate;
//
//  double x_lo, x_hi;
//  double dx;
//  size_t index;
//  
//  if (a != 0)
//    {
//      index = gsl_interp_accel_find (a, x_array, size, x);
//    }
//  else
//    {
//      index = gsl_interp_bsearch (x_array, x, 0, size - 1);
//    }
//  
//  /* evaluate */
//  x_hi = x_array[index + 1];
//  x_lo = x_array[index];
//  dx = x_hi - x_lo;
//  if (dx > 0.0)
//    {
//      const double y_lo = y_array[index];
//      const double y_hi = y_array[index + 1];
//      const double dy = y_hi - y_lo;
//      double delx = x - x_lo;
//      double b_i, c_i, d_i; 
//      coeff_calc(state->c, dy, dx, index,  &b_i, &c_i, &d_i);
//      *dydx = b_i + delx * (2.0 * c_i + 3.0 * d_i * delx);
//      return GSL_SUCCESS;
//    }
//  else
//    {
//      *dydx = 0.0;
//      return GSL_EINVAL;
//    }
//}
//
//
//static
//int
//cspline_eval_deriv2 (const void * vstate,
//                     const double x_array[], const double y_array[], size_t size,
//                     double x,
//                     gsl_interp_accel * a,
//                     double * y_pp)
//{
//  const cspline_state_t *state = (const cspline_state_t *) vstate;
//
//  double x_lo, x_hi;
//  double dx;
//  size_t index;
//  
//  if (a != 0)
//    {
//      index = gsl_interp_accel_find (a, x_array, size, x);
//    }
//  else
//    {
//      index = gsl_interp_bsearch (x_array, x, 0, size - 1);
//    }
//  
//  /* evaluate */
//  x_hi = x_array[index + 1];
//  x_lo = x_array[index];
//  dx = x_hi - x_lo;
//  if (dx > 0.0)
//    {
//      const double y_lo = y_array[index];
//      const double y_hi = y_array[index + 1];
//      const double dy = y_hi - y_lo;
//      double delx = x - x_lo;
//      double b_i, c_i, d_i;
//      coeff_calc(state->c, dy, dx, index,  &b_i, &c_i, &d_i);
//      *y_pp = 2.0 * c_i + 6.0 * d_i * delx;
//      return GSL_SUCCESS;
//    }
//  else
//    {
//      *y_pp = 0.0;
//      return GSL_EINVAL;
//    }
//}
//
//
//static
//int
//cspline_eval_integ (const void * vstate,
//                    const double x_array[], const double y_array[], size_t size,
//                    gsl_interp_accel * acc,
//                    double a, double b,
//                    double * result)
//{
//  const cspline_state_t *state = (const cspline_state_t *) vstate;
//
//  size_t ilast, index_a, index_b;
//  
//  if (acc != 0)
//    {
//      index_a = gsl_interp_accel_find (acc, x_array, size, a);
//      index_b = gsl_interp_accel_find (acc, x_array, size, b);
//    }
//  else
//    {
//      index_a = gsl_interp_bsearch (x_array, a, 0, size - 1);
//      index_b = gsl_interp_bsearch (x_array, b, 0, size - 1);
//    }
//
//  *result = 0.0;
//  
//  /* interior intervals */
//  for(ilast=index_a; ilast<=index_b; ilast++) {
//    const double x_hi = x_array[ilast + 1];
//    const double x_lo = x_array[ilast];
//    const double y_lo = y_array[ilast];
//    const double y_hi = y_array[ilast + 1];
//    const double dx = x_hi - x_lo;
//    const double dy = y_hi - y_lo;
//    if(dx != 0.0) {
//      double b_i, c_i, d_i; 
//      coeff_calc(state->c, dy, dx, ilast,  &b_i, &c_i, &d_i);
//      
//      if (ilast == index_a || ilast == index_b)
//        {
//          double x1 = (ilast == index_a) ? a : x_lo;
//          double x2 = (ilast == index_b) ? b : x_hi;
//          *result += integ_eval(y_lo, b_i, c_i, d_i, x_lo, x1, x2);
//        }
//      else
//        {
//          *result += dx * (y_lo + dx*(0.5*b_i + dx*(c_i/3.0 + 0.25*d_i*dx)));
//        }
//    }
//    else {
//      *result = 0.0;
//      return GSL_EINVAL;
//    }
//  }
//  
//  return GSL_SUCCESS;
//}
//
//static const gsl_interp_type cspline_type = 
//{
//  "cspline", 
//  3,
//  &cspline_alloc,
//  &cspline_init,
//  &cspline_eval,
//  &cspline_eval_deriv,
//  &cspline_eval_deriv2,
//  &cspline_eval_integ,
//  &cspline_free
//};
//
//const gsl_interp_type * gsl_interp_cspline = &cspline_type;
//
//static const gsl_interp_type cspline_periodic_type = 
//{
//  "cspline-periodic", 
//  2,
//  &cspline_alloc,
//  &cspline_init_periodic,
//  &cspline_eval,
//  &cspline_eval_deriv,
//  &cspline_eval_deriv2,
//  &cspline_eval_integ,
//  &cspline_free
//};
//
//const gsl_interp_type * gsl_interp_cspline_periodic = &cspline_periodic_type;
}
