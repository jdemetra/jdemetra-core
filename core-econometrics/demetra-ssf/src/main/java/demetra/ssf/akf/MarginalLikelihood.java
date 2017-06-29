/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.akf;

import demetra.data.DataBlock;
import demetra.likelihood.ILikelihood;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
public class MarginalLikelihood implements ILikelihood {

    /**
     * Respectively: diffuse log-likelihood sum of the squared residuals log
     * determinant of the cov matrix diffuse correction
     */
    private double ll,

    /**
     * Respectively: diffuse log-likelihood sum of the squared e log
 determinant of the cov matrix diffuse correction
     */
    ssqerr, 

    /**
     * Respectively: diffuse log-likelihood sum of the squared e log
 determinant of the cov matrix diffuse correction
     */
    ldet, 

    /**
     * Respectively: diffuse log-likelihood sum of the squared e log
 determinant of the cov matrix diffuse correction
     */
    dcorr, 

    /**
     * Respectively: diffuse log-likelihood sum of the squared e log
 determinant of the cov matrix diffuse correction
     */
    mcorr;

    private int nobs, nd;

    /**
     *
     */
    public MarginalLikelihood() {
    }

    private int m() {
        return nobs - nd;
    }

    /**
     *
     */
    public void clear() {
        ll = 0;
        ssqerr = 0;
        ldet = 0;
        dcorr = 0;
        mcorr = 0;
        nobs = 0;
        nd = 0;
    }

    /**
     *
     * @return
     */
    public int getD() {
        return nd;
    }

    @Override
    public double factor() {
        return Math.exp((ldet + dcorr - mcorr) / (m()));
    }

    @Override
    public double logLikelihood() {
        return ll;
    }

    @Override
    public int dim() {
        return nobs;
    }

    @Override
    public Doubles e() {
        return DataBlock.EMPTY;
    }

    @Override
    public double logDeterminant() {
        return ldet;
    }

    /**
     *
     * @return
     */
    public double ser() {
        return Math.sqrt(ssqerr / (m()));
    }

    @Override
    public double sigma() {
        return ssqerr / (m());
    }

    @Override
    public double ssq() {
        return ssqerr;
    }

    /**
     * Adjust the likelihood if the data have been pre-multiplied by a given
     * scaling factor
     *
     * @param factor The scaling factor
     */
    public void rescale(final double factor) {
        if (factor == 1) {
            return;
        }
        ssqerr /= factor * factor;
        ll += (m()) * Math.log(factor);
    }

    public double getDiffuseCorrection() {
        return dcorr;
    }

    public double getMarginalCorrection() {
        return mcorr;
    }

    public double getMarginalLogLikelihood() {
        return ll + .5 * mcorr;
    }

    /**
     * Initialize the diffuse likelihood. We consider below the GLS problem
     * corresponding to a given state space: y = a * X + e, where X is derived
     * from the initial conditions and e ~ N(0, V)
     *
     * The diffuse likelihood is then:
     *
     * -0.5*(m*log(2*pi)+m*log(ssqerr/m)+m+log|V|+log|X'V^-1*X|-log|X'X| where
     * m=n-d
     *
     * It should be noted that the usual definition (implemented in JD+ 2.0) is
     * -0.5*(n*log(2*pi)+n*log(ssqerr/n)+n+log|V|+log|X'V^-1*X| The difference
     * is thus -0.5*(d*log(2*pi)+d*log(ssqerr)-n*log(n)+m*log(m))
     *
     * The new definition is more coherent with the marginal likelihood. See the
     * paper mentioned above.
     *
     * @param ssqerr The sum of the squared e
     * @param ldet The log of the determinant of V
     * @param dcorr Diffuse correction (= |X'*V^-1*X|)
     * @param xcorr Marginal correction (= |X'*X|)
     * @param n The number of observations
     * @param d The number of diffuse constraints
     * @return
     */
    public boolean set(final double ssqerr, final double ldet, final double dcorr, final double xcorr,
            final int n, final int d) {
        if (d == 0 && dcorr != 0) {
            return false;
        }
        this.ssqerr = ssqerr;
        this.ldet = ldet;
        this.dcorr = dcorr;
        this.mcorr = xcorr;
        this.nobs = n;
        nd = d;
        calcll();
        return true;
    }

    private void calcll() {
        int m = m();
        if (m <= 0) {
            return;
        }
        ll = -.5
                * (m * Math.log(2 * Math.PI) + m
                * (1 + Math.log(ssqerr / m)) + ldet + dcorr - mcorr);

    }

    public void add(ILikelihood ll) {
        nobs += ll.dim();
        ssqerr += ll.ssq();
        ldet += ll.logDeterminant();
        calcll();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ll=").append(this.logLikelihood()).append("\r\n");
        builder.append("n=").append(this.dim()).append("\r\n");
        builder.append("ssq=").append(this.ssq()).append("\r\n");
        builder.append("ldet=").append(this.logDeterminant()).append("\r\n");
        builder.append("dcorr=").append(this.getDiffuseCorrection()).append("\r\n");
        builder.append("mcorr=").append(this.getMarginalCorrection()).append("\r\n");
        return builder.toString();
    }

}
