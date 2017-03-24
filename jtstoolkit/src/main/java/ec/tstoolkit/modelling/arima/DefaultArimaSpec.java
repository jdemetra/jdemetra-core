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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultArimaSpec implements Cloneable, InformationSetSerializable {

    public static final String MEAN = "mean", MU="mu",
            THETA = "theta", D = "d", PHI = "phi",
            BTHETA = "btheta", BD = "bd", BPHI = "bphi";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, MEAN), Boolean.class);
        dic.put(InformationSet.item(prefix, MU), Parameter.class);
        dic.put(InformationSet.item(prefix, D), Integer.class);
        dic.put(InformationSet.item(prefix, BD), Integer.class);
        dic.put(InformationSet.item(prefix, THETA), Parameter[].class);
        dic.put(InformationSet.item(prefix, PHI), Parameter[].class);
        dic.put(InformationSet.item(prefix, BTHETA), Parameter[].class);
        dic.put(InformationSet.item(prefix, BPHI), Parameter[].class);
    }

    private Parameter mu;
    private int d_, bd_;
    private Parameter[] phi_, theta_, bphi_, btheta_;

    @Override
    public DefaultArimaSpec clone() {
        try {
            DefaultArimaSpec spec = (DefaultArimaSpec) super.clone();
            spec.phi_ = Parameter.clone(phi_);
            spec.theta_ = Parameter.clone(theta_);
            spec.bphi_ = Parameter.clone(bphi_);
            spec.btheta_ = Parameter.clone(btheta_);
            return spec;
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
    
    public Parameter getMu(){
        return mu;
    }
    
    public void setMu(Parameter mu){
        this.mu=mu;
    }

    public void fixMu() {
        if (mu != null)
            mu.setType(ParameterType.Fixed);
        
    }

    public void setParameterType(ParameterType type) {
        if (phi_ != null) {
            for (int i = 0; i < phi_.length; ++i) {
                phi_[i].setType(type);
            }
        }
        if (bphi_ != null) {
            for (int i = 0; i < bphi_.length; ++i) {
                bphi_[i].setType(type);
            }
        }
        if (theta_ != null) {
            for (int i = 0; i < theta_.length; ++i) {
                theta_[i].setType(type);
            }
        }
        if (btheta_ != null) {
            for (int i = 0; i < btheta_.length; ++i) {
                btheta_[i].setType(type);
            }
        }

    }

    public void setMAParameterType(ParameterType type) {
        if (theta_ != null) {
            for (int i = 0; i < theta_.length; ++i) {
                theta_[i].setType(type);
            }
        }
        if (btheta_ != null) {
            for (int i = 0; i < btheta_.length; ++i) {
                btheta_[i].setType(type);
            }
        }
    }
    
    public void setARParameterType(ParameterType type) {
        if (phi_ != null) {
            for (int i = 0; i < phi_.length; ++i) {
                phi_[i].setType(type);
            }
        }
        if (bphi_ != null) {
            for (int i = 0; i < bphi_.length; ++i) {
                bphi_[i].setType(type);
            }
        }
    }
    
    public void clearParameters() {
        if (phi_ != null) {
            for (int i = 0; i < phi_.length; ++i) {
                phi_[i] = new Parameter();
            }
        }
        if (bphi_ != null) {
            for (int i = 0; i < bphi_.length; ++i) {
                bphi_[i] = new Parameter();
            }
        }
        if (theta_ != null) {
            for (int i = 0; i < theta_.length; ++i) {
                theta_[i] = new Parameter();
            }
        }
        if (btheta_ != null) {
            for (int i = 0; i < btheta_.length; ++i) {
                btheta_[i] = new Parameter();
            }
        }

    }

    public void clearFreeParameters() {
        if (phi_ != null) {
            for (int i = 0; i < phi_.length; ++i) {
                if (!phi_[i].isFixed()) {
                    phi_[i] = new Parameter();
                }
            }
        }
        if (bphi_ != null) {
            for (int i = 0; i < bphi_.length; ++i) {
                if (!bphi_[i].isFixed()) {
                    bphi_[i] = new Parameter();
                }
            }
        }
        if (theta_ != null) {
            for (int i = 0; i < theta_.length; ++i) {
                if (!theta_[i].isFixed()) {
                    theta_[i] = new Parameter();
                }
            }
        }
        if (btheta_ != null) {
            for (int i = 0; i < btheta_.length; ++i) {
                if (!btheta_[i].isFixed()) {
                    btheta_[i] = new Parameter();
                }
            }
        }
    }

    public boolean hasParameters() {
        return !Parameter.isDefault(phi_) || !Parameter.isDefault(theta_)
                || !Parameter.isDefault(bphi_) || !Parameter.isDefault(btheta_);
    }

    public boolean hasFreeParameters() {
        return Parameter.hasFreeParameters(phi_) || Parameter.hasFreeParameters(theta_)
                || Parameter.hasFreeParameters(bphi_) || Parameter.hasFreeParameters(btheta_);
    }
    
    public boolean hasFixedParameters() {
        return Parameter.hasFixedParameters(phi_) || Parameter.hasFixedParameters(theta_)
                || Parameter.hasFixedParameters(bphi_) || Parameter.hasFixedParameters(btheta_);
    }
    
    public void setArimaComponent(SarimaComponent aspec) {
        Parameter m = aspec.getMu();
        mu =m==null ? null : m.clone();
        setP(aspec.getP());
        setD(aspec.getD());
        setQ(aspec.getQ());
        setBP(aspec.getBP());
        setBD(aspec.getBD());
        setBQ(aspec.getBQ());
        updateParameters(aspec);
    }

    private void updateParameters(SarimaComponent aspec) {
        updateParameters(phi_, aspec.getPhi());
        updateParameters(theta_, aspec.getTheta());
        updateParameters(bphi_, aspec.getBPhi());
        updateParameters(btheta_, aspec.getBTheta());
    }

    private void updateParameters(Parameter[] target, Parameter[] source) {
        if (target == null || source == null || target.length != source.length) {
            return;
        }
        for (int i = 0; i < target.length; ++i) {
            if ((target[i] == null || !target[i].isFixed()) && source[i] != null) {
                target[i] = source[i].clone();
            }
        }
    }

    public void airline() {
        setP(0);
        d_ = 1;
        setQ(1);
        setBP(0);
        bd_ = 1;
        setBQ(1);
        mu = null;
    }

    public void airlineWithMean() {
        setP(0);
        d_ = 1;
        setQ(1);
        setBP(0);
        bd_ = 1;
        setBQ(1);
        mu = new Parameter();
    }

    public int getP() {
        return phi_ == null ? 0 : phi_.length;
    }

    public void setP(int value) {
        phi_ = Parameter.create(value);
    }

    public int getD() {
        return d_;
    }

    public void setD(int value) {
        d_ = value;
    }

    public int getQ() {
        return theta_ == null ? 0 : theta_.length;
    }

    public void setQ(int value) {
        theta_ = Parameter.create(value);
    }

    public int getBP() {
        return bphi_ == null ? 0 : bphi_.length;
    }

    public void setBP(int value) {
        bphi_ = Parameter.create(value);
    }

    public int getBD() {
        return bd_;
    }

    public void setBD(int value) {
        bd_ = value;
    }

    public int getBQ() {
        return btheta_ == null ? 0 : btheta_.length;
    }

    public void setBQ(int value) {
        btheta_ = Parameter.create(value);
    }

    public boolean isAirline() {
        return getP() == 0 && d_ == 1 && getQ() == 1
                && getBP() == 0 && bd_ == 1 && getBQ() == 1;
    }

    public boolean isDefault() {
        return (mu == null) && isAirline()
                && Parameter.isDefault(phi_) && Parameter.isDefault(theta_)
                && Parameter.isDefault(bphi_) && Parameter.isDefault(btheta_);
    }

    public Parameter[] getPhi() {
        return phi_;
    }

    public void setPhi(Parameter[] value) {
        phi_ = value;
    }

    public Parameter[] getTheta() {
        return theta_;
    }

    public void setTheta(Parameter[] value) {
        theta_ = value;
    }

    public Parameter[] getBPhi() {
        return bphi_;
    }

    public void setBPhi(Parameter[] value) {
        bphi_ = value;
    }

    public Parameter[] getBTheta() {
        return btheta_;
    }

    public void setBTheta(Parameter[] value) {
        btheta_ = value;
    }
    
    public SarimaSpecification getSpecification(int freq){
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setP(getP());
        spec.setD(d_);
        spec.setQ(getQ());
        if (freq > 1) {
            spec.setBP(getBP());
            spec.setBD(bd_);
            spec.setBQ(getBQ());
        }
        return spec;
    }
    
    public SarimaModel getArima(int freq) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setP(getP());
        spec.setD(d_);
        spec.setQ(getQ());
        if (freq > 1) {
            spec.setBP(getBP());
            spec.setBD(bd_);
            spec.setBQ(getBQ());
        }

        SarimaModel sarima = new SarimaModel(spec);
        Parameter[] p = phi_;
        if (p != null) {
            for (int i = 0; i < p.length; ++i) {
                if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                    sarima.setPhi(i + 1, p[i].getValue());
                }
            }
        }
        p = theta_;
        if (p != null) {
            for (int i = 0; i < p.length; ++i) {
                if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                    sarima.setTheta(i + 1, p[i].getValue());
                }
            }
        }
        p = bphi_;
        if (freq > 1 && p != null) {
            for (int i = 0; i < p.length; ++i) {
                if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                    sarima.setBPhi(i + 1, p[i].getValue());
                }
            }
        }
        p = btheta_;
        if (freq > 1 && p != null) {
            for (int i = 0; i < p.length; ++i) {
                if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                    sarima.setBTheta(i + 1, p[i].getValue());
                }
            }
        }
        return sarima;
    }

    public void setArima(SarimaModel value) {
        SarimaSpecification spec = value.getSpecification();
        setP(spec.getP());
        d_ = spec.getD();
        setQ(spec.getQ());
        setBP(spec.getBP());
        bd_ = spec.getBD();
        setBQ(spec.getBQ());
        Parameter[] p = phi_;
        if (p != null) {
            for (int i = 0; i < p.length; ++i) {
                p[i] = new Parameter(value.phi(i + 1), ParameterType.Estimated);
            }
        }
        p = theta_;
        if (p != null) {
            for (int i = 0; i < p.length; ++i) {
                p[i] = new Parameter(value.theta(i + 1), ParameterType.Estimated);
            }
        }
        p = bphi_;
        if (p != null) {
            for (int i = 0; i < p.length; ++i) {
                p[i] = new Parameter(value.bphi(i + 1), ParameterType.Estimated);
            }
        }
        p = btheta_;
        if (p != null) {
            for (int i = 0; i < p.length; ++i) {
                p[i] = new Parameter(value.btheta(i + 1), ParameterType.Estimated);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DefaultArimaSpec && equals((DefaultArimaSpec) obj));
    }
    
    private boolean equals(DefaultArimaSpec other) {
        return bd_ == other.bd_ && d_ == other.d_ && Objects.deepEquals(mu, other.mu)
                && Arrays.deepEquals(phi_, other.phi_) && Arrays.deepEquals(theta_, other.theta_)
                && Arrays.deepEquals(bphi_, other.bphi_) && Arrays.deepEquals(btheta_, other.btheta_);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash;
        hash = 53 * hash + this.d_;
        hash = 53 * hash + this.bd_;
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (mu != null) {
            info.add(MU, mu);
        }
        if (getP() != 0) {
            info.add(PHI, phi_);
        }
        if (verbose || d_ != 1) {
            info.add(D, d_);
        }
        if (getQ() != 0) {
            info.add(THETA, theta_);
        }
        if (getBP() != 0) {
            info.add(BPHI, bphi_);
        }
        if (verbose || bd_ != 1) {
            info.add(BD, bd_);
        }
        if (getBQ() != 0) {
            info.add(BTHETA, btheta_);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            // default values
            airline();
            Boolean mean = info.get(MEAN, Boolean.class);
            if (mean != null) {
                mu=new Parameter();
            }
            Parameter m = info.get(MU, Parameter.class);
            if (m != null) {
                mu=m;
            }
            Integer d = info.get(D, Integer.class);
            if (d != null) {
                d_ = d;
            }
            Integer bd = info.get(BD, Integer.class);
            if (bd != null) {
                bd_ = bd;
            }
            setPhi(info.get(PHI, Parameter[].class));
            setTheta(info.get(THETA, Parameter[].class));
            setBPhi(info.get(BPHI, Parameter[].class));
            setBTheta(info.get(BTHETA, Parameter[].class));
            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
