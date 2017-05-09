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
package ec.tss.html.implementation;

import ec.tss.html.AbstractHtmlElement;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlLikelihood extends AbstractHtmlElement implements IHtmlElement {

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
        if (stats.missingCount > 0) {
            stream.write("Number of missing observations = ").write(
                    stats.missingCount).newLine();
        }
        stream.write("Number of effective observations = ").write(
                stats.effectiveObservationsCount).newLine();

        stream.write("Number of estimated parameters = ").write(
                stats.estimatedParametersCount).newLines(2);
        stream.write("Loglikelihood = ").write(stats.logLikelihood).newLine();
        if (stats.transformationAdjustment != 0
                && !Double.isNaN(stats.transformationAdjustment)) {
            stream.write("Transformation adjustment = ").write(
                    stats.transformationAdjustment).newLine();
            stream.write("Adjusted loglikelihood = ").write(
                    stats.adjustedLogLikelihood).newLines(2);
        }
        double stde = Math.sqrt(stats.SsqErr / stats.effectiveObservationsCount);
        stream.write("Standard error of the regression (ML estimate) = ")
                .write(stde).newLine();
        stream.write("AIC = ").write(stats.AIC).newLine();
        stream.write("AICC = ").write(stats.AICC).newLine();
        //stream.write("BIC = ").write(stats.BIC).newLine();
        stream.write("BIC (corrected for length) = ").write(stats.BICC).newLine();
        //stream.write("Hannan-Quinn = ").write(stats.HannanQuinn).newLines(2);
    }
}
