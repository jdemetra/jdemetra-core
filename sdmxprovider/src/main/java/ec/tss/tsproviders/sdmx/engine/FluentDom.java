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
package ec.tss.tsproviders.sdmx.engine;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import java.util.AbstractList;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Philippe Charles
 */
public final class FluentDom {

    private FluentDom() {
        // static class
    }

    public static FluentIterable<Node> elementsByTagName(Document doc, String tagname) {
        return asIterable(doc.getElementsByTagName(tagname));
    }

    public static FluentIterable<Node> childNodes(Node node) {
        return asIterable(node.getChildNodes());
    }

    public static FluentIterable<Node> attributes(Node node) {
        return asIterable(node.getAttributes());
    }

    public static Predicate<Node> nodeNameEndsWith(final String suffix) {
        return new Predicate<Node>() {
            @Override
            public boolean apply(Node input) {
                String name = input.getNodeName();
                return name.endsWith(suffix);
            }
        };
    }

    public static Function<Node, NamedNodeMap> toAttributes() {
        return TO_ATTRIBUTES;
    }

    public static Function<Node, String> toNodeName() {
        return TO_NODE_NAME;
    }

    public static Function<Node, Map.Entry<String, String>> toMapEntry() {
        return TO_MAP_ENTRY;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static FluentIterable<Node> asIterable(final NodeList list) {
        return FluentIterable.from(new AbstractList<Node>() {
            @Override
            public Node get(int index) {
                return list.item(index);
            }

            @Override
            public int size() {
                return list.getLength();
            }
        });
    }

    private static FluentIterable<Node> asIterable(final NamedNodeMap map) {
        return FluentIterable.from(new AbstractList<Node>() {
            @Override
            public Node get(int index) {
                return map.item(index);
            }

            @Override
            public int size() {
                return map.getLength();
            }
        });
    }

    private static final Function<Node, NamedNodeMap> TO_ATTRIBUTES = new Function<Node, NamedNodeMap>() {
        @Override
        public NamedNodeMap apply(Node input) {
            return input.getAttributes();
        }
    };

    private static final Function<Node, String> TO_NODE_NAME = new Function<Node, String>() {
        @Override
        public String apply(Node input) {
            return input.getNodeName();
        }
    };

    private static final Function<Node, Map.Entry<String, String>> TO_MAP_ENTRY = new Function<Node, Map.Entry<String, String>>() {
        @Override
        public Map.Entry<String, String> apply(Node o) {
            return Maps.immutableEntry(o.getNodeName(), o.getNodeValue());
        }
    };
    //</editor-fold>
}
