/*
 * Copyright (c) 2018 University of Illinois at Urbana-Champaign
 * All rights reserved.

 * Developed by: 	  Matias Carrasco Kind & Sahand Hariri
 *                 NCSA/UIUC
 */
package jdplus.ml;

import demetra.data.DoubleSeq;
import demetra.util.IntList;
import java.util.Random;
import demetra.math.matrices.Matrix;
import jdplus.ml.IsolationForests.FinalNode;
import jdplus.ml.IsolationForests.Node;
import jdplus.ml.IsolationForests.TreeBuilder;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class IsolationForest {

    TreeBuilder BUILDER = (X, selection, limit, rnd) -> new Builder(X, limit, rnd).root(selection);

    @lombok.AllArgsConstructor
    static class Builder {

        final Matrix X;
        final double limit;
        final Random rnd;

        Node root(int[] selection) {
            int[] sel = selection;
            if (sel == null) {
                sel = new int[X.getColumnsCount()];
                for (int i = 0; i < sel.length; ++i) {
                    sel[i] = i;
                }
            }
            return node(sel, 0);
        }

        Node node(int[] items, int level) {
            int size = items.length;
            int dim = X.getRowsCount();
            int axis = rnd.nextInt(dim);
            DoubleSeq row = X.row(axis);
            double xmin = row.get(items[0]), xmax = xmin;
            for (int i = 1; i < size; i++) {
                double r = row.get(items[i]);
                if (r < xmin) {
                    xmin = r;
                } else if (r > xmax) {
                    xmax = r;
                }
            }

            double p = xmin + rnd.nextDouble() * (xmax - xmin);
            IntList XL = new IntList(size), XR = new IntList(size);
            for (int i = 0; i < size; i++) {
                double r = row.get(items[i]);
                if (r < p) {
                    XL.add(i);
                } else {
                    XR.add(i);
                }
            }

            // Nodes with empty branches should be avoided, because they will bias
            // the length of the path. Impact on outliers detection is negligible
            // and will be ignored
            Node left, right;
            if (level > limit || XL.size() <= 1) {
                left = FinalNode.of(XL.size());
            } else {
                left = node(XL.toArray(), level + 1);
            }
            if (level > limit || XR.size() <= 1) {
                right = FinalNode.of(XR.size());
            } else {
                right = node(XR.toArray(), level + 1);
            }
            return new INode(items, axis, p, left, right);
        }
    }

   @lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
    static class INode implements Node{

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public int size() {
            return items.length;
        }

        final int[] items;
        final int axis;
        final double threshold;
        final Node left;
        final Node right;

 

        @Override
        public Node branch(DoubleSeq x) {
            if (x.get(axis)<threshold)
                return left;
            else
                return right;
        }

        @Override
        public Node left() {
            return left;
        }

        @Override
        public Node right() {
            return right;
        }
    }
    
}
