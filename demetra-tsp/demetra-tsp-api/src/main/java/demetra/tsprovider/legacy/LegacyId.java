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
package demetra.tsprovider.legacy;

import demetra.design.DemetraPlusLegacy;
import nbbrd.design.Immutable;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;

@DemetraPlusLegacy
@Immutable
final class LegacyId implements Comparable<LegacyId>, Iterable<String> {

    static LegacyId parse(Handler handler, String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            return new LegacyId(handler, handler.split(str));
        } catch (Exception err) {
            return null;
        }
    }

    static LegacyId of(Handler handler, String first, String... others) {
        return new LegacyId(handler, join(first, others));
    }

    private final Handler stringHandler;
    private final String[] data;

    private LegacyId(Handler stringHandler, String[] data) {
        this.stringHandler = stringHandler;
        this.data = data;
    }

    public String get(int index) {
        return data[index];
    }

    public boolean is(int index) {
        return data.length == index + 1;
    }

    public LegacyId extend(String arg0, String... args) {
        return new LegacyId(stringHandler, join(data, arg0, args));
    }

    public LegacyId parent() {
        return data.length > 1 ? new LegacyId(stringHandler, sub(data, 0, data.length - 1)) : null;
    }

    public String tail() {
        return data[data.length - 1];
    }

    public LegacyId[] path() {
        LegacyId[] result = new LegacyId[data.length];
        result[0] = LegacyId.of(stringHandler, data[0]);
        for (int i = 1; i < data.length; i++) {
            result[i] = result[i - 1].extend(data[i]);
        }
        return result;
    }

    public int getCount() {
        return data.length;
    }

    @Override
    public String toString() {
        return stringHandler.join(data);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof LegacyId && equals((LegacyId) obj));
    }

    private boolean equals(LegacyId other) {
        return this.toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(LegacyId other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public Iterator<String> iterator() {
        return Arrays.asList(data).iterator();
    }

    private static String[] join(String arg0, String[] args) {
        String[] result = new String[1 + args.length];
        result[0] = arg0;
        System.arraycopy(args, 0, result, 1, args.length);
        return result;
    }

    private static String[] join(String[] left, String arg0, String... args) {
        String[] result = new String[left.length + 1 + args.length];
        System.arraycopy(left, 0, result, 0, left.length);
        result[left.length] = arg0;
        System.arraycopy(args, 0, result, left.length + 1, args.length);
        return result;
    }

    private static String[] sub(String[] data, int pos, int length) {
        String[] result = new String[length];
        System.arraycopy(data, pos, result, 0, length);
        return result;
    }

    enum Handler {

        PLAIN {
            static final String SEP = "<<>>";

            @Override
            public String join(String[] o) {
                if (0 == o.length) {
                    return "";
                } else {
                    final StringBuilder sb = new StringBuilder(o[0]);
                    for (int i = 1; i < o.length; i++) {
                        sb.append(SEP);
                        sb.append(o[i]);
                    }
                    return sb.toString();
                }
            }

            @Override
            public String[] split(String o) {
                return o.split(SEP);
            }
        },
        BASE64 {
            static final String SEP = "/";

            @Override
            public String join(String[] o) {
                if (0 == o.length) {
                    return "";
                } else {
                    final StringBuilder sb = new StringBuilder(o[0]);
                    for (int i = 1; i < o.length; i++) {
                        sb.append(SEP);
                        sb.append(Base64.getEncoder().encodeToString(o[i].getBytes()));
                    }
                    return sb.toString();
                }
            }

            @Override
            public String[] split(String o) {
                String[] splitResult = o.split(SEP);
                for (int i = 0; i < splitResult.length; i++) {
                    try {
                        splitResult[i] = new String(Base64.getDecoder().decode(splitResult[i]));
                    } catch (Exception e) {
                    }
                }
                return splitResult;
            }
        };

        abstract public String join(String[] o);

        abstract public String[] split(String o);
    }
}
