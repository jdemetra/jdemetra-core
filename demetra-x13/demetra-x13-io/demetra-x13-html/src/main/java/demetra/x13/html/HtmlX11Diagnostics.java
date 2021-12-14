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
package demetra.x13.html;

import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTag;
import java.io.IOException;
import jdplus.x11.X11Utility;
import jdplus.x13.Mstatistics;

/**
 *
 * @author Jean Palate
 */
public class HtmlX11Diagnostics extends AbstractHtmlElement {

    Mstatistics mstats;

    public HtmlX11Diagnostics(Mstatistics mstats) {
        this.mstats = mstats;
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
        writeF2I(stream);

    }

    private void writeF2A(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, mstats.getMode().isMultiplicative() ? F2A_TITLE_MUL : F2A_TITLE_ADD);
        double[][] Q = new double[10][];
        Q[0] = mstats.getOcChanges();
        Q[1] = mstats.getCIcChanges();
        Q[2] = mstats.getIcChanges();
        Q[3] = mstats.getCcChanges();
        Q[4] = mstats.getScChanges();
        Q[5] = mstats.getPChanges();
        Q[6] = mstats.getTDChanges();
        Q[7] = mstats.getOmodChanges();
        Q[8] = mstats.getCImodChanges();
        Q[9] = mstats.getImodChanges();

        int len = Q[0].length;

        stream.open(new HtmlTable().withWidth(50 * F2A_HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < F2A_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell(F2A_HEADERS[j]).withWidth(50));
        }
        stream.close(HtmlTag.TABLEROW);
        for (int i = 1; i <= len; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(50));
            for (int j = 0; j < Q.length; ++j) {
                if (Q[j] != null) {
                    stream.write(new HtmlTableCell(df2.format(Q[j][i - 1])).withWidth(50));
                } else {
                    stream.write(new HtmlTableCell(".").withWidth(50));
                }
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2B(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, mstats.getMode().isMultiplicative() ? F2B_TITLE_MUL : F2B_TITLE_ADD);
        double[][] Q = new double[5][];
        Q[0] = mstats.getImodChanges();
        Q[1] = mstats.getCcChanges();
        Q[2] = mstats.getScChanges();
        Q[3] = mstats.getPChanges();
        Q[4] = mstats.getTDChanges();

        double[] O = mstats.getOmodChanges();

        int len = Q[0].length;

        stream.open(new HtmlTable().withWidth(50 * F2B_HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < F2B_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell(F2B_HEADERS[j]).withWidth(50));
        }
        stream.close(HtmlTag.TABLEROW);
        for (int i = 1; i <= len; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(50));
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
                    stream.write(new HtmlTableCell(df2.format(100 * s * s / tot)).withWidth(50));
                } else {
                    stream.write(new HtmlTableCell(".").withWidth(50));
                }
            }
            stream.write(new HtmlTableCell(df2.format(100)).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(100 * tot / (O[i - 1] * O[i - 1]))).withWidth(50));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2C(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, mstats.getMode().isMultiplicative() ? F2C_TITLE_MUL : F2C_TITLE_ADD);
        stream.open(new HtmlTable().withWidth(50 + 100 * F2C_HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(50));
        for (int j = 0; j < F2C_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell("").withWidth(50));
            stream.write(new HtmlTableCell(F2C_HEADERS[j]).withWidth(50).withClass(Bootstrap4.TEXT_LEFT));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Span").withWidth(50));
        for (int j = 0; j < F2C_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell("Avg").withWidth(50));
            stream.write(new HtmlTableCell("S.D.").withWidth(50));
        }
        stream.close(HtmlTag.TABLEROW);

        boolean mul = mstats.getMode().isMultiplicative();
        int period=mstats.period();
        double[][] QO = X11Utility.calcVariations(mstats.getOc().getValues(), period, mul, mstats.validObservations());
        double[][] QI = X11Utility.calcVariations(mstats.getIc().getValues(), period, mul, mstats.validObservations());
        double[][] QC = X11Utility.calcVariations(mstats.getCc().getValues(), period, mul, mstats.validObservations());
        double[][] QS = X11Utility.calcVariations(mstats.getSc().getValues(), period, mul, mstats.validObservations());
        double[][] QCI = X11Utility.calcVariations(mstats.getCIc().getValues(), period, mul, mstats.validObservations());

        int len = QO[0].length;

        for (int i = 1; i <= len; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QO[0][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QO[1][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QI[0][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QI[1][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QC[0][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QC[1][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QS[0][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QS[1][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QCI[0][i - 1])).withWidth(50));
            stream.write(new HtmlTableCell(df2.format(QCI[1][i - 1])).withWidth(50));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2D(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, F2D_TITLE);
        stream.open(new HtmlTable().withWidth(100));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("CI").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(mstats.getAdrOfCI())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("I").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(mstats.getAdrOfI())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("C").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(mstats.getAdrOfC())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2E(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, F2E_TITLE);
        double[] I = mstats.getIcChanges();
        double[] C = mstats.getCcChanges();
        boolean lt1 = false;
        stream.open(new HtmlTable().withWidth(100));
        for (int i = 1; i <= I.length; ++i) {
            double r = I[i - 1] / C[i - 1];
            boolean bold = (!lt1 && r < 1) || (r < 1 && i == 1);
            stream.open(HtmlTag.TABLEROW);
            if (bold) {
                stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(50).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
                stream.write(new HtmlTableCell(df3.format(r)).withWidth(50).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
                lt1 = true;
            } else {
                stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(50));
                stream.write(new HtmlTableCell(df3.format(r)).withWidth(50));
            }

            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
        stream.write(HtmlTag.IMPORTANT_TEXT, F2E_TITLE2).write(df3.format(mstats.getIcr()));
        stream.newLines(2);
    }

    private void writeF2F(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, F2F_TITLE);
        stream.open(new HtmlTable().withWidth(100));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("I").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(100 * mstats.getVarI())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("C").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(100 * mstats.getVarC())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("S").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(100 * mstats.getVarS())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("P").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(100 * mstats.getVarP())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("TD&H").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(100 * mstats.getVarTD())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Total").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(100 * mstats.getVarTotal())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2G(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, F2G_TITLE);
        double[] c = mstats.getAutoCorrelationsOfIrregular();
        stream.open(new HtmlTable().withWidth(100));
        for (int i = 1; i <= c.length; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(50));
            stream.write(new HtmlTableCell(df3.format(c[i - 1])).withWidth(50));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

    private void writeF2H(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, F2H_TITLE);
        //  stream.write("Cochran Test Result:");
        boolean testResultCochran = mstats.getCochranResult();
        stream.open(new HtmlTable().withWidth(30 + 120 * F2H_HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < F2H_HEADERS.length; ++j) {
            stream.write(new HtmlTableCell(F2H_HEADERS[j]).withWidth(120));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(Double.toString(Math.round(mstats.getTestValue() * 10000d) / 10000d)).withWidth(120));
        stream.write(new HtmlTableCell(Double.toString(Math.round(mstats.getCriticalValue() * 10000d) / 10000d)).withWidth(120));

        if (testResultCochran) {
            stream.write(new HtmlTableCell("Null hypothesis is not rejected.").withWidth(150));
        } else {
            stream.write(new HtmlTableCell("Null hypothesis is rejected.").withWidth(150));
        }

        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);

//        stream.write(" The test statistic: ");
//        stream.write(Math.round(mstats.getTestValue() * 10000d) / 10000d);
//
//        if (testResultCochran) {
//            stream.write(" is equal or less than the critical value: ");
//        } else {
//            stream.write(" is greater than the critical value: ");
//        }
//        stream.write(Math.round(mstats.getCriticalValue() * 10000d) / 10000d);
//
//        if (testResultCochran) {
//            stream.write(" the null hypothesis for identical variances of each period with at least ");
//            stream.write(mstats.getminNumberOfYears());
//            stream.write(" observations, cannot be rejected at a 95% level of confidence and non period-specific variance should be used. ");
//        } else {
//            stream.write(" the null hypothesis for identical variances of each period with at least ");
//            stream.write(mstats.getminNumberOfYears());
//            stream.write(" observations, has to be rejected at a 95% level of confidence and periode-specific variances should be used. ");
//        }
        stream.newLines(2);
    }

    private void writeF2I(HtmlStream stream) throws IOException {
        if (mstats.getRms() != null) {
            HtmlMovingSeasonalityRatios hmsr = new HtmlMovingSeasonalityRatios(mstats.getRms());
            hmsr.write(stream);
        }
    }

    private static final String F2A_TITLE_MUL = "Average percent change without regard to sign over the indicated span",
            F2B_TITLE_MUL = "Relative contributions to the variance of the percent change in the components of the original series",
            F2C_TITLE_MUL = "Average percent change with regard to sign and standard deviation over indicated span",
            F2A_TITLE_ADD = "Average differences without regard to sign over the indicated span",
            F2B_TITLE_ADD = "Relative contributions to the variance of the differences in the components of the original series",
            F2C_TITLE_ADD = "Average differences with regard to sign and standard deviation over indicated span",
            F2D_TITLE = "Average duration of run",
            F2E_TITLE = "I/C Ratio for indicated span",
            F2E_TITLE2 = "I/C Ratio: ",
            F2F_TITLE = "Relative contribution of the components to the stationary portion of the variance in the original series",
            F2G_TITLE = "Autocorrelation of the irregular",
            F2H_TITLE = "Heteroskedasticity (Cochran test on equal variances within each period)";

    private static final String[] F2A_HEADERS = new String[]{
        "Span", "O", "CI", "I", "C", "S", "P", "TD&H", "Mod.O", "Mod.CI", "Mod.I"
    };
    private static final String[] F2B_HEADERS = new String[]{
        "Span", "I", "C", "S", "P", "TD&H", "Total", "Ratio"
    };
    private static final String[] F2C_HEADERS = new String[]{
        "O", "I", "C", "S", "CI"
    };

    private static final String[] F2H_HEADERS = new String[]{"Test statistic", "Critical value (5% level)", "Decision"};
}
