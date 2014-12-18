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
package ec.tss.disaggregation.documents;

import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.utilities.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author pcuser
 */
public class MultiCholetteSpecification implements IProcSpecification, Cloneable {
    
    public static final String CONSTRAINTS = "constraints", CONSTRAINT = "constraint", PARAMETERS = "parameters";
    
    public MultiCholetteSpecification() {
    }
    
    @Override
    public MultiCholetteSpecification clone() {
        try {
            MultiCholetteSpecification cl = (MultiCholetteSpecification) super.clone();
            cl.cholette = cholette.clone();
            cl.constraints = new ArrayList<>();
            cl.constraints.addAll(constraints);
            return cl;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
    
    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(PARAMETERS, cholette.write(verbose));
        if (!constraints.isEmpty()) {
            InformationSet cnt = info.subSet(CONSTRAINTS);
            int idx = 1;
            for (String c : constraints) {
                cnt.set(CONSTRAINT + idx, c);
                ++idx;
            }
        }
        return info;
    }
    
    @Override
    public boolean read(InformationSet info) {
        InformationSet ch = info.getSubSet(PARAMETERS);
        if (ch == null) {
            return false;
        }
        if (!cholette.read(info)) {
            return false;
        }
        InformationSet subSet = info.getSubSet(CONSTRAINTS);
        if (subSet != null) {
            List<Information<String>> select = subSet.select(CONSTRAINT + '*', String.class);
            for (Information<String> c : select) {
                constraints.add(c.value);
            }
        }
        return true;
    }
    private CholetteSpecification cholette = new CholetteSpecification();
    private List<String> constraints = new ArrayList<>();
    
    public CholetteSpecification getParameters() {
        return cholette;
    }
    
    public void clearConstraints() {
        constraints.clear();
    }
    
    public List<String> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }
    
    public void addConstraint(String c) {
        constraints.add(c);
    }
    
    public boolean equals(MultiCholetteSpecification spec) {
        return cholette.equals(spec.cholette)
                && Comparator.equals(constraints, spec.constraints);
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof MultiCholetteSpecification && equals((MultiCholetteSpecification) obj));
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.cholette != null ? this.cholette.hashCode() : 0);
        hash = 47 * hash + (this.constraints != null ? this.constraints.hashCode() : 0);
        return hash;
    }
}
