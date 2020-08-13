/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.spi;

import demetra.data.DoubleSeq;
import demetra.math.Complex;
import demetra.math.algebra.Ring;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Polynomials {
    
    public static enum Solver{
        Default,
        MullerNewton,
        EigenValues,
        FastEigenValues,
        Robust
    }
     
    private final PolynomialsLoader.Processor PROCESSOR = new PolynomialsLoader.Processor();

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public Complex[] rootsOf(DoubleSeq polynomial, Solver solver) {
        return PROCESSOR.get().rootsOf(polynomial, solver);
    }

    public double evaluate(DoubleSeq polynomial, double x) {
        return PROCESSOR.get().evaluate(polynomial, x);
    }
    
    public Complex evaluate(DoubleSeq polynomial, Complex x) {
        return PROCESSOR.get().evaluate(polynomial, x);
    }

    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Processor extends Ring<DoubleSeq>{

        Complex[] rootsOf(DoubleSeq polynomial, Solver solver);
        
        double evaluate(DoubleSeq polynomial, double x);

        Complex evaluate(DoubleSeq polynomial, Complex x);
    }
    
}
