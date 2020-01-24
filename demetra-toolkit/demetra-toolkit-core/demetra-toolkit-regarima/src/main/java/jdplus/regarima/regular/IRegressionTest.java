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


package jdplus.regarima.regular;

import demetra.design.Development;
import demetra.information.InformationSet;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface IRegressionTest {
    /**
     * Test on a linear regression model
     * @param ll The concentrated log-likelihood
     * @param nhp The number of hyper-parameters (outside regression variables)
     * @param ireg The position of the first tested variable 
     * (excluding missing values identified by additive outliers). The first position
     * @param nregs The number of tested variables
     * @param info To put additional information, if need be.
     * @return 
     */
    boolean accept(ConcentratedLikelihoodWithMissing ll, int nhp, int ireg, int nregs, InformationSet info);
}
