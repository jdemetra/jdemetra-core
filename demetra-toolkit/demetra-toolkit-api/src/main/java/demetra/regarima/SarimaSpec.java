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
import demetra.design.LombokWorkaround;
import demetra.util.Validatable;
import java.util.LinkedHashMap;

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

        @LombokWorkaround
        public Builder fixedCoefficient(String key, double[] value) {
            return this;
        }

        public Builder parameterType(ParameterType type) {
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
            return this;
        }

        public Builder maParameterType(ParameterType type) {
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
            return this;
        }

        public Builder arParameterType(ParameterType type) {
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
            return this;
        }

        public Builder clearParameters() {
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
            return this;
        }

        public Builder clearFreeParameters() {
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
            return this;
        }

        public Builder airline() {
            p(0);
            d = 1;
            q(1);
            bp(0);
            bd = 1;
            bq(1);
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

}
