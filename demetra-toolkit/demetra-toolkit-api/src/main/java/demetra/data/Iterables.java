/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.data;

import java.util.Iterator;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Iterables {
    
    public Iterable<Double> of(DoubleSeq seq){
        return () -> new DoubleSeqIterator(seq);
    }

    public Iterable<Double> of(double[] data){
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

