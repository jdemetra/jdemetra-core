/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.design.Development;
import demetra.maths.PolynomialType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.Builder(builderClassName="Builder")
@lombok.Value
public class LightArimaType implements ArimaType{

//    @lombok.Builder.Default
//    private double innovationVariance = 1;
//    @lombok.NonNull
//    @lombok.Builder.Default
//    private PolynomialType ar = PolynomialType.ONE;
//    @lombok.NonNull
//    @lombok.Builder.Default
//    private PolynomialType delta = PolynomialType.ONE;
//    @lombok.NonNull
//    @lombok.Builder.Default
//    private PolynomialType ma = PolynomialType.ONE;
//    private String name;

    private double innovationVariance;
    @lombok.NonNull
    private PolynomialType ar;
    @lombok.NonNull
    private PolynomialType delta;
    @lombok.NonNull
    private PolynomialType ma;
    private String name;
    
    public static Builder builder(){
        Builder builder=new Builder();
        builder.innovationVariance=1;
        builder.ar = PolynomialType.ONE;
        builder.delta = PolynomialType.ONE;
        builder.ma = PolynomialType.ONE;
        return builder;
    }
}
