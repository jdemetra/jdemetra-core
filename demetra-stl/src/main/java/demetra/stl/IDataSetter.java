/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stl;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IDataSetter {

    void set(int idx, double value);

    int getStart();

    int getEnd();

    default int getLength() {
        return getEnd() - getStart();
    }
    
        static IDataSetter of(final double[] data) {
        return new IDataSetter() {
            @Override
            public void set(int idx, double val) {
                data[idx]=val;
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

}
