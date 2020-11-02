/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.timeseries.regression;

import demetra.data.Parameter;
import demetra.design.Development;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class Variable {

    @lombok.NonNull
    private String name;

    @lombok.NonNull
    private ITsVariable core;

    private Parameter[] coefficients;

    @lombok.NonNull
    @lombok.Singular
    private List<String> attributes;
    
    public int dim() {
        return core.dim();
    }

    public boolean isAttribute(String id) {
        return attributes.contains(id);
    }

    public Parameter getCoefficient(int i) {
        return coefficients == null ? Parameter.undefined() : coefficients[i];
    }
    
    /**
     *
     * @param variable Actual variable
     * @param name
     * @param attributes
     * @return
     */
    public static Variable variable(@NonNull final String name, @NonNull final ITsVariable variable, String... attributes) {
        if (attributes == null) {
            return new Variable(name, variable, null, Collections.emptyList());
        } else if (attributes.length == 1) {
            return new Variable(name, variable, null, Collections.singletonList(attributes[0]));
        } else {
            List<String> att = new ArrayList<>(attributes.length);
            for (int i = 0; i < attributes.length; ++i) {
                att.add(attributes[i]);
            }
            return new Variable(name, variable, null, att);
        }
    }

    public int freeCoefficientsCount() {
        if (coefficients == null) {
            return core.dim();
        } else {
            return Parameter.freeParametersCount(coefficients);
        }
    }

    // main types
    /**
     *
     * @return True if all coefficients are fixed, false otherwise
     */
    public boolean isPreadjustment() {
        return coefficients != null && !Parameter.hasFreeParameters(coefficients);
    }

    /**
     *
     * @return True if all coefficients are free, false otherwise
     */
    public boolean isFree() {
        return coefficients == null || Parameter.isFree(coefficients);
    }

    public Variable rename(String name) {
        if (name.equals(this.name)) {
            return this;
        } else {
            return new Variable(name, core, coefficients, attributes);
        }
    }

    public Variable withCoefficient(Parameter coefficient) {
        if (core.dim() != 1) {
            throw new IllegalArgumentException();
        }
        return new Variable(name, core, new Parameter[]{coefficient}, attributes);
    }

    public Variable withCoefficient(Parameter[] coefficients) {
        if (coefficients != null && core.dim() != coefficients.length) {
            throw new IllegalArgumentException();
        }
        return new Variable(name, core, coefficients, attributes);
    }

    public Variable addAttribute(String attribute) {
        if (coefficients != null && core.dim() != coefficients.length) {
            throw new IllegalArgumentException();
        }
        List<String> natts;
        if (attributes.isEmpty()) {
            natts = Collections.singletonList(attribute);
        } else {
            natts = new ArrayList<>(attributes);
            natts.add(attribute);
        }
        return new Variable(name, core, coefficients, natts);
    }

    public Variable addAttributes(String... additionalAttributes) {
        if (coefficients != null && core.dim() != coefficients.length) {
            throw new IllegalArgumentException();
        }
        List<String> natts = new ArrayList<>(attributes);
        for (int i = 0; i < additionalAttributes.length; ++i) {
            natts.add(additionalAttributes[i]);
        }
        return new Variable(name, core, coefficients, natts);
    }

}
