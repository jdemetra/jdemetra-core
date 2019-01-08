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
package demetra.tramo;

import demetra.design.Development;
import demetra.timeseries.TimeSelector;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Data
public final class OutlierSpec implements Cloneable {

    public static final double DEF_DELTATC = .7;
    public static final boolean DEF_EML = false;

    private static final OutlierSpec DEFAULT = new OutlierSpec();

    private boolean ao, ls, tc, so;
    private double deltaTC = DEF_DELTATC;
    private boolean maximumLikelihood = DEF_EML;
    private double criticalValue = 0;
    @lombok.NonNull
    private TimeSelector span = TimeSelector.all();

    public OutlierSpec() {
    }

    @Override
    public OutlierSpec clone() {
        try {
            OutlierSpec c = (OutlierSpec) super.clone();
            c.span = span.clone();
            return c;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public void reset() {
        ao = false;
        ls = false;
        tc = false;
        so = false;
        deltaTC = DEF_DELTATC;
        maximumLikelihood = false;
        criticalValue = 0;
        span = TimeSelector.all();
    }

    public void setDeltaTC(double value) {
        if (value != 0 && value < .3 || value >= 1) {
            throw new TramoException("TC should belong to [0.3, 1.0[");
        }
        deltaTC = value;
    }

    public void setCriticalValue(double value) {
        if (value != 0 && value < 2) {
            throw new TramoException("Critical value should be greater than 2.0");
        }
        criticalValue = value;
    }

    public int getAIO() {
        if (ao && ls && !tc) {
            return 3;
        } else if (ao && tc && !ls) {
            return 1;
        } else {
            return 2;
        }
    }

    public void setAIO(int value) {
        ao = false;
        ls = false;
        tc = false;
        so = false;
        switch (value) {
            case 1:
                ao = true;
                tc = true;
                break;
            case 2:
                ao = true;
                tc = true;
                ls = true;
                break;
            case 3:
                ao = true;
                ls = true;
                break;
        }
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public boolean isUsed() {
        return ao || ls || tc || so;
    }

}
