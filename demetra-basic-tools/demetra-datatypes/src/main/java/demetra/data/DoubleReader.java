/*
 * Copyright 2017 National Bank of Belgium
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

/**
 * Fast iterator to access cells of a sequence of doubles.
 * This "simplified" iterator doesn't control the end of the iterations 
 * (done externally). 
 * @author Jean Palate
 */
public interface DoubleReader {

    /**
     * Returns the current element and advances the iterator 
     * @return 
     */
    double next();

    /**
     * Sets the reader at a given position. The next call to "get" should 
     * return the item at that position
     * @param npos 
     */
    void setPosition(int npos);

    public static DoubleReader of(double[] data) {
        return new CellReaderP(data, 0);
    }

    public static DoubleReader of(double[] data, int pos, int inc) {
        switch (inc) {
            case 1:
                return new CellReaderP(data, pos);
            case -1:
                return new CellReaderM(data, pos);
            default:
                return new DefaultArrayReader(data, pos, inc);
        }
    }
    
    public static DoubleReader defaultReaderOf(DoubleSequence seq){
        return new DefaultReader(seq);
    }
}

class DefaultArrayReader implements DoubleReader {

    final double[] data;
    final int inc;
    final int leftPos;
    int curPos;

    DefaultArrayReader(double[] data, int leftPos, int inc) {
        this.data = data;
        this.leftPos=leftPos;
        this.curPos = leftPos;
        this.inc = inc;
    }

    @Override
    public double next() {
        double val=data[curPos];
        curPos = curPos + inc;
        return val;
    }

    @Override
    public void setPosition(int npos) {
        curPos = leftPos+inc*npos;
    }
}

class DefaultReader implements DoubleReader {

    final DoubleSequence data;
    int curPos;

    DefaultReader(DoubleSequence data) {
        this.data = data;
        this.curPos = 0;
    }

    @Override
    public double next() {
        return data.get(curPos++);
    }

    @Override
    public void setPosition(int npos) {
        curPos = npos;
    }
}

class CellReaderP implements DoubleReader {

    final double[] data;
    final int leftPos;
    int curPos;

    CellReaderP(double[] data, int leftPos) {
        this.data = data;
        this.leftPos=leftPos;
        this.curPos = leftPos;
    }

    @Override
    public double next() {
        return data[curPos++];
    }

    @Override
    public void setPosition(int npos) {
        curPos = leftPos+npos;
    }
}

class CellReaderM implements DoubleReader {

    final double[] data;
    final int leftPos;
    int curPos;

    CellReaderM(double[] data, int leftPos) {
        this.data = data;
        this.leftPos=leftPos;
        this.curPos = leftPos;
    }

    @Override
    public double next() {
        return data[curPos--];
    }

    @Override
    public void setPosition(int npos) {
        curPos = leftPos-npos;
    }
}
