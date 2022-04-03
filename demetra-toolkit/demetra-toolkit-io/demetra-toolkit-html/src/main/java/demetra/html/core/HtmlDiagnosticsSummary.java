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
package demetra.html.core;

import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlStream;
import demetra.html.HtmlTag;
import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcQuality;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlDiagnosticsSummary extends AbstractHtmlElement {

    private List<ProcDiagnostic> diagnostics;

    public HtmlDiagnosticsSummary(List<ProcDiagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (diagnostics == null) {
            return;
        }
        stream.write(HtmlTag.IMPORTANT_TEXT, "summary").newLine();
        writeQuality(stream, null, ProcDiagnostic.summary(diagnostics), Double.NaN);
        stream.newLine().newLine();
        List<String> categories = categories();
        for (String c : categories) {
            stream.write(HtmlTag.IMPORTANT_TEXT, c).newLine();
            diagnostics.stream().filter(d -> d.getCategory().equals(c))
                    .forEach(d -> {
                        try {
                            writeQuality(stream, d.getDiagnostic(), d.getQuality(), d.getValue());
                            stream.newLine();
                        } catch (IOException ex) {
                            Logger.getLogger(HtmlDiagnosticsSummary.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
            stream.newLine();
        }
    }

    private List<String> categories() {
        List<String> cat = new ArrayList<>();
        for (ProcDiagnostic diag : diagnostics) {
            String c = diag.getCategory();
            if (!cat.contains(c)) {
                cat.add(c);
            }
        }
        return cat;
    }

    private void writeQuality(HtmlStream stream, String test, ProcQuality q, double val) throws IOException {
        if (q != ProcQuality.Undefined) {
            if (test != null) {
                stream.write("   " + test + ": ");
            }
            switch (q) {
                case Error:
                    stream.write(HtmlTag.IMPORTANT_TEXT, "Error" + (!Double.isNaN(val) ? " (" + df3.format(val) + ")" : ""), Bootstrap4.TEXT_INFO);
                    break;
                case Severe:
                    stream.write(HtmlTag.IMPORTANT_TEXT, "Severe" + (!Double.isNaN(val) ? " (" + df3.format(val) + ")" : ""), Bootstrap4.TEXT_DANGER);
                    break;
                case Bad:
                    stream.write("Bad" + (!Double.isNaN(val) ? " (" + df3.format(val) + ")" : ""), Bootstrap4.TEXT_DANGER);
                    break;
                case Uncertain:
                    stream.write("Uncertain" + (!Double.isNaN(val) ? " (" + df3.format(val) + ")" : ""), Bootstrap4.TEXT_WARNING);
                    break;
                case Good:
                    stream.write("Good" + (!Double.isNaN(val) ? " (" + df3.format(val) + ")" : ""), Bootstrap4.TEXT_SUCCESS);
                    break;
            }
        }
    }
}
