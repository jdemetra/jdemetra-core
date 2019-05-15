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
@lombok.Value
public class LightArimaType implements ArimaType{

    private double innovationVariance;
    @lombok.NonNull
    private PolynomialType ar;
    @lombok.NonNull
    private PolynomialType delta;
    @lombok.NonNull
    private PolynomialType ma;
    private String name;
    
}
