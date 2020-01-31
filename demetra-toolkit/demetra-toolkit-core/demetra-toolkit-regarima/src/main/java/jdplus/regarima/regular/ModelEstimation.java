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
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.LikelihoodStatistics;
import jdplus.stats.tests.NiidTests;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.Matrix;
import jdplus.regarima.RegArimaModel;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder
@Deprecated
public class ModelEstimation {

    private RegArimaModel<SarimaModel> regarima;
    private ConcentratedLikelihoodWithMissing concentratedLikelihood;
    private LikelihoodStatistics statistics;
    private DoubleSeq score;
    private Matrix parametersCovariance;
    private NiidTests tests;
}
