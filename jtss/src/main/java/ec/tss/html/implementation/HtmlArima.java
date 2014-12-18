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
import ec.tstoolkit.arima.IArimaModel;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.IHtmlElement;
import java.io.IOException;
import java.util.Formatter;

/**
 * 
 * @author Jean Palate & BAYENSK
 */
public class HtmlArima extends AbstractHtmlElement implements IHtmlElement {

    private IArimaModel m_model;
    static final String AR = "AR:   ", D = "D:   ", MA = "MA:   ", VAR = "Innovation variance: ";
    static final double EPS = 1e-6;

    /**
     * 
     * @param model
     */
    public HtmlArima(IArimaModel model) {
        m_model = model;
    }

    /**
     * 
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        writeModel(stream);
    }

    public void writeModel(HtmlStream stream) throws IOException {
        if (m_model.getStationaryARCount() > 0) {
            stream.write(AR).write(m_model.getStationaryAR().toString()).newLine();
        }
        if (m_model.getNonStationaryARCount() > 0) {
            stream.write(D).write(m_model.getNonStationaryAR().toString()).newLine();
        }
        if (m_model.getMACount() > 0) {
            stream.write(MA).write(m_model.getMA().toString()).newLine();
        }
        double var = m_model.getInnovationVariance();
        if (Math.abs(var - 1) > EPS) {
            String val = new Formatter().format("%.5f", Double.valueOf(var)).toString();
            stream.write(VAR).write(val, HtmlStyle.Bold).newLine();
        }
    }

    public void writeShortModel(HtmlStream stream) throws IOException {
        double var = m_model.getInnovationVariance();
        if (Math.abs(var - 1) > EPS) {
            String val = new Formatter().format("%.5f", Double.valueOf(var)).toString();
            stream.write(". " + VAR).write(val);
        }
    }
}
