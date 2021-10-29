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
package demetra.sa.html;

import demetra.arima.SarimaOrders;
import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlClass;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTag;
import demetra.stats.StatisticalTest;
import java.io.IOException;
import java.util.function.IntToDoubleFunction;
import jdplus.sa.tests.FTest;
import jdplus.sa.tests.KruskalWallis;
import jdplus.sa.tests.SeasonalityTests;
import jdplus.sa.tests.SpectralPeaks;
import demetra.stats.AutoCovariances;

/**
 *
 * @author Jean Palate
 */
public class HtmlSeasonalityDiagnostics extends AbstractHtmlElement implements HtmlElement {

    private final SeasonalityTests tests;
    private final StatisticalTest ftest, ftestAMI;
    private final boolean noSeasControl;
    private final StatisticalTest kwTest;

    public HtmlSeasonalityDiagnostics(final SeasonalityTests tests) {
        this(tests, false);
    }

    public HtmlSeasonalityDiagnostics(final SeasonalityTests tests, final boolean noSeasControl) {
        this.tests = tests;
        this.noSeasControl = noSeasControl;
        if (tests != null) {
            int period = tests.getPeriod();
            ftest = new FTest(tests.getDifferencing().getRestrictedOriginal(), period)
                    .model(SarimaOrders.Prespecified.D1).build();
            kwTest = new KruskalWallis(tests.getDifferencing().getDifferenced(), period).build();
        } else {
            ftest = null;
            kwTest = null;
        }
        // Not implemented yet
        ftestAMI = null;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (tests != null) {
            writeSummary(stream);
            writeQS(stream);
            writeFriedman(stream);
            writeKruskalWallis(stream);
            writeSpectrum(stream);
            writePeriodogram(stream);
            writeFTest(stream);
            writeFTestAMI(stream);
        } else {
            stream.write("Series can't be tested");
        }
    }

    public void writeTransformation(HtmlStream stream) throws IOException {
        if (tests.getDifferencing().isMeanCorrection()&& tests.getDifferencing().getDifferencingOrder() == 1) {
            stream.write(HtmlTag.EMPHASIZED_TEXT, "Data have been differenced and corrected for mean").newLines(2);
        } else if (tests.getDifferencing().getDifferencingOrder() > 0) {
            stream.write(HtmlTag.EMPHASIZED_TEXT, "Data have been differenced " + tests.getDifferencing().getDifferencingOrder() + " times").newLine();
            if (tests.getDifferencing().isMeanCorrection()) {
                stream.write(HtmlTag.EMPHASIZED_TEXT, "Data have been corrected for mean").newLine();
            }
            stream.newLine();
        }
    }

    public void writeQS(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "1. Tests on autocorrelations at seasonal lags").newLine();
        writeSummary(stream, tests.getQs().getPvalue());
        stream.newLines(2);
        IntToDoubleFunction ac = AutoCovariances.autoCorrelationFunction(tests.getDifferencing().getDifferenced(), 0);
        int period = tests.getPeriod();
        stream.write("ac(").write(period).write(")=").write(df4.format(ac.applyAsDouble(period))).newLine();
        stream.write("ac(").write(2 * period).write(")=").write(df4.format(ac.applyAsDouble(2*period))).newLines(2);
        stream.write("Distribution: " + tests.getQs().getDescription()).newLine();
        stream.write("Value: " + df4.format(tests.getQs().getValue())).newLine();
        stream.write("PValue: " + df4.format(tests.getQs().getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeFTest(HtmlStream stream) throws IOException {
        if (ftest == null)
            return;
        stream.write(HtmlTag.HEADER4, "6. Tests on regression with fixed seasonal dummies ").newLine();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Data have been differenced and corrected for mean").newLine();
        writeSummary(stream, ftest .getPvalue());
        stream.newLines(2);
        stream.write("Distribution: " + ftest.getDescription()).newLine();
        stream.write("Value: " + df4.format(ftest.getValue())).newLine();
        stream.write("PValue: " + df4.format(ftest.getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeFTestAMI(HtmlStream stream) throws IOException {
        if (ftestAMI == null)
            return;
//        stream.write(HtmlTag.HEADER4, "6bis. Tests on regression with fixed seasonal dummies ").newLine();
//        stream.write(HtmlTag.EMPHASIZED_TEXT, "Regression model (on original series) with ARIMA automatically identified").newLine();
//        stream.write(HtmlTag.EMPHASIZED_TEXT, "model is: " + ftestAMI.getEstimatedModel().model.getArima().toString()).newLine();
//        writeSummary(stream, ftestAMI.getFTest().getPValue());
//        stream.newLines(2);
//        stream.write("Distribution: " + ftestAMI.getFTest().getDistribution().getDescription()).newLine();
//        stream.write("Value: " + df4.format(ftestAMI.getFTest().getValue())).newLine();
//        stream.write("PValue: " + df4.format(ftestAMI.getFTest().getPValue()));
//        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeFriedman(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "2. Non parametric (Friedman) test");
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Based on the rank of the observations in each year").newLines(2);
        writeSummary(stream, tests.getNonParametricTest().getPvalue());
        stream.newLine();
        stream.write("Distribution: " + tests.getNonParametricTest().getDescription()).newLine();
        stream.write("Value: " + df4.format(tests.getNonParametricTest().getValue())).newLine();
        stream.write("PValue: " + df4.format(tests.getNonParametricTest().getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeKruskalWallis(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "3. Non parametric (Kruskal-Wallis) test");
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Based on the rank of the observations").newLines(2);
        writeSummary(stream, kwTest.getPvalue());
        stream.newLine();
        stream.write("Distribution: " + kwTest.getDescription()).newLine();
        stream.write("Value: " + df4.format(kwTest.getValue())).newLine();
        stream.write("PValue: " + df4.format(kwTest.getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeSpectrum(HtmlStream stream) throws IOException {
        if (tests.getSpectralPeaks() == null) {
            return;
        }
        stream.write(HtmlTag.HEADER4, "4. Identification of seasonal peaks in a Tukey periodogram and in an auto-regressive spectrum");
        int diag = 1;
        if (SpectralPeaks.hasHighSeasonalPeaks(tests.getSpectralPeaks())) {
            diag = -1;
        } else if (SpectralPeaks.hasSeasonalPeaks(tests.getSpectralPeaks())) {
            diag = 0;
        }
        stream.newLine();
        writeSummary(stream, diag);
        stream.newLines(2);
        stream.write(HtmlTag.EMPHASIZED_TEXT, "T or t for Tukey periodogram, A or a for auto-regressive spectrum; 'T' or 'A' for very signficant peaks, 't' or 'a' for signficant peaks, '_' otherwise").newLines(2);
        stream.newLine();
        stream.write(SpectralPeaks.format(tests.getSpectralPeaks()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writePeriodogram(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "5. Periodogram");
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Test on the sum of the values of a periodogram at seasonal frequencies").newLines(2);
        stream.newLine();
        StatisticalTest test = tests.getPeriodogramTest();
        writeSummary(stream, test.getPvalue());
        stream.newLines(2);
        stream.write("Distribution: " + test.getDescription()).newLine();
        stream.write("Value: " + df4.format(test.getValue())).newLine();
        stream.write("PValue: " + df4.format(test.getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    private void writeSummary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "Summary");
        writeTransformation(stream);
        stream.open(new HtmlTable().withWidth(300));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Test").withWidth(250));
        stream.write(new HtmlTableCell("Seasonality").withWidth(50));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("1. Auto-correlations at seasonal lags").withWidth(250));
        stream.write(getCellSummary(tests.getQs().getPvalue(), 50));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("2. Friedman (non parametric)").withWidth(250));
        stream.write(getCellSummary(tests.getNonParametricTest().getPvalue(), 50));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("3. Kruskall-Wallis (non parametric)").withWidth(250));
        stream.write(getCellSummary(kwTest.getPvalue(), 50));
        stream.close(HtmlTag.TABLEROW);

        if (tests.getSpectralPeaks() != null) {
            int diag = 1;
            if (SpectralPeaks.hasHighSeasonalPeaks(tests.getSpectralPeaks())) {
                diag = -1;
            } else if (SpectralPeaks.hasSeasonalPeaks(tests.getSpectralPeaks())) {
                diag = 0;
            }
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("4. Spectral peaks").withWidth(250));
            stream.write(getCellSummary(diag, 50));
            stream.close(HtmlTag.TABLEROW);
        }

//        TsData ddata = tests.getDifferencing().getDifferenced();
//        int ifreq = ddata.getFrequency().intValue();
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("5. Periodogram ").withWidth(250));
        stream.write(getCellSummary(tests.getPeriodogramTest().getPvalue(), 50));
        stream.close(HtmlTag.TABLEROW);
//        stream.open(HtmlTag.TABLEROW);
//        stream.write(new HtmlTableCell("5bis. Max Periodogram ", 250));
//        stream.write(getCellSummary(PeriodogramTest.computeMax(ddata, ifreq), 50));
//        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("6. Seasonal dummies").withWidth(250));
        stream.write(getCellSummary(ftest.getPvalue(), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
//        stream.write(new HtmlTableCell("6bis. Seasonal dummies (AMI)").withWidth(250));
//        stream.write(getCellSummary(ftestAMI.getFTest().getPValue(), 50));
//        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);
        stream.write(HtmlTag.LINEBREAK);
    }

    private void writeSummary(HtmlStream stream, double pval) throws IOException {
        int val = (pval > .05) ? 1 : ((pval > .01) ? 0 : -1);
        writeSummary(stream, val);
    }

    private void writeSummary(HtmlStream stream, int val) throws IOException {
        if (noSeasControl) {
            if (val < 0) {
                stream.write(HtmlTag.IMPORTANT_TEXT, "Seasonality present", Bootstrap4.TEXT_DANGER);
            } else if (val == 0) {
                stream.write("Seasonality perhaps present", Bootstrap4.TEXT_WARNING);
            } else {
                stream.write("Seasonality not present", Bootstrap4.TEXT_SUCCESS);
            }
        } else if (val < 0) {
            stream.write("Seasonality present", Bootstrap4.TEXT_SUCCESS);
        } else if (val == 0) {
            stream.write("Seasonality perhaps present", Bootstrap4.TEXT_WARNING);
        } else {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Seasonality not present", Bootstrap4.TEXT_DANGER);
        }

    }

    private HtmlTableCell getCellSummary(double pval, int l) throws IOException {
        int val = (pval > .05) ? 1 : ((pval > .01) ? 0 : -1);
        return getCellSummary(val, l);
    }

    private HtmlTableCell getCellSummary(int val, int l) throws IOException {
        HtmlClass style;
        String txt;
        if (noSeasControl) {
            if (val < 0) {
                txt = "YES";
                style = Bootstrap4.TEXT_DANGER;
            } else if (val == 0) {
                txt = "?";
                style = Bootstrap4.TEXT_WARNING;
            } else {
                txt = "NO";
                style = Bootstrap4.TEXT_SUCCESS;
            }
        } else if (val < 0) {
            txt = "YES";
            style = Bootstrap4.TEXT_SUCCESS;
        } else if (val == 0) {
            txt = "?";
            style = Bootstrap4.TEXT_WARNING;
        } else {
            txt = "NO";
            style = Bootstrap4.TEXT_DANGER;
        }
        return new HtmlTableCell(txt).withWidth(l).withClass(style);
    }
}
