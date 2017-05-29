/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stl;

import demetra.data.Doubles;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IDataGetter {

    double get(int idx);

    int getStart();

    int getEnd();

    default int getLength() {
        return getEnd() - getStart();
    }

    static IDataGetter of(final double[] data) {
        return new IDataGetter() {
            @Override
            public double get(int idx) {
                return data[idx];
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

    static IDataGetter of(final Doubles data) {
        return new IDataGetter() {
            @Override
            public double get(int idx) {
                return data.get(idx);
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
}
