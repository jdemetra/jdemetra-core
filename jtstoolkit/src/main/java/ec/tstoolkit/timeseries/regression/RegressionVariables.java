/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Jean Palate
 */
public class RegressionVariables {

    /**
     *
     * @param <T>
     */
    public static class Item<T extends ITsVariable> {

        /**
         *
         */
        public final T variable;
        /**
         *
         */
        public final Parameter[] coefficients;

        /**
         *
         * @param variable
         * @param coeff
         */
        public Item(T variable, final Parameter[] coeff) {
            if (coeff.length != variable.getDim()) {
                throw new IllegalArgumentException();
            }
            this.variable = variable;
            this.coefficients = coeff;
        }
    }

    private final ArrayList<Item<ITsVariable>> regs = new ArrayList<>();

    public RegressionVariables() {
    }

    public static RegressionVariables makeCopy(RegressionVariables vars) {
        RegressionVariables nvars = new RegressionVariables();
        for (Item item : vars.regs) {
            nvars.regs.add(new Item(item.variable, Parameter.clone(item.coefficients)));
        }
        return nvars;
    }

    public static RegressionVariables select(RegressionVariables vars, Predicate<ITsVariable> predicate) {
        RegressionVariables nvars = new RegressionVariables();
        for (Item item : vars.regs) {
            if (predicate.test(item.variable)) {
                nvars.regs.add(new Item(item.variable, Parameter.clone(item.coefficients)));
            }
        }
        return nvars;
    }

    public <T extends ITsVariable> void add(Item<T> var) {
        regs.add((Item<ITsVariable>) var);
    }

    public void setParameterType(ParameterType type) {
        for (Item item : regs) {
            for (int i = 0; i < item.coefficients.length; ++i) {
                item.coefficients[i].setType(type);
            }
        }
    }

    /**
     *
     * @param domain
     * @return
     */
    public TsData data(TsDomain domain) {
        TsData sum = new TsData(domain, 0);
        for (Item item : regs) {
            if (!Parameter.isDefined(item.coefficients)) {
                return null;
            }
            int dim = item.variable.getDim();
            if (dim == 1) {
                final double c = item.coefficients[0].getValue();
                if (c != 0) {
                    DataBlock tmp = new DataBlock(domain.getLength());
                    item.variable.data(domain, Collections.singletonList(tmp));
                    sum.apply(tmp, (x, y) -> x + c * y);
                }
            } else {
                Matrix tmp = new Matrix(domain.getLength(), dim);
                List<DataBlock> cols = tmp.columnList();
                item.variable.data(domain, cols);
                int pos = 0;
                for (DataBlock col : cols) {
                    final double c = item.coefficients[pos++].getValue();
                    if (c != 0) {
                        sum.apply(col, (x, y) -> x + c * y);
                    }
                }
            }
        }
        return sum;
    }

}
