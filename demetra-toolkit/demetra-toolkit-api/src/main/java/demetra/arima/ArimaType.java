/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.design.Development;
import demetra.maths.PolynomialType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
public interface ArimaType {

    double getInnovationVariance();

    PolynomialType getAr();

    PolynomialType getDelta();

    PolynomialType getMa();

    String getName();

    static Builder builder() {
        return new Builder();
    }

    class Builder {

        private double innovationVariance = 1;
        private PolynomialType ar = PolynomialType.ONE,
                delta = PolynomialType.ONE,
                ma = PolynomialType.ONE;
        private String name;

        public Builder innovationVariance(double var) {
            if (var < 0) {
                throw new ArimaException(ArimaException.INVALID);
            }
            this.innovationVariance = var;
            return this;
        }

        public Builder ar(PolynomialType ar) {
            if (ar == null) {
                throw new IllegalArgumentException();
            }
            this.ar = ar;
            return this;
        }

        public Builder delta(PolynomialType delta) {
            if (delta == null) {
                throw new IllegalArgumentException();
            }
            this.delta = delta;
            return this;
        }

        public Builder ma(PolynomialType ma) {
            if (ma == null) {
                throw new IllegalArgumentException();
            }
            this.ma = ma;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ArimaType build() {
            return new LightArimaType(this.innovationVariance, ar, delta, ma, name);
        }
    }
}
