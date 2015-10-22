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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class AutoModelSpec implements Cloneable, InformationSetSerializable {

    public static final String ENABLED = "enabled",
            PCR = "pcr",
            UB1 = "ub1",
            UB2 = "ub2",
            TSIG = "tsig",
            PC = "pc",
            CANCEL = "cancel",
            FAL = "fal",
            AMICOMPARE = "compare";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, CANCEL), Double.class);
        dic.put(InformationSet.item(prefix, UB1), Double.class);
        dic.put(InformationSet.item(prefix, UB2), Double.class);
        dic.put(InformationSet.item(prefix, TSIG), Double.class);
        dic.put(InformationSet.item(prefix, PC), Double.class);
        dic.put(InformationSet.item(prefix, PCR), Double.class);
        dic.put(InformationSet.item(prefix, ENABLED), Boolean.class);
        dic.put(InformationSet.item(prefix, FAL), Boolean.class);
         dic.put(InformationSet.item(prefix, AMICOMPARE), Boolean.class);
    }

    private double cancel_ = DEF_CANCEL, ub1_ = DEF_UB1, ub2_ = DEF_UB2,
            pcr_ = DEF_PCR, pc_ = DEF_PC, tsig_ = DEF_TSIG;
    private boolean enabled_ = false;
    private boolean fal_ = DEF_FAL;
    private boolean amicompare_ = DEF_AMICOMPARE;
    public static final double DEF_CANCEL = .05, DEF_PCR = .95, DEF_UB1 = .97, DEF_UB2 = .91, DEF_TSIG = 1, DEF_PC = .12;
    public static final boolean DEF_FAL = false, DEF_AMICOMPARE = false;

     public AutoModelSpec() {
    }

    public AutoModelSpec(boolean enabled) {
        enabled_ = enabled;
    }

    public void reset() {
        cancel_ = DEF_CANCEL;
        ub1_ = DEF_UB1;
        ub2_ = DEF_UB2;
        pcr_ = DEF_PCR;
        pc_ = DEF_PC;
        tsig_ = DEF_TSIG;
        enabled_ = false;
        fal_ = DEF_FAL;
        amicompare_ = DEF_AMICOMPARE;
    }

    public double getPcr() {
        return pcr_;
    }

    public void setPcr(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("PCR should belong to [0.8, 1.0]");
        }
        pcr_ = value;
    }

    public double getUb1() {
        return ub1_;
    }

    public void setUb1(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("UB1 should belong to [0.8, 1.0]");
        }
        ub1_ = value;
    }

    public double getUb2() {
        return ub2_;
    }

    public void setUb2(double value) {
        if (value < .8 || value > 1) {
            throw new TramoException("UB1 should belong to [0.8, 1.0]");
        }
        ub2_ = value;
    }

    public double getCancel() {
        return cancel_;
    }

    public void setCancel(double value) {
        if (value < 0 || value > .3) {
            throw new TramoException("UB1 should belong to [0, 0.3]");
        }
        cancel_ = value;
    }

    public double getTsig() {
        return tsig_;
    }

    public void setTsig(double value) {
        if (value <= .5) {
            throw new TramoException("TSIG should be higher than 0.5");
        }
        tsig_ = value;
    }

    public double getPc() {
        return pc_;
    }

    public void setPc(double value) {
        if (value < .1 || value > 0.3) {
            throw new TramoException("PC should belong to [0.1, 0.3]");
        }
        pc_ = value;
    }

    public boolean isDefault() {
        return enabled_ && !fal_ && cancel_ == DEF_CANCEL && pc_ == DEF_PC
                && pcr_ == DEF_PCR && tsig_ == DEF_TSIG && ub1_ == DEF_UB1
                && ub2_ == DEF_UB2 && !amicompare_;
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean value) {
        enabled_ = value;
    }

    public boolean isAmiCompare() {
        return amicompare_;
    }

    public void setAmiCompare(boolean value) {
        amicompare_ = value;
    }

    @Override
    public AutoModelSpec clone() {
        try {
            return (AutoModelSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean equals(AutoModelSpec other) {
        if (other == null) {
            return !enabled_;
        }
        return amicompare_ == other.amicompare_ && fal_ == other.fal_
                && cancel_ == other.cancel_ && enabled_ == other.enabled_ && pc_ == other.pc_
                && pcr_ == other.pcr_ && tsig_ == other.tsig_ && ub1_ == other.ub1_
                && ub2_ == other.ub2_;
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
        return fal_;
    }

    public void setAcceptDefault(boolean fal) {
        fal_ = fal;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof AutoModelSpec && equals((AutoModelSpec) obj));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.enabled_ ? 1 : 0);
        hash = 17 * hash + (this.fal_ ? 1 : 0);
        hash = 17 * hash + (this.amicompare_ ? 1 : 0);
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(ENABLED, enabled_);
        if (verbose || pcr_ != DEF_PCR) {
            info.add(PCR, pcr_);
        }
        if (verbose || pc_ != DEF_PC) {
            info.add(PC, pc_);
        }
        if (verbose || ub1_ != DEF_UB1) {
            info.add(UB1, ub1_);
        }
        if (verbose || ub2_ != DEF_UB2) {
            info.add(UB2, ub2_);
        }
        if (verbose || cancel_ != DEF_CANCEL) {
            info.add(CANCEL, cancel_);
        }
        if (verbose || fal_ != DEF_FAL) {
            info.add(FAL, fal_);
        }
        if (verbose || amicompare_ != DEF_AMICOMPARE) {
            info.add(AMICOMPARE, amicompare_);
        }
        if (verbose || tsig_ != DEF_TSIG) {
            info.add(TSIG, tsig_);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
            Boolean enabled = info.get(ENABLED, Boolean.class);
            if (enabled != null) {
                enabled_ = enabled;
            }
            Double pcr = info.get(PCR, Double.class);
            if (pcr != null) {
                pcr_ = pcr;
            }
            Double ub1 = info.get(UB1, Double.class);
            if (ub1 != null) {
                ub1_ = ub1;
            }
            Double ub2 = info.get(UB2, Double.class);
            if (ub2 != null) {
                ub2_ = ub2;
            }
            Double cancel = info.get(CANCEL, Double.class);
            if (cancel != null) {
                cancel_ = cancel;
            }
            Double pc = info.get(PC, Double.class);
            if (pc != null) {
                pc_ = pc;
            }
            Double tsig = info.get(TSIG, Double.class);
            if (tsig != null) {
                tsig_ = tsig;
            }
            Boolean ami = info.get(AMICOMPARE, Boolean.class);
            if (ami != null) {
                amicompare_ = ami;
            }
            Boolean fal = info.get(FAL, Boolean.class);
            if (fal != null) {
                fal_ = fal;
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
