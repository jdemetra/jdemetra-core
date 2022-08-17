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
package demetra.stl;

import java.util.function.DoubleUnaryOperator;


/**
 *
 * @author Jean Palate
 */
public class StlLegacySpec {

    public static StlLegacySpec defaultSpec(int np, int swindow, boolean robust) {
        StlLegacySpec spec = new StlLegacySpec(np);
        if (robust) {
            spec.no = 15;
            spec.ni = 1;
        } else {
            spec.no = 0;
            spec.ni = 2;
        }
        spec.setDefault(swindow);
        return spec;
    }

    public StlLegacySpec(int np) {
        if (np == 1) {
            throw new IllegalArgumentException("np should be greater than 1");
        }
        this.np = np;
        nl = nextodd(np);
    }

    public void setDefault(int swindow) {
        if (swindow == 1) {
            throw new IllegalArgumentException("swindow should be greater than 2");
        }
        ns = nextodd(swindow);
        nt = nextodd((int) Math.ceil((1.5 * np) / (1 - 1.5 / ns)));
        setDefaultJumps();
    }

    public void setDefaultJumps() {
        nsjump = (int) Math.ceil( .1 * ns);
        ntjump = (int) Math.ceil( .1 * nt);
        nljump = (int) Math.ceil(.1 * nl);
    }

    public static int nextodd(int i) {
        return i % 2 == 1 ? i : (i + 1);
    }

    private boolean multiplicative;

    protected final int np;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int ns = 7;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int nt = 13;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int nl = 13;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int sdeg = 0;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int tdeg = 1;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int ldeg = 1;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int nsjump = 1;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int ntjump = 1;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int nljump = 1;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int ni = 2;
    /**
     * number of inner and outer (robust) iterations
     */
    protected int no = 3;

    protected double wthreshold = 0.001;

    protected DoubleUnaryOperator wfn = x -> {
        double t = 1 - x * x;
        return t * t;
    };

    protected DoubleUnaryOperator loessfn = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };

    /**
     * @return the ns
     */
    public int getNs() {
        return ns;
    }

    /**
     * @param ns the ns to set
     */
    public void setNs(int ns) {
        if (ns < 3 || ns % 2 == 0) {
            throw new IllegalArgumentException("STL");
        }
        this.ns = ns;
    }

    /**
     * @return the nt
     */
    public int getNt() {
        return nt;
    }

    /**
     * @param nt the nt to set
     */
    public void setNt(int nt) {
        if (nt < 3 || nt % 2 == 0) {
            throw new IllegalArgumentException("STL");
        }
        this.nt = nt;
    }

    /**
     * @return the nl
     */
    public int getNl() {
        return nl;
    }

    /**
     * @param nl the nl to set
     */
    public void setNl(int nl) {
        if (nl < 3 || nl % 2 == 0) {
            throw new IllegalArgumentException("STL");
        }
        this.nl = nl;
    }

    /**
     * @return the sdeg
     */
    public int getSdeg() {
        return sdeg;
    }

    /**
     * @param deg the sdeg to set
     */
    public void setSdeg(int deg) {
        if (deg < 0 || deg > 1) {
            throw new IllegalArgumentException("STL");
        }
        sdeg = deg;
    }

    /**
     * @return the tdeg
     */
    public int getTdeg() {
        return tdeg;
    }

    /**
     * @param deg the tdeg to set
     */
    public void setTdeg(int deg) {
        if (deg < 0 || deg > 1) {
            throw new IllegalArgumentException("STL");
        }
        this.tdeg = deg;
    }

    /**
     * @return the ldeg
     */
    public int getLdeg() {
        return ldeg;
    }

    /**
     * @param deg the deg to set
     */
    public void setLdeg(int deg) {
        if (deg < 0 || deg > 1) {
            throw new IllegalArgumentException("STL");
        }
        this.ldeg = deg;
    }

    /**
     * @return the nsjump
     */
    public int getNsjump() {
        return nsjump;
    }

    /**
     * @param njump the njump to set
     */
    public void setNsjump(int njump) {
        if (njump < 1) {
            throw new IllegalArgumentException("STL");
        }
        this.nsjump = njump;
    }

    /**
     * @return the ntjump
     */
    public int getNtjump() {
        return ntjump;
    }

    /**
     * @param njump the njump to set
     */
    public void setNtjump(int njump) {
        if (njump < 1) {
            throw new IllegalArgumentException("STL");
        }
        this.ntjump = njump;
    }

    /**
     * @return the nljump
     */
    public int getNljump() {
        return nljump;
    }

    /**
     * @param njump the njump to set
     */
    public void setNljump(int njump) {
        if (njump < 1) {
            throw new IllegalArgumentException("STL");
        }
        this.nljump = njump;
    }

    /**
     * @return the ni
     */
    public int getNi() {
        return ni;
    }

    /**
     * @param ni the ni to set
     */
    public void setNi(int ni) {
        if (ni < 1) {
            throw new IllegalArgumentException("STL");
        }
        this.ni = ni;
    }

    /**
     * @return the no
     */
    public int getNo() {
        return no;
    }

    /**
     * @param no the no to set
     */
    public void setNo(int no) {
        if (no < 0) {
            throw new IllegalArgumentException("STL");
        }
        this.no = no;
    }

    /**
     * @return the np
     */
    public int getNp() {
        return np;
    }

    /**
     * @return the wthreshold
     */
    public double getWthreshold() {
        return wthreshold;
    }

    /**
     * @param wthreshold the wthreshold to set
     */
    public void setWthreshold(double wthreshold) {
        if (wthreshold < 0 || wthreshold > .2) {
            throw new IllegalArgumentException("STL");
        }
        this.wthreshold = wthreshold;
    }

    /**
     * @return the wfn
     */
    public DoubleUnaryOperator getWfn() {
        return wfn;
    }

    /**
     * @return the wfn
     */
    public DoubleUnaryOperator getLoessfn() {
        return loessfn;
    }
    /**
     * @param wfn the wfn to set
     */
    public void setWfn(DoubleUnaryOperator wfn) {
        this.wfn = wfn;
    }

    /**
     * @return the multiplicative
     */
    public boolean isMultiplicative() {
        return multiplicative;
    }

    /**
     * @param multiplicative the multiplicative to set
     */
    public void setMultiplicative(boolean multiplicative) {
        this.multiplicative = multiplicative;
    }
}
