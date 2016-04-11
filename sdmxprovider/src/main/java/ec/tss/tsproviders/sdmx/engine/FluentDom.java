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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
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

    @Nonnull
    public static Stream<Node> asStream(@Nonnull NodeList list) {
        return asList(list).stream();
    }

    @Nonnull
    public static Stream<Node> asStream(@Nonnull NamedNodeMap map) {
        return asList(map).stream();
    }

    @Deprecated
    public static FluentIterable<Node> elementsByTagName(Document doc, String tagname) {
        return FluentIterable.from(asList(doc.getElementsByTagName(tagname)));
    }

    @Deprecated
    public static FluentIterable<Node> childNodes(Node node) {
        return FluentIterable.from(asList(node.getChildNodes()));
    }

    @Deprecated
    public static FluentIterable<Node> attributes(Node node) {
        return FluentIterable.from(asList(node.getAttributes()));
    }

    @Deprecated
    public static Predicate<Node> nodeNameEndsWith(final String suffix) {
        return o -> o.getNodeName().endsWith(suffix);
    }

    @Deprecated
    public static Predicate<Node> nodeNameEqualTo(String nodeName) {
        return nodeName != null
                ? o -> nodeName.equals(o.getNodeName())
                : o -> o == null;
    }

    @Deprecated
    public static Predicate<Node> localNameEqualTo(String localName) {
        return localName != null
                ? o -> localName.equals(o.getLocalName())
                : o -> o == null;
    }

    @Deprecated
    public static Function<Node, NamedNodeMap> toAttributes() {
        return Node::getAttributes;
    }

    @Deprecated
    public static Function<Node, String> toNodeName() {
        return Node::getNodeName;
    }

    @Deprecated
    public static Function<Node, String> toLocalName() {
        return Node::getLocalName;
    }

    @Deprecated
    public static Function<Node, Map.Entry<String, String>> toMapEntry() {
        return o -> Maps.immutableEntry(o.getNodeName(), o.getNodeValue());
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static List<Node> asList(NodeList list) {
        return new AbstractList<Node>() {
            @Override
            public Node get(int index) {
                return list.item(index);
            }

            @Override
            public int size() {
                return list.getLength();
            }
        };
    }

    private static List<Node> asList(NamedNodeMap map) {
        return new AbstractList<Node>() {
            @Override
            public Node get(int index) {
                return map.item(index);
            }

            @Override
            public int size() {
                return map.getLength();
            }
        };
    }
    //</editor-fold>
}
