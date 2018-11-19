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
import demetra.tramo.TramoException;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class AutoModelSpec{


    private double cancel = DEF_CANCEL, ub1 = DEF_UB1, ub2 = DEF_UB2,
            pcr = DEF_PCR, pc = DEF_PC, tsig = DEF_TSIG;
    private boolean enabled = false;
    private boolean fal = DEF_FAL;
    private boolean amiCompare = DEF_AMICOMPARE;
    public static final double DEF_CANCEL = .05, DEF_PCR = .95, DEF_UB1 = .97, DEF_UB2 = .91, DEF_TSIG = 1, DEF_PC = .12;
    public static final boolean DEF_FAL = false, DEF_AMICOMPARE = false;

    public AutoModelSpec(boolean enabled) {
        this.enabled = enabled;
    }

    public AutoModelSpec(AutoModelSpec other) {
        this.enabled = other.enabled;
        this.amiCompare=other.amiCompare;
        this.cancel=other.cancel;
        this.fal=other.fal;
        this.pc=other.pc;
        this.pcr=other.pcr;
        this.tsig=other.tsig;
        this.ub1=other.ub1;
        this.ub2=other.ub2;
    }
    public void reset() {
        cancel = DEF_CANCEL;
        ub1 = DEF_UB1;
        ub2 = DEF_UB2;
        pcr = DEF_PCR;
        pc = DEF_PC;
        tsig = DEF_TSIG;
        enabled = false;
        fal = DEF_FAL;
        amiCompare = DEF_AMICOMPARE;
    }

    public double getPcr() {
        return pcr;
    }

    public void setPcr(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("PCR should belong to [0.8, 1.0]");
        }
        pcr = value;
    }

    public double getUb1() {
        return ub1;
    }

    public void setUb1(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("UB1 should belong to [0.8, 1.0]");
        }
        ub1 = value;
    }

    public double getUb2() {
        return ub2;
    }

    public void setUb2(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("UB2 should belong to [0.8, 1.0]");
        }
        ub2 = value;
    }

    public double getCancel() {
        return cancel;
    }

    public void setCancel(double value) {
        if (value < 0 || value > .3) {
            throw new TramoException("Cancelation limit should belong to [0, 0.3]");
        }
        cancel = value;
    }

    public double getTsig() {
        return tsig;
    }

    public void setTsig(double value) {
        if (value <= .5) {
            throw new TramoException("TSIG should be higher than 0.5");
        }
        tsig = value;
    }

    public double getPc() {
        return pc;
    }

    public void setPc(double value) {
        if (value < .1 || value > 0.3) {
            throw new TramoException("PC should belong to [0.1, 0.3]");
        }
        pc = value;
    }

    public boolean isDefault() {
        return !enabled && !fal && cancel == DEF_CANCEL && pc == DEF_PC
                && pcr == DEF_PCR && tsig == DEF_TSIG && ub1 == DEF_UB1
                && ub2 == DEF_UB2 && !amiCompare;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    public boolean isAmiCompare() {
        return amiCompare;
    }

    public void setAmiCompare(boolean value) {
        amiCompare = value;
    }

    public boolean equals(AutoModelSpec other) {
        if (other == null) {
            return !enabled;
        }
        return amiCompare == other.amiCompare && fal == other.fal
                && cancel == other.cancel && enabled == other.enabled && pc == other.pc
                && pcr == other.pcr && tsig == other.tsig && ub1 == other.ub1
                && ub2 == other.ub2;
    }

    public static boolean equals(AutoModelSpec l, AutoModelSpec r) {
        if (l == r) {
            return true;
        } else if (l != null) {
            return l.equals(r);
        } else {
            return r.equals(l);
        }
    }

    public boolean isAcceptDefault() {
        return fal;
    }

    public void setAcceptDefault(boolean fal) {
        this.fal = fal;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof AutoModelSpec && equals((AutoModelSpec) obj));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.enabled ? 1 : 0);
        hash = 17 * hash + (this.fal ? 1 : 0);
        hash = 17 * hash + (this.amiCompare ? 1 : 0);
        return hash;
    }
}
