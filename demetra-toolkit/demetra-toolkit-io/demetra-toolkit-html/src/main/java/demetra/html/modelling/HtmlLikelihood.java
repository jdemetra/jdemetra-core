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
package demetra.html.modelling;

import demetra.likelihood.LikelihoodStatistics;
import demetra.html.HtmlElement;
import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlStream;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlLikelihood extends AbstractHtmlElement implements HtmlElement {

    private final LikelihoodStatistics stats;

    /**
     *
     * @param stats
     */
    public HtmlLikelihood(LikelihoodStatistics stats) {
        this.stats = stats;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write("Number of effective observations = ").write(
                stats.getEffectiveObservationsCount()).newLine();

        stream.write("Number of estimated parameters = ").write(
                stats.getEstimatedParametersCount()).newLines(2);
        stream.write("Loglikelihood = ").write(stats.getLogLikelihood()).newLine();
        if (stats.getTransformationAdjustment() != 0
                && !Double.isNaN(stats.getTransformationAdjustment())) {
            stream.write("Transformation adjustment = ").write(
                    stats.getTransformationAdjustment()).newLine();
            stream.write("Adjusted loglikelihood = ").write(
                    stats.getAdjustedLogLikelihood()).newLines(2);
        }
        double stde = Math.sqrt(stats.getSsqErr() / stats.getEffectiveObservationsCount());
        stream.write("Standard error of the regression (ML estimate) = ")
                .write(stde).newLine();
        stream.write("AIC = ").write(stats.getAIC()).newLine();
        stream.write("AICC = ").write(stats.getAICC()).newLine();
        //stream.write("BIC = ").write(stats.BIC).newLine();
        stream.write("BIC (corrected for length) = ").write(stats.getBICC()).newLine();
        //stream.write("Hannan-Quinn = ").write(stats.HannanQuinn).newLines(2);
    }
}
