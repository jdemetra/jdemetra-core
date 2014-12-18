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
        stream.write(HtmlTag.HEADER2, h2, "1 - Distribution").newLine();
        stream.open(HtmlTag.DIV, d1);

        stream.write("Mean", HtmlStyle.Bold).newLines(2);
        stream.open(new HtmlTable(0, 400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Value"));
        stream.write(new HtmlTableHeader("Standard deviation"));
        stream.write(new HtmlTableHeader("T-Stat"));
        stream.write(new HtmlTableHeader("P-Value"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(df4.format(m_tests.getStatistics()
                .getAverage()), 100));
        double stdev = m_tests.getStatistics().getStdev();
        if (m_tests.getMeanTest() != null) {
            stream.write(new HtmlTableCell(df4.format(stdev), 100));
            stream.write(new HtmlTableCell(df4.format(m_tests.getMeanTest()
                    .getValue()), 100));
            double pval = m_tests.getMeanTest()
                    .getPValue();
            stream.write(new HtmlTableCell(df4.format(pval), 100, PValue(pval)));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();

        stream.write("Normality tests", HtmlStyle.Bold)
                .newLines(2);
        stream.open(new HtmlTable(0, 500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Test"));
        stream.write(new HtmlTableHeader("Value"));
        stream.write(new HtmlTableHeader("P-Value"));
        stream.write(new HtmlTableHeader("Distribution"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Skewness", 100));
        if (m_tests.getSkewness() != null) {
            stream.write(new HtmlTableCell(df4.format(m_tests.getSkewness()
                    .getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(m_tests.getSkewness()
                    .getPValue()), 100));
            String ndist = "Normal(";
            ndist = ndist.concat(df2.format(m_tests.getSkewness()
                    .getDistribution().getExpectation()));
            ndist = ndist.concat(";");
            ndist = ndist.concat(df2.format(Math.sqrt(m_tests.getSkewness()
                    .getDistribution().getVariance())));
            ndist = ndist.concat(")");
            stream.write(new HtmlTableCell(ndist, 200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Kurtosis", 100));
        if (m_tests.getKurtosis() != null) {
            stream.write(new HtmlTableCell(df4.format(m_tests.getKurtosis()
                    .getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(m_tests.getKurtosis()
                    .getPValue()), 100));
            String ndist = "Normal(";
            ndist = ndist.concat(df2.format(m_tests.getKurtosis()
                    .getDistribution().getExpectation()));
            ndist = ndist.concat(";");
            ndist = ndist.concat(df2.format(Math.sqrt(m_tests.getKurtosis()
                    .getDistribution().getVariance())));
            ndist = ndist.concat(")");
            stream.write(new HtmlTableCell(ndist, 200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Joint-test", 100));
        if (m_tests.getNormalityTest() != null) {
            stream.write(new HtmlTableCell(df4.format(m_tests
                    .getNormalityTest().getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(m_tests
                    .getNormalityTest().getPValue()), 100));
            stream.write(new HtmlTableCell(m_tests.getNormalityTest()
                    .getDistribution().toString(), 200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);

        stream.close(HtmlTag.DIV).newLine();
    }

    private void DistributionSummary(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.DIV, d1);
        stream.write(HtmlTag.HEADER3, h3, "1. Normality of the residuals")
                .newLine();
        stream.open(new HtmlTable(0, 300));

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("P-value"));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Mean", 200));
        stream.write(new HtmlTableCell(df4.format(m_tests.getMeanTest()
                .getPValue()), 100, PValue(m_tests.getMeanTest().getPValue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Skewness", 200));
        stream.write(new HtmlTableCell(df4.format(m_tests.getSkewness()
                .getPValue()), 100, PValue(m_tests.getSkewness().getPValue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Kurtosis", 200));
        stream.write(new HtmlTableCell(df4.format(m_tests.getKurtosis()
                .getPValue()), 100, PValue(m_tests.getKurtosis().getPValue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Normality", 200));
        stream.write(new HtmlTableCell(df4.format(m_tests.getNormalityTest()
                .getPValue()), 100, PValue(m_tests.getNormalityTest()
                        .getPValue())));
        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Independence(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "2 - Independence tests").newLine();
        stream.open(HtmlTag.DIV, d1);

        AutoCorrelations ac = m_tests.getAutoCorrelations();
        LjungBoxTest lb = m_tests.getLjungBox();
        BoxPierceTest bp = m_tests.getBoxPierce();
        if (ac != null && lb != null) {
            int k = lb.getK();

            stream.write("Ljung-Box and Box-Pierce tests on residuals: ",
                    HtmlStyle.Bold).newLines(2);
            stream.open(new HtmlTable(0, 700));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Lag", 100));
            stream.write(new HtmlTableHeader("Autocorrelation", 100));
            stream.write(new HtmlTableHeader("Standard deviation", 100));
            stream.write(new HtmlTableHeader("Ljung-Box test", 100));
            stream.write(new HtmlTableHeader("P-Value", 100));
            stream.write(new HtmlTableHeader("Box-Pierce test", 100));
            stream.write(new HtmlTableHeader("P-Value", 100));
            stream.close(HtmlTag.TABLEROW);

            for (int i = 1; i <= k; i++) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(Integer.toString(i), 100));
                double v = 1 / Math.sqrt(m_tests.getStatistics()
                        .getObservationsCount());
                boolean bold = Math.abs(ac.autoCorrelation(i)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i)), 100,
                            HtmlStyle.Bold));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i)), 100));
                }
                stream.write(new HtmlTableCell(df4.format(v), 100));
                if (i > m_tests.getHyperParametersCount()) {
                    lb.setK(i);
                    if (lb.isValid()) {
                        stream.write(new HtmlTableCell(df4
                                .format(lb.getValue()), 100));
                        stream.write(new HtmlTableCell(df4.format(lb
                                .getPValue()), 100, PValue(lb.getPValue())));
                    } else {
                        stream.write(new HtmlTableCell("", 100));
                        stream.write(new HtmlTableCell("", 100));
                    }
                    bp.setK(i);
                    if (bp.isValid()) {
                        stream.write(new HtmlTableCell(df4
                                .format(bp.getValue()), 100));
                        stream.write(new HtmlTableCell(df4.format(bp
                                .getPValue()), 100, PValue(bp.getPValue())));
                    } else {
                        stream.write(new HtmlTableCell("", 100));
                        stream.write(new HtmlTableCell("", 100));
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

            stream.write(
                    "Ljung-Box and Box-Pierce tests on seasonal residuals: ",
                    HtmlStyle.Bold).newLines(2);
            stream.open(new HtmlTable(0, 700));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Lag", 100));
            stream.write(new HtmlTableHeader("Autocorrelation", 100));
            stream.write(new HtmlTableHeader("Standard deviation", 100));
            stream.write(new HtmlTableHeader("Ljung-Box test", 100));
            stream.write(new HtmlTableHeader("P-Value", 100));
            stream.write(new HtmlTableHeader("Box-Pierce test", 100));
            stream.write(new HtmlTableHeader("P-Value", 100));
            stream.close(HtmlTag.TABLEROW);

            int f = m_tests.getFrequency();
            for (int i = 1; i <= ks; ++i) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(Integer.toString(i * f), 100));
                double v = 1 / Math.sqrt(m_tests.getStatistics()
                        .getObservationsCount());
                boolean bold = Math.abs(ac.autoCorrelation(i)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i * f)), 100,
                            HtmlStyle.Bold));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i * f)), 100));
                }
                stream.write(new HtmlTableCell(df4.format(v), 100));
                lbs.setK(i);
                if (lbs.isValid()) {
                    stream.write(new HtmlTableCell(df4.format(lbs
                            .getValue()), 100));
                    stream.write(new HtmlTableCell(df4.format(lbs
                            .getPValue()), 100, PValue(lbs.getPValue())));
                } else {
                    stream.write(new HtmlTableCell("", 100));
                    stream.write(new HtmlTableCell("", 100));
                }
                bps.setK(i);
                if (bps.isValid()) {
                    stream.write(new HtmlTableCell(df4.format(bps
                            .getValue()), 100));
                    stream.write(new HtmlTableCell(df4.format(bps
                            .getPValue()), 100, PValue(bps.getPValue())));
                } else {
                    stream.write(new HtmlTableCell("", 100));
                    stream.write(new HtmlTableCell("", 100));
                }
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
        }

        stream.close(HtmlTag.DIV).newLine();
    }

    private void IndependenceSummary(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.DIV, d1);
        stream.write(HtmlTag.HEADER3, h3, "2. Independence of the residuals")
                .newLine();
        stream.open(new HtmlTable(0, 300));

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("P-value"));
        stream.close(HtmlTag.TABLEROW);

        if (m_tests.getLjungBox() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream
                    .write(new HtmlTableCell("Ljung-Box("
                                    + Integer.toString(m_tests.getLjungBox().getK())
                                    + ")", 200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getLjungBox()
                    .getPValue()), 100, PValue(m_tests.getLjungBox()
                            .getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getBoxPierce() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce("
                    + Integer.toString(m_tests.getBoxPierce().getK()) + ")",
                    200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getBoxPierce()
                    .getPValue()), 100, PValue(m_tests.getBoxPierce()
                            .getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getSeasonalLjungBox() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Ljung-Box on seasonality("
                    + Integer.toString(m_tests.getSeasonalLjungBox().getK())
                    + ")", 200));
            stream.write(new HtmlTableCell(df4.format(m_tests
                    .getSeasonalLjungBox().getPValue()), 100, PValue(m_tests
                            .getSeasonalLjungBox().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getSeasonalBoxPierce() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce on seasonality("
                    + Integer.toString(m_tests.getSeasonalBoxPierce().getK())
                    + ")", 200));
            stream.write(new HtmlTableCell(df4.format(m_tests
                    .getSeasonalBoxPierce().getPValue()), 100, PValue(m_tests
                            .getSeasonalBoxPierce().getPValue())));
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
        stream.write(HtmlTag.HEADER2, h2, "4 - Linearity tests").newLine();
        stream.open(HtmlTag.DIV, d1);

        AutoCorrelations ac = m_tests.getAutoCorrelationsOnSquare();
        LjungBoxTest lb = m_tests.getLjungBoxOnSquare();
        BoxPierceTest bp = m_tests.getBoxPierceOnSquare();
        if (ac != null && lb != null) {
            int k = lb.getK();

            stream.write(
                    "Ljung-Box and Box-Pierce tests on square residuals: ",
                    HtmlStyle.Bold).newLines(2);
            stream.open(new HtmlTable(0, 700));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Lag", 100));
            stream.write(new HtmlTableHeader("Autocorrelation", 100));
            stream.write(new HtmlTableHeader("Standard deviation", 100));
            stream.write(new HtmlTableHeader("Ljung-Box test", 100));
            stream.write(new HtmlTableHeader("P-Value", 100));
            stream.write(new HtmlTableHeader("Box-Pierce test", 100));
            stream.write(new HtmlTableHeader("P-Value", 100));
            stream.close(HtmlTag.TABLEROW);

            for (int i = 1; i <= k; i++) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(Integer.toString(i), 100));
                double v = 1 / Math.sqrt(m_tests.getStatistics()
                        .getObservationsCount());
                boolean bold = Math.abs(ac.autoCorrelation(i)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i)), 100,
                            HtmlStyle.Bold));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .autoCorrelation(i)), 100));
                }
                stream.write(new HtmlTableCell(df4.format(v), 100));
                if (i > m_tests.getHyperParametersCount()) {
                    lb.setK(i);
                    if (lb.isValid()) {
                        stream.write(new HtmlTableCell(df4
                                .format(lb.getValue()), 100));
                        stream.write(new HtmlTableCell(df4.format(lb
                                .getPValue()), 100, PValue(lb.getPValue())));
                    } else {
                        stream.write(new HtmlTableCell("", 100));
                        stream.write(new HtmlTableCell("", 100));
                    }
                    bp.setK(i);
                    if (bp.isValid()) {
                        stream.write(new HtmlTableCell(df4
                                .format(bp.getValue()), 100));
                        stream.write(new HtmlTableCell(df4.format(bp
                                .getPValue()), 100, PValue(bp.getPValue())));
                    } else {
                        stream.write(new HtmlTableCell("", 100));
                        stream.write(new HtmlTableCell("", 100));
                    }
                }
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
        }

        stream.close(HtmlTag.DIV).newLine();
    }

    private void LinearitySummary(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.DIV, d1);
        stream.write(HtmlTag.HEADER3, h3, "4. Linearity of the residuals")
                .newLine();
        stream.open(new HtmlTable(0, 300));

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("P-value"));
        stream.close(HtmlTag.TABLEROW);

        if (m_tests.getRuns() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Ljung-Box on squared residuals("
                    + Integer.toString(m_tests.getLjungBoxOnSquare().getK())
                    + ")", 200));
            double pvalue = m_tests.getLjungBoxOnSquare().getPValue();
            stream.write(new HtmlTableCell(df4.format(pvalue), 100,
                    PValue(pvalue)));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getUpAndDownRuns() != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce on squared residuals("
                    + Integer.toString(m_tests.getBoxPierceOnSquare().getK())
                    + ")", 200));
            double pvalue = m_tests.getBoxPierceOnSquare().getPValue();
            stream.write(new HtmlTableCell(df4.format(pvalue), 100,
                    PValue(pvalue)));
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);
        stream.close(HtmlTag.DIV).newLine();
    }

    private HtmlStyle PValue(double val) {
        if (val < badthreshold_) {
            return HtmlStyle.Danger;
        } else if (val < goodthreshold_) {
            return HtmlStyle.Warning;
        } else {
            return HtmlStyle.Success;
        }
    }

    private void Randomness(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "3 - Randomness").newLine();
        stream.open(HtmlTag.DIV, d1);

        TestofRuns runs = m_tests.getRuns();
        TestofUpDownRuns udruns = m_tests.getUpAndDownRuns();
        if (runs != null && udruns != null) {
            stream.write("Runs around the mean",
                    HtmlStyle.Bold).newLines(2);
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

            stream.open(new HtmlTable(0, 500));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Test"));
            stream.write(new HtmlTableHeader("Value"));
            stream.write(new HtmlTableHeader("P-Value"));
            stream.write(new HtmlTableHeader("Distribution"));
            stream.close(HtmlTag.TABLEROW);

            runs.setKind(RunsTestKind.Number);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Number", 100));
            stream.write(new HtmlTableCell(df4.format(runs.getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(runs.getPValue()), 100,
                    PValue(runs.getPValue())));
            String ndist = "Normal(";
            ndist = ndist.concat(df2.format(runs.getDistribution()
                    .getExpectation()));
            ndist = ndist.concat(";");
            ndist = ndist.concat(df2.format(Math.sqrt(runs.getDistribution()
                    .getVariance())));
            ndist = ndist.concat(")");
            stream.write(new HtmlTableCell(ndist, 200));
            stream.close(HtmlTag.TABLEROW);

            runs.setKind(RunsTestKind.Length);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Length", 100));
            stream.write(new HtmlTableCell(df4.format(runs.getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(runs.getPValue()), 100,
                    PValue(runs.getPValue())));
            stream.write(new HtmlTableCell(runs.getDistribution().toString(),
                    200));
            stream.close(HtmlTag.TABLEROW);
            stream.close(HtmlTag.TABLE).newLine();

            stream.write(
                    "Up and down runs: "
                    + Integer.toString(udruns.runsCount(0)))
                    .newLines(2);

            stream.open(new HtmlTable(0, 500));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader("Test"));
            stream.write(new HtmlTableHeader("Value"));
            stream.write(new HtmlTableHeader("P-Value"));
            stream.write(new HtmlTableHeader("Distribution"));
            stream.close(HtmlTag.TABLEROW);

            udruns.setKind(RunsTestKind.Number);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Number", 100));
            stream.write(new HtmlTableCell(df4.format(udruns.getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(udruns.getPValue()), 100,
                    PValue(udruns.getPValue())));
            ndist = "Normal(";
            ndist = ndist.concat(df2.format(udruns.getDistribution()
                    .getExpectation()));
            ndist = ndist.concat(";");
            ndist = ndist.concat(df2.format(Math.sqrt(udruns.getDistribution()
                    .getVariance())));
            ndist = ndist.concat(")");
            stream.write(new HtmlTableCell(ndist, 200));
            stream.close(HtmlTag.TABLEROW);

            udruns.setKind(RunsTestKind.Length);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Length", 100));
            stream.write(new HtmlTableCell(df4.format(udruns.getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(udruns.getPValue()), 100,
                    PValue(udruns.getPValue())));
            stream.write(new HtmlTableCell(udruns.getDistribution().toString(),
                    200));
            stream.close(HtmlTag.TABLEROW);
            stream.close(HtmlTag.TABLE);
        }
        stream.close(HtmlTag.DIV).newLine();
    }

    private void RandomnessSummary(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.DIV, d1);
        stream.write(HtmlTag.HEADER3, h3, "3. Randomness of the residuals")
                .newLine();
        stream.open(new HtmlTable(0, 300));

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("P-value"));
        stream.close(HtmlTag.TABLEROW);

        if (m_tests.getRuns() != null) {
            m_tests.getRuns().setKind(RunsTestKind.Number);
            stream.open(HtmlTag.TABLEROW);
            stream
                    .write(new HtmlTableCell("Runs around the mean: number",
                                    200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getRuns()
                    .getPValue()), 100, PValue(m_tests.getRuns().getPValue())));
            stream.close(HtmlTag.TABLEROW);
            m_tests.getRuns().setKind(RunsTestKind.Length);
            stream.open(HtmlTag.TABLEROW);
            stream
                    .write(new HtmlTableCell("Runs around the mean: length",
                                    200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getRuns()
                    .getPValue()), 100, PValue(m_tests.getRuns().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (m_tests.getUpAndDownRuns() != null) {
            m_tests.getUpAndDownRuns().setKind(RunsTestKind.Number);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Up and Down runs: number", 200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getRuns()
                    .getPValue()), 100, PValue(m_tests.getRuns().getPValue())));
            stream.close(HtmlTag.TABLEROW);
            m_tests.getUpAndDownRuns().setKind(RunsTestKind.Length);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Up and Down runs: length", 200));
            stream.write(new HtmlTableCell(df4.format(m_tests.getRuns()
                    .getPValue()), 100, PValue(m_tests.getRuns().getPValue())));
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Statistics(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "0 - Statistics").newLine();
        stream.open(HtmlTag.DIV, d1);
        double ss = m_tests.getStatistics().getSumSquare();
        stream.write("Sum of squares: ", HtmlStyle.Bold)
                .write(df4.format(ss)).newLine();
        double mse = ss
                / (double) (m_tests.getStatistics().getObservationsCount() - m_tests
                .getHyperParametersCount());
        stream.write("MSE: ", HtmlStyle.Bold).write(
                df4.format(mse)).newLine();
        double stdev = Math.sqrt(mse);
        stream.write("Standard error: ", HtmlStyle.Bold)
                .write(df4.format(stdev)).newLine();
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Summary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Analysis of the residuals")
                .newLine();
        stream.write(HtmlTag.HEADER2, h2, "Summary").newLine();

        DistributionSummary(stream);
        IndependenceSummary(stream);
        RandomnessSummary(stream);
        LinearitySummary(stream);
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        Summary(stream);
        stream.write(HtmlTag.HEADER2, h2, "Details").newLine();
        Statistics(stream);
        Distribution(stream);
        Independence(stream);
        Randomness(stream);
        Linearity(stream);
    }
}
