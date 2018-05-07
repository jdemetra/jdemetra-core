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
package demetra.regarima.ami;

import demetra.arima.IArimaModel;
import demetra.data.DoubleSequence;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.design.Development;
import demetra.design.NewObject;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.polynomials.UnitRoots;
import demetra.sarima.SarimaFixedMapping;
import demetra.sarima.SarimaMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaComponent extends AbstractArimaComponent implements Cloneable {

    private int d, bd, period;
    private Parameter[] phi, theta, bphi, btheta;

    public SarimaComponent() {
    }

    public SarimaComponent(int freq) {
        period = freq;
    }

    @Override
    public SarimaComponent clone() {
        try {
            SarimaComponent cmp = (SarimaComponent) super.clone();
            cmp.phi = Parameter.clone(phi);
            cmp.bphi = Parameter.clone(bphi);
            cmp.theta = Parameter.clone(theta);
            cmp.btheta = Parameter.clone(btheta);
            return cmp;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
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
                if (phi[i] == null || !phi[i].isFixed()) {
                    phi[i] = new Parameter();
                }
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                if (bphi[i] == null || !bphi[i].isFixed()) {
                    bphi[i] = new Parameter();
                }
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i) {
                if (theta[i] == null || !theta[i].isFixed()) {
                    theta[i] = new Parameter();
                }
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i) {
                if (btheta[i] == null || !btheta[i].isFixed()) {
                    btheta[i] = new Parameter();
                }
            }
        }
    }

    public void updateParameters(SarimaComponent aspec) {
        updateParameters(phi, aspec.phi);
        updateParameters(theta, aspec.theta);
        updateParameters(bphi, aspec.bphi);
        updateParameters(btheta, aspec.btheta);
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
        period = freq;
        setP(0);
        d = 1;
        setQ(1);
        setBP(0);
        bd = 1;
        setBQ(1);
        setMean(false);
    }

    public void airlineWithMean(int freq) {
        period = freq;
        setP(0);
        d = 1;
        setQ(1);
        setBP(0);
        bd = 1;
        setBQ(1);
        setMean(true);
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

    /// <summary>Seasonal frequency</summary>
    /// <remarks>
    /// Setting the frequency to 1 (annual model) removes all the seasonal part of the
    /// model. So, you should change the S property before initializing the seasonal part of
    /// the object.
    /// </remarks>
    public int getS() {
        return period;
    }

    public void setS(int value) {
        period = value;
        if (period == 1) {
            bd = 0;
            bphi = null;
            btheta = null;
        }
    }

    public int getBp() {
        return bphi == null ? 0 : bphi.length;
    }

    public void setBP(int value) {
        bphi = Parameter.create(value);
    }

    public int getBd() {
        return bd;
    }

    public void setBD(int value) {
        bd = value;
    }

    public int getBq() {
        return btheta == null ? 0 : btheta.length;
    }

    public void setBQ(int value) {
        btheta = Parameter.create(value);
    }

    public Parameter[] getPhi() {
        return phi;
    }

    public void setPhi(Parameter[] value) {
        phi = Parameter.clone(value);
    }

    public Parameter[] getTheta() {
        return theta;
    }

    public void setTheta(Parameter[] value) {
        theta = Parameter.clone(value);
    }

    public Parameter[] getBPhi() {
        return bphi;
    }

    public void setBPhi(Parameter[] value) {
        bphi = Parameter.clone(value);
    }

    public Parameter[] getBTheta() {
        return btheta;
    }

    public void setBTheta(Parameter[] value) {
        btheta = Parameter.clone(value);
    }

    public int getDifferencingOrder() {
        return d + bd * period;
    }

    public BackFilter getDifferencingFilter() {
        UnitRoots ur = new UnitRoots();
        if (period > 1) {
            for (int i = 0; i < bd; ++i) {
                ur.add(period);
            }
        }
        for (int i = 0; i < d; ++i) {
            ur.add(1);
        }
        return new BackFilter(ur.toPolynomial());
    }

    public int getFrequency() {
        return period;
    }

    public void setFrequency(int freq) {
        period = freq;
        if (freq == 1) {
            bd = 0;
            bphi = null;
            btheta = null;
        }
    }

    @Override
    public SarimaModel getModel() {
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.setP(getP());
        spec.setD(d);
        spec.setQ(getQ());
        spec.setBp(getBp());
        spec.setBd(bd);
        spec.setBq(getBq());

        SarimaModel.Builder builder = SarimaModel.builder(spec);
        Parameter[] p = phi;
        for (int i = 0; i < spec.getP(); ++i) {
            if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                builder.phi(i + 1, p[i].getValue());
            } else {
                builder.phi(i + 1, -.2);
            }
        }
        p = theta;
        for (int i = 0; i < spec.getQ(); ++i) {
            if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                builder.theta(i + 1, p[i].getValue());
            } else {
                builder.theta(i + 1, -.1);
            }
        }
        p = bphi;
        for (int i = 0; i < spec.getBp(); ++i) {
            if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                builder.bphi(i + 1, p[i].getValue());
            } else {
                builder.bphi(i + 1, -.2);
            }
        }
        p = btheta;
        for (int i = 0; i < spec.getBq(); ++i) {
            if (p[i] != null && p[i].getType() != ParameterType.Undefined) {
                builder.btheta(i + 1, p[i].getValue());
            } else {
                builder.btheta(i + 1, -.1);
            }
        }
        return builder.build();
    }

    public SarimaSpecification getSpecification() {
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.setP(getP());
        spec.setD(d);
        spec.setQ(getQ());
        spec.setBp(getBp());
        spec.setBd(bd);
        spec.setBq(getBq());

        return spec;
    }

    public boolean isDefined() {
        return Parameter.isDefined(phi) && Parameter.isDefined(theta)
                && Parameter.isDefined(bphi) && Parameter.isDefined(btheta);
    }

    public boolean isUndefined() {
        return Parameter.isDefault(phi) && Parameter.isDefault(theta)
                && Parameter.isDefault(bphi) && Parameter.isDefault(btheta);
    }

    public void setSpecification(SarimaSpecification spec) {
        setP(spec.getP());
        d = spec.getD();
        setQ(spec.getQ());
        setBP(spec.getBp());
        bd = spec.getBd();
        setBQ(spec.getBq());
        period = spec.getPeriod();
    }

    @Override
    public void setModel(IArimaModel value) {
        setModel((SarimaModel) value);
    }

    public void setModel(SarimaModel value) {
        SarimaSpecification spec = value.specification();
        setP(spec.getP());
        d = spec.getD();
        setQ(spec.getQ());
        setBP(spec.getBp());
        bd = spec.getBd();
        setBQ(spec.getBq());
        period = spec.getPeriod();
        Parameter[] p = phi;
        for (int i = 0; i < spec.getP(); ++i) {
            p[i] = new Parameter(value.phi(i + 1), ParameterType.Estimated);
        }
        p = theta;
        for (int i = 0; i < spec.getQ(); ++i) {
            p[i] = new Parameter(value.theta(i + 1), ParameterType.Estimated);
        }
        p = bphi;
        for (int i = 0; i < spec.getBp(); ++i) {
            p[i] = new Parameter(value.bphi(i + 1), ParameterType.Estimated);
        }
        p = btheta;
        for (int i = 0; i < spec.getBq(); ++i) {
            p[i] = new Parameter(value.btheta(i + 1), ParameterType.Estimated);
        }
    }

    public int getParametersCount() {
        return getP() + getBp() + getQ() + getBq();
    }

    public int getFreeParametersCount() {
        int n = Parameter.countFreeParameters(phi);
        n += Parameter.countFreeParameters(bphi);
        n += Parameter.countFreeParameters(theta);
        n += Parameter.countFreeParameters(btheta);
        return n;
    }

    public int getFixedParametersCount() {
        int n = Parameter.countFixedParameters(phi);
        n += Parameter.countFixedParameters(bphi);
        n += Parameter.countFixedParameters(theta);
        n += Parameter.countFixedParameters(btheta);
        return n;
    }

    /**
     * Returns the p constraints, using the order defined in Tramo: regular AR,
     * seasonal AR, regular MA, seasonal MA
     *
     * @return
     */
    private boolean[] fixedConstraints() {
        int n = getParametersCount();
        boolean[] fixed = new boolean[n];
        int j = 0;
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i, ++j) {
                if (phi[i] != null && phi[i].getType() == ParameterType.Fixed) {
                    fixed[j] = true;
                }
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i, ++j) {
                if (bphi[i] != null && bphi[i].getType() == ParameterType.Fixed) {
                    fixed[j] = true;
                }
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i, ++j) {
                if (theta[i] != null && theta[i].getType() == ParameterType.Fixed) {
                    fixed[j] = true;
                }
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i, ++j) {
                if (btheta[i] != null && btheta[i].getType() == ParameterType.Fixed) {
                    fixed[j] = true;
                }
            }
        }
        return fixed;
    }

    /**
     * Returns the parameters, using the order defined in Tramo: regular AR,
     * seasonal AR, regular MA, seasonal MA
     *
     * @return
     */
    private double[] parameters() {
        int n = getParametersCount();
        double[] p = new double[n];
        int j = 0;
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i, ++j) {
                if (phi[i] != null) {
                    p[j] = phi[i].getValue();
                }
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i, ++j) {
                if (bphi[i] != null) {
                    p[j] = bphi[i].getValue();
                }
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i, ++j) {
                if (theta[i] != null) {
                    p[j] = theta[i].getValue();
                }
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i, ++j) {
                if (btheta[i] != null) {
                    p[j] = btheta[i].getValue();
                }
            }
        }
        return p;
    }

    public void setParameters(DoubleSequence p, DoubleSequence stde, ParameterType type) {
        int j = 0;
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i, ++j) {
                if (phi[i] == null) {
                    phi[i] = new Parameter(p.get(j), type);
                } else if (!phi[i].isFixed()) {
                    phi[i].setValue(p.get(j));
                    phi[i].setType(type);
                }
                if (stde != null) {
                    phi[i].setStde(stde.get(j));
                }
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i, ++j) {
                if (bphi[i] == null) {
                    bphi[i] = new Parameter(p.get(j), type);
                } else if (!bphi[i].isFixed()) {
                    bphi[i].setValue(p.get(j));
                    bphi[i].setType(type);
                }
                if (stde != null) {
                    bphi[i].setStde(stde.get(j));
                }
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i, ++j) {
                if (theta[i] == null) {
                    theta[i] = new Parameter(p.get(j), type);
                } else if (!theta[i].isFixed()) {
                    theta[i].setValue(p.get(j));
                    theta[i].setType(type);
                }
                if (stde != null) {
                    theta[i].setStde(stde.get(j));
                }
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i, ++j) {
                if (btheta[i] == null) {
                    btheta[i] = new Parameter(p.get(j), type);
                } else if (!btheta[i].isFixed()) {
                    btheta[i].setValue(p.get(j));
                    btheta[i].setType(type);
                }
                if (stde != null) {
                    btheta[i].setStde(stde.get(j));
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SarimaComponent && equals((SarimaComponent) obj));
    }

    private boolean equals(SarimaComponent other) {
        return other.bd == bd
                && other.d == d
                && other.period == period
                && Arrays.deepEquals(phi, phi)
                && Arrays.deepEquals(bphi, bphi)
                && Arrays.deepEquals(theta, theta)
                && Arrays.deepEquals(btheta, btheta);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.d;
        hash = 97 * hash + this.bd;
        hash = 97 * hash + this.period;
        hash = 97 * hash + Arrays.deepHashCode(this.phi);
        hash = 97 * hash + Arrays.deepHashCode(this.theta);
        hash = 97 * hash + Arrays.deepHashCode(this.bphi);
        hash = 97 * hash + Arrays.deepHashCode(this.btheta);
        return hash;
    }
    
        public IParametricMapping<SarimaModel> defaultMapping() {
        if (getFixedParametersCount() == 0) {
            return SarimaMapping.of(getSpecification());
        } else {
            return new SarimaFixedMapping(getSpecification(), DoubleSequence.ofInternal(parameters()), fixedConstraints());
        }
    }


}
