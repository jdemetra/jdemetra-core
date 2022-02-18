/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.math.functions.gsl.integration;

import internal.jdplus.math.functions.gsl.Utility;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
class QK {

    private double[] fv1;
    private double[] fv2;
    private double result;
    private double abserr;
    private double resabs;
    private double resasc;

    static QK of(DoubleUnaryOperator fn, double a, double b,
            final double xgk[], final double wg[], final double wgk[]) {

        int n = xgk.length;
        QK qk = new QK(n);
        final double center = 0.5 * (a + b);
        final double half_length = 0.5 * (b - a);
        final double abs_half_length = Math.abs(half_length);
        final double f_center = fn.applyAsDouble(center);

        double result_gauss = 0;
        double result_kronrod = f_center * wgk[n - 1];

        double result_abs = Math.abs(result_kronrod);
        double result_asc;
        double mean, err;

        int j;

        if (n % 2 == 0) {
            result_gauss = f_center * wg[n / 2 - 1];
        }

        for (j = 0; j < (n - 1) / 2; j++) {
            final int jtw = j * 2 + 1;
            final double abscissa = half_length * xgk[jtw];
            final double fval1 = fn.applyAsDouble(center - abscissa);
            final double fval2 = fn.applyAsDouble(center + abscissa);
            final double fsum = fval1 + fval2;
            qk.fv1[jtw] = fval1;
            qk.fv2[jtw] = fval2;
            result_gauss += wg[j] * fsum;
            result_kronrod += wgk[jtw] * fsum;
            result_abs += wgk[jtw] * (Math.abs(fval1) + Math.abs(fval2));
        }

        for (j = 0; j < n / 2; j++) {
            int jtwm1 = j * 2;
            final double abscissa = half_length * xgk[jtwm1];
            final double fval1 = fn.applyAsDouble(center - abscissa);
            final double fval2 = fn.applyAsDouble(center + abscissa);
            qk.fv1[jtwm1] = fval1;
            qk.fv2[jtwm1] = fval2;
            result_kronrod += wgk[jtwm1] * (fval1 + fval2);
            result_abs += wgk[jtwm1] * (Math.abs(fval1) + Math.abs(fval2));
        }

        mean = result_kronrod * 0.5;

        result_asc = wgk[n - 1] * Math.abs(f_center - mean);

        for (j = 0; j < n - 1; j++) {
            result_asc += wgk[j] * (Math.abs(qk.fv1[j] - mean) + Math.abs(qk.fv2[j] - mean));
        }

        /* scale by the width of the integration region */
        err = (result_kronrod - result_gauss) * half_length;

        result_kronrod *= half_length;
        result_abs *= abs_half_length;
        result_asc *= abs_half_length;

        qk.result = result_kronrod;
        qk.resabs = result_abs;
        qk.resasc = result_asc;
        qk.abserr = Utility.rescale_error(err, result_abs, result_asc);
        return qk;
    }

    private QK(int n) {
        fv1 = new double[n];
        fv2 = new double[n];
    }

    /**
     * @return the fv1
     */
    public double[] getFv1() {
        return fv1;
    }

    /**
     * @return the fv2
     */
    public double[] getFv2() {
        return fv2;
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

    /**
     * @return the resabs
     */
    public double getResabs() {
        return resabs;
    }

    /**
     * @return the resasc
     */
    public double getResasc() {
        return resasc;
    }

    /**
     * @param resasc the resasc to set
     */
    public void setResasc(double resasc) {
        this.resasc = resasc;
    }

    IntegrationResult result() {
        return new IntegrationResult(result, abserr, resabs, resasc);
    }

}
