/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.functions;

import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class GridSearchTest {

    public GridSearchTest() {
    }

    @Test
    public void testSimple() {
        GridSearch min = GridSearch.builder()
                .bounds(-1, 1)
                .functionPrecision(1e-15)
                .parametersPrecision(1e-8)
                .maxIter(1000)
                .build();

        min.minimize(MyFunction.FN.evaluate(DoubleSeq.of(0)));
        assertEquals(min.getResult().getParameters().get(0), -.5+Math.sqrt(69)/6, 1e-6);
    }

}

// (x-.5)(x^2+2x-4) = x^3 + 1.5 x^2 - 5 x + 2 
// f' = 3 x^2 + 3 x - 5
// f''= 6 x + 3
class MyFunction implements IFunction {

    static MyFunction FN = new MyFunction();

    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {
        return new IFunctionPoint() {
            @Override

            public IFunctionDerivatives derivatives() {
                return new IFunctionDerivatives() {
                    @Override
                    public IFunction getFunction() {
                        return FN;
                    }

                    @Override
                    public DoubleSeq gradient() {
                        double x = parameters.get(0);
                        return DoubleSeq.of(3 * x * x + 3 * x - 5);
                    }

                    @Override
                    public void hessian(FastMatrix hessian) {
                        double x = parameters.get(0);
                        hessian.set(0, 0, 6 * x + 3);
                    }
                };
            }

            @Override
            public IFunction getFunction() {
                return FN;
            }

            @Override
            public DoubleSeq getParameters() {
                return parameters;
            }

            @Override
            public double getValue() {
                double x = parameters.get(0);
                return x * x * x + 1.5 * x * x - 5 * x + 2;
            }

        };
    }

    @Override
    public IParametersDomain getDomain() {
        return new IParametersDomain() {
            @Override
            public boolean checkBoundaries(DoubleSeq inparams) {
                return true;
            }

            @Override
            public double epsilon(DoubleSeq inparams, int idx) {
                return 1e-15;
            }

            @Override
            public int getDim() {
                return 1;
            }

            @Override
            public double lbound(int idx) {
                return -Double.MAX_VALUE;
            }

            @Override
            public double ubound(int idx) {
                return Double.MAX_VALUE;
            }

            @Override
            public ParamValidation validate(DataBlock ioparams) {
                return ParamValidation.Valid;
            }
        };
    }

}
