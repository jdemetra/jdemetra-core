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
package demetra.util;

import demetra.design.PrimitiveReplacementOf;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * A List of int's; as full an implementation of the java.util.List interface as
 * possible, with an eye toward minimal creation of objects
 *
 * the mimicry of List is as follows: <ul> <li> if possible, operations
 * designated 'optional' in the List interface are attempted <li> wherever the
 * List interface refers to an Object, substitute int <li> wherever the List
 * interface refers to a Collection or List, substitute IntList </ul>
 *
 * the mimicry is not perfect, however: <ul> <li> operations involving Iterators
 * or ListIterators are not supported <li> remove(Object) becomes removeValue to
 * distinguish it from remove(int index) <li> subList is not supported </ul>
 *
 * @author Marc Johnson
 */
@PrimitiveReplacementOf(generic = List.class, primitive = int.class)
public final class IntList {

    private static final int DEFAULT_SIZE = 128;
    private int[] array;
    private int limit;
    private int fillval = 0;

    /**
     * create an IntList of default size
     */
    public IntList() {
        this(DEFAULT_SIZE);
    }

    public IntList(final int initialCapacity) {
        this(initialCapacity, 0);
    }

    /**
     * create a copy of an existing IntList
     *
     * @param list the existing IntList
     */
    public IntList(final IntList list) {
        this(list.array.length);
        System.arraycopy(list.array, 0, array, 0, array.length);
        limit = list.limit;
    }

    /**
     * create an IntList with a predefined initial size
     *
     * @param initialCapacity the size for the internal array
     * @param fillvalue
     */
    public IntList(final int initialCapacity, int fillvalue) {
        array = new int[initialCapacity];
        if (fillval != 0) {
            fillval = fillvalue;
            Arrays.fill(array, fillval);
        }
        limit = 0;
    }

    /**
     * add the specfied value at the specified index
     *
     * @param index the index where the new value is to be added
     * @param value the new value
     *
     * @exception IndexOutOfBoundsException if the index is out of range (index
     * < 0 || index > size()).
     */
    public void add(final int index, final int value) {
        if (index > limit) {
            throw new IndexOutOfBoundsException();
        } else if (index == limit) {
            add(value);
        } else {

            // index < limit -- insert into the middle
            if (limit == array.length) {
                growArray(limit * 2);
            }
            System.arraycopy(array, index, array, index + 1,
                    limit - index);
            array[ index] = value;
            limit++;
        }
    }

    /**
     * Appends the specified element to the end of this list
     *
     * @param value element to be appended to this list.
     *
     * @return true (as per the general contract of the Collection.add method).
     */
    public boolean add(final int value) {
        if (limit == array.length) {
            growArray(limit * 2);
        }
        array[ limit++] = value;
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
    public boolean addAll(final IntList c) {
        if (c.limit != 0) {
            if ((limit + c.limit) > array.length) {
                growArray(limit + c.limit);
            }
            System.arraycopy(c.array, 0, array, limit, c.limit);
            limit += c.limit;
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
    public boolean addAll(final int index, final IntList c) {
        if (index > limit) {
            throw new IndexOutOfBoundsException();
        }
        if (c.limit != 0) {
            if ((limit + c.limit) > array.length) {
                growArray(limit + c.limit);
            }

            // make a hole
            System.arraycopy(array, index, array, index + c.limit,
                    limit - index);

            // fill it in
            System.arraycopy(c.array, 0, array, index, c.limit);
            limit += c.limit;
        }
        return true;
    }

    /**
     * Removes all of the elements from this list. This list will be empty after
     * this call returns (unless it throws an exception).
     */
    public void clear() {
        limit = 0;
    }

    /**
     * Returns true if this list contains the specified element. More formally,
     * returns true if and only if this list contains at least one element e
     * such that o == e
     *
     * @param o element whose presence in this list is to be tested.
     *
     * @return true if this list contains the specified element.
     */
    public boolean contains(final int o) {
        boolean rval = false;

        for (int j = 0; !rval && (j < limit); j++) {
            if (array[ j] == o) {
                rval = true;
            }
        }
        return rval;
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
    public boolean containsAll(final IntList c) {
        boolean rval = true;

        if (this != c) {
            for (int j = 0; rval && (j < c.limit); j++) {
                if (!contains(c.array[ j])) {
                    rval = false;
                }
            }
        }
        return rval;
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
            IntList other = (IntList) o;

            if (other.limit == limit) {

                // assume match
                rval = true;
                for (int j = 0; rval && (j < limit); j++) {
                    rval = array[ j] == other.array[ j];
                }
            }
        }
        return rval;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     *
     * @return the element at the specified position in this list.
     *
     * @exception IndexOutOfBoundsException if the index is out of range (index
     * < 0 || index >= size()).
     */
    public int get(final int index) {
        if (index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return array[ index];
    }

    /**
     * Returns the hash code value for this list. The hash code of a list is
     * defined to be the result of the following calculation:
     *
     * <code>
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
        int hash = 0;

        for (int j = 0; j < limit; j++) {
            hash = (31 * hash) + array[ j];
        }
        return hash;
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element. More formally,
     * returns the lowest index i such that (o == get(i)), or -1 if there is no
     * such index.
     *
     * @param o element to search for.
     *
     * @return the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int indexOf(final int o) {
        int rval = 0;

        for (; rval < limit; rval++) {
            if (o == array[ rval]) {
                break;
            }
        }
        if (rval == limit) {
            rval = -1;   // didn't find it
        }
        return rval;
    }

    /**
     * Returns true if this list contains no elements.
     *
     * @return true if this list contains no elements.
     */
    public boolean isEmpty() {
        return limit == 0;
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element. More formally,
     * returns the highest index i such that (o == get(i)), or -1 if there is no
     * such index.
     *
     * @param o element to search for.
     *
     * @return the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(final int o) {
        int rval = limit - 1;

        for (; rval >= 0; rval--) {
            if (o == array[ rval]) {
                break;
            }
        }
        return rval;
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
    public int remove(final int index) {
        if (index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        int rval = array[ index];

        System.arraycopy(array, index + 1, array, index, limit - index);
        limit--;
        return rval;
    }

    /**
     * Removes the first occurrence in this list of the specified element
     * (optional operation). If this list does not contain the element, it is
     * unchanged. More formally, removes the element with the lowest index i
     * such that (value.equals(get(i))) (if such an element exists).
     *
     * @param value element to be removed from this list, if present.
     *
     * @return true if this list contained the specified element.
     */
    public boolean removeValue(final int value) {
        for (int j = 0; j < limit; j++) {
            if (value == array[j]) {
                int nextIndex = j + 1;
                if (nextIndex < limit) {
                    System.arraycopy(array, nextIndex, array, j, limit - nextIndex);
                }
                limit--;
                return true;
            }
        }
        return false;
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
    public boolean removeAll(final IntList c) {
        boolean rval = false;

        for (int j = 0; j < c.limit; j++) {
            if (removeValue(c.array[ j])) {
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
    public boolean retainAll(final IntList c) {
        boolean rval = false;

        for (int j = 0; j < limit;) {
            if (!c.contains(array[ j])) {
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
     * @return the element previously at the specified position.
     *
     * @exception IndexOutOfBoundsException if the index is out of range (index
     * < 0 || index >= size()).
     */
    public int set(final int index, final int element) {
        if (index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        int rval = array[ index];

        array[ index] = element;
        return rval;
    }

    /**
     * Returns the number of elements in this list. If this list contains more
     * than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     *
     * @return the number of elements in this IntList
     */
    public int size() {
        return limit;
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence. Obeys the general contract of the Collection.toArray method.
     *
     * @return an array containing all of the elements in this list in proper
     * sequence.
     */
    public int[] toArray() {
        int[] rval = new int[limit];

        System.arraycopy(array, 0, rval, 0, limit);
        return rval;
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
    public int[] toArray(final int[] a) {
        int[] rval;

        if (a.length == limit) {
            System.arraycopy(array, 0, a, 0, limit);
            rval = a;
        } else {
            rval = toArray();
        }
        return rval;
    }

    private void growArray(final int new_size) {
        int size = (new_size == array.length) ? new_size + 1
                : new_size;
        int[] new_array = new int[size];

        if (fillval != 0) {
            Arrays.fill(new_array, array.length, new_array.length, fillval);
        }

        System.arraycopy(array, 0, new_array, 0, limit);
        array = new_array;
    }
    
    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * @return a sequential {@code Stream} over the elements in this collection
     * @see java.util.Collection#stream()
     * @since 2.2.0
     */
    public IntStream stream() {
        return Arrays.stream(array, 0, limit);
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
    public void replaceAll(IntUnaryOperator operator) {
        java.util.Objects.requireNonNull(operator);
        for (int i = 0; i < limit; i++) {
            array[i] = operator.applyAsInt(array[i]);
        }
    }

    /**
     * Sorts this list.
     *
     * @see java.util.List#sort(java.util.Comparator)
     * @since 2.2.0
     */
    public void sort() {
        Arrays.sort(array, 0, limit);
    }

    /**
     * Creates a {@link Spliterator} over the elements in this list.
     *
     * @return a {@code Spliterator} over the elements in this list
     * @see java.util.List#spliterator()
     * @since 2.2.0
     */
    public Spliterator.OfInt spliterator() {
        return Arrays.spliterator(array, 0, limit);
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
    public boolean removeIf(IntPredicate filter) {
        java.util.Objects.requireNonNull(filter);
        boolean removed = false;
        int i = 0;
        while (i < limit) {
            if (filter.test(array[i])) {
                remove(i);
                removed = true;
            } else {
                i++;
            }
        }
        return removed;
    }

    /**
     * Performs the given action for each element of the {@code List} until
     * all elements have been processed or the action throws an exception.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @see java.lang.Iterable#forEach(java.util.function.Consumer)
     * @since 2.2.0
     */
    public void forEach(IntConsumer action) {
        java.util.Objects.requireNonNull(action);
        for (int i = 0; i < limit; i++) {
            action.accept(array[i]);
        }
    }
}   // end public class IntList

