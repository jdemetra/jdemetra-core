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
package demetra.data;

import demetra.design.Development;
import demetra.design.PrimitiveReplacementOf;
import demetra.util.IntList;
import internal.data.InternalDoubleSeq;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

/**
 * Same as {@link IntList} but for doubles.
 *
 * @author Philippe Charles
 */
@Development(status = Development.Status.Release)
@PrimitiveReplacementOf(generic = List.class, primitive = double.class)
public final class DoubleList implements DoubleVector {

    private static final int DEFAULT_SIZE = 128;

    private double[] values;
    private int length;

    /**
     * create an DoubleList of default size
     */
    public DoubleList() {
        this(DEFAULT_SIZE);
    }

    /**
     * create an DoubleList with a predefined initial size
     *
     * @param initialCapacity the size for the internal array
     */
    public DoubleList(int initialCapacity) {
        values = new double[initialCapacity];
        length = 0;
    }

    /**
     * create a copy of an existing DoubleSeq
     *
     * @param list the existing DoubleSeq
     */
    public DoubleList(DoubleSeq list) {
        values = list.toArray();
        length = values.length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public double get(int index) {
        if (index >= length) {
            throw new IndexOutOfBoundsException();
        }
        return values[index];
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public double[] toArray() {
        double[] result = new double[length];
        System.arraycopy(values, 0, result, 0, length);
        return result;
    }

    @Override
    public void copyTo(double[] buffer, int offset) {
        System.arraycopy(values, 0, buffer, offset, length);
    }

    @Override
    public DoubleStream stream() {
        return Arrays.stream(values, 0, length);
    }

    @Override
    public DoubleVectorCursor cursor() {
        return new Cell();
    }

    @Override
    public DoubleSeq extract(int start, int elength) {
        return new InternalDoubleSeq.SubDoubleSeq(values, start, elength);
    }

    @Override
    public String toString() {
        return DoubleSeq.format(this);
    }

    /**
     * add the specified value at the specified index
     *
     * @param index the index where the new value is to be added
     * @param value the new value
     *
     * @exception IndexOutOfBoundsException if the index is out of range (index
     * < 0 || index > size()).
     */
    public void add(int index, double value) {
        if (index > length) {
            throw new IndexOutOfBoundsException();
        } else if (index == length) {
            add(value);
        } else {

            // index < limit -- insert into the middle
            if (length == values.length) {
                growArray(length * 2);
            }
            System.arraycopy(values, index, values, index + 1, length - index);
            values[index] = value;
            length++;
        }
    }

    /**
     * Appends the specified element to the end of this list
     *
     * @param value element to be appended to this list.
     *
     * @return true (as per the general contract of the Collection.add method).
     */
    public boolean add(double value) {
        if (length == values.length) {
            growArray(length * 2);
        }
        values[length++] = value;
        return true;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator. The behavior of this operation is unspecified if
     * the specified collection is modified while the operation is in progress.
     * (Note that this will occur if the specified collection is this list, and
     * it's nonempty.)
     *
     * @param c collection whose elements are to be added to this list.
     *
     * @return true if this list changed as a result of the call.
     */
    public boolean addAll(DoubleSeq c) {
        if (!c.isEmpty()) {
            if ((length + c.length()) > values.length) {
                growArray(length + c.length());
            }
            c.copyTo(values, length);
            length += c.length();
        }
        return true;
    }

    /**
     * Inserts all of the elements in the specified collection into this list at
     * the specified position. Shifts the element currently at that position (if
     * any) and any subsequent elements to the right (increases their indices).
     * The new elements will appear in this list in the order that they are
     * returned by the specified collection's iterator. The behavior of this
     * operation is unspecified if the specified collection is modified while
     * the operation is in progress. (Note that this will occur if the specified
     * collection is this list, and it's nonempty.)
     *
     * @param index index at which to insert first element from the specified
     * collection.
     * @param c elements to be inserted into this list.
     *
     * @return true if this list changed as a result of the call.
     *
     * @exception IndexOutOfBoundsException if the index is out of range (index
     * < 0 || index > size())
     */
    public boolean addAll(int index, DoubleSeq c) {
        if (index > length) {
            throw new IndexOutOfBoundsException();
        }
        if (c.length() != 0) {
            if ((length + c.length()) > values.length) {
                growArray(length + c.length());
            }

            // make a hole
            System.arraycopy(values, index, values, index + c.length(), length - index);

            // fill it in
            c.copyTo(values, index);
            length += c.length();
        }
        return true;
    }

    /**
     * Removes all of the elements from this list. This list will be empty after
     * this call returns (unless it throws an exception).
     */
    public void clear() {
        length = 0;
    }

    /**
     * Returns true if this list contains the specified element. More formally,
     * returns true if and only if this list contains at least one element e
     * such that o == e
     *
     * @param value element whose presence in this list is to be tested.
     *
     * @return true if this list contains the specified element.
     */
    public boolean contains(double value) {
        return anyMatch(o -> o == value);
    }

    /**
     * Returns true if this list contains all of the elements of the specified
     * collection.
     *
     * @param c collection to be checked for containment in this list.
     *
     * @return true if this list contains all of the elements of the specified
     * collection.
     */
    public boolean containsAll(DoubleSeq c) {
        return this != c ? allMatch(o -> contains(o)) : true;
    }

    /**
     * Compares the specified object with this list for equality. Returns true
     * if and only if the specified object is also a list, both lists have the
     * same size, and all corresponding pairs of elements in the two lists are
     * equal. (Two elements e1 and e2 are equal if e1 == e2.) In other words,
     * two lists are defined to be equal if they contain the same elements in
     * the same order. This definition ensures that the equals method works
     * properly across different implementations of the List interface.
     *
     * @param o the object to be compared for equality with this list.
     *
     * @return true if the specified object is equal to this list.
     */
    @Override
    public boolean equals(final Object o) {
        boolean rval = this == o;

        if (!rval && (o != null) && (o.getClass() == this.getClass())) {
            DoubleList other = (DoubleList) o;

            if (other.length == length) {

                // assume match
                rval = true;
                for (int j = 0; rval && (j < length); j++) {
                    rval = values[j] == other.values[j];
                }
            }
        }
        return rval;
    }

    /**
     * Returns the hash code value for this list. The hash code of a list is
     * defined to be the result of the following calculation:
     *
     * <code>
     *
     * hashCode = 1;
     * Iterator i = list.iterator();
     * while (i.hasNext()) {
     *      Object obj = i.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     * }
     * </code>
     *
     * This ensures that list1.equals(list2) implies that
     * list1.hashCode()==list2.hashCode() for any two lists, list1 and list2, as
     * required by the general contract of Object.hashCode.
     *
     * @return the hash code value for this list.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element. More formally,
     * returns the lowest index i such that (o == get(i)), or -1 if there is no
     * such index.
     *
     * @param value element to search for.
     *
     * @return the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int indexOf(double value) {
        return indexOf(o -> o == value);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element. More formally,
     * returns the highest index i such that (o == get(i)), or -1 if there is no
     * such index.
     *
     * @param value element to search for.
     *
     * @return the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(double value) {
        return lastIndexOf(o -> o == value);
    }

    /**
     * Removes the element at the specified position in this list. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param index the index of the element to removed.
     *
     * @return the element previously at the specified position.
     *
     * @exception IndexOutOfBoundsException if the index is out of range (index
     * < 0 || index >= size()).
     */
    public double remove(final int index) {
        if (index >= length) {
            throw new IndexOutOfBoundsException();
        }
        double rval = values[index];

        System.arraycopy(values, index + 1, values, index, length - index);
        length--;
        return rval;
    }

    /**
     * Removes the first occurrence in this list of the specified element
     * (optional operation). If this list does not contain the element, it is
     * unchanged. More formally, removes the element with the lowest index i
     * such that (o.equals(get(i))) (if such an element exists).
     *
     * @param o element to be removed from this list, if present.
     *
     * @return true if this list contained the specified element.
     */
    public boolean removeValue(final double o) {
        boolean rval = false;

        for (int j = 0; !rval && (j < length); j++) {
            if (o == values[j]) {
                if (j + 1 < length) {
                    System.arraycopy(values, j + 1, values, j, length - j);
                }
                length--;
                rval = true;
            }
        }
        return rval;
    }

    /**
     * Removes from this list all the elements that are contained in the
     * specified collection
     *
     * @param c collection that defines which elements will be removed from this
     * list.
     *
     * @return true if this list changed as a result of the call.
     */
    public boolean removeAll(DoubleSeq c) {
        boolean rval = false;

        for (int j = 0; j < c.length(); j++) {
            if (removeValue(c.get(j))) {
                rval = true;
            }
        }
        return rval;
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection. In other words, removes from this list all the
     * elements that are not contained in the specified collection.
     *
     * @param c collection that defines which elements this set will retain.
     *
     * @return true if this list changed as a result of the call.
     */
    public boolean retainAll(final DoubleList c) {
        boolean rval = false;

        for (int j = 0; j < length;) {
            if (!c.contains(values[j])) {
                remove(j);
                rval = true;
            } else {
                j++;
            }
        }
        return rval;
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     *
     * @exception IndexOutOfBoundsException if the index is out of range (index
     * < 0 || index >= size()).
     */
    @Override
    public void set(int index, double element) {
        if (index >= length) {
            throw new IndexOutOfBoundsException();
        }
        values[index] = element;
    }

    /**
     * Returns the number of elements in this list. If this list contains more
     * than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     *
     * @return the number of elements in this DoubleList
     */
    public int size() {
        return length;
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence. Obeys the general contract of the Collection.toArray(Object[])
     * method.
     *
     * @param a the array into which the elements of this list are to be stored,
     * if it is big enough; otherwise, a new array is allocated for this
     * purpose.
     *
     * @return an array containing the elements of this list.
     */
    public double[] toArray(double[] a) {
        if (a.length == length) {
            System.arraycopy(values, 0, a, 0, length);
            return a;
        } else {
            return toArray();
        }
    }

    /**
     * Replaces each element of this list with the result of applying the
     * operator to that element. Errors or runtime exceptions thrown by the
     * operator are relayed to the caller.
     *
     * @param operator the operator to apply to each element
     * @throws NullPointerException if the specified operator is null
     * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
     * @since 2.2.0
     */
    public void replaceAll(DoubleUnaryOperator operator) {
        java.util.Objects.requireNonNull(operator);
        for (int i = 0; i < length; i++) {
            values[i] = operator.applyAsDouble(values[i]);
        }
    }

    /**
     * Sorts this list.
     *
     * @see java.util.List#sort(java.util.Comparator)
     * @since 2.2.0
     */
    public void sort() {
        Arrays.sort(values, 0, length);
    }

    /**
     * Removes all of the elements of this collection that satisfy the given
     * predicate. Errors or runtime exceptions thrown during iteration or by the
     * predicate are relayed to the caller.
     *
     * @param filter a predicate which returns {@code true} for elements to be
     * removed
     * @return {@code true} if any elements were removed
     * @throws NullPointerException if the specified filter is null
     * @see java.util.Collection#removeIf(java.util.function.Predicate)
     * @since 2.2.0
     */
    public boolean removeIf(DoublePredicate filter) {
        java.util.Objects.requireNonNull(filter);
        boolean removed = false;
        int i = 0;
        while (i < length) {
            if (filter.test(values[i])) {
                remove(i);
                removed = true;
            } else {
                i++;
            }
        }
        return removed;
    }

    private void growArray(int newSize) {
        int size = (newSize == values.length) ? newSize + 1 : newSize;
        double[] newArray = new double[size];
        System.arraycopy(values, 0, newArray, 0, length);
        values = newArray;
    }

    private final class Cell implements DoubleVectorCursor {

        private int pos = 0;

        @Override
        public double getAndNext() {
            return values[pos++];
        }

        @Override
        public void skip(int n) {
            pos += n;
        }

        @Override
        public void moveTo(int npos) {
            pos = npos;
        }

        @Override
        public void setAndNext(double newValue) throws IndexOutOfBoundsException {
            values[pos++] = newValue;
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            values[pos] = fn.applyAsDouble(values[pos]);
            pos++;
        }
    }
}
