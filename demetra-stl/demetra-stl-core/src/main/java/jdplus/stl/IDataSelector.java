/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl;

import jdplus.data.DataBlock;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IDataSelector extends IDataGetter, IDataSetter {

    @Override
    default int getLength() {
        return getEnd() - getStart();
    }

    static IDataSelector of(final double[] data) {
        return new IDataSelector() {

            @Override
            public double get(int idx) {
                return data[idx];
            }

            @Override
            public void set(int idx, double val) {
                data[idx] = val;
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return data.length;
            }

            @Override
            public int getLength() {
                return data.length;
            }
        };
    }

    static IDataSelector of(final double[] data, final int start) {
        return new IDataSelector() {

            @Override
            public double get(int idx) {
                return data[idx-start];
            }

            @Override
            public void set(int idx, double val) {
                data[idx-start] = val;
            }

            @Override
            public int getStart() {
                return start;
            }

            @Override
            public int getEnd() {
                return data.length+start;
            }

            @Override
            public int getLength() {
                return data.length;
            }
        };
    }

    static IDataSelector of(final DataBlock data) {
        return new IDataSelector() {

            @Override
            public double get(int idx) {
                return data.get(idx);
            }

            @Override
            public void set(int idx, double val) {
                data.set(idx, val);
            }

            @Override
            public int getStart() {
                return 0;
            }

            @Override
            public int getEnd() {
                return data.length();
            }

            @Override
            public int getLength() {
                return data.length();
            }
        };
    }

    static IDataSelector of(final DataBlock data, final int start) {
        return new IDataSelector() {

            @Override
            public double get(int idx) {
                return data.get(idx-start);
            }

            @Override
            public void set(int idx, double val) {
                data.set(idx-start, val);
            }

            @Override
            public int getStart() {
                return start;
            }

            @Override
            public int getEnd() {
                return data.length()+start;
            }

            @Override
            public int getLength() {
                return data.length();
            }
        };
    }
}
