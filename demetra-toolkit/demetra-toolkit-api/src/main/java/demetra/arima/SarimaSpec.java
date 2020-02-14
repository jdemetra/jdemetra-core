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
package demetra.arima;

import demetra.design.Development;
import demetra.data.ParameterSpec;
import demetra.util.Validatable;
import org.checkerframework.checker.nullness.qual.NonNull;

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

    private Validator validator;
    /**
     * Period of the Arima model. Should be 0 when the period is unspecified
     */
    private int d, bd;
    private @NonNull
    ParameterSpec[] phi, theta, bphi, btheta;

    private static final ParameterSpec[] EMPTY = new ParameterSpec[0];

    public static class Builder implements Validatable.Builder<SarimaSpec> {

        private Builder() {
            phi = EMPTY;
            theta = EMPTY;
            bphi = EMPTY;
            btheta = EMPTY;
        }

        public Builder p(int value) {
            phi = value == 0 ? EMPTY : ParameterSpec.make(value);
            return this;
        }

        public Builder d(int value) {
            d = value;
            return this;
        }

        public Builder q(int value) {
            theta = value == 0 ? EMPTY : ParameterSpec.make(value);
            return this;
        }

        public Builder bp(int value) {
            bphi = value == 0 ? EMPTY : ParameterSpec.make(value);
            return this;
        }

        public Builder bd(int value) {
            bd = value;
            return this;
        }

        public Builder bq(int value) {
            btheta = value == 0 ? EMPTY : ParameterSpec.make(value);
            return this;
        }

        public Builder phi(@NonNull ParameterSpec[] value) {
            phi = value.clone();
            return this;
        }

        public Builder theta(@NonNull ParameterSpec[] value) {
            theta = value.clone();
            return this;
        }

        public Builder bphi(@NonNull ParameterSpec[] value) {
            bphi = value.clone();
            return this;
        }

        public Builder btheta(@NonNull ParameterSpec[] value) {
            btheta = value.clone();
            return this;
        }
    }

    private static final SarimaSpec AIRLINE = new SarimaSpec(null, 1, 1, 
            EMPTY, ParameterSpec.make(1), EMPTY, ParameterSpec.make(1));

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

    public boolean isFixed() {
        return ParameterSpec.isFixed(phi) && ParameterSpec.isFixed(bphi)
                && ParameterSpec.isFixed(theta) && ParameterSpec.isFixed(btheta);
    }

    public boolean hasFixedParameters() {
        return !ParameterSpec.isFree(phi) || !ParameterSpec.isFree(bphi)
                || !ParameterSpec.isFree(theta) || !ParameterSpec.isFree(btheta);
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
