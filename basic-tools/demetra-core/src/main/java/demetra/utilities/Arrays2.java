/*
 * Copyright 2017 National Bank copyOf Belgium
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
package demetra.utilities;

import java.util.*;

/**
 *
 * @author Jean Palate, Philippe Charles
 */
public final class Arrays2 {

    /**
     * Copies a range of elements from an
     * <code>Array</code> starting at the first element and pastes them into
     * another
     * <code>Array</code> starting at the first element. The length is specified
     * as a 32-bit integer.
     *
     * <a href="http://msdn2.microsoft.com/en-us/library/k4yx47a1(VS.80).aspx">
     * http://msdn2.microsoft.com/en-us/library/k4yx47a1(VS.80).aspx</a>
     *
     * @param sourceArray The <code>Array</code> that contains the data to copy.
     * @param destinationArray The <code>Array</code> that receives the data.
     * @param length A 32-bit integer that represents the number of elements to
     * copy.
     */
    public static void copy(final double[] sourceArray,
            final double[] destinationArray, final int length) {
        System.arraycopy(sourceArray, 0, destinationArray, 0, length);
    }

    /**
     * Reverses the sequence of the elements in the entire one-dimensional
     * <code>Array</code>. <p> <a
     * href="http://msdn2.microsoft.com/en-us/library/d3877932.aspx">http://
     * msdn2.microsoft.com/en-us/library/d3877932.aspx</a>
     *
     * @param array The one-dimensional <code>Array</code> to reverse.
     */
    public static void reverse(final double[] array) {
        reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of the given array.
     *
     * @param array
     * @param start
     * @param n
     */
    public static void reverse(final double[] array, final int start, final int n) {
        int i = start, j = start + n - 1;
        while (i < j) {
            swap(array, i++, j--);
        }
    }

    /**
     * Reverses the order of the given array.
     *
     * @param array
     */
    public static void reverse(final int[] array) {
        reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of the given array.
     *
     * @param array
     * @param start
     * @param n
     */
    public static void reverse(final int[] array, final int start, final int n) {
        int i = start, j = start + n - 1;
        while (i < j) {
            swap(array, i++, j--);
        }
    }

    /**
     * Reverses the order of the given array.
     *
     * @param array
     */
    public static void reverse(final Object[] array) {
        reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of the given array.
     *
     * @param array
     * @param start
     * @param n
     */
    public static void reverse(final Object[] array, final int start, final int n) {
        int i = start, j = start + n - 1;
        while (i < j) {
            swap(array, i++, j--);
        }
    }

    public static void swap(double array[], int a, int b) {
        double t = array[a];
        array[a] = array[b];
        array[b] = t;
    }

    public static void swap(int array[], int a, int b) {
        int t = array[a];
        array[a] = array[b];
        array[b] = t;
    }

    public static void swap(Object array[], int a, int b) {
        Object t = array[a];
        array[a] = array[b];
        array[b] = t;
    }

    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(double[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static <T> T[] copyOf(T[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static <T> T[] concat(T[] first, T[] second) {
        if (first == null)
            return second;
        else if (second == null)
            return first;
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Concatenate several arrays of doubles.
     *
     * @param arrays
     * @return
     */
    public static double[] concat(double[]... arrays) {
        int totalLength = 0;
        for (double[] o : arrays) {
            totalLength += o.length;
        }
        double[] result = new double[totalLength];
        int destPos = 0;
        for (double[] o : arrays) {
            System.arraycopy(o, 0, result, destPos, o.length);
            destPos += o.length;
        }
        return result;
    }
    
    /**
     * Remove missing values (NaN) from an existing array
     *
     * @param array The considered array
     * @return The same array is returned if it doesn't contain missing values
     */
    public static double[] compact(final double[] array) {
        int nm = 0;
        for (int i = 0; i < array.length; ++i) {
            if (Double.isNaN(array[i])) {
                ++nm;
            }
        }
        if (nm == 0) {
            return array;
        }
        double[] narray = new double[array.length - nm];
        for (int i = 0, j = 0; i < array.length; ++i) {
            if (!Double.isNaN(array[i])) {
                narray[j++] = array[i];
            }
        }
        return narray;
    }

    /**
     * Shifts the specified array of doubles.<br>Note that the array is
     * <u>not</u> shifted in a circular way. <p>Examples:<br>
     * <code>fshift({1,2,3,4}, 1) => {1,1,2,3}</code><br>
     * <code>fshift({1,2,3,4}, 2) => {1,2,1,2}</code><br>
     * <code>fshift({1,2,3,4}, -1) => {2,3,4,4}</code><br>
     * <code>fshift({1,2,3,4}, -2) => {3,4,3,4}</code><br>
     *
     * @param array the array to be shifted
     * @param shiftsize the shift size; <tt>size &gt 0</tt> runs a right shift
     * while <tt>size &lt 0</tt> runs a left shift
     */
    public static void shift(double[] array, int shiftsize) {
        shift(array, shiftsize, 0, array.length);
    }

    /**
     * Shifts the specified range of the specified array of doubles. The range
     * to be shifted extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.<br>Note that the array is <u>not</u> shifted
     * in a circular way.
     *
     * @param array the array to be shifted
     * @param shiftsize the shift size; <tt>size &gt 0</tt> runs a right shift
     * while <tt>size &lt 0</tt> runs a left shift
     * @param fromIndex the index of the first element (inclusive)
     * @param toIndex the index of the last element (exclusive)
     */
    public static void shift(double[] array, int shiftsize, int fromIndex, int toIndex) {
        if (shiftsize > 0) {
            System.arraycopy(array, fromIndex, array, fromIndex + shiftsize, toIndex - fromIndex - shiftsize);
        } else {
            System.arraycopy(array, fromIndex - shiftsize, array, fromIndex, toIndex - fromIndex + shiftsize);
        }
    }

    public static boolean isArray(Object o) {
        return o != null && o.getClass().isArray();
    }

    public static <T> boolean arrayEquals(T oldValue, T newValue) {
        if (isArray(oldValue) && isArray(newValue) && oldValue.getClass().getComponentType().equals(newValue.getClass().getComponentType())) {
            Class<?> type = oldValue.getClass().getComponentType();
            if (type.equals(long.class)) {
                return Arrays.equals((long[]) oldValue, (long[]) newValue);
            } else if (type.equals(int.class)) {
                return Arrays.equals((int[]) oldValue, (int[]) newValue);
            } else if (type.equals(short.class)) {
                return Arrays.equals((short[]) oldValue, (short[]) newValue);
            } else if (type.equals(char.class)) {
                return Arrays.equals((char[]) oldValue, (char[]) newValue);
            } else if (type.equals(byte.class)) {
                return Arrays.equals((byte[]) oldValue, (byte[]) newValue);
            } else if (type.equals(boolean.class)) {
                return Arrays.equals((boolean[]) oldValue, (boolean[]) newValue);
            } else if (type.equals(double.class)) {
                return Arrays.equals((double[]) oldValue, (double[]) newValue);
            } else if (type.equals(float.class)) {
                return Arrays.equals((float[]) oldValue, (float[]) newValue);
            } else {
                return Arrays.equals((Object[]) oldValue, (Object[]) newValue);
            }
        }
        return false;
    }
    //
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    /**
     * Similar to {@link Collections#unmodifiableList(java.util.List)} but for
     * arrays.
     *
     * @param <T>
     * @param array
     * @return
     */
    public static <T> List<T> unmodifiableList(T... array) {
        return new UnmodifiableList<>(array);
    }

    private static class UnmodifiableList<E> extends AbstractList<E>
            implements RandomAccess, java.io.Serializable {

        private static final long serialVersionUID = -2764017481108945198L;
        private final E[] a;

        UnmodifiableList(E[] array) {
            this.a = java.util.Objects.requireNonNull(array);
        }

        @Override
        public int size() {
            return a.length;
        }

        @Override
        public Object[] toArray() {
            return a.clone();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            int size = size();
            if (a.length < size) {
                return Arrays.copyOf(this.a, size,
                        (Class<? extends T[]>) a.getClass());
            }
            System.arraycopy(this.a, 0, a, 0, size);
            if (a.length > size) {
                a[size] = null;
            }
            return a;
        }

        @Override
        public E get(int index) {
            return a[index];
        }

        @Override
        public int indexOf(Object o) {
            if (o == null) {
                for (int i = 0; i < a.length; i++) {
                    if (a[i] == null) {
                        return i;
                    }
                }
            } else {
                for (int i = 0; i < a.length; i++) {
                    if (o.equals(a[i])) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }
    }

    private Arrays2() {
        // static class
    }
}
