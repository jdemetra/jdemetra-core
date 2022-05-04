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
package demetra.tramoseats.io.html;

import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTag;
import demetra.html.core.HtmlProcessingLog;
import demetra.html.modelling.HtmlRegSarima;
import demetra.html.modelling.HtmlUcarima;
import demetra.html.core.HtmlDiagnosticsSummary;
import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcessingLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sarima.SarimaModel;
import jdplus.seats.SeatsResults;
import jdplus.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.TramoSeatsResults;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlTramoSeatsSummary extends AbstractHtmlElement implements HtmlElement {

    private final ProcessingLog infos_;
    private final RegSarimaModel preprocessing_;
    private final SeatsResults decomposition_;
    private final String[] names_;
    private final ArimaModel[] list_;
    private final List<ProcDiagnostic> diags_=new ArrayList<>();
    private final String title_;

    public HtmlTramoSeatsSummary(String title, TramoSeatsResults results, String[] names, ArimaModel[] list) {
        title_ = title;
        preprocessing_ = results.getPreprocessing();
        decomposition_ = results.getDecomposition();
        names_ = names;
        list_ = list;
        TramoSeatsFactory.INSTANCE.fillDiagnostics(diags_, results);
        infos_=results.getLog();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeTitle(stream);
        writeInformation(stream);
        if (preprocessing_ == null && decomposition_ == null)
            return;
        writePreprocessing(stream);
        writeDecomposition(stream);
        writeDiagnostics(stream);
    }

    private void writeTitle(HtmlStream stream) throws IOException {
        if (title_ != null) {
            stream.write(HtmlTag.HEADER1, title_).newLine();
        }
    }

    private void writeInformation(HtmlStream stream) throws IOException {
        stream.write(new HtmlProcessingLog(infos_));
    }

    private void writePreprocessing(HtmlStream stream) throws IOException {
        if (preprocessing_ == null)
            return;
        stream.write(HtmlTag.HEADER2, "Pre-processing (Tramo)").newLine();
        stream.write(new HtmlRegSarima(preprocessing_, true));
    }

    private void writeDecomposition(HtmlStream stream) throws IOException {
        if (decomposition_ == null)
            return;
        stream.write(HtmlTag.HEADER2, "Decomposition (Seats)").newLine();
        SarimaModel tmodel = preprocessing_.arima();
        IArimaModel smodel = decomposition_.getUcarimaModel().getModel();
        if (tmodel == null || smodel == null) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "No decomposition").newLine();
        } else {
            boolean changed = !ArimaModel.same(tmodel, smodel, 1e-4);
            if (changed) {
                stream.write(HtmlTag.IMPORTANT_TEXT, "Model changed by Seats").newLine();
            }

            UcarimaModel ucm = decomposition_.getUcarimaModel();
            HtmlUcarima arima = new HtmlUcarima(ucm.getModel(), list_, names_);
            arima.writeSummary(stream);
            stream.newLine();
        }
    }

    private void writeDiagnostics(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Diagnostics").newLine();
        stream.write(new HtmlDiagnosticsSummary(diags_));
    }
}
