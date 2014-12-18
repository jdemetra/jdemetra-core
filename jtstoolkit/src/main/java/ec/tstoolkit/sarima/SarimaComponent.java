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

package ec.tstoolkit.sarima;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.arima.AbstractArimaComponent;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaComponent extends AbstractArimaComponent implements Cloneable {

    private int m_D, m_BD, m_S;
    private Parameter[] m_phi, m_theta, m_bphi, m_btheta;

    public SarimaComponent() {
    }

    public SarimaComponent(int freq) {
        m_S=freq;
    }

    @Override
    public SarimaComponent clone() {
        try {
            SarimaComponent cmp = (SarimaComponent) super.clone();
            cmp.m_phi = Parameter.clone(m_phi);
            cmp.m_bphi = Parameter.clone(m_bphi);
            cmp.m_theta = Parameter.clone(m_theta);
            cmp.m_btheta = Parameter.clone(m_btheta);
            return cmp;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    public void setParameterType(ParameterType type) {
        if (m_phi != null) {
            for (int i = 0; i < m_phi.length; ++i) {
                m_phi[i].setType(type);
            }
        }
        if (m_bphi != null) {
            for (int i = 0; i < m_bphi.length; ++i) {
                m_bphi[i].setType(type);
            }
        }
        if (m_theta != null) {
            for (int i = 0; i < m_theta.length; ++i) {
                m_theta[i].setType(type);
            }
        }
        if (m_btheta != null) {
            for (int i = 0; i < m_btheta.length; ++i) {
                m_btheta[i].setType(type);
            }
        }

    }

    public void clearParameters() {
        if (m_phi != null) {
            for (int i = 0; i < m_phi.length; ++i) {
                m_phi[i] = new Parameter();
            }
        }
        if (m_bphi != null) {
            for (int i = 0; i < m_bphi.length; ++i) {
                m_bphi[i] = new Parameter();
            }
        }
        if (m_theta != null) {
            for (int i = 0; i < m_theta.length; ++i) {
                m_theta[i] = new Parameter();
            }
        }
        if (m_btheta != null) {
            for (int i = 0; i < m_btheta.length; ++i) {
                m_btheta[i] = new Parameter();
            }
        }

    }

    public void clearFreeParameters() {
        if (m_phi != null) {
            for (int i = 0; i < m_phi.length; ++i) {
                if (m_phi[i] == null || !m_phi[i].isFixed()) {
                    m_phi[i] = new Parameter();
                }
            }
        }
        if (m_bphi != null) {
            for (int i = 0; i < m_bphi.length; ++i) {
                if (m_bphi[i] == null || !m_bphi[i].isFixed()) {
                    m_bphi[i] = new Parameter();
                }
            }
        }
        if (m_theta != null) {
            for (int i = 0; i < m_theta.length; ++i) {
                if (m_theta[i] == null || !m_theta[i].isFixed()) {
                    m_theta[i] = new Parameter();
                }
            }
        }
        if (m_btheta != null) {
            for (int i = 0; i < m_btheta.length; ++i) {
                if (m_btheta[i] == null || !m_btheta[i].isFixed()) {
                    m_btheta[i] = new Parameter();
                }
            }
        }
    }

    public void updateParameters(SarimaComponent aspec) {
        updateParameters(m_phi, aspec.m_phi);
        updateParameters(m_theta, aspec.m_theta);
        updateParameters(m_bphi, aspec.m_bphi);
        updateParameters(m_btheta, aspec.m_btheta);
    }

    private void updateParameters(Parameter[] target, Parameter[] source) {
        if (target == null || source == null || target.length != source.length) {
            return;
        }
        for (int i = 0; i < target.length; ++i) {
            if ((target[i] == null || target[i].getType() != ParameterType.Fixed)
                    && source[i] != null) {
                target[i] = source[i].clone();
            }
        }
    }

    public void airline(int freq) {
        m_S = freq;
        setP(0);
        m_D = 1;
        setQ(1);
        setBP(0);
        m_BD = 1;
        setBQ(1);
        setMean(false);
    }

    public void airlineWithMean(int freq) {
        m_S = freq;
        setP(0);
        m_D = 1;
        setQ(1);
        setBP(0);
        m_BD = 1;
        setBQ(1);
        setMean(true);
    }

    public int getP() {
        return m_phi == null ? 0 : m_phi.length;
    }

    public void setP(int value) {
        m_phi = Parameter.create(value);
    }

    public int getD() {
        return m_D;
    }

    public void setD(int value) {
        m_D = value;
    }

    public int getQ() {
        return m_theta == null ? 0 : m_theta.length;
    }

    public void setQ(int value) {
        m_theta = Parameter.create(value);
    }

    /// <summary>Seasonal frequency</summary>
    /// <remarks>
    /// Setting the frequency to 1 (annual model) removes all the seasonal part of the
    /// model. So, you should change the S property before initializing the seasonal part of
    /// the object.
    /// </remarks>
    public int getS() {
        return m_S;
    }

    public void setS(int value) {
        m_S = value;
        if (m_S == 1) {
            m_BD = 0;
            m_bphi = null;
            m_btheta = null;
        }
    }

    public int getBP() {
        return m_bphi == null ? 0 : m_bphi.length;
    }

    public void setBP(int value) {
        m_bphi = Parameter.create(value);
    }

    public int getBD() {
        return m_BD;
    }

    public void setBD(int value) {
        m_BD = value;
    }

    public int getBQ() {
        return m_btheta == null ? 0 : m_btheta.length;
    }

    public void setBQ(int value) {
        m_btheta = Parameter.create(value);
    }

    public Parameter[] getPhi() {
        return m_phi;
    }

    public void setPhi(Parameter[] value) {
        m_phi = Parameter.clone(value);
    }

    public Parameter[] getTheta() {
        return m_theta;
    }

    public void setTheta(Parameter[] value) {
        m_theta = Parameter.clone(value);
    }

    public Parameter[] getBPhi() {
        return m_bphi;
    }

    public void setBPhi(Parameter[] value) {
        m_bphi = Parameter.clone(value);
    }

    public Parameter[] getBTheta() {
        return m_btheta;
    }

    public void setBTheta(Parameter[] value) {
        m_btheta = Parameter.clone(value);
    }

    public int getDifferencingOrder() {
        return m_D + m_BD * m_S;
    }

    public BackFilter getDifferencingFilter() {
        UnitRoots ur = new UnitRoots();
        if (m_S > 1) {
            for (int i = 0; i < m_BD; ++i) {
                ur.add(m_S);
            }
        }
        for (int i = 0; i < m_D; ++i) {
            ur.add(1);
        }
        return new BackFilter(ur.toPolynomial());
    }

    public int getFrequency(){
        return m_S;
    }

    public void setFrequency(int freq){
        m_S=freq;
        if (freq == 1){
            m_BD=0;
            m_bphi=null;
            m_btheta=null;
        }
    }

    @Override
    public SarimaModel getModel() {
        SarimaSpecification spec = new SarimaSpecification(m_S);
        spec.setP(getP());
        spec.setD(m_D);
        spec.setQ(getQ());
        spec.setBP(getBP());
        spec.setBD(m_BD);
        spec.setBQ(getBQ());

        SarimaModel sarima = new SarimaModel(spec);
        Parameter[] p = m_phi;
        for (int i = 0; i < spec.getP(); ++i) {
            if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                sarima.setPhi(i + 1, p[i].getValue());
            }
        }
        p = m_theta;
        for (int i = 0; i < spec.getQ(); ++i) {
            if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                sarima.setTheta(i + 1, p[i].getValue());
            }
        }
        p = m_bphi;
        for (int i = 0; i < spec.getBP(); ++i) {
            if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                sarima.setBPhi(i + 1, p[i].getValue());
            }
        }
        p = m_btheta;
        for (int i = 0; i < spec.getBQ(); ++i) {
            if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                sarima.setBTheta(i + 1, p[i].getValue());
            }
        }
        return sarima;
    }

    public SarimaSpecification getSpecification() {
        SarimaSpecification spec = new SarimaSpecification(m_S);
        spec.setP(getP());
        spec.setD(m_D);
        spec.setQ(getQ());
        spec.setBP(getBP());
        spec.setBD(m_BD);
        spec.setBQ(getBQ());

        return spec;
    }

    public boolean isDefined() {
        return Parameter.isDefined(m_phi) && Parameter.isDefined(m_theta)
                && Parameter.isDefined(m_bphi) && Parameter.isDefined(m_btheta);
    }

    public boolean isUndefined() {
        return Parameter.isDefault(m_phi) && Parameter.isDefault(m_theta)
                && Parameter.isDefault(m_bphi) && Parameter.isDefault(m_btheta);
    }

    public void setSpecification(SarimaSpecification spec) {
        setP(spec.getP());
        m_D = spec.getD();
        setQ(spec.getQ());
        setBP(spec.getBP());
        m_BD = spec.getBD();
        setBQ(spec.getBQ());
        m_S = spec.getFrequency();
    }

    @Override
    public void setModel(IArimaModel value) {
        setModel((SarimaModel) value);
    }

    public void setModel(SarimaModel value) {
        SarimaSpecification spec = value.getSpecification();
        setP(spec.getP());
        m_D = spec.getD();
        setQ(spec.getQ());
        setBP(spec.getBP());
        m_BD = spec.getBD();
        setBQ(spec.getBQ());
        m_S = spec.getFrequency();
        Parameter[] p = m_phi;
        for (int i = 0; i < spec.getP(); ++i) {
            p[i] = new Parameter(value.phi(i + 1), ParameterType.Estimated);
        }
        p = m_theta;
        for (int i = 0; i < spec.getQ(); ++i) {
            p[i] = new Parameter(value.theta(i + 1), ParameterType.Estimated);
        }
        p = m_bphi;
        for (int i = 0; i < spec.getBP(); ++i) {
            p[i] = new Parameter(value.bphi(i + 1), ParameterType.Estimated);
        }
        p = m_btheta;
        for (int i = 0; i < spec.getBQ(); ++i) {
            p[i] = new Parameter(value.btheta(i + 1), ParameterType.Estimated);
        }
    }

    public int getParametersCount() {
        return getP() + getBP() + getQ() + getBQ();
    }

    public int getFreeParametersCount() {
        int n = Parameter.countFreeParameters(m_phi);
        n += Parameter.countFreeParameters(m_bphi);
        n += Parameter.countFreeParameters(m_theta);
        n += Parameter.countFreeParameters(m_btheta);
        return n;
    }

    public int getFixedParametersCount() {
        int n = Parameter.countFixedParameters(m_phi);
        n += Parameter.countFixedParameters(m_bphi);
        n += Parameter.countFixedParameters(m_theta);
        n += Parameter.countFixedParameters(m_btheta);
        return n;
    }

    /**
     * Returns the p constraints, using the order defined in Tramo:
     * regular AR, seasonal AR, regular MA, seasonal MA
     * @return
     */
    @NewObject
    public boolean[] getFixedConstraints() {
        int n = getParametersCount();
        boolean[] fixed = new boolean[n];
        int j = 0;
        if (m_phi != null) {
            for (int i = 0; i < m_phi.length; ++i, ++j) {
                if (m_phi[i] != null && m_phi[i].getType() == ParameterType.Fixed) {
                    fixed[j] = true;
                }
            }
        }
        if (m_bphi != null) {
            for (int i = 0; i < m_bphi.length; ++i, ++j) {
                if (m_bphi[i] != null && m_bphi[i].getType() == ParameterType.Fixed) {
                    fixed[j] = true;
                }
            }
        }
        if (m_theta != null) {
            for (int i = 0; i < m_theta.length; ++i, ++j) {
                if (m_theta[i] != null && m_theta[i].getType() == ParameterType.Fixed) {
                    fixed[j] = true;
                }
            }
        }
        if (m_btheta != null) {
            for (int i = 0; i < m_btheta.length; ++i, ++j) {
                if (m_btheta[i] != null && m_btheta[i].getType() == ParameterType.Fixed) {
                    fixed[j] = true;
                }
            }
        }
        return fixed;
    }

    /**
     * Returns the parameters, using the order defined in Tramo:
     * regular AR, seasonal AR, regular MA, seasonal MA
     * @return
     */
    @NewObject
    public double[] getParameters() {
        int n = getParametersCount();
        double[] p = new double[n];
        int j = 0;
        if (m_phi != null) {
            for (int i = 0; i < m_phi.length; ++i, ++j) {
                if (m_phi[i] != null) {
                    p[j] = m_phi[i].getValue();
                }
            }
        }
        if (m_bphi != null) {
            for (int i = 0; i < m_bphi.length; ++i, ++j) {
                if (m_bphi[i] != null) {
                    p[j] = m_bphi[i].getValue();
                }
            }
        }
        if (m_theta != null) {
            for (int i = 0; i < m_theta.length; ++i, ++j) {
                if (m_theta[i] != null) {
                    p[j] = m_theta[i].getValue();
                }
            }
        }
        if (m_btheta != null) {
            for (int i = 0; i < m_btheta.length; ++i, ++j) {
                if (m_btheta[i] != null) {
                    p[j] = m_btheta[i].getValue();
                }
            }
        }
        return p;
    }

    public void setParameters(IReadDataBlock p, IReadDataBlock stde, ParameterType type) {
        int j = 0;
        if (m_phi != null) {
            for (int i = 0; i < m_phi.length; ++i, ++j) {
                if (m_phi[i] == null) {
                    m_phi[i] = new Parameter(p.get(j), type);
                } else if (!m_phi[i].isFixed()) {
                    m_phi[i].setValue(p.get(j));
                    m_phi[i].setType(type);
                }
                if (stde != null) {
                    m_phi[i].setStde(stde.get(j));
                }
            }
        }
        if (m_bphi != null) {
            for (int i = 0; i < m_bphi.length; ++i, ++j) {
                if (m_bphi[i] == null) {
                    m_bphi[i] = new Parameter(p.get(j), type);
                } else if (!m_bphi[i].isFixed()) {
                    m_bphi[i].setValue(p.get(j));
                    m_bphi[i].setType(type);
                }
                if (stde != null) {
                    m_bphi[i].setStde(stde.get(j));
                }
            }
        }
        if (m_theta != null) {
            for (int i = 0; i < m_theta.length; ++i, ++j) {
                if (m_theta[i] == null) {
                    m_theta[i] = new Parameter(p.get(j), type);
                } else if (!m_theta[i].isFixed()) {
                    m_theta[i].setValue(p.get(j));
                    m_theta[i].setType(type);
                }
                if (stde != null) {
                    m_theta[i].setStde(stde.get(j));
                }
            }
        }
        if (m_btheta != null) {
            for (int i = 0; i < m_btheta.length; ++i, ++j) {
                 if (m_btheta[i] == null) {
                    m_btheta[i] = new Parameter(p.get(j), type);
                } else if (!m_btheta[i].isFixed()) {
                    m_btheta[i].setValue(p.get(j));
                    m_btheta[i].setType(type);
                }
                if (stde != null) {
                    m_btheta[i].setStde(stde.get(j));
                 }
            }
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SarimaComponent && equals((SarimaComponent) obj));
    }
    
    private boolean equals(SarimaComponent other) {
        return other.m_BD == m_BD
                && other.m_D == m_D
                && other.m_S == m_S
                && Arrays.deepEquals(m_phi, m_phi)
                && Arrays.deepEquals(m_bphi, m_bphi)
                && Arrays.deepEquals(m_theta, m_theta)
                && Arrays.deepEquals(m_btheta, m_btheta);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.m_D;
        hash = 97 * hash + this.m_BD;
        hash = 97 * hash + this.m_S;
        hash = 97 * hash + Arrays.deepHashCode(this.m_phi);
        hash = 97 * hash + Arrays.deepHashCode(this.m_theta);
        hash = 97 * hash + Arrays.deepHashCode(this.m_bphi);
        hash = 97 * hash + Arrays.deepHashCode(this.m_btheta);
        return hash;
    }
}
