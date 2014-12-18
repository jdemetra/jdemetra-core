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
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class HtmlX13Preprocessing extends AbstractHtmlElement {

    private PreprocessingModel model_;

    public HtmlX13Preprocessing(PreprocessingModel model) {
        model_ = model;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeLogLevel(stream);
        writeCalendar(stream);
        writeEaster(stream);
    }

    private void writeLogLevel(HtmlStream stream) throws IOException {
        // log/level
        InformationSet pinfo = model_.info_;
        if (pinfo == null) {
            return;
        }
        InformationSet tinfo = pinfo.getSubSet(PreprocessingDictionary.TRANSFORMATION);
        if (tinfo == null) {
            return;
        }
        LikelihoodStatistics levelStats = tinfo.get("stats_level", LikelihoodStatistics.class);
        LikelihoodStatistics logStats = tinfo.get("stats_log", LikelihoodStatistics.class);
        if (levelStats != null && logStats != null) {
            stream.write(HtmlTag.HEADER1, h1, "Log/level transformation");
            stream.newLine();
            stream.write(HtmlTag.HEADER2, h2, "Likelihood statistics for model fit to untransformed series.");
            stream.write(new HtmlLikelihood(levelStats));
            stream.write(HtmlTag.HEADER2, h2, "Likelihood statistics for model fit to log transformed series.");
            stream.write(new HtmlLikelihood(logStats));
            stream.write(HtmlTag.LINEBREAK);
        }
    }

    private void writeCalendar(HtmlStream stream) throws IOException {
        // log/level
        InformationSet pinfo = model_.info_;
        if (pinfo == null) {
            return;
        }
        InformationSet cinfo = pinfo.getSubSet(PreprocessingDictionary.CALENDAR);
        if (cinfo == null) {
            return;
        }
        LikelihoodStatistics tdStats = cinfo.get("stats_td", LikelihoodStatistics.class);
        LikelihoodStatistics ntdStats = cinfo.get("stats_ntd", LikelihoodStatistics.class);
        if (tdStats != null && ntdStats != null) {
            stream.write(HtmlTag.HEADER1, h1, "Trading days");
            stream.newLine();
            stream.write(HtmlTag.HEADER2, h2, "Likelihood statistics for model fit without td.");
            stream.write(new HtmlLikelihood(ntdStats));
            stream.write(HtmlTag.HEADER2, h2, "Likelihood statistics for model fit with td.");
            stream.write(new HtmlLikelihood(tdStats));
            stream.write(HtmlTag.LINEBREAK);
        }
    }

    private void writeEaster(HtmlStream stream) throws IOException {
        // log/level
        InformationSet pinfo = model_.info_;
        if (pinfo == null) {
            return;
        }
        InformationSet einfo = pinfo.getSubSet(PreprocessingDictionary.EASTER);
        if (einfo == null) {
            return;
        }
        List<Information<LikelihoodStatistics>> stats = einfo.select(LikelihoodStatistics.class);
        if (stats == null || stats.isEmpty()) {
            return;
        }
        stream.write(HtmlTag.HEADER1, h1, "Easter effect");
        for (Information<LikelihoodStatistics> linfo : stats) {
            stream.newLine();
            stream.write(HtmlTag.HEADER2, h2, "Likelihood statistics for model fit with " + linfo.name.substring(6));
            stream.write(new HtmlLikelihood(linfo.value));
        }
        stream.write(HtmlTag.LINEBREAK);
    }
}
