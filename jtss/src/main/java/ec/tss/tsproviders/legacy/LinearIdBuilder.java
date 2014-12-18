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
package ec.tss.tsproviders.legacy;

import ec.tstoolkit.design.Immutable;
import java.util.Arrays;
import java.util.Iterator;

@Deprecated
@Immutable
public final class LinearIdBuilder implements IIdBuilder {

    // Factory Methods ->
    public static LinearIdBuilder parse(IStringHandler stringHandler, String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            return new LinearIdBuilder(stringHandler, stringHandler.split(str));
        } catch (Exception err) {
            return null;
        }
    }

    public static LinearIdBuilder from(IStringHandler stringHandler, String arg0,
            String... args) {

        return new LinearIdBuilder(stringHandler, join(arg0, args));
    }
    // <-
    private final IStringHandler stringHandler;
    private final String[] data;
    private String cache;

    private LinearIdBuilder(IStringHandler stringHandler, String[] data) {
        this.stringHandler = stringHandler;
        this.data = data;
    }

    @Override
    public String get(int index) {
        return data[index];
    }

    @Override
    public boolean is(int index) {
        return data.length == index + 1;
    }

    @Override
    public IIdBuilder extend(String arg0, String... args) {
        return new LinearIdBuilder(stringHandler, join(data, arg0, args));
    }

    @Override
    public IIdBuilder parent() {
        return data.length > 1 ? new LinearIdBuilder(stringHandler, sub(data, 0, data.length - 1)) : null;
    }

    @Override
    public String tail() {
        return data[data.length - 1];
    }

    @Override
    public IIdBuilder[] path() {
        IIdBuilder[] result = new IIdBuilder[data.length];
        result[0] = LinearIdBuilder.from(stringHandler, data[0]);
        for (int i = 1; i < data.length; i++) {
            result[i] = result[i - 1].extend(data[i]);
        }
        return result;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public String toString() {
        if (cache == null) {
            cache = stringHandler.aggregate(data);
        }
        return cache;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof LinearIdBuilder && equals((LinearIdBuilder) obj));
    }
    
    private boolean equals(LinearIdBuilder other) {
        return this.toString().equals(other.toString());
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    // IComparable<IIdBuilder> Members ->
    @Override
    public int compareTo(IIdBuilder other) {
        return toString().compareTo(other.toString());
    }

    // <-
    @Override
    public Iterator<String> iterator() {
        return Arrays.asList(data).iterator();
    }

    public static String[] join(String arg0, String[] args) {
        String[] result = new String[1 + args.length];
        result[0] = arg0;
        System.arraycopy(args, 0, result, 1, args.length);
        return result;
    }

    public static String[] join(String[] left, String arg0, String... args) {
        String[] result = new String[left.length + 1 + args.length];
        System.arraycopy(left, 0, result, 0, left.length);
        result[left.length] = arg0;
        System.arraycopy(args, 0, result, left.length + 1, args.length);
        return result;
    }

    public static String[] sub(String[] data, int pos, int length) {
        String[] result = new String[length];
        System.arraycopy(data, pos, result, 0, length);
        return result;
    }
}
