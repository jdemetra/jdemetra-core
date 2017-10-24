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
@lombok.Builder(toBuilder=true)
@lombok.Value
public class ArimaType {
    double innovationVariance;
    @lombok.NonNull PolynomialType phi;
    @lombok.NonNull PolynomialType delta;
    @lombok.NonNull PolynomialType theta;
    String name;

    public static ArimaTypeBuilder builder(){
        ArimaTypeBuilder builder= new ArimaTypeBuilder();
        builder.innovationVariance=1;
        builder.phi=PolynomialType.ONE;
        builder.delta=PolynomialType.ONE;
        builder.theta=PolynomialType.ONE;
        return builder;
    }
}
