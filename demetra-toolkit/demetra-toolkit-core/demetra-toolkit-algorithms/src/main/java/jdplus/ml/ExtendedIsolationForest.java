/*
 * Copyright (c) 2018 University of Illinois at Urbana-Champaign
 * All rights reserved.

 * Developed by: 	  Matias Carrasco Kind & Sahand Hariri
 *                 NCSA/UIUC
 */
package jdplus.ml;

import demetra.data.DoubleSeq;
import demetra.util.IntList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import jdplus.data.DataBlock;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class ExtendedIsolationForest {

    public static enum Method {
        Default,
        Original
    }

    static double innerProduct(double[] X1, double[] X2) {
        double result = 0.0;
        for (int i = 0; i < X1.length; i++) {
            result += X1[i] * X2[i];
        }
        return result;
    }

    static double cFactor(int N) {
        double Nd = (double) N, Ndc = Nd - 1;
        double result;
        result = 2.0 * ((Math.log(Ndc) + EULER_CONSTANT) - Ndc / Nd);
        return result;

    }

    /*
     * Sample k elements from the range [0, N[ without replacement
     * k should be <= N
     * Source: https://www.gormanalysis.com/blog/random-numbers-in-cpp/
     */
    @Deprecated
    static int[] sampleWithoutReplacementLegacy(int k, int N, boolean shuffle, Random rnd) {

        // Create an unordered set to store the samples
        HashSet<Integer> samples = new HashSet();

        // Sample and insert values into samples
        for (int r = N - k; r < N; ++r) {
            int v = rnd.nextInt(r + 1);
            if (!samples.add(v)) {
                samples.add(r);
            }
        }

        int[] result = new int[k];
        int i = 0;
        for (Integer j : samples) {
            result[i++] = j;
        }

        // shuffle the results
        if (shuffle) {
            for (int j = 0; j < k; ++j) {
                int idx = rnd.nextInt(k);
                if (idx != j) {
                    int tmp = result[j];
                    result[j] = result[idx];
                    result[idx] = tmp;
                }
            }
        }
        return result;
    }

    static int[] sampleWithoutReplacement(int k, int N, boolean shuffle, Random rnd) {

        // Create an unordered set to store the samples
        int[] sample = new int[k];
        BitSet flags = new BitSet(N);

        for (int i = 0, r = N - k; r < N; ++r, ++i) {
            int v = rnd.nextInt(r + 1);
            if (flags.get(v)) {
                flags.set(r);
                sample[i] = r;
            } else {
                flags.set(v);
                sample[i] = v;
            }
        }

        if (shuffle) // shuffle the results
        {
            for (int j = 0; j < k; ++j) {
                int idx = rnd.nextInt(k);
                if (idx != j) {
                    int tmp = sample[j];
                    sample[j] = sample[idx];
                    sample[idx] = tmp;
                }
            }
        }
        return sample;
    }

    private static final double EULER_CONSTANT = 0.5772156649;

    @lombok.Getter
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
    static class Node {

        boolean isFinal() {
            return items == null;
        }

        int size() {
            return items == null ? size : items.size();
        }

        final int size;
        final IntList items;
        final int level;
        final double[] normalVector;
        final double[] point;
        final double pdotn;
        final Node left;
        final Node right;

        static Node finalNode(int size) {
            switch (size) {
                case 0:
                    return ZERO;
                case 1:
                    return ONE;
                default:
                    return new Node(size, null, -1, null, null, 0, null, null);
            }

        }

        private static final Node ZERO = new Node(0, null, -1, null, null, 0, null, null),
                ONE = new Node(1, null, -1, null, null, 0, null, null);
    }

    @lombok.Getter
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    static class iTree {

        final Matrix X;
        final int extensionLevel;
        final double limit;
        final Random rnd;
        final Method method;
        int exnodes;
        Node root;

        static iTree build(Matrix X, int extensionLevel, double limit, Random rnd) {
            IntList all = new IntList();
            int size = X.getColumnsCount();
            for (int i = 0; i < size; ++i) {
                all.add(i);
            }
            return build(X, all, extensionLevel, limit, rnd, Method.Default);
        }

        static iTree build(Matrix X, IntList selection, int extensionLevel, double limit, Random rnd, Method method) {
            if (rnd == null) {
                rnd = new Random();
            }
            iTree tree = new iTree(X, extensionLevel, limit, rnd, method, 0, null);
            tree.root = tree.addNode(selection, 0);
            return tree;
        }

        private Node addNode(IntList items, int level) {
            // final node
            int size = items.size();
            if (level >= limit || size <= 1) {
                exnodes += 1;
                return Node.finalNode(size);
            }

            switch (method) {
                case Original:
                    return addNode1(items, level);
                default:
                    return addNode0(items, level);
            }

        }

        private Node addNode0(IntList items, int level) {
            // final node
            int size = items.size();
            int dim = X.getRowsCount();
            // point, normal
            double[] p = new double[dim], n = new double[dim];
            double[] xmin = new double[dim], xmax = new double[dim];
            for (int i = 0; i < dim; ++i) {
                double cmin = X.get(i, items.get(0)), cmax = cmin;
                for (int j = 1; j < size; ++j) {
                    double cur = X.get(i, items.get(j));
                    if (cur < cmin) {
                        cmin = cur;
                    } else if (cur > cmax) {
                        cmax = cur;
                    }
                }
                xmin[i] = cmin;
                xmax[i] = cmax;
            }
            // Pick a random point on splitting hyperplane 
            // Pick a random normal vector according to specified extension level 
            for (int i = 0; i < dim; i++) {
                double r = rnd.nextDouble();
                // No check for rounding issues
                p[i] = xmin[i] + r * (xmax[i] - xmin[i]);
            }
            IntList XL = new IntList(size), XR = new IntList(size);
            DataBlock N = DataBlock.of(n), P = DataBlock.of(p);
            double pdotn = 0;
            int o = 0;
            int omax = 3 * dim;
            do {
                XL.clear();
                XR.clear();
                for (int i = 0; i < dim; i++) {
                    n[i] = rnd.nextGaussian();
                }
                int k = dim - extensionLevel - 1;
                if (k > 0) {
                    int[] zeroidx = sampleWithoutReplacement(k, dim, false, rnd);
                    for (int i = 0; i < zeroidx.length; ++i) {
                        n[zeroidx[i]] = 0;
                    }
                }
                // Implement splitting criterion 

                pdotn = P.dot(N);
                double q = pdotn;
                items.forEach(i -> {
                    double innerprod = N.dot(X.column(i));
                    if (innerprod < q) {
                        XL.add(i);
                    } else {
                        XR.add(i);
                    }
                });
            } while (o++ < omax && (XL.isEmpty() || XR.isEmpty()));

            // Nodes with empty branches should be avoided, because they will bias
            // the length of the path. Impact on outliers detection is negligible
            // and will be ignored
            Node left = addNode(XL, level + 1);
            Node right = addNode(XR, level + 1);
            return new Node(-1, items, level, n, p, pdotn, left, right);
        }

        private Node addNode1(IntList items, int level) {
            // final node
            int size = items.size();
            int dim = X.getRowsCount();
            // point, normal
            double[] p = new double[dim], n = new double[dim];
            double[] xmin = new double[dim], xmax = new double[dim];
            for (int i = 0; i < dim; ++i) {
                double cmin = X.get(i, items.get(0)), cmax = cmin;
                for (int j = 1; j < size; ++j) {
                    double cur = X.get(i, items.get(j));
                    if (cur < cmin) {
                        cmin = cur;
                    } else if (cur > cmax) {
                        cmax = cur;
                    }
                }
                xmin[i] = cmin;
                xmax[i] = cmax;
            }
            // Pick a random point on splitting hyperplane 
            // Pick a random normal vector according to specified extension level 
            for (int i = 0; i < dim; i++) {
                double r = rnd.nextDouble();
                // No check for rounding issues
                p[i] = xmin[i] + r * (xmax[i] - xmin[i]);
            }
            IntList XL = new IntList(size), XR = new IntList(size);
            DataBlock N = DataBlock.of(n), P = DataBlock.of(p);
            double pdotn = 0;
            for (int i = 0; i < dim; i++) {
                n[i] = rnd.nextGaussian();
            }
            int k = dim - extensionLevel - 1;
            if (k > 0) {
                int[] zeroidx = sampleWithoutReplacement(k, dim, false, rnd);
                for (int i = 0; i < zeroidx.length; ++i) {
                    n[zeroidx[i]] = 0;
                }
            }
            // Implement splitting criterion 

            pdotn = P.dot(N);
            double q = pdotn;
            items.forEach(i -> {
                double innerprod = N.dot(X.column(i));
                if (innerprod < q) {
                    XL.add(i);
                } else {
                    XR.add(i);
                }
            });

            Node left = addNode(XL, level + 1);
            Node right = addNode(XR, level + 1);
            return new Node(-1, items, level, n, p, pdotn, left, right);
        }

    }

    @lombok.experimental.UtilityClass
    static class Path {

        static double findPath(Node node, DoubleSeq x) {
            if (node.isFinal()) {
                int size = node.size();
                switch (size) {
                    case 0:
                        return 0; // unused node
                    case 1:
                        return 1; // true final level
                    default:
                        return 1 + ExtendedIsolationForest.cFactor(size);
                }
            } else {
                double xdotn = DataBlock.of(node.getNormalVector()).dot(x);
                if (xdotn < node.getPdotn()) {
                    return 1 + findPath(node.getLeft(), x);
                } else {
                    return 1 + findPath(node.getRight(), x);
                }
            }
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    @lombok.Builder(builderClassName="Builder")
    public static class iForest {

        @lombok.NonNull 
        final Matrix X;
        
        int extensionLevel;
        int limit;
        iTree[] trees;
        double c;
        final Random rnd;
        final Method method;

        public static Builder builder(){
            Builder builder=new Builder();
            builder.extensionLevel=-1;
            builder.method=Method.Default;
            return builder;
        }
        
        public static class Builder{
            public iForest build(){
                if (X == null)
                    throw new IllegalArgumentException();
                Random brnd=rnd == null ? new Random() : rnd;
                int ex=extensionLevel == -1 ? X.getRowsCount() - 1 : extensionLevel;
                return new iForest(X, ex, limit, null, 0, brnd, method);
            }
        }
        
        public void fit(int ntrees, int sampleSize) {
            int climit = limit <= 0 ? (int) Math.ceil(Math.log(sampleSize) / Math.log(2)) : limit;
            c = cFactor(sampleSize);
            trees = new iTree[ntrees];
            if (sampleSize < X.getColumnsCount()) {
                for (int i = 0; i < ntrees; ++i) {
                    int[] sample = sampleWithoutReplacement(sampleSize, X.getColumnsCount(), false, rnd);
                    IntList selection = new IntList(sampleSize);
                    for (int j = 0; j < sample.length; ++j) {
                        selection.add(sample[j]);
                    }
                    trees[i] = iTree.build(X, selection, extensionLevel, climit, rnd, method);
                }
            } else {
                for (int i = 0; i < ntrees; ++i) {
                    trees[i] = iTree.build(X, extensionLevel, climit, rnd);
                }
            }
        }

        public double[] predict(Matrix x) {
            Matrix z = x == null ? X : x;
            int size = z.getColumnsCount();
            double[] S = new double[size];
            for (int i = 0; i < size; i++) {
                DoubleSeq cur = z.column(i);
                double htemp = 0.0;
                for (iTree tree : trees) {
                    htemp += Path.findPath(tree.getRoot(), cur);
                }
                double havg = htemp / trees.length;
                S[i] = Math.pow(2.0, -havg / c);
            }
            return S;
        }

    }

}
