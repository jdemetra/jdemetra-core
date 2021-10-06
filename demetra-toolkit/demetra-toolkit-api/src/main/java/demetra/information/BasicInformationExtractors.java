/*
 * Copyright 2021 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.information;

import nbbrd.design.Development;
import demetra.util.WildCards;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
class BasicInformationExtractors {

    static final String LSTART = "(", LEND = ")";

    static String listKey(String prefix, int item) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append(LSTART).append(item).append(LEND);
        return builder.toString();
    }

    static int listItem(String prefix, String key) {
        if (!key.startsWith(prefix)) {
            return Integer.MIN_VALUE;
        }
        int start = prefix.length() + LSTART.length();
        int end = key.length() - LEND.length();
        if (end <= start) {
            return Integer.MIN_VALUE;
        }
        String s = key.substring(start, end);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
        }
    }

    static String wcKey(String prefix, char wc) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append(LSTART);
        builder.append(wc).append(LEND);
        return builder.toString();
    }

    static class AtomicExtractor<S> implements BasicInformationExtractor<S> {

        final String name;
        final Class<?> targetClass;
        final Function<S, ?> fn;

        <T> AtomicExtractor(String name, final Class<T> targetClass, final Function<S, T> fn) {
            this.name = name;
            this.targetClass = targetClass;
            this.fn = fn;
        }

        @Override
        public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
            dic.put(BasicInformationExtractor.concatenate(prefix, name), targetClass);
        }

        @Override
        public boolean contains(String id) {
            return id.equals(name);
        }

        @Override
        public <T> T getData(S source, String id, Class<T> tclass) {
            if (source == null) {
                return null;
            }
            if (tclass.isAssignableFrom(targetClass) && id.equals(name)) {
                return (T) fn.apply(source);
            } else {
                return null;
            }
        }

        @Override
        public <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map) {
            if (tclass.isAssignableFrom(targetClass)) {
                if (wc.match(name)) {
                    map.put(name, (T) fn.apply(source));
                }
            }
        }
    }

    static class ArrayExtractor<S> implements BasicInformationExtractor<S> {

        final String name;
        final Class<?> targetClass;
        final BiFunction<S, Integer, ?> fn;
        final int start, end;

        <T> ArrayExtractor(String name, int start, int end, final Class<T> targetClass, final BiFunction<S, Integer, T> fn) {
            this.name = name;
            this.targetClass = targetClass;
            this.fn = fn;
            this.start = start;
            this.end = end;
        }

        @Override
        public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
            if (compact) {
                String item = wcKey(name, start == end ? '?' : '*');
                dic.put(BasicInformationExtractor.concatenate(prefix, item), targetClass);
            } else {
                if (start == end) {
                    dic.put(BasicInformationExtractor.concatenate(prefix, listKey(name, start)), targetClass);
                } else {
                    for (int i = start; i < end; ++i) {
                        dic.put(BasicInformationExtractor.concatenate(prefix, listKey(name, i)), targetClass);
                    }
                }
            }
        }

        @Override
        public boolean contains(String id) {
            int idx = listItem(name, id);
            return idx != Integer.MIN_VALUE;
        }

        @Override
        public <T> T getData(S source, String id, Class<T> tclass) {
            if (source == null) {
                return null;
            }
            if (tclass.isAssignableFrom(targetClass)) {
                int idx = listItem(name, id);
                if (idx == Integer.MIN_VALUE) {
                    return null;
                }
                return (T) fn.apply(source, idx);
            }
            return null;
        }

        @Override
        public <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map) {
            if (tclass.isAssignableFrom(targetClass)) {
                int endc = end;
                if (start == end) {
                    ++endc;
                }
                for (int i = start; i < end; ++i) {
                    String key = listKey(name, i);
                    if (wc.match(key)) {
                        Object obj = fn.apply(source, i);
                        // We stop when the requested object is unavailable
                        if (obj == null) {
                            return;
                        }
                        map.put(key, (T) obj);
                    }
                }
            }
        }
    }

    static class ExtractorDelegate<S, T> implements BasicInformationExtractor<S> {

        final String name;
        final Function<S, T> fn;
        final Class<T> target;

        ExtractorDelegate(String name, final Class<T> target, final Function<S, T> fn) {
            this.name = (name == null || name.length() == 0) ? null : name;
            this.fn = fn;
            this.target = target;
        }

        @Override
        public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
            InformationExtractors.fillDictionary(target, BasicInformationExtractor.concatenate(prefix, name), dic, compact);
        }

        @Override
        public boolean contains(String id) {
            if (name == null) {
                return InformationExtractors.contains(target, id);
            }
            if (id.length() <= name.length()) {
                return false;
            }
            if (id.startsWith(name) && id.charAt(name.length()) == BasicInformationExtractor.SEP) {
                return InformationExtractors.contains(target, id.substring(name.length() + 1));
            } else {
                return false;
            }
        }

        @Override
        public <Q> Q getData(S source, String id, Class<Q> qclass) {
            if (source == null) {
                return null;
            }
            T t = fn.apply(source);
            if (t == null) {
                return null;
            }
            String subitem = id;
            if (name != null) {
                if (id.length() <= name.length()) {
                    return null;
                }
                if (!id.startsWith(name) || id.charAt(name.length()) != BasicInformationExtractor.SEP) {
                    return null;
                }
                subitem = id.substring(name.length() + 1);
            }
            return InformationExtractors.getData(target, t, subitem, qclass);
        }

        @Override
        public <Q> void searchAll(S source, WildCards wc, Class<Q> tclass, Map<String, Q> map) {
            String wcs = wc.toString();
            if (name != null) {
                if (wcs.startsWith(name) && wcs.charAt(name.length()) == BasicInformationExtractor.SEP) {
                    String subwcs = wcs.substring(name.length() + 1);
                    Map<String, Q> tmap=new LinkedHashMap<>();
                    InformationExtractors.searchAll(target, fn.apply(source), new WildCards(subwcs), tclass, tmap);
                    if (! tmap.isEmpty()){
                        tmap.forEach((key, value)->map.put(BasicInformationExtractor.concatenate(name, key), value));
                    }
                    return;
                }
            }
            InformationExtractors.searchAll(target, fn.apply(source), wc, tclass, map);
        }

    }

    static class ArrayExtractorDelegate<S, T> implements BasicInformationExtractor<S> {

        final String name;
        final BiFunction<S, Integer, T> fn;
        final Class<T> target;
        final int start, end;

        ArrayExtractorDelegate(String name, int start, int end, final Class<T> target, final BiFunction<S, Integer, T> fn) {
            this.name = name;
            this.fn = fn;
            this.target = target;
            this.start = start;
            this.end = end;
        }

        @Override
        public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
            if (compact) {
                String item = wcKey(name, start == end ? '?' : '*');
                InformationExtractors.fillDictionary(target, BasicInformationExtractor.concatenate(prefix, item), dic, compact);
            } else {
                if (start == end) {
                    String item = listKey(name, start);
                    InformationExtractors.fillDictionary(target, BasicInformationExtractor.concatenate(prefix, item), dic, compact);
                } else {
                    for (int i = start; i < end; ++i) {
                        String item = listKey(name, i);
                        InformationExtractors.fillDictionary(target, BasicInformationExtractor.concatenate(prefix, item), dic, compact);
                    }
                }
            }
        }

        @Override
        public boolean contains(String id) {
            int idx = listItem(name, id);
            return InformationExtractors.contains(target, detail(id));
        }

        @Override
        public <Q> Q getData(S source, String id, Class<Q> qclass) {
            if (source == null) {
                return null;
            }
            int idx = listItem(name, id);
            if (idx == Integer.MIN_VALUE) {
                return null;
            }
            T t = (T) fn.apply(source, idx);
            if (t == null) {
                return null;
            }
            String detail = detail(id);
            return InformationExtractors.getData(target, t, detail, qclass);
        }

        static String detail(String key) {
            int pos = key.indexOf(LEND);
            if (pos <= 0) {
                return null;
            } else {
                return key.substring(pos + 2);
            }
        }
    }
}
