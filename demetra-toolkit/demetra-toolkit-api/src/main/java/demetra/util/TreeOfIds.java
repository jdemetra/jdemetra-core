/*
 * Copyright 2020 National Bank of Belgium
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Jean Palate
 */
public final class TreeOfIds {

    private static final class TreeNode {

        TreeNode(@Nullable Id id) {
            this.id = id;
            this.children = new ArrayList<>();
        }
        public final Id id;
        public final List<TreeNode> children;

        Id[] getChildrenIds() {
            Id[] result = new Id[children.size()];
            int i = 0;
            for (TreeNode node : children) {
                result[i++] = node.id;
            }
            return result;
        }
    }

    private final TreeNode root;
    private final Map<Id, TreeNode> nodes;

    public TreeOfIds(@NonNull List<Id> items) {
        this.root = new TreeNode(null);
        this.nodes = new HashMap<>();
        for (Id id : items) {
            Id[] path = id.path();
            TreeNode prev = null;
            for (int i = 0; i < path.length; ++i) {
                TreeNode cur = nodes.get(path[i]);
                if (cur == null) {
                    cur = new TreeNode(path[i]);
                    if (prev == null) {
                        root.children.add(cur);
                    } else {
                        prev.children.add(cur);
                    }
                    nodes.put(path[i], cur);
                }
                prev = cur;
            }
        }
    }

    public boolean contains(Id id) {
        return nodes.containsKey(id);
    }

    @NonNull
    public Id[] roots() {
        return root.getChildrenIds();
    }

    @NonNull
    public Id[] children(@Nullable Id cur) {
        if (cur == null) {
            return roots();
        }
        TreeNode croot = nodes.get(cur);
        if (croot == null) {
            return new Id[0];
        }
        return croot.getChildrenIds();
    }
}
