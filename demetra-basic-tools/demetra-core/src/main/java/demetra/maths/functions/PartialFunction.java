/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.functions;

import demetra.data.DoubleSequence;

/**
 *
 * @author palatej
 */
public class PartialFunction implements IFunction {

    private final PartialMapping mapping;
    private final IFunction fn;

    public PartialFunction(final PartialMapping mapping, final IFunction fn) {
        this.mapping = mapping;
        this.fn = fn;
    }

    class Point implements IFunctionPoint {

        private final DoubleSequence pt;

        Point(DoubleSequence pt) {
            this.pt = pt;
        }

        @Override
        public IFunction getFunction() {
            return PartialFunction.this;
        }

        @Override
        public DoubleSequence getParameters() {
            return pt;
        }

        @Override
        public double getValue() {
            return fn.evaluate(mapping.convert(pt)).getValue();
        }

    }

    @Override
    public IFunctionPoint evaluate(DoubleSequence parameters) {
        return new Point(parameters);
    }

    @Override
    public PartialMapping getDomain() {
        return mapping;
    }

}
