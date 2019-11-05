/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.maths.functions.gsl.integration;

import internal.jdplus.maths.functions.gsl.integration.QK15;
import internal.jdplus.maths.functions.gsl.integration.QK;
import jdplus.maths.polynomials.Polynomial;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class QK15Test {

    public QK15Test() {
    }

    @Test
    public void testPolynomial() {
        QK q = QK15.of(x -> x * x * x - 3 * x * x + 5 * x - 11, -2, 3);
        Polynomial p = Polynomial.of(0, -11, 2.5, -1, .25);
        double z = p.evaluateAt(3) - p.evaluateAt(-2);
        assertEquals(q.getResult(), z, 1e-9);
    }

    @Test
    public void testLog() {
        QK q = QK15.of(x -> Math.log(x), 2, 3);
        double z = 3 * Math.log(3) - 2 * Math.log(2) - 1;
        assertEquals(q.getResult(), z, 1e-9);
    }
}
