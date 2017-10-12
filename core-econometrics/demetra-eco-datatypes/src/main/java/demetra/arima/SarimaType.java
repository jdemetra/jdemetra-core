/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.maths.PolynomialType;
import lombok.Builder;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Data
public class SarimaType {
    double innovationVariance=1;
    @lombok.NonNull PolynomialType phi=PolynomialType.ONE;
    int d;
    @lombok.NonNull PolynomialType theta=PolynomialType.ONE;
    int period=1;
    PolynomialType bphi;
    int bd;
    PolynomialType btheta;
    
}
