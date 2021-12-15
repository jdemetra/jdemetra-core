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
package demetra.html.stat;

import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlClass;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTableHeader;
import demetra.html.HtmlTag;
import demetra.stats.StatisticalTest;
import java.io.IOException;
import java.util.function.IntToDoubleFunction;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.stats.DescriptiveStatistics;
import jdplus.stats.tests.NiidTests;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlNiidTest extends AbstractHtmlElement implements HtmlElement {

    private final NiidTests tests;
    private final DescriptiveStatistics stats;
    private final double badthreshold_ = ResidualsDiagnosticsConfiguration.NBAD;
    private final double goodthreshold_ = ResidualsDiagnosticsConfiguration.NUNC;

    /**
     *
     * @param tests
     */
    public HtmlNiidTest(NiidTests tests) {
        this.tests = tests;
        stats = DescriptiveStatistics.of(this.tests.data());
    }

    private void Distribution(HtmlStream stream) throws IOException {

        StatisticalTest skewness = tests.skewness();
        StatisticalTest kurtosis = tests.kurtosis();
        StatisticalTest mean = tests.meanTest();
        StatisticalTest normality = tests.normalityTest();

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
        stream.write(new HtmlTableCell(df4.format(stats.getAverage())).withWidth(100));
        double stdev = stats.getStdev();
        if (mean != null) {
            stream.write(new HtmlTableCell(df4.format(stdev)).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(mean.getValue())).withWidth(100));
            double pval = mean.getPvalue();
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
        if (skewness != null) {
            stream.write(new HtmlTableCell(df4.format(skewness.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(skewness.getPvalue())).withWidth(100));
            stream.write(new HtmlTableCell(skewness.getDescription()).withWidth(200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Kurtosis").withWidth(100));
        if (kurtosis != null) {
            stream.write(new HtmlTableCell(df4.format(kurtosis.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(kurtosis.getPvalue())).withWidth(100));
            stream.write(new HtmlTableCell(kurtosis.getDescription()).withWidth(200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Joint-test").withWidth(100));
        if (normality != null) {
            stream.write(new HtmlTableCell(df4.format(normality.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(normality.getPvalue())).withWidth(100));
            stream.write(new HtmlTableCell(normality.getDescription()).withWidth(200));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);

        stream.close(HtmlTag.DIV).newLine();
    }

    private void DistributionSummary(HtmlStream stream) throws IOException {
        StatisticalTest skewness = tests.skewness();
        StatisticalTest kurtosis = tests.kurtosis();
        StatisticalTest mean = tests.meanTest();
        StatisticalTest normality = tests.normalityTest();

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
        stream.write(new HtmlTableCell(df4.format(mean.getPvalue())).withWidth(100).withClass(PValue(mean.getPvalue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Skewness").withWidth(200));
        stream.write(new HtmlTableCell(df4.format(skewness.getPvalue())).withWidth(100).withClass(PValue(skewness.getPvalue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Kurtosis").withWidth(200));
        stream.write(new HtmlTableCell(df4.format(kurtosis.getPvalue())).withWidth(100).withClass(PValue(kurtosis.getPvalue())));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Normality").withWidth(200));
        stream.write(new HtmlTableCell(df4.format(normality.getPvalue())).withWidth(100).withClass(PValue(normality.getPvalue())));
        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Independence(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "2 - Independence tests").newLine();
        stream.open(HtmlTag.DIV);

        IntToDoubleFunction ac = tests.autoCorrelations();
        StatisticalTest lb = tests.ljungBox();
        StatisticalTest bp = tests.boxPierce();
        if (ac != null && lb != null) {
            int k = tests.getK();

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
                double v = 1 / Math.sqrt(stats.getObservationsCount());
                boolean bold = Math.abs(ac.applyAsDouble(i)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .applyAsDouble(i))).withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .applyAsDouble(i))).withWidth(100));
                }
                stream.write(new HtmlTableCell(df4.format(v)).withWidth(100));
                if (i > tests.getHyperParametersCount()) {
                    StatisticalTest lbi = tests.ljungBox(i);
                    if (lbi != null) {
                        stream.write(new HtmlTableCell(df4
                                .format(lbi.getValue())).withWidth(100));
                        stream.write(new HtmlTableCell(df4.format(lbi.getPvalue())).withWidth(100).withClass(PValue(lbi.getPvalue())));
                    } else {
                        stream.write(new HtmlTableCell("").withWidth(100));
                        stream.write(new HtmlTableCell("").withWidth(100));
                    }
                    StatisticalTest bpi = tests.boxPierce(i);
                    if (bpi != null) {
                        stream.write(new HtmlTableCell(df4
                                .format(bpi.getValue())).withWidth(100));
                        stream.write(new HtmlTableCell(df4.format(bpi.getPvalue())).withWidth(100).withClass(PValue(bpi.getPvalue())));
                    } else {
                        stream.write(new HtmlTableCell("").withWidth(100));
                        stream.write(new HtmlTableCell("").withWidth(100));
                    }
                }
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE).newLine();
        }

        StatisticalTest lbs = tests.seasonalLjungBox();
        StatisticalTest bps = tests.seasonalBoxPierce();
        if (ac != null && lbs != null) {
            int ks = tests.getKs();

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

            int f = tests.getPeriod();
            for (int i = 1; i <= ks; ++i) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(Integer.toString(i * f)).withWidth(100));
                double v = 1 / Math.sqrt(stats.getObservationsCount());
                boolean bold = Math.abs(ac.applyAsDouble(i * f)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac.applyAsDouble(i * f))).withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac.applyAsDouble(i * f))).withWidth(100));
                }
                stream.write(new HtmlTableCell(df4.format(v)).withWidth(100));
                StatisticalTest lbi = tests.seasonalLjungBox(i);
                if (lbi != null) {
                    stream.write(new HtmlTableCell(df4.format(lbi
                            .getValue())).withWidth(100));
                    stream.write(new HtmlTableCell(df4.format(lbi.getPvalue())).withWidth(100).withClass(PValue(lbi.getPvalue())));
                } else {
                    stream.write(new HtmlTableCell("").withWidth(100));
                    stream.write(new HtmlTableCell("").withWidth(100));
                }
                StatisticalTest bpi = tests.seasonalBoxPierce(i);
                if (bpi != null) {
                    stream.write(new HtmlTableCell(df4.format(bpi.getValue())).withWidth(100));
                    stream.write(new HtmlTableCell(df4.format(bpi.getPvalue())).withWidth(100).withClass(PValue(bpi.getPvalue())));
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
        StatisticalTest lb = tests.ljungBox();
        StatisticalTest bp = tests.boxPierce();

        if (lb != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Ljung-Box("
                    + Integer.toString(tests.getK())
                    + ")").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(lb.getPvalue())).withWidth(100)
                    .withClass(PValue(lb.getPvalue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (bp != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce("
                    + Integer.toString(tests.getK()) + ")").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(bp.getPvalue())).withWidth(100).
                    withClass(PValue(bp.getPvalue())));
            stream.close(HtmlTag.TABLEROW);
        }

        StatisticalTest lbs = tests.seasonalLjungBox();
        StatisticalTest bps = tests.seasonalBoxPierce();
        if (lbs != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Ljung-Box on seasonality("
                    + Integer.toString(tests.getKs())
                    + ")").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(lbs.getPvalue()))
                    .withWidth(100).withClass(PValue(lbs.getPvalue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (bps != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce on seasonality("
                    + Integer.toString(tests.getKs())
                    + ")").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(bps.getPvalue())).withWidth(100).withClass(PValue(bps.getPvalue())));
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE).newLine();
//        stream.write("Durbin-Watson statistic: "
//                + df4.format(tests.getAutoCorrelations()
//                        .getDurbinWatson())).newLine();
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Linearity(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "4 - Linearity tests").newLine();
        stream.open(HtmlTag.DIV);

        IntToDoubleFunction ac = tests.autoCorrelationsOnSquares();
        StatisticalTest lb = tests.ljungBoxOnSquare();
        StatisticalTest bp = tests.boxPierceOnSquare();
        if (ac != null && lb != null) {
            int k = tests.getK();

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
                double v = 1 / Math.sqrt(stats.getObservationsCount());
                boolean bold = Math.abs(ac.applyAsDouble(i)) > v;
                if (bold) {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .applyAsDouble(i))).withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
                } else {
                    stream.write(new HtmlTableCell(df4.format(ac
                            .applyAsDouble(i))).withWidth(100));
                }
                stream.write(new HtmlTableCell(df4.format(v)).withWidth(100));
                if (i > tests.getHyperParametersCount()) {
                    StatisticalTest lbi = tests.ljungBoxOnSquare(i);
                    if (lbi != null) {
                        stream.write(new HtmlTableCell(df4
                                .format(lbi.getValue())).withWidth(100));
                        stream.write(new HtmlTableCell(df4.format(lbi.getPvalue()))
                                .withWidth(100).withClass(PValue(lbi.getPvalue())));
                    } else {
                        stream.write(new HtmlTableCell("").withWidth(100));
                        stream.write(new HtmlTableCell("").withWidth(100));
                    }
                    StatisticalTest bpi = tests.boxPierceOnSquare(i);
                    if (bpi != null) {
                        stream.write(new HtmlTableCell(df4
                                .format(bpi.getValue())).withWidth(100));
                        stream.write(new HtmlTableCell(df4.format(bpi.getPvalue())).withWidth(100)
                                .withClass(PValue(bpi.getPvalue())));
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
        StatisticalTest lb2 = tests.ljungBoxOnSquare();
        StatisticalTest bp2 = tests.boxPierceOnSquare();
        if (lb2 != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Ljung-Box on squared residuals("
                    + Integer.toString(tests.getK())
                    + ")").withWidth(200));
            double pvalue = lb2.getPvalue();
            stream.write(new HtmlTableCell(df4.format(pvalue)).withWidth(100).withClass(PValue(pvalue)));
            stream.close(HtmlTag.TABLEROW);
        }

        if (bp2 != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Box-Pierce on squared residuals("
                    + Integer.toString(tests.getK())
                    + ")").withWidth(200));
            double pvalue = bp2.getPvalue();
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

        StatisticalTest lruns = tests.runsLength();
        StatisticalTest nruns = tests.runsNumber();
        StatisticalTest ludruns = tests.upAndDownRunsLength();
        StatisticalTest nudruns = tests.upAndDownRunsNumbber();
        stream.write(HtmlTag.IMPORTANT_TEXT, "Runs around the mean").newLines(2);
//            stream.write("Number of values above the central line: "
//                    + Integer.toString(tests.getRuns().getPCount()))
//                    .newLine();
//            stream.write("Number of values below the central line: "
//                    + Integer.toString(tests.getRuns().getMCount()))
//                    .newLines(2);
//            stream.write("Runs: " + Integer.toString(runs.runsCount(0)))
//                    .newLines(2);

        stream.open(new HtmlTable().withWidth(500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Test"));
        stream.write(new HtmlTableHeader("Value"));
        stream.write(new HtmlTableHeader("P-Value"));
        stream.write(new HtmlTableHeader("Distribution"));
        stream.close(HtmlTag.TABLEROW);
        if (nruns != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Number").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(nruns.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(nruns.getPvalue())).withWidth(100).withClass(PValue(nruns.getPvalue())));
            stream.write(new HtmlTableCell(nruns.getDescription()).withWidth(200));
            stream.close(HtmlTag.TABLEROW);
        }
        if (lruns != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Length").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(lruns.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(lruns.getPvalue())).withWidth(100).withClass(PValue(lruns.getPvalue())));
            stream.write(new HtmlTableCell(lruns.getDescription()).withWidth(200));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE).newLine();
        stream./*write(
                    "Up and down runs: "
                    + Integer.toString(udruns.runsCount(0)))
                    .*/newLines(2);

        stream.open(new HtmlTable().withWidth(500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Test"));
        stream.write(new HtmlTableHeader("Value"));
        stream.write(new HtmlTableHeader("P-Value"));
        stream.write(new HtmlTableHeader("Distribution"));
        stream.close(HtmlTag.TABLEROW);
        if (nudruns != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Number").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(nudruns.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(nudruns.getPvalue())).withWidth(100).withClass(PValue(nudruns.getPvalue())));
            stream.write(new HtmlTableCell(nudruns.getDescription()).withWidth(200));
            stream.close(HtmlTag.TABLEROW);
        }
        if (ludruns != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Length").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(ludruns.getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(ludruns.getPvalue())).withWidth(100).withClass(PValue(ludruns.getPvalue())));
            stream.write(new HtmlTableCell(ludruns.getDescription()).withWidth(200));
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
        StatisticalTest lruns = tests.runsLength();
        StatisticalTest nruns = tests.runsNumber();
        StatisticalTest ludruns = tests.upAndDownRunsLength();
        StatisticalTest nudruns = tests.upAndDownRunsNumbber();

        if (nruns != null) {
            stream.open(HtmlTag.TABLEROW);
            stream
                    .write(new HtmlTableCell("Runs around the mean: number").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(nruns
                    .getPvalue())).withWidth(100).withClass(PValue(nruns.getPvalue())));
            stream.close(HtmlTag.TABLEROW);
        }
        if (lruns != null) {
            stream.open(HtmlTag.TABLEROW);
            stream
                    .write(new HtmlTableCell("Runs around the mean: length").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(lruns.getPvalue()))
                    .withWidth(100).withClass(PValue(lruns.getPvalue())));
            stream.close(HtmlTag.TABLEROW);
        }

        if (nudruns != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Up and Down runs: number").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(nudruns.getPvalue())).withWidth(100)
                    .withClass(PValue(nudruns.getPvalue())));
            stream.close(HtmlTag.TABLEROW);
        }
        if (ludruns != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Up and Down runs: length").withWidth(200));
            stream.write(new HtmlTableCell(df4.format(ludruns.getPvalue())).withWidth(100).
                    withClass(PValue(ludruns.getPvalue())));
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);
        stream.close(HtmlTag.DIV).newLine();
    }

    private void Statistics(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "0 - Statistics").newLine();
        stream.open(HtmlTag.DIV);
        double ss = stats.getSumSquare();
        stream.write(HtmlTag.IMPORTANT_TEXT, "Sum of squares: ").write(df4.format(ss)).newLine();
        double mse = ss
                / (double) (stats.getObservationsCount() - tests
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
