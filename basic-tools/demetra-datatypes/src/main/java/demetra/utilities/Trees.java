/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.utilities;

import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class Trees {

    private Trees() {
        // static class
    }

    /**
     * https://en.wikipedia.org/wiki/Breadth-first_search
     *
     * @param <T>
     * @param root
     * @param children
     * @return
     */
    @Nonnull
    public static <T> Iterable<T> breadthFirstIterable(
            @Nonnull T root,
            @Nonnull Function<? super T, ? extends Stream<? extends T>> children) {
        return new BreadthFirstIterable(root, children);
    }

    @Nonnull
    public static <T> Stream<T> breadthFirstStream(
            @Nonnull T root,
            @Nonnull Function<? super T, ? extends Stream<? extends T>> children) {
        return StreamSupport.stream(breadthFirstIterable(root, children).spliterator(), false);
    }

    /**
     * https://en.wikipedia.org/wiki/Depth-first_search
     *
     * @param <T>
     * @param root
     * @param children
     * @return
     */
    @Nonnull
    public static <T> Iterable<T> depthFirstIterable(
            @Nonnull T root,
            @Nonnull Function<? super T, ? extends Stream<? extends T>> children) {
        return new DepthFirstIterable(root, children);
    }

    @Nonnull
    public static <T> Stream<T> depthFirstStream(
            @Nonnull T root,
            @Nonnull Function<? super T, ? extends Stream<? extends T>> children) {
        return StreamSupport.stream(depthFirstIterable(root, children).spliterator(), false);
    }

    @Nonnull
    public static <T> void prettyPrint(
            @Nonnull T root,
            @Nonnull Function<? super T, ? extends Stream<? extends T>> children,
            @Nonnegative int maxLevel,
            @Nonnull Function<? super T, ? extends CharSequence> toString,
            @Nonnull Appendable appendable) throws IOException {
        appendable.append(toString.apply(root)).append(System.lineSeparator());
        List<?> list = children.apply(root).collect(Collectors.toList());
        if (maxLevel > 0) {
            for (int i = 0; i < list.size(); i++) {
                prettyPrint((T) list.get(i), children, maxLevel - 1, appendable, toString, "", i == list.size() - 1);
            }
        }
    }

    @Nonnull
    public static <T> String prettyPrintToString(
            @Nonnull T root,
            @Nonnull Function<? super T, ? extends Stream<? extends T>> children,
            @Nonnegative int maxLevel,
            @Nonnull Function<? super T, ? extends CharSequence> toString) {
        StringBuilder result = new StringBuilder();
        try {
            prettyPrint(root, children, maxLevel, toString, result);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result.toString();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class BreadthFirstIterable<T> implements Iterable<T> {

        private final T root;
        private final Function<? super T, ? extends Stream<? extends T>> children;

        private BreadthFirstIterable(T root, Function<? super T, ? extends Stream<? extends T>> children) {
            this.root = root;
            this.children = children;
        }

        @Override
        public Iterator<T> iterator() {
            Deque<T> queue = new LinkedList<>();
            queue.add(root);
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return !queue.isEmpty();
                }

                @Override
                public T next() {
                    T result = queue.removeFirst();
                    children.apply(result).forEach(queue::add);
                    return result;
                }
            };
        }
    }

    private static final class DepthFirstIterable<T> implements Iterable<T> {

        private final T root;
        private final Function<? super T, ? extends Stream<? extends T>> toChildren;

        private DepthFirstIterable(T root, Function<? super T, ? extends Stream<? extends T>> toChildren) {
            this.root = root;
            this.toChildren = toChildren;
        }

        @Override
        public Iterator<T> iterator() {
            Stack<Iterator<? extends T>> stack = new Stack<>();
            stack.push(Collections.singleton(root).iterator());
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return !stack.isEmpty() && stack.peek().hasNext();
                }

                @Override
                public T next() {
                    Iterator<? extends T> top = stack.peek();
                    T result = top.next();
                    if (!top.hasNext()) {
                        stack.pop();
                    }
                    Iterator<? extends T> tmp = toChildren.apply(result).iterator();
                    if (tmp.hasNext()) {
                        stack.push(tmp);
                    }
                    return result;
                }
            };
        }
    }

    private static <T> void prettyPrint(
            @Nonnull T item,
            @Nonnull Function<? super T, ? extends Stream<? extends T>> children,
            @Nonnegative int maxLevel,
            @Nonnull Appendable appendable,
            @Nonnull Function<? super T, ? extends CharSequence> toString,
            @Nonnull String prefix,
            boolean last) throws IOException {
        appendable.append(prefix);
        if (last) {
            appendable.append("`-");
            prefix += "   ";
        } else {
            appendable.append("|-");
            prefix += "|  ";
        }
        appendable.append(toString.apply(item)).append(System.lineSeparator());
        if (maxLevel > 0) {
            List<?> list = children.apply(item).collect(Collectors.toList());
            for (int i = 0; i < list.size(); i++) {
                prettyPrint((T) list.get(i), children, maxLevel - 1, appendable, toString, prefix, i == list.size() - 1);
            }
        }
    }
    //</editor-fold>
}
