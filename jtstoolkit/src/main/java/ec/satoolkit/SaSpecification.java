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
package ec.satoolkit;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.information.InformationSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class SaSpecification implements ISaSpecification, Cloneable {

    /**
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * @return the specification
     */
    public Spec getSpecification() {
        return specification;
    }

    /**
     * @param specification the specification to set
     */
    public void setSpecification(Spec specification) {
        this.specification = specification;
    }

    @Override
    public String toLongString() {
        return method.name();
    }

    public static enum Method {

        None, TramoSeats, X13
    }

    public static enum Spec {

        RSA0, RSA1, RSA2, RSA3, RSA4, RSA5
    }
    private Method method = Method.None;
    private Spec specification = Spec.RSA0;

    public boolean isDefault() {
        return method == Method.None;
    }

    public boolean equals(SaSpecification other) {
         return Objects.equals(method, other.method) && Objects.equals(specification, other.specification);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SaSpecification && equals((SaSpecification) obj));
    }
    
    @Override
    public SaSpecification clone() {
        try {
            return (SaSpecification) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(METHOD, method.name());
        if (specification != Spec.RSA0 || verbose) {
            info.add(SPEC, specification.name());
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            String m = info.get(METHOD, String.class);
            if (m != null) {
                method = Method.valueOf(m);
            }
            String s = info.get(SPEC, String.class);
            if (s != null) {
                specification = Spec.valueOf(s);
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    public ISaSpecification getFullSpecification() {
        if (method == SaSpecification.Method.X13) {
            switch (specification) {
                case RSA0:
                    return X13Specification.RSA0;
                case RSA1:
                    return X13Specification.RSA1;
                case RSA2:
                    return X13Specification.RSA2;
                case RSA3:
                    return X13Specification.RSA3;
                case RSA4:
                    return X13Specification.RSA4;
                case RSA5:
                    return X13Specification.RSA5;
            }
        } else if (method == SaSpecification.Method.TramoSeats) {
            switch (specification) {
                case RSA0:
                    return TramoSeatsSpecification.RSA0;
                case RSA1:
                    return TramoSeatsSpecification.RSA1;
                case RSA2:
                    return TramoSeatsSpecification.RSA2;
                case RSA3:
                    return TramoSeatsSpecification.RSA3;
                case RSA4:
                    return TramoSeatsSpecification.RSA4;
                case RSA5:
                    return TramoSeatsSpecification.RSA5;
            }
        }
        return null;
    }

    public static final String METHOD = "method", SPEC = "spec";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, METHOD), String.class);
        dic.put(InformationSet.item(prefix, SPEC), String.class);
    }

}
