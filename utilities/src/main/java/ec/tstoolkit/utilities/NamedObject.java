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
package ec.tstoolkit.utilities;

import com.google.common.base.Objects;

/**
 *
 * @author Jean Palate
 */
public final class NamedObject<T> implements Comparable<NamedObject<T>> {

    public final String name;
    public final T object;

    public NamedObject(String name, T obj) {
        this.name = name;
        this.object = obj;
    }

    @Override
    public int compareTo(NamedObject<T> o) {
        int result = name.compareTo(o.name);
        if (result != 0) {
            return result;
        }
        if (object instanceof Comparable) {
            return ((Comparable) object).compareTo(o.object);
        }
        return Objects.equal(object, o.object) ? 0 : 1;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, object);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof NamedObject && equals((NamedObject) obj));
    }

    private boolean equals(NamedObject that) {
        return Objects.equal(this.name, that.name) && Objects.equal(this.object, that.object);
    }
}
