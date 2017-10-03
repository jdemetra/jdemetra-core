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
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.timeseries.analysis.Differenciation;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlDifference extends AbstractHtmlElement implements IHtmlElement {

    private String[] names_ = new String[]{"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    private Differenciation diff_;
    private Differenciation.DifferentiationType type_;
    private int years_;
    private double limit_;

    public HtmlDifference(Differenciation diff, Differenciation.DifferentiationType type, int years, double limit) {
        diff_ = diff;
        type_ = type;
        years_ = years;
        limit_ = limit;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeDifference(stream);
    }

    private void writeDifference(HtmlStream stream) throws IOException {
        stream.open(new HtmlTable().withWidth(630));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(150));
        for (String names_1 : names_) {
            stream.write(new HtmlTableCell(names_1).withWidth(40));
        }
        stream.close(HtmlTag.TABLEROW);

        TsData data = diff_.getDifference(type_);
        TsData new_ = diff_.getNew();
        TsData old_ = diff_.getOld();
        int year = data.getDomain().getEnd().getYear() - years_;
        for (int i = 0; i < years_; i++) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(year + i) + " new"));
            for (int j = 0; j < 12; j++) {
                stream.write(new HtmlTableCell(Double.toString(new_.get(j + (i * 12)))));
            }
            stream.close(HtmlTag.TABLEROW);

            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(year + i) + " old"));
            for (int j = 0; j < 12; j++) {
                stream.write(new HtmlTableCell(Double.toString(old_.get(j + (i * 12)))));
            }
            stream.close(HtmlTag.TABLEROW);

            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(year + i) + " diff"));
            for (int j = 0; j < 12; j++) {
                switch (type_) {
                    case Difference: {
                        stream.write(new HtmlTableCell(dg2.format(data.get(j + (i * 12)))).withWidth(100));
                        break;
                    }
                    case Percentage: {
                        stream.write(new HtmlTableCell(df2.format(data.get(j + (i * 12))) + "%").withWidth(100));
                        break;
                    }
                }
            }
            stream.close(HtmlTag.TABLEROW);
        }
    }
}
