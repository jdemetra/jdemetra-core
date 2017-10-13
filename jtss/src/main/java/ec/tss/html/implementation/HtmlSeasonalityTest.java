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

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.satoolkit.diagnostics.FriedmanTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.satoolkit.diagnostics.SeasonalityTest;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.Bootstrap4;
import static ec.tss.html.Bootstrap4.FONT_ITALIC;
import ec.tss.html.HtmlClass;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSeasonalityTest extends AbstractHtmlElement {

    private TsData m_si;
    private CombinedSeasonalityTest m_stests;
    private FriedmanTest m_ftest;
    private double m_badthreshold = 0.1;
    private double m_goodthresohold = 0.01;
    private ec.satoolkit.diagnostics.ResidualSeasonalityDiagnostics.Configuration m_config = ec.satoolkit.diagnostics.ResidualSeasonalityDiagnostics.defaultConfiguration
            .clone();

    /**
     *
     * @param output
     */
    public HtmlSeasonalityTest(ISeriesDecomposition output) {
        TsData s = output.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        TsData i = output.getSeries(ComponentType.Irregular, ComponentInformation.Value);
        if (s == null || s.getFrequency() == TsFrequency.Yearly) {
            return;
        }
        boolean mul = output.getMode() != DecompositionMode.Additive;
        if (mul) {
            m_si = TsData.multiply(s, i);
        } else {
            m_si = TsData.add(s, i);
        }
        m_stests = new CombinedSeasonalityTest(m_si, mul);
        m_ftest = new FriedmanTest(m_si);

    }

    public HtmlSeasonalityTest(TsData si, boolean mul) {
        if (si == null || si.getFrequency() == TsFrequency.Yearly) {
            return;
        }
        m_si = si;
        m_stests = new CombinedSeasonalityTest(m_si, mul);
        m_ftest = new FriedmanTest(m_si);

    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void CombinedSeasonalityTest(HtmlStream stream) throws IOException {

        stream.write(HtmlTag.HEADER1, "Combined seasonality test")
                .newLine();
        switch (m_stests.getSummary()) {
            case None:
                stream.write(HtmlTag.IMPORTANT_TEXT, "Identifiable seasonality not present", Bootstrap4.TEXT_DANGER);
                break;
            case ProbablyNone:
                stream.write("Identifiable seasonality probably not present", Bootstrap4.TEXT_WARNING);
                break;
            case Present:
                stream.write("Identifiable seasonality present", Bootstrap4.TEXT_SUCCESS);
                break;
        }
        stream.newLines(2);
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void EvolutionSeasonalityTest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Evolutive seasonality test")
                .newLine();
        SeasonalityTest stest = m_stests.getEvolutiveSeasonality();

        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Sum of squares").withWidth(100));
        stream.write(new HtmlTableCell("Degrees of freedom").withWidth(100));
        stream.write(new HtmlTableCell("Mean square").withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Between years").withWidth(100).withClass(FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSM())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDFInterPeriod())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSM() / stest.getDFInterPeriod())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Error").withWidth(100).withClass(FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSR())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDFResidual())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSR() / stest.getDFResidual())).withWidth(100));
        stream.close(HtmlTag.TABLE).newLines(2);

        stream.write("Value: " + stest.getValue()).newLine();
        stream.write(
                "Distribution: " + stest.getDistribution().getDescription())
                .newLine();
        stream.write("PValue: " + df4.format(stest.getPValue()),
                getRPValueClass(stest.getPValue(), 0.01, 0.05)).newLine();
        if (stest.getPValue() > 0.2) {
            stream
                    .write(
                            "No evidence of moving seasonality at the 20 per cent level")
                    .newLines(2);
        } else if (stest.getPValue() < 0.05) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Moving seasonality present at the 5 per cent level").newLines(2);
        }
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void FKWTests(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1,
                "Non parametric tests for stable seasonality").newLine();
        stream.write(HtmlTag.HEADER2, "Friedman test").newLine();
        stream.write(
                "Friedman statistic = " + df4.format(m_ftest.getValue()))
                .newLine();
        stream.write("Distribution: " + m_ftest.getDistribution().toString())
                .newLine();
        stream.write("P-Value: ").write(df4.format(m_ftest.getPValue()),
                getPValueClass(m_ftest.getPValue())).newLines(2);
        if (m_ftest.getPValue() < 0.01) {
            stream.write("Stable seasonality present at the 1 per cent level")
                    .newLines(2);
        } else if (m_ftest.getPValue() > 0.05) {
            stream
                    .write(
                            "No evidence of stable seasonality at the 5 per cent level")
                    .newLines(2);
        }

        stream.write(HtmlTag.HEADER2, "Kruskall-Wallis test").newLine();
        KruskalWallisTest kw = m_stests
                .getNonParametricTestForStableSeasonality();
        stream
                .write(
                        "Kruskall-Wallis statistic = "
                        + Double.toString(kw.getValue())).newLine();
        stream.write("Distribution: " + kw.getDistribution().toString())
                .newLine();
        stream.write("P-Value: ").write(df4.format(kw.getPValue()),
                getPValueClass(kw.getPValue())).newLine();
        if (kw.getPValue() < 0.01) {
            stream.write("Stable seasonality present at the 1 per cent level")
                    .newLines(2);
        } else if (kw.getPValue() > 0.05) {
            stream
                    .write(
                            "No evidence of stable seasonality at the 5 per cent level")
                    .newLines(2);
        }
    }

    private HtmlClass getPValueClass(double val) {
        if (val > m_badthreshold) {
            return Bootstrap4.TEXT_DANGER;
        } else if (val > m_goodthresohold) {
            return Bootstrap4.TEXT_WARNING;
        } else {
            return Bootstrap4.TEXT_SUCCESS;
        }
    }

    private HtmlClass getRPValueClass(double val, double lb, double ub) {
        if (val < ub) {
            return Bootstrap4.TEXT_DANGER;
        } else if (val < lb) {
            return Bootstrap4.TEXT_WARNING;
        } else {
            return Bootstrap4.TEXT_SUCCESS;
        }
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void StableSeasonalityTest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Test for the presence of seasonality assuming stability")
                .newLine();
        SeasonalityTest stest = m_stests.getStableSeasonality();

        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Sum of squares").withWidth(100));
        stream.write(new HtmlTableCell("Degrees of freedom").withWidth(100));
        stream.write(new HtmlTableCell("Mean square").withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Between periods").withWidth(100).withClass(FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSM())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDFInterPeriod())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSM() / stest.getDFInterPeriod())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Residual").withWidth(100).withClass(FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSR())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDFResidual())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSR() / stest.getDFResidual())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Total").withWidth(100).withClass(FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSQ())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDFTot())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSQ() / stest.getDFTot())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();

        stream.write("Value: " + stest.getValue()).newLine();
        stream.write(
                "Distribution: " + stest.getDistribution().getDescription())
                .newLine();
        stream.write("PValue: " + df4.format(stest.getPValue()),
                getPValueClass(stest.getPValue())).newLine();
        if (stest.getPValue() < 0.01) {
            stream.write("Seasonality present at the 1 per cent level")
                    .newLines(2);
        } else if (stest.getPValue() > 0.05) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "No evidence of seasonality at the 5 per cent level").newLines(2);
        }
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        if (m_si == null) {
            stream.write("Series can't be tested");
            return;
        }
        FKWTests(stream);
        StableSeasonalityTest(stream);
        EvolutionSeasonalityTest(stream);
        CombinedSeasonalityTest(stream);
//        ResidualSeasonality(stream);
    }
}
