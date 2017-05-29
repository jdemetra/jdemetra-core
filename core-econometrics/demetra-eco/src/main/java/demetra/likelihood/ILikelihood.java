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
package demetra.likelihood;

import demetra.design.Development;
import demetra.data.Doubles;

/**
 * The ILikelihood interface formalizes the likelihood function of an usual
 * gaussian model, in its log form. We suppose that the density of the model can
 * be expressed, after transformations, as p(y) = ... see documentation. The
 * Sigma parameter is concentrated out of the likelihood. The likelihood can
 * then be expressed as -0.5*(constant + logdeterminant + ResidualsCount *
 * log(sigma)) Sigma is defined as SsqErr / ResidualsCount
 *
 */
@Development(status = Development.Status.Release)
public interface ILikelihood {

    /**
     * Aikake Information Criterion for a given number of (hyper-)parameters
     * AIC=2*nparams-2*ll
     *
     * @param nparams The number of parameters
     * @return The AIC. Models with lower AIC shoud be preferred.
     */
    default double AIC(final int nparams) {
        return -2 * getLogLikelihood() + 2 * nparams;
    }

    /**
     *
     * @param nparams
     * @return
     */
    default double BIC(final int nparams) {
        return -2 * getLogLikelihood() + nparams * Math.log(getN());
    }

    /**
     * Return the log-determinant
     *
     * @return
     */
    double getLogDeterminant();

    /**
     * @return The determinantal factor (n-th root).
     */
    double getFactor();

    /**
     * @return Log of the likelihood
     */
    double getLogLikelihood();

    /**
     * @return Square root of Sigma.
     */
    int getN();

    /**
     * @return The Standardized innovations
     */
    Doubles getResiduals();

    /**
     * Gets the ML estimate of the standard error of the model. ser=sqrt(ssq/n)
     *
     * @return A positive number.
     */
    default double getSer() {
        return Math.sqrt(getSsqErr() / getN());
    }

    /**
     * Gets the ML estimate of the variance of the model. sigma=ssq/n
     *
     * @return A positive number.
     */
    default double getSigma() {
        return getSsqErr() / getN();
    }

    /**
     * @return Sum of the squared standardized innovations
     */
    double getSsqErr();

}
