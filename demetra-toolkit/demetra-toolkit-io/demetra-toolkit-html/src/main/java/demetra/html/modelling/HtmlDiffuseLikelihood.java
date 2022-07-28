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

import demetra.html.HtmlElement;
import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlStream;
import java.io.IOException;
import jdplus.stats.likelihood.DiffuseLikelihoodStatistics;

/**
 *
 * @author Jean Palate
 */
public class HtmlDiffuseLikelihood extends AbstractHtmlElement implements HtmlElement {

    private final DiffuseLikelihoodStatistics stats;

    /**
     *
     * @param stats
     */
    public HtmlDiffuseLikelihood(DiffuseLikelihoodStatistics stats) {
        this.stats = stats;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        if (stats.getDiffuseCount() > 0) {
            stream.write("Number of observations = ").write(
                    stats.getObservationsCount()).newLine();
            stream.write("Number of diffuse effects = ").write(
                    stats.getDiffuseCount()).newLine();
        }
        stream.write("Number of effective observations = ").write(
                stats.getEffectiveObservationsCount()).newLine();

        stream.write("Number of estimated parameters = ").write(
                stats.getEstimatedParametersCount()).newLines(2);
        stream.write("Loglikelihood = ").write(df6.format(stats.getLogLikelihood())).newLine();
        if (stats.getDiffuseCount() > 0) {
            stream.write("Diffuse correction = ").write(
                    df6.format(stats.getDiffuseCorrection())).newLine();
        }
        if (stats.getTransformationAdjustment() != 0
                && !Double.isNaN(stats.getTransformationAdjustment())) {
            stream.write("Transformation adjustment = ").write(
                    df6.format(stats.getTransformationAdjustment())).newLine();
            stream.write("Adjusted loglikelihood = ").write(
                    df6.format(stats.getAdjustedLogLikelihood())).newLines(2);
        }
        double stde = Math.sqrt(stats.getSsqErr() / stats.getEffectiveObservationsCount());
        stream.write("Standard error of the regression (ML estimate) = ")
                .write(dg6.format(stde)).newLine();
        stream.write("AIC = ").write(df6.format(stats.aic())).newLine();
        stream.write("AICC = ").write(df6.format(stats.aicc())).newLine();
        stream.write("BIC = ").write(df6.format(stats.bic())).newLine();
    }
}
