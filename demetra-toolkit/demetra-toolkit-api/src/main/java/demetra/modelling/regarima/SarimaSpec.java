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
package demetra.modelling.regarima;

import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.design.Development;
import demetra.arima.SarimaSpecification;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class SarimaSpec implements Validatable<SarimaSpec> {

    @Override
    public SarimaSpec validate() throws IllegalArgumentException {
        if (validator != null) {
            validator.checkP(getP());
            validator.checkD(getD());
            validator.checkQ(getQ());
            validator.checkBp(getBp());
            validator.checkBd(getBd());
            validator.checkBq(getBq());
        }
        return this;
    }

    public static interface Validator {

        void checkP(int p);

        void checkD(int d);

        void checkQ(int q);

        void checkBp(int bp);

        void checkBd(int bd);

        void checkBq(int bq);
    }

    private final Validator validator;
    private int d, bd;
    private Parameter[] phi, theta, bphi, btheta;

    public static class Builder implements Validatable.Builder<SarimaSpec> {

        public Builder airline() {
            phi=Parameter.create(0);
            bphi=Parameter.create(0);
            theta=Parameter.create(1);
            btheta=Parameter.create(1);
            d = 1;
            bd = 1;
            return this;
        }

        public Builder p(int value) {
            phi = Parameter.create(value);
            return this;
        }

        public Builder d(int value) {
            d = value;
            return this;
        }

        public Builder q(int value) {
            theta = Parameter.create(value);
            return this;
        }

        public Builder bp(int value) {
            bphi = Parameter.create(value);
            return this;
        }

        public Builder bd(int value) {
            bd = value;
            return this;
        }

        public Builder bq(int value) {
            btheta = Parameter.create(value);
            return this;
        }

        public Builder phi(Parameter[] value) {
            phi = Parameter.clone(value);
            return this;
        }

        public Builder theta(Parameter[] value) {
            theta = Parameter.clone(value);
            return this;
        }

        public Builder bphi(Parameter[] value) {
            phi = Parameter.clone(value);
            return this;
        }

        public Builder btheta(Parameter[] value) {
            theta = Parameter.clone(value);
            return this;
        }

        public Builder validator(Validator validator) {
            this.validator = validator;
            return this;
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

    public int getP() {
        return phi == null ? 0 : phi.length;
    }

    public int getQ() {
        return theta == null ? 0 : theta.length;
    }

    public int getBp() {
        return bphi == null ? 0 : bphi.length;
    }

    public int getBq() {
        return btheta == null ? 0 : btheta.length;
    }

    public boolean isAirline() {
        return getP() == 0 && d == 1 && getQ() == 1
                && getBp() == 0 && bd == 1 && getBq() == 1;
    }

    public boolean isDefault() {
        return isAirline()
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

    private static void clearFreeParameters(Parameter[] P) {
        if (P != null) {
            for (int i = 0; i < P.length; ++i) {
                if (!P[i].isFixed()) {
                    P[i] = new Parameter();
                }
            }
        }
    }

    private static void clearParameters(Parameter[] P) {
        if (P != null) {
            for (int i = 0; i < P.length; ++i) {
                P[i] = new Parameter();
            }
        }
    }

    private static void setParameterType(Parameter[] P, ParameterType type) {
        if (P != null) {
            for (int i = 0; i < P.length; ++i) {
                P[i].setType(type);
            }
        }
    }

    public SarimaSpec clearFreeParameters() {
        Builder builder = toBuilder();
        clearFreeParameters(builder.phi);
        clearFreeParameters(builder.bphi);
        clearFreeParameters(builder.theta);
        clearFreeParameters(builder.btheta);
        return builder.build();
    }

    public SarimaSpec clearParameters() {
        Builder builder = toBuilder();
        clearParameters(builder.phi);
        clearParameters(builder.bphi);
        clearParameters(builder.theta);
        clearParameters(builder.btheta);
        return builder.build();
    }

    public SarimaSpec setParameterType(ParameterType type) {
        Builder builder = toBuilder();
        setParameterType(builder.phi, type);
        setParameterType(builder.bphi, type);
        setParameterType(builder.theta, type);
        setParameterType(builder.btheta, type);
        return builder.build();
    }

    public SarimaSpec setMaParameterType(ParameterType type) {
        Builder builder = toBuilder();
        setParameterType(builder.theta, type);
        setParameterType(builder.btheta, type);
        return builder.build();
    }

    public SarimaSpec setArParameterType(ParameterType type) {
        Builder builder = toBuilder();
        setParameterType(builder.phi, type);
        setParameterType(builder.bphi, type);
        return builder.build();
    }

}
