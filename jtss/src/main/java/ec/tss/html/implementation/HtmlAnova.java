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
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tstoolkit.stats.Anova;
import ec.tstoolkit.stats.Anova.Row;
import ec.tstoolkit.stats.StatisticalTest;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class HtmlAnova extends AbstractHtmlElement {

    private final Anova anova;
    private final String[] titles;

    public HtmlAnova(Anova anova, String[] titles) {
        this.anova = anova;
        this.titles = titles;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.open(new HtmlTable(0, 600));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Degrees of freedom", 100));
        stream.write(new HtmlTableCell("Sum of squares", 100));
        stream.write(new HtmlTableCell("Mean square", 100));
        stream.write(new HtmlTableCell("F value", 100));
        stream.write(new HtmlTableCell("Pr(>F)", 100));
        stream.close(HtmlTag.TABLEROW);
        List<Row> rows = anova.getRows();
        int idx = 0;
        for (Row row : rows) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell((titles != null && idx < titles.length) ? titles[idx] : "", 100));
            stream.write(new HtmlTableCell(Integer.toString(row.df), 100));
            stream.write(new HtmlTableCell(df2.format(row.ssq), 100));
            stream.write(new HtmlTableCell(df2.format(row.mssq()), 100));
            StatisticalTest ftest = row.ftest();
            stream.write(new HtmlTableCell(df2.format(ftest.getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(ftest.getPValue()), 100));
            stream.close(HtmlTag.TABLEROW);
            ++idx;
        }
        stream.close(HtmlTag.TABLE);
    }
}
