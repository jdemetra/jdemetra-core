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

import demetra.data.DoubleSeq;

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

    static IDataGetter of(final DoubleSeq data) {
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
