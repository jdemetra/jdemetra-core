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

package demetra.html.stats;

import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTag;
import demetra.timeseries.TsData;
import java.io.IOException;
import jdplus.stats.DescriptiveStatistics;

/**
 *
 * @author Jean Palate
 */
public class HtmlTsDataDifferenceDocument extends AbstractHtmlElement implements HtmlElement{
    
    private final boolean mul;
    private final TsData differences;
    
    public HtmlTsDataDifferenceDocument(TsData s0, TsData s1, boolean mul){
        this.mul=mul;
        differences=mul? (TsData.divide(s0, s1).fn(z->z-1))
                : TsData.subtract(s0, s1);
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        DescriptiveStatistics stats = DescriptiveStatistics.of(differences.getValues());
        stream.write(HtmlTag.IMPORTANT_TEXT, mul ? "Relative differences" : "Differences").newLines(2);
        stream.write("Max :").write(stats.getMax()).newLines(1);
        stream.write("Min :").write(stats.getMin()).newLines(1);
        stream.write("Average :").write(stats.getAverage()).newLines(1);
        stream.write("Stdev :").write(stats.getStdev()).newLines(1);
    }
    
}
