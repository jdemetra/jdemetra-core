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

import static ec.tss.html.Bootstrap4.FONT_WEIGHT_BOLD;
import ec.tss.html.*;
import ec.tss.sa.diagnostics.ResidualsDiagnosticsConfiguration;
import ec.tstoolkit.stats.*;
import java.io.IOException;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlNiidTest extends AbstractHtmlElement implements IHtmlElement {

    private NiidTests m_tests;
    private double badthreshold_ = ResidualsDiagnosticsConfiguration.NBAD;
    private double goodthreshold_ = ResidualsDiagnosticsConfiguration.NUNC;

    /**
     *
     * @param tests
     */
    public HtmlNiidTest(NiidTests tests) {
        m_tests = tests;

        // TODO Add config in constructor
        ResidualsDiagnosticsConfiguration config = new ResidualsDiagnosticsConfiguration();
        badthreshold_ = config.getNIIDBad();
        goodthreshold_ = config.getNIIDUncertain();
    }

    private void Distribution(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "1 - Distribution").newLine();
        stream.open(HtmlTag.DIV);

        stream.write(HtmlTag.IMPORTANT_TEXT, "Mean").newLines(2);
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Value"));
        stream.write(new HtmlTableHeader("Standard deviation"));
        stream.write(new HtmlTableHeader("T-Stat"));
        stream.write(new HtmlTableHeader("P-Value"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(df4.format(m_tests.getStatistics()
                .getAverage())).withWidth(100));
        double stdev = m_tests.getStatistics().getStdev();
        if (m_tests.getMeanTest() != null) {
            stream.write(new HtmlTableCell(df4.format(stdev)).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(m_tests.getMeanTest()
                    .getValue())).withWidth(100));
            double pval = m_tests.getMeanTest()
                    .getPValue();
            stream.write(new HtmlTableCell(df4.format(pval)).withWidth(100).withClass(PValue(pval)));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();

        stream.write(HtmlTag.IMPORTANT_TEXT, "Normality tests").newLines(2);
        stream.open(new HtmlTable().withWidth(500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Test"));
        stream.write(new HtmlTableHeader("Value"));
        stream.write(new HtmlTableHeader("P-Value"));
        stream.write(new HtmlTableHeader("Distribution"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Skewness").withWidth(100));
        if (m_tests.getSkewness() != null) {
            stream.write(new HtmlTableCell(df4.format(m_tests.getSkewness()
                    .getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(m_tests.getSkewness()
                    .getPValue())).withWidth(100));
            String ndist = "Normal(";
            ndist = ndist.concat(df2.format(m_tests.getSkewness()
                    .getDistribution().getExpectation()));
            ndist = ndist.concat(";");
            ndist = ndist.concat(df2.format(Math.sqrt(m_tests.getSkewness()
                    .getDistribution().getVariance())));
            ndist = ndist.concat(")");
            stream.write(new HtmlTableCell(ndist).withWidth(200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Kurtosis").withWidth(100));
        if (m_tests.getKurtosis() != null) {
            stream.write(new HtmlTableCell(df4.format(m_tests.getKurtosis()
                    .getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(m_tests.getKurtosis()
                    .getPValue())).withWidth(100));
            String ndist = "Normal(";
            ndist = ndist.concat(df2.format(m_tests.getKurtosis()
                    .getDistribution().getExpectation()));
            ndist = ndist.concat(";");
            ndist = ndist.concat(df2.format(Math.sqrt(m_tests.getKurtosis()
                    .getDistribution().getVariance())));
            ndist = ndist.concat(")");
            stream.write(new HtmlTableCell(ndist).withWidth(200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Joint-test").withWidth(100));
        if (m_tests.getNormalityTest() != null) {
            stream.write(new HtmlTableCell(df4.format(m_tests
                    .getNormalityTest().getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(m_tests
                    .getNormalityTest().getPValue())).withWidth(100));
            stream.write(new HtmlTableCell(m_tests.getNormalityTest()
                    .getDistribution().toString()).withWidth(200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);

        stream.close(HtmlTag.DIV).newLine();
    }

    private void DistributionSummary(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.DIV);
        stream.write(HtmlTag.HEADER3, "1. Normality of the residuals")
                .newLine();
        stream.open(new HtmlTable().withWidth(300));

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("P-value"));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Mean").withWidth(200));
        stream.write(new HtmlTableCell(df4.format(m_tests.getMeanTest()
                .getPValue())).withWidth(100).withClass(PValue(m_tests.getMeanTest().getPValue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Skewness").withWidth(200));
        stream.write(new HtmlTableCell(df4.format(m_tests.getSkewness()
                .getPValue())).withWidth(100).withClass(PValue(m_tests.getSkewness().getPValue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Kurtosis").withWidth(200));
        stream.write(new HtmlTableCell(df4.format(m_tests.getKurtosis()
                .getPValue())).withWidth(100).withClass(PValue(m_tests.getKurtosis().getPValue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Normality").withWidth(200));
        stream.write(new HtmlTableCell(df4.format(m_tests.getNormalityTest()
                .getPValue())).withWidth(100).withClass(PValue(m_tests.getNormalityTest().getPValue())));
        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Independence(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "2 - Independence tests").newLine();
        stream.open(HtmlTag.DIV);

        AutoCorrelations ac = m_tests.getAutoCorrelations();
        LjungBoxTest lb = m_tests.getLjungBox();
        BoxPierceTest bp = m_tests.getBoxPierce();
        if (ac != null && lb != null) {
            int k = lb.getK();

            stream.write(HtmlTag.IMPORTANT_TEXT, "Ljung-Box and Box-Pierce tests on residuals: ").newLines(2);
            stream.open(new HtmlTable().withWidth(700));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Lag").withWidth(100));
            stream.write(new HtmlTableHeader("Autocorrelation").withWidth(100));
            stream.write(new HtmlTableHeader("Standard deviation").withWidth(100));
            stream.write(new HtmlTableHeader("Ljung-Box test").withWidth(100));
            stream.write(new HtmlTableHeader("P-Value").withWidth(100));
            stream.write(new HtmlTableHeader("Box-Pierce test").withWidth(100));
            stream.write(new HtmlTableHeader("P-Value").withWidth(100));
            stream.close(HtmlTag.TABLEROW);

            for (int i = 1; i <= k; i++) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(100));
                double v = 1 / Math.sqrt(m_tests.getStatistics()
                        .getObservationsCount());
                boolean bold = Math.abs(ac.autoCorrelation(i)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i))).withWidth(100).withClass(FONT_WEIGHT_BOLD));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i))).withWidth(100));
                }
                stream.write(new HtmlTableCell(df4.format(v)).withWidth(100));
                if (i > m_tests.getHyperParametersCount()) {
                    lb.setK(i);
                    if (lb.isValid()) {
                        stream.write(new HtmlTableCell(df4
                                .format(lb.getValue())).withWidth(100));
                        stream.write(new HtmlTableCell(df4.format(lb
                                .getPValue())).withWidth(100).withClass(PValue(lb.getPValue())));
                    } else {
                        stream.write(new HtmlTableCell("").withWidth(100));
                        stream.write(new HtmlTableCell("").withWidth(100));
                    }
                    bp.setK(i);
                    if (bp.isValid()) {
                        stream.write(new HtmlTableCell(df4
                                .format(bp.getValue())).withWidth(100));
                        stream.write(new HtmlTableCell(df4.format(bp
                                .getPValue())).withWidth(100).withClass(PValue(bp.getPValue())));
                    } else {
                        stream.write(new HtmlTableCell("").withWidth(100));
                        stream.write(new HtmlTableCell("").withWidth(100));
                    }
                }
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE).newLine();
        }

        LjungBoxTest lbs = m_tests.getSeasonalLjungBox();
        BoxPierceTest bps = m_tests.getSeasonalBoxPierce();
        if (ac != null && lbs != null) {
            int ks = lbs.getK();

            stream.write(HtmlTag.IMPORTANT_TEXT, "Ljung-Box and Box-Pierce tests on seasonal residuals: ").newLines(2);
            stream.open(new HtmlTable().withWidth(700));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Lag").withWidth(100));
            stream.write(new HtmlTableHeader("Autocorrelation").withWidth(100));
            stream.write(new HtmlTableHeader("Standard deviation").withWidth(100));
            stream.write(new HtmlTableHeader("Ljung-Box test").withWidth(100));
            stream.write(new HtmlTableHeader("P-Value").withWidth(100));
            stream.write(new HtmlTableHeader("Box-Pierce test").withWidth(100));
            stream.write(new HtmlTableHeader("P-Value").withWidth(100));
            stream.close(HtmlTag.TABLEROW);

            int f = m_tests.getFrequency();
            for (int i = 1; i <= ks; ++i) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(Integer.toString(i * f)).withWidth(100));
                double v = 1 / Math.sqrt(m_tests.getStatistics()
                        .getObservationsCount());
                boolean bold = Math.abs(ac.autoCorrelation(i)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i * f))).withWidth(100).withClass(FONT_WEIGHT_BOLD));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i * f))).withWidth(100));
                }
                stream.write(new HtmlTableCell(df4.format(v)).withWidth(100));
                lbs.setK(i);
                if (lbs.isValid()) {
                    stream.write(new HtmlTableCell(df4.format(lbs
                            .getValue())).withWidth(100));
                    stream.write(new HtmlTableCell(df4.format(lbs
                            .getPValue())).withWidth(100).withClass(PValue(lbs.getPValue())));
                } else {
                    stream.write(new HtmlTableCell("").withWidth(100));
                    stream.write(new HtmlTableCell("").withWidth(100));
                }
                bps.setK(i);
                if (bps.isValid()) {
                    stream.write(new HtmlTableCell(df4.format(bps
                            .getValue())).withWidth(100));
                    stream.write(new HtmlTableCell(df4.format(bps
                            .getPValue())).withWidth(100).withClass(PValue(bps.getPValue())));
                } else {
                    stream.write(new HtmlTableCell("").withWidth(100));
                    stream.write(new HtmlTableCell("").withWidth(100));
                }
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
        }

        stream.close(HtmlTag.DIV).newLine();
    }

    private void IndependenceSummary(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.DIV);
        stream.write(HtmlTag.HEADER3, "2. Independence of the residuals")
                .newLine();
        stream.open(new HtmlTable().withWidth(300));

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("P-value"));
        stream.close(HtmlTag.TABLEROW);

        if (m_tests.getLjungBox() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream
                    .write(new HtmlTableCell("Ljung-Box("
                            + Integer.toString(m_tests.getLjungBox().getK())
                            + ")").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getLjungBox()
                    .getPValue())).withWidth(100).withClass(PValue(m_tests.getLjungBox().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getBoxPierce() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce("
                    + Integer.toString(m_tests.getBoxPierce().getK()) + ")").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getBoxPierce()
                    .getPValue())).withWidth(100).withClass(PValue(m_tests.getBoxPierce().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getSeasonalLjungBox() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Ljung-Box on seasonality("
                    + Integer.toString(m_tests.getSeasonalLjungBox().getK())
                    + ")").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(m_tests
                    .getSeasonalLjungBox().getPValue())).withWidth(100).withClass(PValue(m_tests.getSeasonalLjungBox().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getSeasonalBoxPierce() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce on seasonality("
                    + Integer.toString(m_tests.getSeasonalBoxPierce().getK())
                    + ")").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(m_tests
                    .getSeasonalBoxPierce().getPValue())).withWidth(100).withClass(PValue(m_tests.getSeasonalBoxPierce().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE).newLine();
        stream.write(
                "Durbin-Watson statistic: "
                + df4.format(m_tests.getAutoCorrelations()
                        .getDurbinWatson())).newLine();
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Linearity(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "4 - Linearity tests").newLine();
        stream.open(HtmlTag.DIV);

        AutoCorrelations ac = m_tests.getAutoCorrelationsOnSquare();
        LjungBoxTest lb = m_tests.getLjungBoxOnSquare();
        BoxPierceTest bp = m_tests.getBoxPierceOnSquare();
        if (ac != null && lb != null) {
            int k = lb.getK();

            stream.write(HtmlTag.IMPORTANT_TEXT, "Ljung-Box and Box-Pierce tests on square residuals: ").newLines(2);
            stream.open(new HtmlTable().withWidth(700));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Lag").withWidth(100));
            stream.write(new HtmlTableHeader("Autocorrelation").withWidth(100));
            stream.write(new HtmlTableHeader("Standard deviation").withWidth(100));
            stream.write(new HtmlTableHeader("Ljung-Box test").withWidth(100));
            stream.write(new HtmlTableHeader("P-Value").withWidth(100));
            stream.write(new HtmlTableHeader("Box-Pierce test").withWidth(100));
            stream.write(new HtmlTableHeader("P-Value").withWidth(100));
            stream.close(HtmlTag.TABLEROW);

            for (int i = 1; i <= k; i++) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(100));
                double v = 1 / Math.sqrt(m_tests.getStatistics()
                        .getObservationsCount());
                boolean bold = Math.abs(ac.autoCorrelation(i)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i))).withWidth(100).withClass(FONT_WEIGHT_BOLD));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i))).withWidth(100));
                }
                stream.write(new HtmlTableCell(df4.format(v)).withWidth(100));
                if (i > m_tests.getHyperParametersCount()) {
                    lb.setK(i);
                    if (lb.isValid()) {
                        stream.write(new HtmlTableCell(df4
                                .format(lb.getValue())).withWidth(100));
                        stream.write(new HtmlTableCell(df4.format(lb
                                .getPValue())).withWidth(100).withClass(PValue(lb.getPValue())));
                    } else {
                        stream.write(new HtmlTableCell("").withWidth(100));
                        stream.write(new HtmlTableCell("").withWidth(100));
                    }
                    bp.setK(i);
                    if (bp.isValid()) {
                        stream.write(new HtmlTableCell(df4
                                .format(bp.getValue())).withWidth(100));
                        stream.write(new HtmlTableCell(df4.format(bp
                                .getPValue())).withWidth(100).withClass(PValue(bp.getPValue())));
                    } else {
                        stream.write(new HtmlTableCell("").withWidth(100));
                        stream.write(new HtmlTableCell("").withWidth(100));
                    }
                }
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
        }

        stream.close(HtmlTag.DIV).newLine();
    }

    private void LinearitySummary(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.DIV);
        stream.write(HtmlTag.HEADER3, "4. Linearity of the residuals")
                .newLine();
        stream.open(new HtmlTable().withWidth(300));

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("P-value"));
        stream.close(HtmlTag.TABLEROW);

        if (m_tests.getRuns() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Ljung-Box on squared residuals("
                    + Integer.toString(m_tests.getLjungBoxOnSquare().getK())
                    + ")").withWidth(200));
            double pvalue = m_tests.getLjungBoxOnSquare().getPValue();
            stream.write(new HtmlTableCell(df4.format(pvalue)).withWidth(100).withClass(PValue(pvalue)));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getUpAndDownRuns() != null && m_tests.getBoxPierceOnSquare() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce on squared residuals("
                    + Integer.toString(m_tests.getBoxPierceOnSquare().getK())
                    + ")").withWidth(200));
            double pvalue = m_tests.getBoxPierceOnSquare().getPValue();
            stream.write(new HtmlTableCell(df4.format(pvalue)).withWidth(100).withClass(PValue(pvalue)));
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);
        stream.close(HtmlTag.DIV).newLine();
    }

    private HtmlClass PValue(double val) {
        if (val < badthreshold_) {
            return Bootstrap4.TEXT_DANGER;
        } else if (val < goodthreshold_) {
            return Bootstrap4.TEXT_WARNING;
        } else {
            return Bootstrap4.TEXT_SUCCESS;
        }
    }

    private void Randomness(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "3 - Randomness").newLine();
        stream.open(HtmlTag.DIV);

        TestofRuns runs = m_tests.getRuns();
        TestofUpDownRuns udruns = m_tests.getUpAndDownRuns();
        if (runs != null && udruns != null) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Runs around the mean").newLines(2);
            runs.setKind(RunsTestKind.Number);
            stream.write(
                    "Number of values above the central line: "
                    + Integer.toString(m_tests.getRuns().getPCount()))
                    .newLine();
            stream.write(
                    "Number of values below the central line: "
                    + Integer.toString(m_tests.getRuns().getMCount()))
                    .newLines(2);
            stream.write("Runs: " + Integer.toString(runs.runsCount(0)))
                    .newLines(2);

            stream.open(new HtmlTable().withWidth(500));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Test"));
            stream.write(new HtmlTableHeader("Value"));
            stream.write(new HtmlTableHeader("P-Value"));
            stream.write(new HtmlTableHeader("Distribution"));
            stream.close(HtmlTag.TABLEROW);

            runs.setKind(RunsTestKind.Number);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Number").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(runs.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(runs.getPValue())).withWidth(100).withClass(PValue(runs.getPValue())));
            String ndist = "Normal(";
            ndist = ndist.concat(df2.format(runs.getDistribution()
                    .getExpectation()));
            ndist = ndist.concat(";");
            ndist = ndist.concat(df2.format(Math.sqrt(runs.getDistribution()
                    .getVariance())));
            ndist = ndist.concat(")");
            stream.write(new HtmlTableCell(ndist).withWidth(200));
            stream.close(HtmlTag.TABLEROW);

            runs.setKind(RunsTestKind.Length);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Length").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(runs.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(runs.getPValue())).withWidth(100).withClass(PValue(runs.getPValue())));
            stream.write(new HtmlTableCell(runs.getDistribution().toString()).withWidth(200));
            stream.close(HtmlTag.TABLEROW);
            stream.close(HtmlTag.TABLE).newLine();

            stream.write(
                    "Up and down runs: "
                    + Integer.toString(udruns.runsCount(0)))
                    .newLines(2);

            stream.open(new HtmlTable().withWidth(500));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Test"));
            stream.write(new HtmlTableHeader("Value"));
            stream.write(new HtmlTableHeader("P-Value"));
            stream.write(new HtmlTableHeader("Distribution"));
            stream.close(HtmlTag.TABLEROW);

            udruns.setKind(RunsTestKind.Number);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Number").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(udruns.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(udruns.getPValue())).withWidth(100).withClass(PValue(udruns.getPValue())));
            ndist = "Normal(";
            ndist = ndist.concat(df2.format(udruns.getDistribution()
                    .getExpectation()));
            ndist = ndist.concat(";");
            ndist = ndist.concat(df2.format(Math.sqrt(udruns.getDistribution()
                    .getVariance())));
            ndist = ndist.concat(")");
            stream.write(new HtmlTableCell(ndist).withWidth(200));
            stream.close(HtmlTag.TABLEROW);

            udruns.setKind(RunsTestKind.Length);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Length").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(udruns.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(udruns.getPValue())).withWidth(100).withClass(PValue(udruns.getPValue())));
            stream.write(new HtmlTableCell(udruns.getDistribution().toString()).withWidth(200));
            stream.close(HtmlTag.TABLEROW);
            stream.close(HtmlTag.TABLE);
        }
        stream.close(HtmlTag.DIV).newLine();
    }

    private void RandomnessSummary(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.DIV);
        stream.write(HtmlTag.HEADER3, "3. Randomness of the residuals")
                .newLine();
        stream.open(new HtmlTable().withWidth(300));

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("P-value"));
        stream.close(HtmlTag.TABLEROW);

        if (m_tests.getRuns() != null) {
            m_tests.getRuns().setKind(RunsTestKind.Number);
            stream.open(HtmlTag.TABLEROW);
            stream
                    .write(new HtmlTableCell("Runs around the mean: number").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getRuns()
                    .getPValue())).withWidth(100).withClass(PValue(m_tests.getRuns().getPValue())));
            stream.close(HtmlTag.TABLEROW);
            m_tests.getRuns().setKind(RunsTestKind.Length);
            stream.open(HtmlTag.TABLEROW);
            stream
                    .write(new HtmlTableCell("Runs around the mean: length").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getRuns()
                    .getPValue())).withWidth(100).withClass(PValue(m_tests.getRuns().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getUpAndDownRuns() != null) {
            m_tests.getUpAndDownRuns().setKind(RunsTestKind.Number);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Up and Down runs: number").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getRuns()
                    .getPValue())).withWidth(100).withClass(PValue(m_tests.getRuns().getPValue())));
            stream.close(HtmlTag.TABLEROW);
            m_tests.getUpAndDownRuns().setKind(RunsTestKind.Length);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Up and Down runs: length").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getRuns()
                    .getPValue())).withWidth(100).withClass(PValue(m_tests.getRuns().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Statistics(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "0 - Statistics").newLine();
        stream.open(HtmlTag.DIV);
        double ss = m_tests.getStatistics().getSumSquare();
        stream.write(HtmlTag.IMPORTANT_TEXT, "Sum of squares: ").write(df4.format(ss)).newLine();
        double mse = ss
                / (double) (m_tests.getStatistics().getObservationsCount() - m_tests
                .getHyperParametersCount());
        stream.write(HtmlTag.IMPORTANT_TEXT, "MSE: ").write(df4.format(mse)).newLine();
        double stdev = Math.sqrt(mse);
        stream.write(HtmlTag.IMPORTANT_TEXT, "Standard error: ").write(df4.format(stdev)).newLine();
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Summary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Analysis of the residuals")
                .newLine();
        stream.write(HtmlTag.HEADER2, "Summary").newLine();

        DistributionSummary(stream);
        IndependenceSummary(stream);
        RandomnessSummary(stream);
        LinearitySummary(stream);
    }

    /**
     *
     * @param stream
     *
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        Summary(stream);
        stream.write(HtmlTag.HEADER2, "Details").newLine();
        Statistics(stream);
        Distribution(stream);
        Independence(stream);
        Randomness(stream);
        Linearity(stream);
    }
}
