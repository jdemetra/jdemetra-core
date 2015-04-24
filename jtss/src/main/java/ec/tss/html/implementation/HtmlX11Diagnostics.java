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

import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.SeriesEvolution;
import ec.satoolkit.x11.SigmavecOption;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import java.io.IOException;

/**
 *
 * @author pcuser
 */
public class HtmlX11Diagnostics extends AbstractHtmlElement {

    Mstatistics stats_;

    public HtmlX11Diagnostics(Mstatistics mstats) {
        stats_ = mstats;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeF2A(stream);
        writeF2B(stream);
        writeF2C(stream);
        writeF2D(stream);
        writeF2E(stream);
        writeF2F(stream);
        writeF2G(stream);
        writeF2H(stream);
        
    }

    private void writeF2A(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, stats_.getMode().isMultiplicative() ? F2A_TITLE_MUL : F2A_TITLE_ADD);
        double[][] Q = new double[10][];
        Q[0] = stats_.getOcChanges();
        Q[1] = stats_.getCIcChanges();
        Q[2] = stats_.getIcChanges();
        Q[3] = stats_.getCcChanges();
        Q[4] = stats_.getScChanges();
        Q[5] = stats_.getPChanges();
        Q[6] = stats_.getTDChanges();
        Q[7] = stats_.getOmodChanges();
        Q[8] = stats_.getCImodChanges();
        Q[9] = stats_.getImodChanges();

        int len = Q[0].length;

        stream.open(new HtmlTable(0, 50 * F2A_HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < F2A_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell(F2A_HEADERS[j], 50));
        }
        stream.close(HtmlTag.TABLEROW);
        for (int i = 1; i <= len; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i), 50));
            for (int j = 0; j < Q.length; ++j) {
                if (Q[j] != null) {
                    stream.write(new HtmlTableCell(df2.format(Q[j][i - 1]), 50));
                } else {
                    stream.write(new HtmlTableCell(".", 50));
                }
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2B(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, stats_.getMode().isMultiplicative() ? F2B_TITLE_MUL : F2B_TITLE_ADD);
        double[][] Q = new double[5][];
        Q[0] = stats_.getImodChanges();
        Q[1] = stats_.getCcChanges();
        Q[2] = stats_.getScChanges();
        Q[3] = stats_.getPChanges();
        Q[4] = stats_.getTDChanges();

        double[] O = stats_.getOmodChanges();

        int len = Q[0].length;

        stream.open(new HtmlTable(0, 50 * F2B_HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < F2B_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell(F2B_HEADERS[j], 50));
        }
        stream.close(HtmlTag.TABLEROW);
        for (int i = 1; i <= len; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i), 50));
            double tot = 0;
            for (int j = 0; j < Q.length; ++j) {
                if (Q[j] != null) {
                    double s = Q[j][i - 1];
                    s *= s;
                    tot += s;
                }
            }
            for (int j = 0; j < Q.length; ++j) {
                if (Q[j] != null) {
                    double s = Q[j][i - 1];
                    stream.write(new HtmlTableCell(df2.format(100 * s * s / tot), 50));
                } else {
                    stream.write(new HtmlTableCell(".", 50));
                }
            }
            stream.write(new HtmlTableCell(df2.format(100), 50));
            stream.write(new HtmlTableCell(df2.format(100 * tot / (O[i - 1] * O[i - 1])), 50));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2C(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, stats_.getMode().isMultiplicative() ? F2C_TITLE_MUL : F2C_TITLE_ADD);
        stream.open(new HtmlTable(0, 50 + 100 * F2C_HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 50));
        for (int j = 0; j < F2C_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell("", 50));
            stream.write(new HtmlTableCell(F2C_HEADERS[j], 50, HtmlStyle.Left));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Span", 50));
        for (int j = 0; j < F2C_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell("Avg", 50));
            stream.write(new HtmlTableCell("S.D.", 50));
        }
        stream.close(HtmlTag.TABLEROW);

        boolean mul = stats_.getMode().isMultiplicative();
        double[][] QO = SeriesEvolution.calcVariations(stats_.getOc(), null, mul);
        double[][] QI = SeriesEvolution.calcVariations(stats_.getIc(), null, mul);
        double[][] QC = SeriesEvolution.calcVariations(stats_.getCc(), null, mul);
        double[][] QS = SeriesEvolution.calcVariations(stats_.getSc(), null, mul);
        double[][] QCI = SeriesEvolution.calcVariations(stats_.getCIc(), null, mul);

        int len = QO[0].length;

        for (int i = 1; i <= len; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i), 50));
            stream.write(new HtmlTableCell(df2.format(QO[0][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QO[1][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QI[0][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QI[1][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QC[0][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QC[1][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QS[0][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QS[1][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QCI[0][i - 1]), 50));
            stream.write(new HtmlTableCell(df2.format(QCI[1][i - 1]), 50));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2D(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, F2D_TITLE);
        stream.open(new HtmlTable(0, 100));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("CI", 50));
        stream.write(new HtmlTableCell(df2.format(stats_.getAdrOfCI()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("I", 50));
        stream.write(new HtmlTableCell(df2.format(stats_.getAdrOfI()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("C", 50));
        stream.write(new HtmlTableCell(df2.format(stats_.getAdrOfC()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2E(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, F2E_TITLE);
        double[] I = stats_.getIcChanges();
        double[] C = stats_.getCcChanges();
        boolean lt1 = false;
        stream.open(new HtmlTable(0, 100));
        for (int i = 1; i <= I.length; ++i) {
            double r = I[i - 1] / C[i - 1];
            boolean bold = (!lt1 && r < 1) || (r < 1 && i == 1);
            stream.open(HtmlTag.TABLEROW);
            if (bold) {
                stream.write(new HtmlTableCell(Integer.toString(i), 50, HtmlStyle.Bold));
                stream.write(new HtmlTableCell(df3.format(r), 50, HtmlStyle.Bold));
                lt1 = true;
            } else {
                stream.write(new HtmlTableCell(Integer.toString(i), 50));
                stream.write(new HtmlTableCell(df3.format(r), 50));
            }

            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2F(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, F2F_TITLE);
        stream.open(new HtmlTable(0, 100));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("I", 50));
        stream.write(new HtmlTableCell(df2.format(100 * stats_.getVarI()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("C", 50));
        stream.write(new HtmlTableCell(df2.format(100 * stats_.getVarC()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("S", 50));
        stream.write(new HtmlTableCell(df2.format(100 * stats_.getVarS()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("P", 50));
        stream.write(new HtmlTableCell(df2.format(100 * stats_.getVarP()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("TD&H", 50));
        stream.write(new HtmlTableCell(df2.format(100 * stats_.getVarTD()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Total", 50));
        stream.write(new HtmlTableCell(df2.format(100 * stats_.getVarTotal()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2G(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, F2G_TITLE);
        double[] c = stats_.getAutoCorrelationsOfIrregular();
        stream.open(new HtmlTable(0, 100));
        for (int i = 1; i <= c.length; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i), 50));
            stream.write(new HtmlTableCell(df3.format(c[i - 1]), 50));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }
    
     private void writeF2H(HtmlStream stream) throws IOException {
         stream.write(HtmlTag.HEADER2, h2, F2H_TITLE);
         stream.write("Cochran Test Result:");
         boolean testResultCochran = stats_.getCochranResult();
         stream.write(" The test statistic: ");
         stream.write(Math.round(stats_.getTestValue()* 10000d) / 10000d);

         if (testResultCochran) {
             stream.write(" is equal or less than the critical value: ");
         } else {
             stream.write(" is greater than the critical value: ");
         }
         stream.write(Math.round(stats_.getCriticalValue()* 10000d) / 10000d);

         if (testResultCochran) {
             stream.write(" the null hypothesis for identical variances of each period with at least ");
             stream.write(stats_.getminNumberOfYears());
             stream.write(" observations, cannot be rejected at a 95% level of confidence and non period-specific variance should be used. ");
         } else {
             stream.write(" the null hypothesis for identical variances of each period with at least ");
                stream.write(stats_.getminNumberOfYears());
                stream.write(" observations, has to be rejected at a 95% level of confidence and periode-specific variances should be used. ");}

         stream.newLines(2);
    }
    
    private static final String F2A_TITLE_MUL = "Average percent change without regard to sign over the indicated span",
            F2B_TITLE_MUL = "Relative contributions to the variance of the percent change in the components of the original series",
            F2C_TITLE_MUL = "Average percent change with regard to sign and standard deviation over indicated span",
            F2A_TITLE_ADD = "Average differences without regard to sign over the indicated span",
            F2B_TITLE_ADD = "Relative contributions to the variance of the differences in the components of the original series",
            F2C_TITLE_ADD = "Average differences with regard to sign and standard deviation over indicated span",
            F2D_TITLE = "Average duration of run",
            F2E_TITLE = "I/C Ratio for indicated span",
            F2F_TITLE = "Relative contribution of the components to the stationary portion of the variance in the original series",
            F2G_TITLE = "Autocorrelation of the irregular",
            F2H_TITLE = "Heteroskedasticity";
        
    private static final String[] F2A_HEADERS = new String[]{
        "Span", "O", "CI", "I", "C", "S", "P", "TD&H", "Mod.O", "Mod.CI", "Mod.I"
    };
    private static final String[] F2B_HEADERS = new String[]{
        "Span", "I", "C", "S", "P", "TD&H", "Total", "Ratio"
    };
    private static final String[] F2C_HEADERS = new String[]{
        "O", "I", "C", "S", "CI"
    };
}
