/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.linearfilters;

import demetra.data.DataBlock;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IFilterOutput {

    public static IFilterOutput of(final DataBlock buffer, final int startPos) {
        return new IFilterOutput() {
            @Override
            public int getStart() {
                return startPos;
            }

            @Override
            public int getEnd() {
                return startPos + buffer.length();
            }

            @Override
            public void set(int pos, double value) {
                buffer.set(pos - startPos, value);
            }

            @Override
            public void add(int pos, double value) {
                buffer.add(pos - startPos, value);
            }
        };
    }

    public static IFilterOutput of(final double[] buffer, final int startPos) {
        return new IFilterOutput() {
            @Override
            public int getStart() {
                return startPos;
            }

            @Override
            public int getEnd() {
                return startPos + buffer.length;
            }

            @Override
            public void set(int pos, double value) {
                buffer[pos - startPos]=value;
            }

            @Override
            public void add(int pos, double value) {
                buffer[pos - startPos]=value;
            }
        };
    }

    int getStart();

    int getEnd();

    void set(int pos, double value);

    void add(int pos, double value);
}
