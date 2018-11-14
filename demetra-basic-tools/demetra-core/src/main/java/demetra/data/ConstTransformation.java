/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.data;

import demetra.design.Development;
import demetra.data.transformation.DataTransformation;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class ConstTransformation implements DataTransformation {

    /**
     *
     * @param c
     * @return
     */
    public static ConstTransformation difference(double c) {
        return new ConstTransformation(OperationType.Diff, c);
    }

    /**
     *
     * @param c
     * @return
     */
    public static ConstTransformation product(double c) {
        return new ConstTransformation(OperationType.Product, c);
    }

    /**
     *
     * @param c
     * @return
     */
    public static ConstTransformation ratio(double c) {
        return new ConstTransformation(OperationType.Ratio, c);
    }

    /**
     *
     * @param c
     * @return
     */
    public static ConstTransformation sum(double c) {
        return new ConstTransformation(OperationType.Sum, c);
    }

    /**
     *
     * @param y
     * @return
     */
    public static ConstTransformation unit(double u) {
        return new ConstTransformation(OperationType.Product, u);
    }

    /**
     *
     */
    public final OperationType op;

    /**
     *
     */
    public final double value;
    
    private ConstTransformation(OperationType type, double val) {
        this.op = type;
        this.value = val;
    }

    /**
     *
     * @return
     */
    @Override
    public DataTransformation converse() {
        return new ConstTransformation(op.reverse(), value);
    }

    /**
     *
     * @param data
     * @param ljacobian
     * @return
     */
    @Override
    public DoubleSequence transform(DoubleSequence data, LogJacobian ljacobian) {
        double[] x = data.toArray();
        DataBlock X = DataBlock.ofInternal(x);
        switch (op) {
            case Diff:
                X.sub(value);
                break;
            case Product:
                X.mul(value);
                if (ljacobian != null) {
                    ljacobian.value += (ljacobian.end - ljacobian.start)
                            * Math.log(value);
                }
                break;
            case Sum:
                X.add(value);
                break;
            case Ratio:
                if (value == 0) {
                    return null;
                } else {
                    X.div(value);
                    if (ljacobian != null) {
                        ljacobian.value -= (ljacobian.end - ljacobian.start)
                                * Math.log(value);
                    }
                }
                break;
            default:
                return data;
        }
        return DoubleSequence.ofInternal(x);
    }
    
}
