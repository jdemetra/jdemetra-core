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
package ec.tstoolkit.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TreesTest {

    private static final class Node {

        private final String name;
        private final List<Node> children;

        public Node(String name) {
            this.name = name;
            this.children = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        Stream<Node> childrenStream() {
            return children.stream();
        }
    }

    @Test
    public void testBreadthFirstIterable() {
        Node n1 = new Node("1");
        Node n2 = new Node("2");
        n1.children.add(n2);
        Node n3 = new Node("3");
        n1.children.add(n3);
        Node n4 = new Node("4");
        n1.children.add(n4);
        n2.children.add(new Node("5"));
        n2.children.add(new Node("6"));

        assertThat(Trees.breadthFirstStream(n1, Node::childrenStream).map(Node::getName))
                .containsExactly("1", "2", "3", "4", "5", "6");
    }

    @Test
    public void testDepthFirstIterable() {
        Node n1 = new Node("1");
        Node n2 = new Node("2");
        n1.children.add(n2);
        Node n3 = new Node("5");
        n1.children.add(n3);
        Node n4 = new Node("6");
        n1.children.add(n4);
        n2.children.add(new Node("3"));
        n2.children.add(new Node("4"));

        assertThat(Trees.depthFirstStream(n1, Node::childrenStream).map(Node::getName))
                .containsExactly("1", "2", "3", "4", "5", "6");
    }
}
