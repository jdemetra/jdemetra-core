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

import ec.tss.Ts;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlTsDifferenceDocument extends AbstractHtmlElement implements IHtmlElement{
    
    private final Ts s0_, s1_;
    private final boolean mul_;
    private final TsData diff_;
    
    public HtmlTsDifferenceDocument(Ts s0, Ts s1, boolean mul){
        s0_=s0;
        s1_=s1;
        mul_=mul;
        diff_=mul? (TsData.divide(s0.getTsData(), s1.getTsData()).minus(1))
                : TsData.subtract(s0.getTsData(), s1.getTsData());
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        DescriptiveStatistics stats = new DescriptiveStatistics(diff_);
        stream.write(HtmlTag.IMPORTANT_TEXT, mul_ ? "Relative differences" : "Differences").newLines(2);
        stream.write("Max :").write(stats.getMax()).newLines(1);
        stream.write("Min :").write(stats.getMin()).newLines(1);
        stream.write("Average :").write(stats.getAverage()).newLines(1);
        stream.write("Stdev :").write(stats.getStdev()).newLines(1);
    }
    
}
