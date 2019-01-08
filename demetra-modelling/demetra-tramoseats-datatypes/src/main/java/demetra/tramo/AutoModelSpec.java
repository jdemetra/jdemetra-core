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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Data
public final class AutoModelSpec implements Cloneable{

    public static final double DEF_CANCEL = .05, DEF_PCR = .95, DEF_UB1 = .97, DEF_UB2 = .91, DEF_TSIG = 1, DEF_PC = .12;
    public static final boolean DEF_FAL = false, DEF_AMICOMPARE = false;

    private double cancel = DEF_CANCEL, ub1 = DEF_UB1, ub2 = DEF_UB2,
            pcr = DEF_PCR, pc = DEF_PC, tsig = DEF_TSIG;
    private boolean enabled = false;
    private boolean acceptDefault = DEF_FAL;
    private boolean amiCompare = DEF_AMICOMPARE;

    public AutoModelSpec(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public AutoModelSpec clone(){
        try {
            return (AutoModelSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
    
    public void reset() {
        cancel = DEF_CANCEL;
        ub1 = DEF_UB1;
        ub2 = DEF_UB2;
        pcr = DEF_PCR;
        pc = DEF_PC;
        tsig = DEF_TSIG;
        enabled = false;
        acceptDefault = DEF_FAL;
        amiCompare = DEF_AMICOMPARE;
    }

    public void setPcr(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("PCR should belong to [0.8, 1.0]");
        }
        pcr = value;
    }

    public void setUb1(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("UB1 should belong to [0.8, 1.0]");
        }
        ub1 = value;
    }

    public void setUb2(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("UB2 should belong to [0.8, 1.0]");
        }
        ub2 = value;
    }

    public void setCancel(double value) {
        if (value < 0 || value > .3) {
            throw new TramoException("Cancelation limit should belong to [0, 0.3]");
        }
        cancel = value;
    }

    public void setTsig(double value) {
        if (value <= .5) {
            throw new TramoException("TSIG should be higher than 0.5");
        }
        tsig = value;
    }

    public void setPc(double value) {
        if (value < .1 || value > 0.3) {
            throw new TramoException("PC should belong to [0.1, 0.3]");
        }
        pc = value;
    }

    public boolean isDefault() {
        return !enabled && !acceptDefault && cancel == DEF_CANCEL && pc == DEF_PC
                && pcr == DEF_PCR && tsig == DEF_TSIG && ub1 == DEF_UB1
                && ub2 == DEF_UB2 && !amiCompare;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    public void setAmiCompare(boolean value) {
        amiCompare = value;
    }

    public void setAcceptDefault(boolean fal) {
        this.acceptDefault = fal;
    }

}
