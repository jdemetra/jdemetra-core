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
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlTramo extends AbstractHtmlElement implements IHtmlElement {

    private PreprocessingModel model_;

    public HtmlTramo(PreprocessingModel model) {
        model_ = model;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeLogLevel(stream);
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

        Double level = tinfo.get("level", Double.class);
        Double log = tinfo.get("log", Double.class);
        if (level != null && log != null) {
            stream.write(HtmlTag.HEADER1, h1, "Log/level transformation");
            stream.newLine();
            stream.write(HtmlTag.HEADER2, h2, "Objective function for level: ").write(level.toString()).newLine();
            stream.write(HtmlTag.HEADER2, h2, "Objective function for log: ").write(log.toString()).newLine();
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
        stream.write(HtmlTag.HEADER1, h1, "Trading days");
        stream.newLine();
        stream.write(HtmlTag.LINEBREAK);
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
        stream.write(HtmlTag.HEADER1, h1, "Easter effect");
        stream.write(HtmlTag.LINEBREAK);
    }

}
