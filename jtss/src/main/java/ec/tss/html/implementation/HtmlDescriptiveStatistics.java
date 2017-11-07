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
import java.io.IOException;
import ec.tstoolkit.data.DescriptiveStatistics;

/**
 *
 * @author Jean Palate
 */
public class HtmlDescriptiveStatistics extends AbstractHtmlElement {

    private final String header;
    private final DescriptiveStatistics stats;
    private final boolean summary;

    public HtmlDescriptiveStatistics(final DescriptiveStatistics stats, final String header, final boolean summary) {
        this.stats = stats;
        this.header = header;
        this.summary = summary;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (header != null) {
            stream.write(HtmlTag.HEADER1, header).newLine();
        }
        if (summary)
            writeSummary(stream);
        else
            writeComplete(stream);
    }

    private void writeSummary(HtmlStream stream) throws IOException {
        stream.write("Average: ").write(df4.format(stats.getAverage())).newLine();
        stream.write("Standard deviation: ").write(df4.format(stats.getStdev())).newLine();
        stream.write("Min: ").write(df4.format(stats.getMin())).newLine();
        stream.write("Max: ").write(df4.format(stats.getMax())).newLine();
    }

    private void writeComplete(HtmlStream stream) throws IOException {
        writeSummary(stream);
    }
}
