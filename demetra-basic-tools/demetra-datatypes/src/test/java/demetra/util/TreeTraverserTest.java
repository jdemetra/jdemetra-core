/*
 * Copyright 2018 National Bank of Belgium
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

import java.util.Collections;
import java.util.function.Function;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Philippe Charles
 */
public class TreeTraverserTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatNullPointerException().isThrownBy(() -> TreeTraverser.of(null, o -> Collections.emptyList()));
        assertThatNullPointerException().isThrownBy(() -> TreeTraverser.of("", null));
    }

    @Test
    public void testBreadthFirst() {
        TreeNode root = new DefaultMutableTreeNode(1) {
            {
                add(new DefaultMutableTreeNode(2) {
                    {
                        add(new DefaultMutableTreeNode(5) {
                            {
                                add(new DefaultMutableTreeNode(9));
                                add(new DefaultMutableTreeNode(10));
                            }
                        });
                        add(new DefaultMutableTreeNode(6));
                    }
                });
                add(new DefaultMutableTreeNode(3));
                add(new DefaultMutableTreeNode(4) {
                    {
                        add(new DefaultMutableTreeNode(7) {
                            {
                                add(new DefaultMutableTreeNode(11));
                                add(new DefaultMutableTreeNode(12));
                            }
                        });
                        add(new DefaultMutableTreeNode(8));
                    }
                });
            }
        };

        TreeTraverser<TreeNode> tree = TreeTraverser.of(root, TreeTraverserTest::children);

        assertThat(tree.breadthFirstIterable())
                .extracting("UserObject", Integer.class)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

        assertThat(tree.breadthFirstStream())
                .extracting("UserObject", Integer.class)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    }

    @Test
    public void testDepthFirst() {
        TreeNode root = new DefaultMutableTreeNode(1) {
            {
                add(new DefaultMutableTreeNode(2) {
                    {
                        add(new DefaultMutableTreeNode(3) {
                            {
                                add(new DefaultMutableTreeNode(4));
                                add(new DefaultMutableTreeNode(5));
                            }
                        });
                        add(new DefaultMutableTreeNode(6));
                    }
                });
                add(new DefaultMutableTreeNode(7));
                add(new DefaultMutableTreeNode(8) {
                    {
                        add(new DefaultMutableTreeNode(9) {
                            {
                                add(new DefaultMutableTreeNode(10));
                                add(new DefaultMutableTreeNode(11));
                            }
                        });
                        add(new DefaultMutableTreeNode(12));
                    }
                });
            }
        };

        TreeTraverser<TreeNode> tree = TreeTraverser.of(root, TreeTraverserTest::children);

        assertThat(tree.depthFirstIterable())
                .extracting("UserObject", Integer.class)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

        assertThat(tree.depthFirstStream())
                .extracting("UserObject", Integer.class)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    }

    @Test
    public void testPrettyPrint() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(1) {
            {
                add(new DefaultMutableTreeNode(2) {
                    {
                        add(new DefaultMutableTreeNode(3) {
                            {
                                add(new DefaultMutableTreeNode(4));
                                add(new DefaultMutableTreeNode(5));
                            }
                        });
                        add(new DefaultMutableTreeNode(6));
                    }
                });
                add(new DefaultMutableTreeNode(7));
                add(new DefaultMutableTreeNode(8) {
                    {
                        add(new DefaultMutableTreeNode(9) {
                            {
                                add(new DefaultMutableTreeNode(10));
                                add(new DefaultMutableTreeNode(11));
                            }
                        });
                        add(new DefaultMutableTreeNode(12));
                    }
                });
            }
        };

        TreeTraverser<DefaultMutableTreeNode> tree = TreeTraverser.of(root, TreeTraverserTest::children);
        Function<DefaultMutableTreeNode, String> toString = o -> o.getUserObject().toString();
        String nl = System.lineSeparator();

        assertThat(tree.prettyPrintToString(0, toString))
                .isEqualTo(
                        "1" + nl
                );

        assertThat(tree.prettyPrintToString(1, toString))
                .isEqualTo(
                        "1" + nl
                        + "|-2" + nl
                        + "|-7" + nl
                        + "`-8" + nl
                );

        assertThat(tree.prettyPrintToString(2, toString))
                .isEqualTo(
                        "1" + nl
                        + "|-2" + nl
                        + "|  |-3" + nl
                        + "|  `-6" + nl
                        + "|-7" + nl
                        + "`-8" + nl
                        + "   |-9" + nl
                        + "   `-12" + nl
                );

        assertThat(tree.prettyPrintToString(3, toString))
                .isEqualTo(
                        "1" + nl
                        + "|-2" + nl
                        + "|  |-3" + nl
                        + "|  |  |-4" + nl
                        + "|  |  `-5" + nl
                        + "|  `-6" + nl
                        + "|-7" + nl
                        + "`-8" + nl
                        + "   |-9" + nl
                        + "   |  |-10" + nl
                        + "   |  `-11" + nl
                        + "   `-12" + nl
                );
    }

    private static <X extends TreeNode> Iterable<X> children(X root) {
        return Collections.list(root.children());
    }
}
