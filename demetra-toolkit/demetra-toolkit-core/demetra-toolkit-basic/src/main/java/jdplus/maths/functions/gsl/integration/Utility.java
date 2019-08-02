/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.gsl.integration;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Utility {

    public final double GSL_DBL_EPSILON = 2.2204460492503131e-16,
            GSL_DBL_MIN = 2.2250738585072014e-308,
            GSL_DBL_MAX = 1.7976931348623157e+308;

    double rescale_error(final double er, final double result_abs, final double result_asc) {
        double err = Math.abs(er);

        if (result_asc != 0 && err != 0) {
            double scale = Math.pow((200 * err / result_asc), 1.5);

            if (scale < 1) {
                err = result_asc * scale;
            } else {
                err = result_asc;
            }
        }
        if (result_abs > GSL_DBL_MIN / (50 * GSL_DBL_EPSILON)) {
            double min_err = 50 * GSL_DBL_EPSILON * result_abs;

            if (min_err > err) {
                err = min_err;
            }
        }
        return err;
    }

    boolean test_positivity(double result, double resabs) {
        return (Math.abs(result) >= (1 - 50 * GSL_DBL_EPSILON) * resabs);
    }

    boolean subinterval_too_small(double a1, double a2, double b2) {
        final double e = GSL_DBL_EPSILON;
        final double u = GSL_DBL_MIN;
        double tmp = (1 + 100 * e) * (Math.abs(a2) + 1000 * u);
        return Math.abs(a1) <= tmp && Math.abs(b2) <= tmp;
    }

}
