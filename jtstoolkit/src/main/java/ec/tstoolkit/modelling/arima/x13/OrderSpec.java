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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class OrderSpec implements InformationSetSerializable {

    public static final String TYPE = "type", REGULAR = "regular", SEASONAL = "seasonal";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, TYPE), String.class);
        dic.put(InformationSet.item(prefix, REGULAR), Integer.class);
        dic.put(InformationSet.item(prefix, SEASONAL), Integer.class);
    }

    public enum Type {

        Fixed,
        Max
    }

    public OrderSpec(int regular, int seasonal, Type type) {
        this.regular = regular;
        this.seasonal = seasonal;
        this.type = type;
    }
    public int regular;
    public int seasonal;
    public Type type;

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OrderSpec && equals((OrderSpec) obj));
    }

    private boolean equals(OrderSpec other) {
        return regular == other.regular && seasonal == other.seasonal && type == other.type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.regular;
        hash = 29 * hash + this.seasonal;
        hash = 29 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(TYPE, type.name());
        if (verbose || regular != 0) {
            info.add(REGULAR, regular);
        }
        if (verbose || seasonal != 0) {
            info.add(SEASONAL, seasonal);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            String type_ = info.get(TYPE, String.class);
            Integer regular_ = info.get(REGULAR, Integer.class);
            Integer seasonal_ = info.get(SEASONAL, Integer.class);
            
            if (type_ == null) {
                return false;
            }
            type = Type.valueOf(type_);

            if (regular_ != null) {
                regular = regular_;
            }

            if (seasonal_ != null) {
                seasonal = seasonal_;
            }
            
            return true;
        } catch (Exception err) {
            return false;
        }
    }
}
