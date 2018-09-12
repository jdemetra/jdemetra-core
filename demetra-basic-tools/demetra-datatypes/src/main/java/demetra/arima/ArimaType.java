/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.maths.PolynomialType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Builder(toBuilder = true)
@lombok.Value
public class ArimaType {

    @lombok.Builder.Default
    private double innovationVariance = 1;
    @lombok.NonNull
    @lombok.Builder.Default
    private PolynomialType ar = PolynomialType.ONE;
    @lombok.NonNull
    @lombok.Builder.Default
    private PolynomialType delta = PolynomialType.ONE;
    @lombok.NonNull
    @lombok.Builder.Default
    private PolynomialType ma = PolynomialType.ONE;
    private String name;

}
