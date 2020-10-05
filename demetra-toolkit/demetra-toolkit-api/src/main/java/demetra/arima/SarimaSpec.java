/*
 * Copyright 2020 National Bank of Belgium
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

import demetra.design.Development;
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
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
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

    private Validator validator;
    /**
     * Period of the Arima model. Should be 0 when the period is unspecified
     */
    private int d, bd;
    private @NonNull
    Parameter[] phi, theta, bphi, btheta;

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

        public Builder phi(@NonNull Parameter[] value) {
            phi = value.clone();
            return this;
        }

        public Builder theta(@NonNull Parameter[] value) {
            theta = value.clone();
            return this;
        }

        public Builder bphi(@NonNull Parameter[] value) {
            bphi = value.clone();
            return this;
        }

        public Builder btheta(@NonNull Parameter[] value) {
            btheta = value.clone();
            return this;
        }

        Builder free() {
            Parameter.freeParameters(phi);
            Parameter.freeParameters(theta);
            Parameter.freeParameters(bphi);
            Parameter.freeParameters(btheta);
            return this;
        }

        Builder reset() {
            Parameter.resetParameters(phi);
            Parameter.resetParameters(theta);
            Parameter.resetParameters(bphi);
            Parameter.resetParameters(btheta);
            return this;
        }

    }

    private static final SarimaSpec AIRLINE = new SarimaSpec(null, 1, 1,
            EMPTY, Parameter.make(1), EMPTY, Parameter.make(1));

    public static SarimaSpec airline() {
        return AIRLINE;
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

    public SarimaSpec resetParameters() {
        if (!hasEstimatedParameters()) {
            return this;
        }
        return toBuilder().reset().build();
    }

    public SarimaSpec freeParameters() {
        if (!hasEstimatedParameters()) {
            return this;
        }
        return toBuilder().free().build();
    }

    public SarimaOrders specification(int period) {
        SarimaOrders spec = new SarimaOrders(period);
        spec.setP(getP());
        spec.setD(d);
        spec.setQ(getQ());
        if (period > 1) {
            spec.setBp(getBp());
            spec.setBd(bd);
            spec.setBq(getBq());
        }
        return spec;
    }

}
