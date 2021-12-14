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
package demetra.x13.html;

import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTag;
import demetra.html.modelling.HtmlRegArima;
import demetra.x11.X11Results;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlX13Summary extends AbstractHtmlElement  {

//    private final List<ProcessingInformation> infos_;
//    private final PreprocessingModel preprocessing_;
//    private final X11Results decomposition_;
//    private final InformationSet diags_;
//    private final String title_;
//
//    public HtmlX13Summary(String title, CompositeResults results, InformationSet diags) {
//        title_ = title;
//        infos_ = results.getProcessingInformation();
//        preprocessing_ = GenericSaResults.getPreprocessingModel(results);
//        decomposition_ = GenericSaResults.getDecomposition(results, X11Results.class);
//        if (diags != null) {
//            diags_ = diags;
//        } else {
//            diags_ = SaManager.createDiagnostics(results);
//        }
//    }

    @Override
    public void write(HtmlStream stream) throws IOException {
//        writeTitle(stream);
//        writeInformation(stream);
//        if (preprocessing_ == null && decomposition_ == null)
//            return;
//        writePreprocessing(stream);
//        writeDiagnostics(stream);
    }

//    private void writeTitle(HtmlStream stream) throws IOException {
//        if (title_ != null) {
//            stream.write(HtmlTag.HEADER1, title_).newLine();
//        }
//    }
//
//    private void writeInformation(HtmlStream stream) throws IOException {
//        stream.write(new HtmlProcessingInformation(infos_));
//    }
//
//    private void writePreprocessing(HtmlStream stream) throws IOException {
//        if (preprocessing_ == null) {
//            stream.write(HtmlTag.HEADER2, "No pre-processing").newLine();
//        } else {
//            stream.write(HtmlTag.HEADER2, "Pre-processing (RegArima)").newLine();
//            stream.write(new HtmlRegArima(preprocessing_, true));
//        }
//    }
//
//    private void writeDecomposition(HtmlStream stream) throws IOException {
//    }
//
//    private void writeDiagnostics(HtmlStream stream) throws IOException {
//        stream.write(HtmlTag.HEADER2, "Diagnostics").newLine();
//        stream.write(new HtmlDiagnosticSummary(diags_));
//    }
}
