/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package ec.tstoolkit.data;

import ec.tstoolkit.design.IntValue;

/**
 *
 * @author gianluca
 */
public enum WindowType implements IntValue {

    Square(0),
    Welch(1),
    Tukey(2),
    Bartlett(3),
    Hamming(4),
    Parzen(5);

    public static WindowType valueOf(int value) {
        return IntValue.valueOf(WindowType.class, value).orElse(null);
    }
    private final int value;

    WindowType(final int value) {
        this.value = value;
    }

    /**
     * Returns the value of this WinType as an int.
     *
     * @return
     */
    @Override
    public int intValue() {
        return value;
    }

    private static double parzen(int idx, int size) {
        double tmp = idx / (double) size;
        if (idx <= size / 2) {
            return 1.0 - 6.0 * Math.pow(tmp, 2.0) + 6 * Math.pow(tmp, 3.0);
        } else {
            return 2 * Math.pow(1.0 - tmp, 3);
        }
    }

    public double[] window(int winSize) {
        double[] window = new double[winSize];
        double dsize = winSize;
        switch (this) {
            case Welch:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 1.0 - (i / (double) winSize) * (i / dsize);
                }
                break;
            case Tukey:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 0.5 * (1 + Math.cos(Math.PI * i / dsize));
                }
                break;
            case Bartlett:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 1.0 - i / dsize;
                }
                break;
            case Hamming:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 0.54 + 0.46 * Math.cos(Math.PI * i / dsize);
                }
                break;
            case Parzen:
                for (int i = 0; i < winSize; i++) {
                    window[i] = parzen(i, winSize);
                }
                break;
            case Square:
                for (int i = 0; i < winSize; i++) {
                    window[i] = 1.0;
                }
                break;
        }
        return window;
    }

}
