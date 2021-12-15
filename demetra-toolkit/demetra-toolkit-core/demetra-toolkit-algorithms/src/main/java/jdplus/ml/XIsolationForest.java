package jdplus.ml;

import demetra.data.DoubleSeq;
import demetra.util.IntList;
import java.util.Random;
import jdplus.data.DataBlock;
import demetra.math.matrices.Matrix;
import jdplus.ml.IsolationForests.Node;
import jdplus.ml.IsolationForests.TreeBuilder;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class XIsolationForest {

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
            double[] n = new double[dim];
            for (int i = 0; i < dim; i++) {
                n[i] = rnd.nextGaussian();
            }
            double[] d = new double[size];
            IntList XL = new IntList(size), XR = new IntList(size);
            DataBlock N = DataBlock.of(n);
            double xdotn = X.column(items[0]).dot(N);
            d[0] = xdotn;
            double min = xdotn, max = xdotn;
            for (int i = 1; i < size; ++i) {
                xdotn = X.column(items[i]).dot(N);
                d[i] = xdotn;
                if (xdotn < min) {
                    min = xdotn;
                } else if (xdotn > max) {
                    max = xdotn;
                }
            }

            // Implement splitting criterion 
            double p = rnd.nextDouble() * (max - min) + min;
            for (int i = 0; i < size; ++i) {
                if (d[i] < p) {
                    XL.add(i);
                } else {
                    XR.add(i);
                }
            }

            Node left, right;
            if (level > limit || XL.size() <= 1) {
                left = IsolationForests.FinalNode.of(XL.size());
            } else {
                left = node(XL.toArray(), level + 1);
            }
            if (level > limit || XR.size() <= 1) {
                right = IsolationForests.FinalNode.of(XR.size());
            } else {
                right = node(XR.toArray(), level + 1);
            }
            return new XNode(items, N, p, left, right);
        }

    }

    
    @lombok.AllArgsConstructor
    static class XNode implements Node{

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public int size() {
            return items.length;
        }
        
        @Override
        public Node left(){
            return left;
        }
        
        @Override
        public Node right(){
            return right;
        }

        final int[] items;
        final DataBlock normalVector;
        final double threshold;
        final Node left;
        final Node right;

        @Override
        public Node branch(DoubleSeq x) {
            double d=x.dot(normalVector);
            return d<threshold ? left : right;
        }
        
    }
    
    TreeBuilder BUILDER= (X, selection, limit, rnd)->new Builder(X, limit, rnd).root(selection);
 
}
