/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.spi;

import demetra.maths.Complex;
import demetra.maths.PolynomialType;
import demetra.maths.spi.Polynomials;
import jdplus.maths.polynomials.MullerNewtonSolver;
import jdplus.maths.polynomials.Polynomial;
import jdplus.maths.polynomials.PolynomialException;
import jdplus.maths.polynomials.RootsSolver;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = Polynomials.Processor.class)
public class PolynomialsProcessor implements Polynomials.Processor {
    
    @Override
    public Complex[] roots(PolynomialType p) {
        RootsSolver solver=RootsSolver.fastSolver();
        Polynomial P=Polynomial.of(p);
        if (solver.factorize(P))
            return solver.roots();
        else
            throw new PolynomialException();
    }
    
}
