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
package demetra.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 * @param <T>
 * @since 2.2.0
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TreeTraverser<T> {

    @Nonnull
    public static <Z> TreeTraverser<Z> of(
            @Nonnull Z root,
            @Nonnull Function<? super Z, ? extends Iterable<? extends Z>> children) {
        return new TreeTraverser(root, children);
    }

    @lombok.NonNull
    private final T root;

    @lombok.NonNull
    private final Function<? super T, ? extends Iterable<? extends T>> children;

    /**
     * https://en.wikipedia.org/wiki/Breadth-first_search
     *
     * @return
     */
    @Nonnull
    public Iterable<T> breadthFirstIterable() {
        return () -> new BreadthFirstIterator(root, children);
    }

    @Nonnull
    public Stream<T> breadthFirstStream() {
        return StreamSupport.stream(breadthFirstIterable().spliterator(), false);
    }

    /**
     * https://en.wikipedia.org/wiki/Depth-first_search
     *
     * @return
     */
    @Nonnull
    public Iterable<T> depthFirstIterable() {
        return () -> new DepthFirstIterator(root, children);
    }

    @Nonnull
    public Stream<T> depthFirstStream() {
        return StreamSupport.stream(depthFirstIterable().spliterator(), false);
    }

    @Nonnull
    public String prettyPrintToString(
            @Nonnegative int maxLevel,
            @Nonnull Function<? super T, ? extends CharSequence> formatter) {
        StringBuilder result = new StringBuilder();
        try {
            prettyPrintTo(result, maxLevel, formatter);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result.toString();
    }

    @Nonnull
    public void prettyPrintTo(
            @Nonnull Appendable appendable,
            @Nonnegative int maxLevel,
            @Nonnull Function<? super T, ? extends CharSequence> formatter) throws IOException {
        appendable.append(formatter.apply(root)).append(System.lineSeparator());
        if (maxLevel > 0) {
            for (Iterator<? extends T> i = children.apply(root).iterator(); i.hasNext();) {
                prettyPrintTo(appendable, maxLevel - 1, formatter, i.next(), "", !i.hasNext());
            }
        }
    }

    private void prettyPrintTo(
            @Nonnull Appendable appendable,
            @Nonnegative int maxLevel,
            @Nonnull Function<? super T, ? extends CharSequence> formatter,
            @Nonnull T node,
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
        appendable.append(formatter.apply(node)).append(System.lineSeparator());
        if (maxLevel > 0) {
            for (Iterator<? extends T> i = children.apply(node).iterator(); i.hasNext();) {
                prettyPrintTo(appendable, maxLevel - 1, formatter, i.next(), prefix, !i.hasNext());
            }
        }
    }

    private static <T> LinkedList<T> newLinkedList(T item) {
        LinkedList<T> result = new LinkedList<>();
        result.add(item);
        return result;
    }

    private static final class BreadthFirstIterator<T> implements Iterator<T> {

        private final Deque<T> queue;
        private final Function<? super T, ? extends Iterable<? extends T>> children;

        private BreadthFirstIterator(T root, Function<? super T, ? extends Iterable<? extends T>> children) {
            this.queue = newLinkedList(root);
            this.children = children;
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public T next() {
            T result = queue.removeFirst();
            for (T o : children.apply(result)) {
                queue.addLast(o);
            }
            return result;
        }
    }

    private static final class DepthFirstIterator<T> implements Iterator<T> {

        private final Deque<Iterator<? extends T>> stack;
        private final Function<? super T, ? extends Iterable<? extends T>> children;

        private DepthFirstIterator(T root, Function<? super T, ? extends Iterable<? extends T>> children) {
            this.stack = newLinkedList(Collections.singleton(root).iterator());
            this.children = children;
        }

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
            Iterator<? extends T> tmp = children.apply(result).iterator();
            if (tmp.hasNext()) {
                stack.push(tmp);
            }
            return result;
        }
    }
}
