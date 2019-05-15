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
import demetra.arima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Data
public final class SarimaSpec implements Cloneable{
    
    public static interface Validator{
        void checkP(int p);
        void checkD(int d);
        void checkQ(int q);
        void checkBp(int bp);
        void checkBd(int bd);
        void checkBq(int bq);
    }

    private final Validator validator;
    private Parameter mu;
    private int d, bd;
    private Parameter[] phi, theta, bphi, btheta;
    
    public SarimaSpec(Validator validator){
        this.validator=validator;
    }

    @Override
    public SarimaSpec clone(){
        try {
            SarimaSpec c=(SarimaSpec) super.clone();
            c.phi = Parameter.clone(phi);
            c.theta = Parameter.clone(theta);
            c.bphi = Parameter.clone(bphi);
            c.btheta = Parameter.clone(btheta);
            if (mu != null)
                c.mu=mu.clone();
            return c;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean isMean() {
        return mu != null;
    }

    public void setMean(boolean mean) {
        mu = mean ? new Parameter() : null;
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
        if (validator != null)
            validator.checkP(value);
        phi = Parameter.create(value);
    }

    public void setD(int value) {
        if (validator != null)
            validator.checkD(value);
        d = value;
    }

    public int getQ() {
        return theta == null ? 0 : theta.length;
    }

    public void setQ(int value) {
        if (validator != null)
            validator.checkQ(value);
        theta = Parameter.create(value);
    }

    public int getBp() {
        return bphi == null ? 0 : bphi.length;
    }

    public void setBp(int value) {
        if (validator != null)
            validator.checkBp(value);
        bphi = Parameter.create(value);
    }

    public void setBd(int value) {
        if (validator != null)
            validator.checkBd(value);
        bd = value;
    }

    public int getBq() {
        return btheta == null ? 0 : btheta.length;
    }

    public void setBq(int value) {
        if (validator != null)
            validator.checkBq(value);
        btheta = Parameter.create(value);
    }

    public void setPhi(Parameter[] value) {
        if (validator != null && value != null)
            validator.checkP(value.length);
        phi = value;
    }

    public void setTheta(Parameter[] value) {
        if (validator != null && value != null)
            validator.checkQ(value.length);
        theta = value;
    }

    public void setBphi(Parameter[] value) {
        if (validator != null && value != null)
            validator.checkBp(value.length);
        phi = value;
    }

    public void setBtheta(Parameter[] value) {
        if (validator != null && value != null)
            validator.checkBq(value.length);
        theta = value;
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

}
