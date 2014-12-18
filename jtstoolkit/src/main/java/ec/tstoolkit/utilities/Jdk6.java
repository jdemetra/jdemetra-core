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

import ec.tstoolkit.dstats.T;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Helper class to simplify code. Should be rendered useless by Jdk7.
 *
 * @author Philippe Charles
 */
public final class Jdk6 {

    private Jdk6() {
        // static class
    }

    /**
     * Creates an empty {@link ArrayList} with an initial capacity of ten.
     *
     * @param <T>
     * @return
     */
    @Deprecated
    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * Creates an {@link ArrayList} that contains the specified elements. The
     * resulting list will have a capacity greater than the initial elements and
     * will be variable-sized.<br> For fixed-sized list, use {@link Arrays#asList(T[])
     * } instead.
     *
     * @param <T>
     * @param elements
     * @return
     */
    public static <T> ArrayList<T> newArrayList(Iterable<? extends T> elements) {
        if (elements instanceof Collection) {
            //return new ArrayList<T>((Collection<T>) elements);
            Collection<? extends T> tmp = (Collection<? extends T>) elements;
            ArrayList<T> result = new ArrayList<>(tmp.size());
            result.addAll(tmp);
            return result;
        }
        ArrayList<T> result = new ArrayList<>();
        for (T t : elements) {
            result.add(t);
        }
        return result;
    }

    @Deprecated
    public static <T> ArrayList<T> newArrayListWithInitialCapacity(int initialCapacity) {
        return new ArrayList<>(initialCapacity);
    }

    @Deprecated
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static final class Collections {

        private Collections() {
            // static class
        }

        /**
         * Shortcut for
         * <code>collection.toArray(new T[collection.size()])</code>
         *
         * @param <T>
         * @param list
         * @param type
         * @return
         */
        public static <T> T[] toArray(Collection<? extends T> list, Class<T> type) {
            return list.toArray((T[]) Array.newInstance(type, list.size()));
        }

        public static boolean isNullOrEmpty(Collection<?> c) {
            return c == null || c.isEmpty();
        }

        public static boolean hasEmptyElements(Collection<?> c) {
            for (Object obj : c) {
                if (obj == null) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class Double {

        private Double() {
            // static class
        }

        /**
         * Returns a hash code for {@code value}; same as
         * {@code ((Double) value).hashCode()}.
         *
         * @param value a {@code double} value
         * @return a hash code for the value
         */
        public static int hashCode(double value) {
            long bits = java.lang.Double.doubleToLongBits(value);
            return (int) (bits ^ (bits >>> 32));
        }
    }

    public static final class Integer {

        private Integer() {
            // static class
        }

        /**
         * Compares two int values numerically.
         *
         * @param x the first int to compare
         * @param y the second int to compare
         * @return the value 0 if <code>x == y</code>; a value less than 0 if
         * <code>x < y</code>; and a value greater than 0 if <code>x > y</code>
         *
         */
        @NextJdk("http://docs.oracle.com/javase/7/docs/api/java/lang/Integer.html#compare(int, int)")
        @Deprecated
        public static int compare(int x, int y) {
            return (x < y ? -1 : (x == y ? 0 : 1));
        }
    }

    public static final class Long {

        private Long() {
            // static class
        }

        /**
         * Compares two long values numerically.
         *
         * @param x the first long to compare
         * @param y the second long to compare
         * @return the value 0 if <code>x == y</code>; a value less than 0 if
         * <code>x < y</code>; and a value greater than 0 if <code>x > y</code>
         *
         */
        @NextJdk("http://docs.oracle.com/javase/7/docs/api/java/lang/Long.html#compare(long, long)")
        @Deprecated
        public static int compare(long x, long y) {
            return (x < y ? -1 : (x == y ? 0 : 1));
        }
    }
}
