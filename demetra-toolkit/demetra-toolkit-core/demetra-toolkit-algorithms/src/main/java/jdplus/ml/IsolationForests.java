/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.ml;

import demetra.data.DoubleSeq;
import demetra.math.matrices.Matrix;
import demetra.util.IntList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class IsolationForests {
    
    double innerProduct(double[] X1, double[] X2) {
        double result = 0.0;
        for (int i = 0; i < X1.length; i++) {
            result += X1[i] * X2[i];
        }
        return result;
    }

    double cFactor(int N) {
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
    int[] sampleWithoutReplacementLegacy(int k, int N, boolean shuffle, Random rnd) {

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

    int[] sampleWithoutReplacement(int k, int N, boolean shuffle, Random rnd) {

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

    final double EULER_CONSTANT = 0.5772156649;
    
    public static interface Node{
        default boolean isFinal(){return false;}
        int size();
        Node branch(DoubleSeq x);
        Node left();
        Node right();
    }
    
    public static class FinalNode implements Node{
        
        private final int size;
        public FinalNode(int size){
            this.size=size;
        }
        
        public static Node of(int size){
            switch (size){
                case 0 : return ZERO;
                case 1: return ONE;
                default:return new FinalNode(size);
            }
        }
        
        @Override
        public boolean isFinal(){return true;}
        
        @Override
        public int size(){return size;}
        @Override
        public Node branch(DoubleSeq x){return null;}
        @Override
        public Node left(){return null;}
        @Override
        public Node right(){return null;}

        private static final Node ZERO = new FinalNode(0),
                ONE = new FinalNode(1);
    }

    public double findPath(Node node, DoubleSeq x) {
            if (node.isFinal()) {
                int size = node.size();
                switch (size) {
                    case 0:
                        return 0; // unused node
                    case 1:
                        return 1; // true final level
                    default:
                        return 1 + cFactor(size);
                }
            } else {
                return 1+findPath(node.branch(x), x);
            }
        }
    
    @FunctionalInterface
    public static interface TreeBuilder{
        
        Node root(final Matrix X, final int[] selection, final double limit, final Random rnd);
        
        public static TreeBuilder legacy(){
            return IsolationForest.BUILDER;
        }
        
        public static TreeBuilder extended(int extensionLevel){
            return ExtendedIsolationForest.builder(extensionLevel);
        }
        
        public static TreeBuilder smooth(){
            return XIsolationForest.BUILDER;
        }
        
    }
    
    
    @lombok.Data
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    @lombok.Builder(builderClassName="Builder")
    public static class Forest {

        @lombok.NonNull 
        final Matrix X;
        
        int limit;
        Node[] trees;
        double c;
        TreeBuilder treeBuilder;
        final Random rnd;

        public static class Builder{
            public Forest build(){
                if (X == null)
                    throw new IllegalArgumentException();
                Random brnd=rnd == null ? new Random() : rnd;
                return new Forest(X, limit, null, c, treeBuilder, brnd);
            }
        }
        
        public void fit(int ntrees, int sampleSize) {
            int climit = limit <= 0 ? (int) Math.ceil(Math.log(sampleSize) / Math.log(2)) : limit;
            c = cFactor(sampleSize);
            trees = new Node[ntrees];
            if (sampleSize < X.getColumnsCount()) {
                for (int i = 0; i < ntrees; ++i) {
                    int[] sample = sampleWithoutReplacement(sampleSize, X.getColumnsCount(), false, rnd);
                    IntList selection = new IntList(sampleSize);
                    for (int j = 0; j < sample.length; ++j) {
                        selection.add(sample[j]);
                    }
                    trees[i] = treeBuilder.root(X, selection.toArray(), climit, rnd);
                }
            } else {
                for (int i = 0; i < ntrees; ++i) {
                    trees[i] = treeBuilder.root(X, null, climit, rnd);
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
                for (Node root : trees) {
                    htemp += findPath(root, cur);
                }
                double havg = htemp / trees.length;
                S[i] = Math.pow(2.0, -havg / c);
            }
            return S;
        }

    }
    
}
