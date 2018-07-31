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
package demetra.regarima;

import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.design.Development;
import demetra.sarima.SarimaSpecification;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public abstract class DefaultArimaSpec {

    private Parameter mu;
    private int d, bd;
    private Parameter[] phi, theta, bphi, btheta;
    
    protected DefaultArimaSpec(){
        
    }

    protected DefaultArimaSpec(DefaultArimaSpec spec) {
        d = spec.d;
        bd = spec.bd;
        phi = Parameter.clone(spec.phi);
        theta = Parameter.clone(spec.theta);
        bphi = Parameter.clone(spec.bphi);
        btheta = Parameter.clone(spec.btheta);
    }

    public boolean isMean() {
        return mu != null;
    }

    public void setMean(boolean mean) {
        mu = mean ? new Parameter() : null;
    }

    public Parameter getMu() {
        return mu;
    }

    public void setMu(Parameter mu) {
        this.mu = mu;
    }

    public void fixMu() {
        if (mu != null) {
            mu.setType(ParameterType.Fixed);
        }

    }

    public void setParameterType(ParameterType type) {
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i) {
                phi[i].setType(type);
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                bphi[i].setType(type);
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i) {
                theta[i].setType(type);
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i) {
                btheta[i].setType(type);
            }
        }

    }

    public void setMAParameterType(ParameterType type) {
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i) {
                theta[i].setType(type);
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i) {
                btheta[i].setType(type);
            }
        }
    }

    public void setARParameterType(ParameterType type) {
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i) {
                phi[i].setType(type);
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                bphi[i].setType(type);
            }
        }
    }

    public void clearParameters() {
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i) {
                phi[i] = new Parameter();
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                bphi[i] = new Parameter();
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i) {
                theta[i] = new Parameter();
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i) {
                btheta[i] = new Parameter();
            }
        }

    }

    public void clearFreeParameters() {
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i) {
                if (!phi[i].isFixed()) {
                    phi[i] = new Parameter();
                }
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                if (!bphi[i].isFixed()) {
                    bphi[i] = new Parameter();
                }
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i) {
                if (!theta[i].isFixed()) {
                    theta[i] = new Parameter();
                }
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i) {
                if (!btheta[i].isFixed()) {
                    btheta[i] = new Parameter();
                }
            }
        }
    }

    public boolean hasParameters() {
        return !Parameter.isDefault(phi) || !Parameter.isDefault(theta)
                || !Parameter.isDefault(bphi) || !Parameter.isDefault(btheta);
    }

    public boolean hasFreeParameters() {
        return Parameter.hasFreeParameters(phi) || Parameter.hasFreeParameters(theta)
                || Parameter.hasFreeParameters(bphi) || Parameter.hasFreeParameters(btheta);
    }

    public boolean hasFixedParameters() {
        return Parameter.hasFixedParameters(phi) || Parameter.hasFixedParameters(theta)
                || Parameter.hasFixedParameters(bphi) || Parameter.hasFixedParameters(btheta);
    }

     public void airline() {
        setP(0);
        d = 1;
        setQ(1);
        setBp(0);
        bd = 1;
        setBq(1);
        mu = null;
    }

    public void airlineWithMean() {
        setP(0);
        d = 1;
        setQ(1);
        setBp(0);
        bd = 1;
        setBq(1);
        mu = new Parameter();
    }

    public int getP() {
        return phi == null ? 0 : phi.length;
    }

    public void setP(int value) {
        phi = Parameter.create(value);
    }

    public int getD() {
        return d;
    }

    public void setD(int value) {
        d = value;
    }

    public int getQ() {
        return theta == null ? 0 : theta.length;
    }

    public void setQ(int value) {
        theta = Parameter.create(value);
    }

    public int getBp() {
        return bphi == null ? 0 : bphi.length;
    }

    public void setBp(int value) {
        bphi = Parameter.create(value);
    }

    public int getBd() {
        return bd;
    }

    public void setBd(int value) {
        bd = value;
    }

    public int getBq() {
        return btheta == null ? 0 : btheta.length;
    }

    public void setBq(int value) {
        btheta = Parameter.create(value);
    }

    public boolean isAirline() {
        return getP() == 0 && d == 1 && getQ() == 1
                && getBp() == 0 && bd == 1 && getBq() == 1;
    }

    public boolean isDefault() {
        return (mu == null) && isAirline()
                && Parameter.isDefault(phi) && Parameter.isDefault(theta)
                && Parameter.isDefault(bphi) && Parameter.isDefault(btheta);
    }

    public Parameter[] getPhi() {
        return phi;
    }

    public void setPhi(Parameter[] value) {
        phi = value;
    }

    public Parameter[] getTheta() {
        return theta;
    }

    public void setTheta(Parameter[] value) {
        theta = value;
    }

    public Parameter[] getBPhi() {
        return bphi;
    }

    public void setBPhi(Parameter[] value) {
        bphi = value;
    }

    public Parameter[] getBTheta() {
        return btheta;
    }

    public void setBTheta(Parameter[] value) {
        btheta = value;
    }

    public SarimaSpecification getSpecification(int period) {
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.setP(getP());
        spec.setD(d);
        spec.setQ(getQ());
        if (period > 1) {
            spec.setBp(getBq());
            spec.setBd(bd);
            spec.setBq(getBq());
        }
        return spec;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DefaultArimaSpec && equals((DefaultArimaSpec) obj));
    }

    private boolean equals(DefaultArimaSpec other) {
        return bd == other.bd && d == other.d && Objects.equals(mu, other.mu)
                && Arrays.deepEquals(phi, other.phi) && Arrays.deepEquals(theta, other.theta)
                && Arrays.deepEquals(bphi, other.bphi) && Arrays.deepEquals(btheta, other.btheta);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash;
        hash = 53 * hash + this.d;
        hash = 53 * hash + this.bd;
        return hash;
    }

}
