/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.maths.RealPolynomial;

/**
 *
 * @author Jean Palate
 */
public interface ArimaType {

    double getInnovationVariance();

    RealPolynomial getAr();

    RealPolynomial getDelta();

    RealPolynomial getMa();

}
