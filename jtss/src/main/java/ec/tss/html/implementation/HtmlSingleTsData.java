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

import ec.tss.html.CssProperty;
import ec.tss.html.CssStyle;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.timeseries.simplets.YearIterator;
import java.io.IOException;
import java.util.Formatter;

/**
 * 
 * @author Jean Palate
 */
public class HtmlSingleTsData implements IHtmlElement {

    private CssStyle rowHeaders;
    private String fmt = "%.3f";
    private TsData m_ts;
    private String m_name;
    /**
     *
     */
    public static final CssStyle defaultrowHeaders = new CssStyle();

    static {
        defaultrowHeaders.add(CssProperty.FONT_STYLE, "italic");
        defaultrowHeaders.add(CssProperty.MARGIN_LEFT, "10 px");
        defaultrowHeaders.add(CssProperty.MARGIN_RIGHT, "10 px");
    }

    /**
     * 
     * @param ts
     * @param name
     */
    public HtmlSingleTsData(TsData ts, String name) {
        m_ts = ts;
        m_name = name;
    }

    /**
     * 
     * @return
     */
    public String getFormat() {
        return fmt;
    }

    /**
     * 
     * @return
     */
    public CssStyle getRowHeadersStyle() {
        return rowHeaders;
    }

    /**
     * 
     * @param fmt
     */
    public void setFormat(String fmt) {
        this.fmt = fmt;
    }

    /**
     * 
     * @param rowHeaders
     */
    public void setRowHeadersStyle(final CssStyle rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        if (m_name != null) {
            stream.write(HtmlTag.HEADER3, m_name);
        }
        // year x periods
        stream.open(new HtmlTable(0, 100));
        stream.open(HtmlTag.TABLEHEADER);
        for (int i = 0; i < m_ts.getFrequency().intValue(); ++i) {
            stream.write(HtmlTag.TABLECELL, TsPeriod.formatShortPeriod(m_ts.getFrequency(), i));
        }
        stream.close(HtmlTag.TABLEHEADER);

        int nfreq = m_ts.getFrequency().intValue();
        YearIterator iter = new YearIterator(m_ts);
        while (iter.hasMoreElements()) {
            stream.open(HtmlTag.TABLEROW);
            TsDataBlock block = iter.nextElement();
            stream.write(HtmlTag.TABLECELL,
                    rowHeaders == null ? defaultrowHeaders : rowHeaders,
                    Integer.toString(block.start.getYear()));
            int start = block.start.getPosition();
            int n = block.data.getLength();
            for (int i = 0; i < start; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            for (int i = 0; i < n; ++i) {
                if (Double.isFinite(block.data.get(i))) {
                    Formatter formatter = new Formatter();
                    formatter.format(fmt, block.data.get(i));
                    stream.write(HtmlTag.TABLECELL, formatter.toString());
                } else {
                    stream.write(HtmlTag.TABLECELL, ".");
                }
            }

            for (int i = start + n; i < nfreq; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
    }
}
