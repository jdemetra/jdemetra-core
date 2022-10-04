/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.arima;

import demetra.arima.SarimaOrders;
import nbbrd.design.Development;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.util.Validatable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Specification of Sarima models.
 * The specification is independent of the periodicity of the model (provided at
 * run time).
 * The specification will provide information on the parameters (unknown, fixed,
 * or initial)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
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

    private Validator validator;
    /**
     * Period of the Arima model. Should be 0 when the period is unspecified
     */
    private int period;
    private int d, bd;
    private @NonNull
    Parameter[] phi, theta, bphi, btheta;

    // Defensive getters !
    public Parameter[] getPhi() {
        return phi == EMPTY ? EMPTY : phi.clone();
    }

    public Parameter[] getBphi() {
        return bphi == EMPTY ? EMPTY : bphi.clone();
    }

    public Parameter[] getTheta() {
        return theta == EMPTY ? EMPTY : theta.clone();
    }

    public Parameter[] getBtheta() {
        return btheta == EMPTY ? EMPTY : btheta.clone();
    }

    private static final Parameter[] EMPTY = new Parameter[0];

    public static class Builder implements Validatable.Builder<SarimaSpec> {

        private Builder() {
            phi = EMPTY;
            theta = EMPTY;
            bphi = EMPTY;
            btheta = EMPTY;
        }

        public Builder p(int value) {
            phi = value == 0 ? EMPTY : Parameter.make(value);
            return this;
        }

        public Builder d(int value) {
            d = value;
            return this;
        }

        public Builder q(int value) {
            theta = value == 0 ? EMPTY : Parameter.make(value);
            return this;
        }

        public Builder bp(int value) {
            bphi = value == 0 ? EMPTY : Parameter.make(value);
            return this;
        }

        public Builder bd(int value) {
            bd = value;
            return this;
        }

        public Builder bq(int value) {
            btheta = value == 0 ? EMPTY : Parameter.make(value);
            return this;
        }

        public Builder phi(Parameter[] value) {
            if (value == null || value.length == 0) {
                phi = EMPTY;
            } else {
                phi = value.clone();
            }
            return this;
        }

        public Builder theta(Parameter[] value) {
            if (value == null || value.length == 0) {
                theta = EMPTY;
            } else {
                theta = value.clone();
            }
            return this;
        }

        public Builder bphi(Parameter[] value) {
            if (value == null || value.length == 0) {
                bphi = EMPTY;
            } else {
                bphi = value.clone();
            }
            return this;
        }

        public Builder btheta(Parameter[] value) {
            if (value == null || value.length == 0) {
                btheta = EMPTY;
            } else {
                btheta = value.clone();
            }
            return this;
        }

        public Builder airline() {
            phi = EMPTY;
            bphi = EMPTY;
            theta = Parameter.make(1);
            btheta = Parameter.make(1);
            d = 1;
            bd = 1;
            return this;
        }

        Builder free(SarimaSpec ref) {
            if (ref == null) {
                phi = Parameter.freeParameters(phi);
                bphi = Parameter.freeParameters(bphi);
                theta = Parameter.freeParameters(theta);
                btheta = Parameter.freeParameters(btheta);
            } else {
                phi = Parameter.freeParameters(phi, ref.phi);
                bphi = Parameter.freeParameters(bphi, ref.bphi);
                theta = Parameter.freeParameters(theta, ref.theta);
                btheta = Parameter.freeParameters(btheta, ref.btheta);
            }
            return this;
        }

        Builder fix() {
            phi = Parameter.fixParameters(phi);
            bphi = Parameter.fixParameters(bphi);
            theta = Parameter.fixParameters(theta);
            btheta = Parameter.fixParameters(btheta);
            return this;
        }

        Builder reset(SarimaSpec ref) {
            if (ref == null) {
                phi = Parameter.resetParameters(phi);
                bphi = Parameter.resetParameters(bphi);
                theta = Parameter.resetParameters(theta);
                btheta = Parameter.resetParameters(btheta);
            } else {
                phi = Parameter.resetParameters(phi, ref.phi);
                bphi = Parameter.resetParameters(bphi, ref.bphi);
                theta = Parameter.resetParameters(theta, ref.theta);
                btheta = Parameter.resetParameters(btheta, ref.btheta);
            }
            return this;
        }
    }

    private static final SarimaSpec AIRLINE = new SarimaSpec(null, 0, 1, 1,
            EMPTY, Parameter.make(1), EMPTY, Parameter.make(1));
    private static final SarimaSpec WN = new SarimaSpec(null, 0, 0, 0,
            EMPTY, EMPTY, EMPTY, EMPTY);

    public static SarimaSpec airline() {
        return AIRLINE;
    }

    public static SarimaSpec whiteNoise() {
        return WN;
    }

    public int getP() {
        return phi.length;
    }

    public int getQ() {
        return theta.length;
    }

    public int getBp() {
        return bphi.length;
    }

    public int getBq() {
        return btheta.length;
    }

    public boolean isAirline() {
        return getP() == 0 && d == 1 && getQ() == 1
                && getBp() == 0 && bd == 1 && getBq() == 1;
    }

    public int parametersCount() {
        return phi.length + bphi.length + theta.length + btheta.length;
    }

    public int freeParametersCount() {
        return Parameter.freeParametersCount(phi) + Parameter.freeParametersCount(bphi)
                + Parameter.freeParametersCount(theta) + Parameter.freeParametersCount(btheta);
    }

    public boolean hasFixedParameters() {
        return !Parameter.isFree(phi) || !Parameter.isFree(bphi)
                || !Parameter.isFree(theta) || !Parameter.isFree(btheta);
    }

    public boolean hasFreeParameters() {
        return Parameter.hasFreeParameters(phi) || Parameter.hasFreeParameters(bphi)
                || Parameter.hasFreeParameters(theta) || Parameter.hasFreeParameters(btheta);
    }

    public boolean hasEstimatedParameters() {
        return Parameter.hasParameters(phi, ParameterType.Estimated) || Parameter.hasParameters(bphi, ParameterType.Estimated)
                || !Parameter.hasParameters(theta, ParameterType.Estimated) || Parameter.hasParameters(btheta, ParameterType.Estimated);
    }

    public boolean isUndefined() {
        return Parameter.isDefault(phi) && Parameter.isDefault(theta)
                && Parameter.isDefault(bphi) && Parameter.isDefault(btheta);
    }

    public boolean isDefined() {
        return Parameter.isDefined(phi) && Parameter.isDefined(theta)
                && Parameter.isDefined(bphi) && Parameter.isDefined(btheta);
    }

    public SarimaSpec withPeriod(int period) {
        if (this.period == period) {
            return this;
        }
        if (period == 1) {
            return new SarimaSpec(null, 1, d, 0, phi, theta, EMPTY, EMPTY);
        } else {
            return new SarimaSpec(null, period, d, bd, phi, theta, bphi, btheta);
        }
    }

    public SarimaSpec resetParameters(SarimaSpec ref) {
        return toBuilder().reset(ref).build();
    }

    public SarimaSpec freeParameters(SarimaSpec ref) {
        return toBuilder().free(ref).build();
    }

    public SarimaSpec fixParameters() {
        if (!hasFreeParameters()) {
            return this;
        }
        return toBuilder().fix().build();
    }

    public SarimaOrders orders() {
        SarimaOrders spec = new SarimaOrders(period);
        spec.setP(getP());
        spec.setD(d);
        spec.setQ(getQ());
        if (period != 1) {
            spec.setBp(getBp());
            spec.setBd(bd);
            spec.setBq(getBq());
        }
        return spec;
    }

    public int getDifferencingOrder() {
        return d + bd * period;
    }

}
