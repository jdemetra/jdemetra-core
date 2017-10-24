/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.information;

import demetra.utilities.WildCards;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
public interface InformationExtractor<S> {

    static final String LSTART = "(", LEND = ")";

    static <S, Q> InformationExtractor<S> extractor(final String name, final Class<Q> targetClass,
            final Function<S, Q> fn) {
        return new AtomicExtractor<>(name, targetClass, fn);
    }

    static <S, Q> InformationExtractor<S> delegate(final String name, final InformationExtractor<Q> extractor, final Function<S, Q> fn) {
        return new ExtractorDelegate<>(name, extractor, fn);
    }

    static <S, Q> InformationExtractor<S> delegateArray(final String name, final int start, final int end, final InformationExtractor<Q> extractor, final BiFunction<S, Integer, Q> fn) {
        return new ArrayExtractorDelegate<>(name, start, end, extractor, fn);
    }

    static <S, Q> InformationExtractor<S> array(final String name, final int start, final int end,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        return new ArrayExtractor<>(name, start, end, targetClass, fn);
    }

    static <S, Q> InformationExtractor<S> array(final String name, final int defparam,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        return new ArrayExtractor<>(name, defparam, defparam, targetClass, fn);
    }

    void fillDictionary(String prefix, Map<String, Class> dic, boolean compact);

    boolean contains(String id);

    <T> T getData(S source, String id, Class<T> tclass);

    <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map);

    static class AtomicExtractor<S> implements InformationExtractor<S> {

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
            dic.put(InformationSet.concatenate(prefix, name), targetClass);
        }

        @Override
        public boolean contains(String id) {
            return id.equals(name);
        }

        @Override
        public <T> T getData(S source, String id, Class<T> tclass) {
            if (tclass.isAssignableFrom(targetClass) && id.equals(name)) {
                return (T) fn.apply(source);
            } else {
                return null;
            }
        }

        @Override
        public <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map) {
            if (wc.match(name)) {
                if (tclass.isAssignableFrom(targetClass)) {
                    map.put(name, (T) fn.apply(source));
                }
            }
        }
    }

    static class ArrayExtractor<S> implements InformationExtractor<S> {

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
            dic.put(InformationSet.concatenate(prefix, name), targetClass);
        }

        @Override
        public boolean contains(String id) {
            if (start == end) {
                return isIParamItem(name, id);
            } else {
                int idx = listItem(name, id);
                return idx >= start && idx < end;
            }
        }

        @Override
        public <T> T getData(S source, String id, Class<T> tclass) {
            if (tclass.isAssignableFrom(targetClass)) {
                int idx = listItem(name, id);
                if (idx == Integer.MIN_VALUE) {
                    return null;
                }
                if (start == end) {
                    return (T) fn.apply(source, idx);
                } else if (idx >= start && idx < end) {
                    return (T) fn.apply(source, idx);
                }
            }
            return null;
        }

        static String listKey(String prefix, int item) {
            StringBuilder builder = new StringBuilder();
            builder.append(prefix).append(LSTART).append(item).append(LEND);
            return builder.toString();
        }

        static String wcKey(String prefix, char wc) {
            StringBuilder builder = new StringBuilder();
            builder.append(prefix).append(LSTART);
            builder.append(wc).append(LEND);
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

        static boolean isIParamItem(String prefix, String key) {
            if (!key.startsWith(prefix)) {
                return false;
            }
            int start = prefix.length() + LSTART.length();
            int end = key.length() - LEND.length();
            if (end <= start) {
                return false;
            }
            String s = key.substring(start, end);
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        @Override
        public <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map) {
            if (tclass.isAssignableFrom(targetClass)) {
                // far to be optimal... TO IMPROVE
                for (int i = start; i <= end; ++i) {
                    String key = listKey(name, i);
                    if (wc.match(key)) {
                        map.put(key, (T) fn.apply(source, i));
                    }
                }
            }
        }
    }

    static class ExtractorDelegate<S, T> implements InformationExtractor<S> {

        final String name;
        final Function<S, T> fn;
        final InformationExtractor<T> extractor;

        ExtractorDelegate(String name, final InformationExtractor<T> extractor, final Function<S, T> fn) {
            this.name = name;
            this.fn = fn;
            this.extractor = extractor;
        }

        @Override
        public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
            extractor.fillDictionary(InformationSet.concatenate(prefix, name), dic, compact);
        }

        @Override
        public boolean contains(String id) {
            if (id.length() <= name.length()) {
                return false;
            }
            if (id.startsWith(name) && id.charAt(name.length()) == InformationSet.SEP) {
                return extractor.contains(id.substring(name.length() + 1));
            } else {
                return false;
            }
        }

        @Override
        public <Q> Q getData(S source, String id, Class<Q> qclass) {
            String subitem = id.substring(name.length() + 1);
            T t = fn.apply(source);
            if (t == null) {
                return null;
            } else {
                return extractor.getData(t, subitem, qclass);
            }
        }

        @Override
        public <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map) {
            extractor.searchAll(fn.apply(source), wc, tclass, map);
        }

    }

    static class ArrayExtractorDelegate<S, T> implements InformationExtractor<S> {

        final String name;
        final BiFunction<S, Integer, T> fn;
        final InformationExtractor<T> extractor;
        final int start, end;

        ArrayExtractorDelegate(String name, int start, int end, final InformationExtractor<T> extractor, final BiFunction<S, Integer, T> fn) {
            this.name = name;
            this.fn = fn;
            this.extractor = extractor;
            this.start = start;
            this.end = end;
        }

        @Override
        public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
            extractor.fillDictionary(InformationSet.concatenate(prefix, name), dic, compact);
        }

        @Override
        public boolean contains(String id) {
            if (start == end) {
                return isIParamItem(name, id) && extractor.contains(detail(id));
            } else {
                int idx = listItem(name, id);
                if (idx >= start && idx < end) {
                    return extractor.contains(detail(id));
                } else {
                    return false;
                }
            }
        }

        @Override
        public <Q> Q getData(S source, String id, Class<Q> qclass) {
            int idx = listItem(name, id);
            if (idx == Integer.MIN_VALUE) {
                return null;
            }
            T t = null;
            if (start == end) {
                t = (T) fn.apply(source, idx);
            } else if (idx >= start && idx < end) {
                t = (T) fn.apply(source, idx);
            }
            if (t == null) {
                return null;
            }
            String detail = detail(id);
            return extractor.getData(t, detail, qclass);
        }

        static int listItem(String prefix, String key) {
            if (!key.startsWith(prefix)) {
                return Integer.MIN_VALUE;
            }
            int start = prefix.length() + LSTART.length();
            int end = key.indexOf(LEND);
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

        static String detail(String key) {
            int pos = key.indexOf(LEND);
            if (pos <= 0) {
                return null;
            } else {
                return key.substring(pos + 2);
            }
        }

        static boolean isIParamItem(String prefix, String key) {
            if (!key.startsWith(prefix)) {
                return false;
            }
            int start = prefix.length() + LSTART.length();
            int end = key.indexOf(LEND);
            if (end <= start) {
                return false;
            }
            String s = key.substring(start, end);
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        @Override
        public <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
