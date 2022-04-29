/*
 * Copyright 2022 National Bank of Belgium
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
