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
package demetra.x12;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class OrderSpec{

    public enum Type {

        Fixed,
        Max
    }

    public OrderSpec(int regular, int seasonal, Type type) {
        this.regular = regular;
        this.seasonal = seasonal;
        this.type = type;
    }
    public final int regular;
    public final int seasonal;
    public final Type type;

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

}
