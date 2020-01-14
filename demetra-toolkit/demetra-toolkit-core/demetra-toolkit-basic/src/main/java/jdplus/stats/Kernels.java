/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats;

import java.util.function.DoubleUnaryOperator;
import jdplus.math.polynomials.Polynomial;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Kernels {

    public Kernel UNIFORM = new Kernel() {
        @Override
        public double lowerBound() {
            return -1;
        }

        @Override
        public double upperBound() {
            return 1;
        }

        @Override
        public DoubleUnaryOperator asFunction() {
            return x -> .5;
        }

        @Override
        public double moment(int order) {
            if (order % 2 == 1) {
                return 0;
            } else {
                return 1.0 / (order + 1);
            }
        }
    };

    public Kernel TRIANGULAR = new Kernel() {
        @Override
        public double lowerBound() {
            return -1;
        }

        @Override
        public double upperBound() {
            return 1;
        }

        @Override
        public DoubleUnaryOperator asFunction() {
            return x -> 1 - Math.abs(x);
        }

        @Override
        public double moment(int order) {
            if (order % 2 == 1) {
                return 0;
            } else {
                int d = (order + 1) * (order + 2);
                return 2.0 / d;
            }
        }
    };

    /**
     * 3/4(1-u^2)
     *
     */
    public Kernel EPANECHNIKOV = new Kernel() {
        @Override
        public double lowerBound() {
            return -1;
        }

        @Override
        public double upperBound() {
            return 1;
        }

        @Override
        public DoubleUnaryOperator asFunction() {
            return x -> 0.75 * (1 - x * x);
        }

        @Override
        public double moment(int order) {
            if (order % 2 == 1) {
                return 0;
            } else {
                int d = (order + 1) * (order + 3);
                return 3.0 / d;
            }
        }

    };

    public Polynomial epanechnikovAsPolynomial() {
        return Polynomial.of(.75, 0, -.75);
    }

    public Polynomial biWeightAsPolynomial() {
        return Polynomial.of(15.0 / 16.0, 0, -15.0 / 8.0, 0, 15.0 / 16.0);
    }

    public Polynomial triWeightAsPolynomial() {
        return Polynomial.of(35.0 / 32.0, 0, -105.0 / 32.0, 0, 105.0 / 32.0, 0, -35.0 / 32.0);
    }

    /**
     * 15/16*(1-u^2)^2
     */
    public Kernel BIWEIGHT = new Kernel() {
        @Override
        public double lowerBound() {
            return -1;
        }

        @Override
        public double upperBound() {
            return 1;
        }

        @Override
        public DoubleUnaryOperator asFunction() {
            return x -> {
                double z = 1 - x * x;
                return 0.9375 * z * z;
            };
        }

        @Override
        public double moment(int order) {
            if (order % 2 == 1) {
                return 0;
            } else {
                int d = (order + 1) * (order + 3) * (order + 5);
                return 15.0 / d;
            }
        }
    };

    /**
     * 35/32*(1-u^2)^3
     */
    public Kernel TRIWEIGHT = new Kernel() {
        @Override
        public double lowerBound() {
            return -1;
        }

        @Override
        public double upperBound() {
            return 1;
        }

        @Override
        public DoubleUnaryOperator asFunction() {
            return x -> {
                double z = 1 - x * x;
                return 1.09375 * z * z * z;
            };
        }

        @Override
        public double moment(int order) {
            if (order % 2 == 1) {
                return 0;
            } else {
                int d = (order + 1) * (order + 3) * (order + 5) * (order + 7);
                return 105.0 / d;
            }
        }
    };

    /**
     * k*((m+1)^2-(m+1)^2 u^2)((m+2)^2-(m+1)^2 u^2)((m+3)^2-(m+1)^2 u^2)
     */
    public Kernel henderson(int length) {
        final Polynomial p = hendersonAsPolynomial(length);
        return new Kernel() {
            @Override
            public double lowerBound() {
                return -1;
            }

            @Override
            public double upperBound() {
                return 1;
            }

            @Override
            public DoubleUnaryOperator asFunction() {
                return x -> p.evaluateAt(x);
            }

            @Override
            public double moment(int order) {
                if (order % 2 == 1) {
                    return 0;
                } else {
                    double z = 0;
                    for (int j = 0; j <= 6; j += 2) {
                        z += 2 * p.get(j) / (1 + j + order);
                    }
                    return z;
                }
            }
        };
    }

    /**
     * Kernel corresponding to an Henderson filter[-m,m] (length = 2*m+1)
     * @param m
     * @return 
     */
    public Polynomial hendersonAsPolynomial(int m) {
        int q1 = (m + 1) * (m + 1), q2 = (m + 2) * (m + 2), q3 = (m + 3) * (m + 3);
        Polynomial p = Polynomial.of(q1 * q2 * q3, 0, -q1 * q1 * q2 - q1 * q1 * q3 - q1 * q2 * q3, 0, q1 * q1 * (q1 + q2 + q3), 0, -q1 * q1 * q1);
        return p.divide(p.integrate(-1, 1));
    }
}
