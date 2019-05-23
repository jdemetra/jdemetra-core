/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.msts;

import java.util.ArrayList;
import java.util.List;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class CompositeModel {

    private MstsMapping mapping;
    private final List<ModelItem> items = new ArrayList<>();
    private final List<ModelEquation> equations = new ArrayList<>();

    public int getEquationsCount() {
        return equations.size();
    }

    public int getItemsCount() {
        return items.size();
    }

    public ModelItem getItem(int pos) {
        return items.get(pos);
    }
    
    public String[] getCmpsName(){
        return items.stream().map(item->item.getName()).toArray(n->new String[n]);
    }

    public ModelEquation getEquation(int pos) {
        return equations.get(pos);
    }

    public void add(ModelItem item) {
        this.items.add(item);
        mapping = null;
    }

    public void add(ModelEquation eq) {
        this.equations.add(eq);
        mapping = null;
    }

    public MstsMapping getMapping() {
        return mapping;
    }

    public void build() {
        mapping = new MstsMapping();
        for (ModelItem item : items) {
            item.addTo(mapping);
        }
        for (ModelEquation eq : equations) {
            eq.addTo(mapping);
        }
    }

    public double[] defaultParameters() {
        if (mapping == null) {
            build();
        }
        return mapping.getDefaultParameters().toArray();
    }

    public double[] fullDefaultParameters() {
        if (mapping == null) {
            build();
        }
        return mapping.modelParameters(mapping.getDefaultParameters()).toArray();
    }

    public CompositeModelEstimation estimate(FastMatrix data, double eps, boolean marginal, boolean rescaling, double[] parameters) {
        if (mapping == null) {
            build();
        }
        return CompositeModelEstimation.estimationOf(this, data, eps, marginal, rescaling, parameters);
    }

    public CompositeModelEstimation compute(FastMatrix data, double[] parameters, boolean marginal, boolean concentrated) {
        if (mapping == null) {
            build();
        }
        return CompositeModelEstimation.computationOf(this, data, DoubleSeq.of(parameters), marginal, concentrated);
    }
}
