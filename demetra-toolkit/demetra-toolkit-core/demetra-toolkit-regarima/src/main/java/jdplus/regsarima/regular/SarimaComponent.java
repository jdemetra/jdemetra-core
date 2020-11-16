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
package jdplus.regsarima.regular;

import demetra.data.Parameter;
import demetra.data.ParameterType;
import nbbrd.design.Development;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.UnitRoots;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.sarima.estimation.SarimaFixedMapping;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import java.util.Arrays;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaComponent {

    private int d, bd, period;
    private Parameter[] phi, theta, bphi, btheta;

    public SarimaComponent() {
    }

    public SarimaComponent(int freq) {
        period = freq;
    }

    public void copy(SarimaComponent other) {
        d = other.d;
        bd = other.bd;
        period = other.period;
        phi = other.phi == null ? null : other.phi.clone();
        bphi = other.bphi == null ? null : other.bphi.clone();
        theta = other.theta == null ? null : other.theta.clone();
        btheta = other.btheta == null ? null : other.btheta.clone();
    }

    public void setParameterType(ParameterType type) {
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i) {
                phi[i] = phi[i].withType(type);
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                bphi[i] = bphi[i].withType(type);
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i) {
                theta[i] = theta[i].withType(type);
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i) {
                btheta[i] = btheta[i].withType(type);
            }
        }
    }

    public void clearParameters() {
        setParameterType(ParameterType.Undefined);
    }

    public void clearFreeParameters() {
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i) {
                if (phi[i] == null || !phi[i].isFixed()) {
                    phi[i] = Parameter.undefined();
                }
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                if (bphi[i] == null || !bphi[i].isFixed()) {
                    bphi[i] = Parameter.undefined();
                }
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i) {
                if (theta[i] == null || !theta[i].isFixed()) {
                    theta[i] = Parameter.undefined();
                }
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i) {
                if (btheta[i] == null || !btheta[i].isFixed()) {
                    btheta[i] = Parameter.undefined();
                }
            }
        }

    }

    public void airline(int freq) {
        period = freq;
        setP(0);
        d = 1;
        setQ(1);
        setBp(0);
        bd = 1;
        setBq(1);
    }

    public int getP() {
        return phi == null ? 0 : phi.length;
    }

    public void setP(int value) {
        phi = Parameter.make(value);
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
        theta = Parameter.make(value);
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

    public void setBp(int value) {
        bphi = Parameter.make(value);
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
        btheta = Parameter.make(value);
    }

    public Parameter[] getPhi() {
        return phi;
    }

    public void setPhi(Parameter[] value) {
        phi = value == null ? null : value.clone();
    }

    public Parameter[] getTheta() {
        return theta;
    }

    public void setTheta(Parameter[] value) {
        theta = value == null ? null : value.clone();
    }

    public Parameter[] getBphi() {
        return bphi;
    }

    public void setBphi(Parameter[] value) {
        bphi = value == null ? null : value.clone();
    }

    public Parameter[] getBtheta() {
        return btheta;
    }

    public void setBtheta(Parameter[] value) {
        btheta = value == null ? null : value.clone();
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
        return new BackFilter(ur.asPolynomial());
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int freq) {
        period = freq;
        if (freq == 1) {
            bd = 0;
            bphi = null;
            btheta = null;
        }
    }

    public SarimaModel getModel() {
        SarimaOrders spec = new SarimaOrders(period);
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

    public SarimaOrders specification() {
        SarimaOrders spec = new SarimaOrders(period);
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

    public void setSpecification(SarimaOrders spec) {
        setP(spec.getP());
        d = spec.getD();
        setQ(spec.getQ());
        setBp(spec.getBp());
        bd = spec.getBd();
        setBq(spec.getBq());
        period = spec.getPeriod();
    }

    public void setModel(SarimaModel value) {
        SarimaOrders spec = value.orders();
        setP(spec.getP());
        d = spec.getD();
        setQ(spec.getQ());
        setBp(spec.getBp());
        bd = spec.getBd();
        setBq(spec.getBq());
        period = spec.getPeriod();
        Parameter[] p = phi;
        for (int i = 0; i < spec.getP(); ++i) {
            p[i] = Parameter.estimated(value.phi(i + 1));
        }
        p = theta;
        for (int i = 0; i < spec.getQ(); ++i) {
            p[i] = Parameter.estimated(value.theta(i + 1));
        }
        p = bphi;
        for (int i = 0; i < spec.getBp(); ++i) {
            p[i] = Parameter.estimated(value.bphi(i + 1));
        }
        p = btheta;
        for (int i = 0; i < spec.getBq(); ++i) {
            p[i] = Parameter.estimated(value.btheta(i + 1));
        }
    }

    public int getParametersCount() {
        return getP() + getBp() + getQ() + getBq();
    }

    public int getFreeParametersCount() {
        int n = Parameter.freeParametersCount(phi);
        n += Parameter.freeParametersCount(bphi);
        n += Parameter.freeParametersCount(theta);
        n += Parameter.freeParametersCount(btheta);
        return n;
    }

    public int getFixedParametersCount() {
        int n = Parameter.fixedParametersCount(phi);
        n += Parameter.fixedParametersCount(bphi);
        n += Parameter.fixedParametersCount(theta);
        n += Parameter.fixedParametersCount(btheta);
        return n;
    }

    /**
     * Returns the p constraints, using the order defined in Tramo: regular AR,
     * seasonal AR, regular MA, seasonal MA
     *
     * @return An array of boolean corresponding to all the parameters. Yhe item
     * i of
     * the array is true if the corresponding parameter is fixed, false
     * otherwise.
     */
    public boolean[] fixedConstraints() {
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
    public double[] parameters() {
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

    public void setFreeParameters(DoubleSeq p) {
        DoubleSeqCursor cursor = p.cursor();
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i) {
                if (!phi[i].isFixed()) {
                    phi[i] = Parameter.estimated(cursor.getAndNext());
                }
            }
        }
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                if (!bphi[i].isFixed()) {
                    bphi[i] = Parameter.estimated(cursor.getAndNext());
                }
            }
        }
        if (theta != null) {
            for (int i = 0; i < theta.length; ++i) {
                if (!theta[i].isFixed()) {
                    theta[i] = Parameter.estimated(cursor.getAndNext());
                }
            }
        }
        if (btheta != null) {
            for (int i = 0; i < btheta.length; ++i) {
                if (!btheta[i].isFixed()) {
                    btheta[i] = Parameter.estimated(cursor.getAndNext());
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

    public IArimaMapping<SarimaModel> defaultMapping() {
        if (getFixedParametersCount() == 0) {
            return SarimaMapping.of(specification());
        } else {
            return new SarimaFixedMapping(specification(), DoubleSeq.of(parameters()), fixedConstraints());
        }
    }

}
