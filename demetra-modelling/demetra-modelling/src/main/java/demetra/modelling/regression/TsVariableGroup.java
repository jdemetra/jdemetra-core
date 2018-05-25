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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.timeseries.TimeSeriesDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 * @param <D>
 */
public class TsVariableGroup<D extends TimeSeriesDomain<?>> implements ITsVariable<D> {

    public static <E extends TimeSeriesDomain<?>> Builder<E> builder(Class<E> eclass) {
        return new Builder<>();
    }

    @BuilderPattern(TsVariable.class)
    public static class Builder<E extends TimeSeriesDomain<?>> {

        private String name;
        private String desc;
        private List<ITsVariable> vars = new ArrayList<>();

        public Builder add(ITsVariable<E> var) {
            vars.add(var);
            return this;
        }

        public Builder add(ITsVariable<E>... vars) {
            for (int i = 0; i < vars.length; ++i) {
                this.vars.add(vars[i]);
            }
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String desc) {
            this.desc = desc;
            return this;
        }

        public TsVariableGroup<E> build() {
            if (vars.isEmpty() || name == null) {
                throw new IllegalArgumentException("Incomplete information");
            }
            return new TsVariableGroup<>(vars.toArray(new ITsVariable[vars.size()]), name, desc == null ? name : desc);
        }
    }
    private final ITsVariable[] vars;
    private final String name, desc;

    public TsVariableGroup(@Nonnull ITsVariable[] vars, @Nonnull String name, @Nonnull String desc) {
        this.vars = vars;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public void data(D domain, List<DataBlock> data) {
        for (int i = 0, n0 = 0; i < vars.length; ++i) {
            int n1 = n0 + vars[i].getDim();
            vars[i].data(domain, data.subList(n0, n1));
            n0 = n1;
        }
    }

    @Override
    public String getDescription(D context) {
        return desc == null ? "" : desc; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDim() {
        int n = 0;
        for (int i = 0; i < vars.length; ++i) {
            n += vars[i].getDim();
        }
        return n;
    }

    @Override
    public String getItemDescription(int idx, D context) {
        int cur = idx;
        for (int i = 0; i < vars.length; ++i) {
            int dim = vars[i].getDim();
            if (cur < dim) {
                return vars[i].getItemDescription(cur, context);
            }
            cur -= dim;
        }
        return "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TsVariableGroup<D> rename(String newname) {
        return new TsVariableGroup<>(vars, newname, desc.equals(name) ? newname : desc);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TsVariableGroup) {
            TsVariableGroup x = (TsVariableGroup) other;
            return Arrays.deepEquals(vars, x.vars);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Arrays.deepHashCode(this.vars);
        return hash;
    }

}
