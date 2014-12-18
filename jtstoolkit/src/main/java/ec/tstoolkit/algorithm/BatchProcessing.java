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
package ec.tstoolkit.algorithm;

import ec.tstoolkit.information.InformationSet;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 *
 * @author Jean Palate
 */
public class BatchProcessing implements IProcessing<InformationSet, CompositeResults>, Cloneable {

    /**
     *
     * @param <I> General input of the batch
     * @param <S> Input of the node
     */
    public static interface ILink<S> {

        S createInput(InformationSet input, CompositeResults context);
    }

    public static class Link<S> implements ILink<S> {

        private final String id;
        private final Class<S> sclass;

        public Link(String id, Class<S> sclass) {
            this.id = id;
            this.sclass = sclass;
        }

        @Override
        public S createInput(InformationSet input, CompositeResults context) {
            return context.getData(id, sclass);
        }
    }

    public static class Links<S> implements ILink<S[]> {

        private final String[] id;
        private final Class<S> sclass;

        public Links(String[] id, Class<S> sclass) {
            this.id = id;
            this.sclass = sclass;
        }

        @Override
        public S[] createInput(InformationSet input, CompositeResults context) {
            S[] rslt = (S[]) Array.newInstance(sclass,id.length);
            for (int i = 0; i < rslt.length; ++i) {
                rslt[i] = context.getData(id[i], sclass);
            }
            return rslt;
        }
    }

    public static class OutputLink<R extends IProcResults> implements ILink<R> {

        private final String id;
        private final Class<R> rclass;

        public OutputLink(String id, Class<R> rclass) {
            this.id = id;
            this.rclass = rclass;
        }

        @Override
        public R createInput(InformationSet input, CompositeResults context) {
            return context.get(id, rclass);
        }
    }

    public static class InputLink<S> implements ILink<S> {

        private final String id;
        private final Class<S> sclass;

        public InputLink(String id, Class<S> sclass) {
            this.id = id;
            this.sclass = sclass;
        }

        @Override
        public S createInput(InformationSet input, CompositeResults context) {
            return input.search(id, sclass);
        }
    }

//     public static class InputLinks<S> implements ILink<S[]> {
//
//        private final String id;
//        private final Class<S> sclass;
//
//        public InputLinks(String id, Class<S> sclass) {
//            this.id = id;
//            this.sclass = sclass;
//        }
//
//        @Override
//        public S[] createInput(InformationSet input, CompositeResults context) {
//            input.select(id);
//            S[] rslt = Array.newInstance(sclass,id.length);
//            for (int i = 0; i < rslt.length; ++i) {
//                rslt[i] = context.getData(id[i], sclass);
//            }
//            return rslt;
//        }
//    }

   /**
     *
     * @param <S> Input of the processing
     * @param <R> Output of the processing
     */
    public static class Node<S, R extends IProcResults> {

        public final String name;
        public final ILink<S> link;
        public final IProcessing<S, R> processing;

        public Node(String name, ILink<S> link, IProcessing<S, R> processing) {
            this.name = name;
            this.link = link;
            this.processing = processing;
        }

        public boolean process(InformationSet input, CompositeResults context) {
            S sinput = link.createInput(input, context);
            R r = processing.process(sinput);
            if (r == null) {
                return false;
            } else {
                context.put(name, r, name);
                return true;
            }
        }
    }
    private ArrayList<Node<?, ?>> nodes_ = new ArrayList<>();

    @Override
    public BatchProcessing clone() {
        try {
            BatchProcessing p = (BatchProcessing) super.clone();
            return p;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public void add(Node<?, ?> node) {
        nodes_.add(node);
    }

    @Override
    public CompositeResults process(InformationSet input) {
        CompositeResults results = new CompositeResults();
        for (Node<?, ?> cur : nodes_) {
            cur.process(input, results);
        }
        return results;
    }
}
