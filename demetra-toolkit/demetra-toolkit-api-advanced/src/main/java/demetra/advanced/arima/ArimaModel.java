/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.advanced.arima;

/**
 *
 * @author PALATEJ
 */

import demetra.data.DoubleSeq;
import nbbrd.design.Development;

/**
 * Generic ARIMA model (defined by its stationary AR, non-stationary AR,
 * MA polynomials and innovation variance).
 *
 * ar(B)delta(b)y(t)=ma(b)e(t),
 * where B is the backshift operator and e(t)~N(0,innovationVariance)
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Builder(builderClassName = "Builder")
public class ArimaModel{

    /**
     * Name of the model (optional); null by default
     */
    String name;
    /**
     * Innovation variance. 1 by default
     */
    double innovationVariance;
    /**
     * Stationary auto-regressive polynomial (1+ar[0]B...); True signs.
     * Doesn't contain the constant term (always 1)
     * All the roots of the stationary polynomial should be outside the unit
     * circle (not checked)
     */
    @lombok.NonNull
    DoubleSeq ar;
    /**
     * Non-stationary auto-regressive polynomial (1, delta(1)...); True signs.
     * Doesn't contain the constant term (always 1)
     * All the roots of the non-stationary polynomial should be on the unit
     * circle (not checked)
     */
    @lombok.NonNull
    DoubleSeq delta;
    /**
     * Moving-average polynomial (1, theta(1)...); True signs.
     * Doesn't contain the constant term (always 1)
     */
    @lombok.NonNull
    DoubleSeq ma;

    /**
     * Rename the model.
     *
     * @param nname
     * @return
     */
    public ArimaModel rename(String nname) {
        return new ArimaModel(nname, innovationVariance, ar, delta, ma);
    }

    public static Builder builder() {
        return new Builder()
                .innovationVariance(1)
                .ar(DoubleSeq.one())
                .delta(DoubleSeq.one())
                .ma(DoubleSeq.one());
    }
}
