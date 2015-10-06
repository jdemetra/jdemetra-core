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

import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.satoolkit.diagnostics.PeriodogramTest;
import ec.tss.html.*;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityTests;
import ec.tstoolkit.modelling.arima.tramo.SpectralPeaks;
import ec.tstoolkit.stats.AutoCorrelations;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlSeasonalityDiagnostics extends AbstractHtmlElement implements IHtmlElement {

    private final SeasonalityTests tests;
    private final FTest ftest, ftestAMI;
    private final boolean noSeasControl;
    private final KruskalWallisTest kwTest;

    public HtmlSeasonalityDiagnostics(final SeasonalityTests tests) {
        this(tests, false);
    }

    public HtmlSeasonalityDiagnostics(final SeasonalityTests tests, final boolean noSeasControl) {
        this.tests = tests;
        this.noSeasControl = noSeasControl;
        if (tests != null) {
            ftest = new FTest();
            ftest.test(tests.getDifferencing().original);
            ftestAMI = new FTest();
            ftestAMI.testAMI(tests.getDifferencing().original);
            kwTest = new KruskalWallisTest(tests.getDifferencing().differenced);
        } else {
            ftest = null;
            ftestAMI=null;
            kwTest = null;
        }

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
        if (tests.getDifferencing().mean && tests.getDifferencing().getDifferencingOrder() == 1) {
            stream.write("Data have been differenced and corrected for mean", HtmlStyle.Italic).newLines(2);
        } else {
            if (tests.getDifferencing().getDifferencingOrder() > 0) {
                stream.write("Data have been differenced " + tests.getDifferencing().getDifferencingOrder() + " times", HtmlStyle.Italic).newLine();
                if (tests.getDifferencing().mean) {
                    stream.write("Data have been corrected for mean", HtmlStyle.Italic).newLine();
                }
                stream.newLine();
            }
        }
    }

    public void writeQS(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "1. Tests on autocorrelations at seasonal lags").newLine();
        writeSummary(stream, tests.getQs().getPValue());
        stream.newLines(2);
        AutoCorrelations ac = new AutoCorrelations(tests.getDifferencing().differenced);
        int ifreq = tests.getDifferencing().original.getFrequency().intValue();
        stream.write("ac(").write(ifreq).write(")=").write(df4.format(ac.autoCorrelation(ifreq))).newLine();
        stream.write("ac(").write(2 * ifreq).write(")=").write(df4.format(ac.autoCorrelation(2 * ifreq))).newLines(2);
        stream.write("Distribution: " + tests.getQs().getDistribution().getDescription()).newLine();
        stream.write("Value: " + df4.format(tests.getQs().getValue())).newLine();
        stream.write("PValue: " + df4.format(tests.getQs().getPValue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeFTest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "6. Tests on regression with fixed seasonal dummies ").newLine();
        stream.write("Regression model (on original series) with (0 1 1)(0 0 0) noises + mean", HtmlStyle.Italic).newLine();
        writeSummary(stream, ftest.getFTest().getPValue());
        stream.newLines(2);
        stream.write("Distribution: " + ftest.getFTest().getDistribution().getDescription()).newLine();
        stream.write("Value: " + df4.format(ftest.getFTest().getValue())).newLine();
        stream.write("PValue: " + df4.format(ftest.getFTest().getPValue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeFTestAMI(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "6bis. Tests on regression with fixed seasonal dummies ").newLine();
        stream.write("Regression model (on original series) with ARIMA automatically identified", HtmlStyle.Italic).newLine();
        stream.write("model is: "+ftestAMI.getEstimatedModel().model.getArima().toString(), HtmlStyle.Italic).newLine();
        writeSummary(stream, ftestAMI.getFTest().getPValue());
        stream.newLines(2);
        stream.write("Distribution: " + ftestAMI.getFTest().getDistribution().getDescription()).newLine();
        stream.write("Value: " + df4.format(ftestAMI.getFTest().getValue())).newLine();
        stream.write("PValue: " + df4.format(ftestAMI.getFTest().getPValue()));
        stream.write(HtmlTag.LINEBREAK);
    }
    public void writeFriedman(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "2. Non parametric (Friedman) test");
        stream.write("Based on the rank of the observations in each year", HtmlStyle.Italic).newLines(2);
        writeSummary(stream, tests.getNonParametricTest().getPValue());
        stream.newLine();
        stream.write("Distribution: " + tests.getNonParametricTest().getDistribution().getDescription()).newLine();
        stream.write("Value: " + df4.format(tests.getNonParametricTest().getValue())).newLine();
        stream.write("PValue: " + df4.format(tests.getNonParametricTest().getPValue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeKruskalWallis(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "3. Non parametric (Kruskal-Wallis) test");
        stream.write("Based on the rank of the observations", HtmlStyle.Italic).newLines(2);
        writeSummary(stream, kwTest.getPValue());
        stream.newLine();
        stream.write("Distribution: " + kwTest.getDistribution().getDescription()).newLine();
        stream.write("Value: " + df4.format(kwTest.getValue())).newLine();
        stream.write("PValue: " + df4.format(kwTest.getPValue()));
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
        stream.write("T or t for Tukey periodogram, A or a for auto-regressive spectrum; 'T' or 'A' for very signficant peaks, 't' or 'a' for signficant peaks, '_' otherwise", HtmlStyle.Italic).newLines(2);
        stream.newLine();
        stream.write(SpectralPeaks.format(tests.getSpectralPeaks()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writePeriodogram(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "5. Periodogram");
        stream.write("Test on the sum of the values of a periodogram at seasonal frequencies", HtmlStyle.Italic).newLines(2);
        stream.newLine();
        StatisticalTest test = tests.getPeriodogramTest();
        writeSummary(stream, test.getPValue());
        stream.newLines(2);
        stream.write("Distribution: " + test.getDistribution().getDescription()).newLine();
        stream.write("Value: " + df4.format(test.getValue())).newLine();
        stream.write("PValue: " + df4.format(test.getPValue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    private void writeSummary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "Summary");
        writeTransformation(stream);
        stream.open(new HtmlTable(0, 300));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Test", 250));
        stream.write(new HtmlTableCell("Seasonality", 50));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("1. Auto-correlations at seasonal lags", 250));
        stream.write(getCellSummary(tests.getQs().getPValue(), 50));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("2. Friedman (non parametric)", 250));
        stream.write(getCellSummary(tests.getNonParametricTest().getPValue(), 50));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("3. Kruskall-Wallis (non parametric)", 250));
        stream.write(getCellSummary(kwTest.getPValue(), 50));
        stream.close(HtmlTag.TABLEROW);

        if (tests.getSpectralPeaks() != null) {
            int diag = 1;
            if (SpectralPeaks.hasHighSeasonalPeaks(tests.getSpectralPeaks())) {
                diag = -1;
            } else if (SpectralPeaks.hasSeasonalPeaks(tests.getSpectralPeaks())) {
                diag = 0;
            }
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("4. Spectral peaks", 250));
            stream.write(getCellSummary(diag, 50));
            stream.close(HtmlTag.TABLEROW);
        }

        TsData ddata=tests.getDifferencing().differenced;
//        int ifreq = ddata.getFrequency().intValue();
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("5. Periodogram ", 250));
        stream.write(getCellSummary(tests.getPeriodogramTest().getPValue(), 50));
        stream.close(HtmlTag.TABLEROW);
//        stream.open(HtmlTag.TABLEROW);
//        stream.write(new HtmlTableCell("5bis. Max Periodogram ", 250));
//        stream.write(getCellSummary(PeriodogramTest.computeMax(ddata, ifreq), 50));
//        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("6. Seasonal dummies", 250));
        stream.write(getCellSummary(ftest.getFTest().getPValue(), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("6bis. Seasonal dummies (AMI)", 250));
        stream.write(getCellSummary(ftestAMI.getFTest().getPValue(), 50));
        stream.close(HtmlTag.TABLEROW);

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
                stream.write("Seasonality present", HtmlStyle.Bold, HtmlStyle.Danger);
            } else if (val == 0) {
                stream.write("Seasonality perhaps present", HtmlStyle.Warning);
            } else {
                stream.write("Seasonality not present", HtmlStyle.Success);
            }
        } else if (val < 0) {
            stream.write("Seasonality present", HtmlStyle.Success);
        } else if (val == 0) {
            stream.write("Seasonality perhaps present", HtmlStyle.Warning);
        } else {
            stream.write("Seasonality not present", HtmlStyle.Bold, HtmlStyle.Danger);
        }

    }

    private HtmlTableCell getCellSummary(double pval, int l) throws IOException {
        int val = (pval > .05) ? 1 : ((pval > .01) ? 0 : -1);
        return getCellSummary(val, l);
    }

    private HtmlTableCell getCellSummary(int val, int l) throws IOException {
        HtmlStyle[] style;
        String txt;
        if (noSeasControl) {
            if (val < 0) {
                txt = "YES";
                style = new HtmlStyle[]{HtmlStyle.Danger, HtmlStyle.Bold};
            } else if (val == 0) {
                txt = "?";
                style = new HtmlStyle[]{HtmlStyle.Warning};
            } else {
                txt = "NO";
                style = new HtmlStyle[]{HtmlStyle.Success};
            }
        } else {
            if (val < 0) {
                txt = "YES";
                style = new HtmlStyle[]{HtmlStyle.Success};
            } else if (val == 0) {
                txt = "?";
                style = new HtmlStyle[]{HtmlStyle.Warning};
            } else {
                txt = "NO";
                style = new HtmlStyle[]{HtmlStyle.Danger, HtmlStyle.Bold};
            }
        }
        return new HtmlTableCell(txt, l, style);
    }
}
