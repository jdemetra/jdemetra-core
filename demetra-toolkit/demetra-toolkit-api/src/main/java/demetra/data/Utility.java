/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import java.util.Iterator;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Utility {
    
    public Iterable<Double> asIterable(DoubleSeq seq){
        return () -> new DoubleSeqIterator(seq);
    }

    public Iterable<Double> asIterable(double[] data){
        return () -> new DoublesIterator(data);
    }

    static class DoubleSeqIterator implements Iterator<Double> {

        private final DoubleSeqCursor cursor;
        private final int n;
        private int pos;

        DoubleSeqIterator(DoubleSeq seq) {
            this.cursor = seq.cursor();
            this.n = seq.length();
            this.pos = 0;
        }

        @Override
        public boolean hasNext() {
            return pos < n;
        }

        @Override
        public Double next() {
            ++pos;
            return cursor.getAndNext();
        }
    }

    static class DoublesIterator implements Iterator<Double> {

        private final double[] data;
        private int pos;

        DoublesIterator(double[] data) {
            this.data=data;
            this.pos = 0;
        }

        @Override
        public boolean hasNext() {
            return pos < data.length;
        }

        @Override
        public Double next() {
            return data[pos++];
        }
    }
}

