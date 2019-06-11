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
package jdplus.arima;

import java.util.concurrent.atomic.AtomicReference;
import demetra.design.Development;
import demetra.design.Immutable;
import jdplus.maths.linearfilters.BackFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.maths.polynomials.Polynomial;
import internal.jdplus.arima.AutoCovarianceComputers;

/**
 * The auto-covariance function provides the auto-covariance of any stationary
 * Linear model for any lags. The auto-covariances are computed recursively. The
 * object stores previously computed values for faster processing. Example: in
 * the case of the Arima model (1-.5B)y=(1+.8B)e, var(e)=4, The auto-covariance
 * function is created as follows: AutoCovarianceFunction acf=new
 * AutoCovarianceFunction( new double[]{1, -.5}, new double[]{1, +.8}, 4); the
 * variance is retrieved by acf.get(0); the auto-covariance of rank 7 is
 * retrieved by acf.get(7);
 *
 * @author Jean Palate
 */
@Immutable(lazy = true)
@Development(status = Development.Status.Alpha)
public final class AutoCovarianceFunction {

    @FunctionalInterface
    public static interface Computer {

        /**
         * Computes the auto-covariances for the given arima model for lags [0, rank[
         *
         * @param ar
         * @param ma
         * @param rank
         * @return
         */
        double[] ac(Polynomial ma, Polynomial ar, int rank);
    }

    private static final AtomicReference<Computer> DEF_COMPUTER = new AtomicReference<>(AutoCovarianceComputers.defaultComputer(null));

    @FunctionalInterface
    public static interface SymmetricComputer {

        /**
         * Computes the auto-covariances for the given symmetric filter for lags [0, rank[
         *
         * @param ar
         * @param sma
         * @param rank
         * @return
         */
        double[] ac(SymmetricFilter sma, Polynomial ar, int rank);
    }

    private static final AtomicReference<SymmetricComputer> DEF_SYMCOMPUTER = new AtomicReference<>(AutoCovarianceComputers.defaultSymmetricComputer(null));

    public static void setDefautComputer(Computer computer) {
        DEF_COMPUTER.set(computer);
    }

    private static final int BLOCK = 36;
    private final Polynomial ar, ma;
    private final SymmetricFilter sma;
    private volatile double[] ac;
    private final double ivar;

    public AutoCovarianceFunction(final Polynomial ma, final Polynomial ar, final double var) {
        this.ma = ma;
        this.ar = ar;
        if (ar.degree() == 0) {
            this.sma = SymmetricFilter.fromFilter(new BackFilter(ma), var);
        } else {
            this.sma = null;
        }
        this.ivar = var;
    }

    public AutoCovarianceFunction(final SymmetricFilter sma, final Polynomial ar) {
        this.sma = sma;
        this.ar = ar;
        this.ma = null;
        this.ivar = 1;
    }

    /**
     * Gets all the auto-covariances up to a given rank.
     *
     * @param n The number of requested auto-covariances
     * @return An array of n values, going from the variance up to the
     * auto-covariance of rank(n-1).
     */
    public double[] values(final int n) {
        prepare(n);
        double[] a = new double[n];
        int nmax = Math.min(n, ac.length);
        System.arraycopy(ac, 0, a, 0, nmax);
        return a;
    }

    public double get(final int k) {
        prepare(k + 1);
        if (k >= ac.length) {
            return 0;
        } else {
            return ac[k];
        }
    }

    /**
     * Computes the auto-covariances up to the given rank (included).
     *
     * @param rank The rank to be computed.
     */
    public void prepare(int rank) {
        if (rank == 0) {
            rank = BLOCK;
        } else {
            int r = rank % BLOCK;
            if (r != 0) {
                rank += BLOCK - r;
            }
        }
        double[] acov = ac;
        if (acov == null || acov.length <= rank) {
            synchronized (this) {
                acov = ac;
                if (acov == null || acov.length <= rank) {
                    ac = ac(acov, rank);
                }
            }
        }
    }

    // ac is only used in the synchronized block
    private double[] ac(double[] acov, int rank) {
        if (acov == null) {
            if (sma == null) {
                acov = DEF_COMPUTER.get().ac(ma, ar, rank);
                if (ivar != 1) {
                    for (int i = 0; i < acov.length; ++i) {
                        acov[i] *= ivar;
                    }
                }
            } else {
                acov = DEF_SYMCOMPUTER.get().ac(sma, ar, rank);
            }
        }
        if (acov.length <= rank) {
            double[] tmp = new double[rank + 1];
            System.arraycopy(acov, 0, tmp, 0, acov.length);
            int p = ar.degree();
            for (int r = acov.length; r <= rank; ++r) {
                double s = 0;
                for (int j = 1; j <= p; ++j) {
                    s += ar.get(j) * tmp[r - j];
                }
                tmp[r] = -s;
            }
            acov = tmp;
        }
        return acov;
    }

    /**
     * Gets the last rank with an auto-covariance different from 0
     *
     * @return The rank of the last non-null auto-covariance. -1 if the
     * auto-covariance function is unbounded.
     */
    public int getBound() {
        if (!hasBound()) {
            return -1;
        }
        return ma.degree() + 1;
    }

    /**
     * Checks that the auto-covariance is bounded.
     *
     * @return True if the auto-covariance function is bounded, which means that
     * the auto-regressive polynomial is 1; false otherwise.
     */
    public boolean hasBound() {
        return ar.degree() + 1 == 1;
    }
}
