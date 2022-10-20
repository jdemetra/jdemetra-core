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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import nbbrd.design.Development;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 * @param <V>
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.EqualsAndHashCode(exclude = {"attributes"})
@lombok.Builder(toBuilder = true)
public class Variable<V extends ITsVariable> {
    
    @lombok.NonNull
    private String name;
    
    @lombok.NonNull
    private V core;
    
    private Parameter[] coefficients;
    
    @lombok.NonNull
    @lombok.Singular
    private Map<String, String> attributes;
    
    public int dim() {
        return core.dim();
    }
    
    public boolean hasAttribute(String id) {
        return attributes.containsKey(id);
    }
    
    public String attribute(String id) {
        return attributes.get(id);
    }
    
    public boolean isAttribute(String key, String value) {
        String val = attributes.get(key);
        return val != null && val.equals(value);
    }
    
    public boolean test(Predicate<ITsVariable> pred){
        if (core instanceof ModifiedTsVariable mcore)
            return pred.test(mcore.getVariable());
        else 
            return pred.test(core);
    }
    
    @NonNull
    public Parameter getCoefficient(int i) {
        return coefficients == null ? Parameter.undefined() : coefficients[i];
    }
    
    @NonNull
    public Parameter[] getCoefficients() {
        return coefficients == null ? Parameter.make(core.dim()) : coefficients.clone();
    }

    /**
     *
     * @param variable Actual variable
     * @param name
     * @param attributes
     * @return
     */
    public static Variable variable(@NonNull final String name, @NonNull final ITsVariable variable, Map<String, String> attributes) {
        if (attributes == null) {
            return new Variable(name, variable, null, Collections.emptyMap());
        } else {
            return new Variable(name, variable, null, Collections.unmodifiableMap(attributes));
        }
    }
    
    public static Variable variable(@NonNull final String name, @NonNull final ITsVariable variable) {
        return new Variable(name, variable, null, Collections.emptyMap());
    }
    
    public int freeCoefficientsCount() {
        if (coefficients == null) {
            return core.dim();
        } else {
            return Parameter.freeParametersCount(coefficients);
        }
    }

   public int fixedCoefficientsCount() {
        if (coefficients == null) {
            return 0;
        } else {
            return Parameter.fixedParametersCount(coefficients);
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
        return Parameter.isFree(coefficients);
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
        if (coefficient == null && this.coefficients == null) {
            return this;
        }
        return new Variable(name, core, coefficient == null ? null : new Parameter[]{coefficient}, attributes);
    }
    
    public Variable withCoefficients(Parameter[] coefficients) {
        if (coefficients != null && core.dim() != coefficients.length) {
            throw new IllegalArgumentException();
        }
        if (coefficients == null && this.coefficients == null) {
            return this;
        }
        return new Variable(name, core, coefficients, attributes);
    }
    
    public Variable withCore(ITsVariable ncore) {
        if (coefficients != null && ncore.dim() != coefficients.length) {
            throw new IllegalArgumentException();
        }
        return new Variable(name, ncore, coefficients, attributes);
    }
    
    public Variable withoutAttribute(String key) {
        if (!attributes.containsKey(key)) {
            return this;
        }
        Map<String, String> natts;
        natts = new HashMap<>(attributes);
        natts.remove(key);
        return natts.isEmpty() ? new Variable(name, core, coefficients, Collections.emptyMap())
                : new Variable(name, core, coefficients, Collections.unmodifiableMap(natts));
    }
    
    public Variable addAttribute(String key, String value) {
        Map<String, String> natts;
        natts = new HashMap<>(attributes);
        natts.put(key, value);
        return new Variable(name, core, coefficients, Collections.unmodifiableMap(natts));
    }
    
    public Variable removeAttribute(String key) {
        Map<String, String> natts;
        natts = new HashMap<>(attributes);
        natts.remove(key);
        return new Variable(name, core, coefficients, Collections.unmodifiableMap(natts));
    }

    /**
     * Same as without(oldkey).addAttribute(newKey, newValue)
     *
     * @param oldkey
     * @param newkey
     * @param newvalue
     * @return
     */
    public Variable replaceAttribute(String oldkey, String newkey, String newvalue) {
        Map<String, String> natts;
        natts = new HashMap<>(attributes);
        natts.remove(oldkey);
        natts.put(newkey, newvalue);
        return new Variable(name, core, coefficients, Collections.unmodifiableMap(natts));
    }
    
    public Variable addAttributes(Map<String, String> additionalAttributes) {
        if (additionalAttributes.isEmpty()) {
            return this;
        }
        if (attributes.isEmpty()) {
            return new Variable(name, core, coefficients, Collections.unmodifiableMap(additionalAttributes));
        }
        Map<String, String> natts = new HashMap<>(attributes);
        natts.putAll(additionalAttributes);
        return new Variable(name, core, coefficients, Collections.unmodifiableMap(natts));
    }
    
    public static final String EXCLUDED = "excluded";
    
    public boolean isExcluded() {
        return attributes.containsKey(EXCLUDED);
    }
    
    public Variable exclude(boolean excluded) {
        if (isExcluded() == excluded) {
            return this;
        }
        if (!excluded) {
            return removeAttribute(EXCLUDED).withCoefficients(null);
        } else {
            return addAttribute(EXCLUDED, Boolean.toString(excluded)).withCoefficients(Parameter.zero(core.dim()));
        }
    }
    
}
