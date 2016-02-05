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
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
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

        stream.write(HtmlTag.HEADER1, h1, "Combined seasonality test")
                .newLine();
        switch (m_stests.getSummary()) {
            case None:
                stream.write("Identifiable seasonality not present",
                        HtmlStyle.Bold, HtmlStyle.Danger);
                break;
            case ProbablyNone:
                stream.write("Identifiable seasonality probably not present",
                        HtmlStyle.Warning);
                break;
            case Present:
                stream.write("Identifiable seasonality present",
                        HtmlStyle.Success);
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
        stream.write(HtmlTag.HEADER1, h1, "Evolutive seasonality test")
                .newLine();
        SeasonalityTest stest = m_stests.getEvolutiveSeasonality();

        stream.open(new HtmlTable(0, 400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Sum of squares", 100));
        stream.write(new HtmlTableCell("Degrees of freedom", 100));
        stream.write(new HtmlTableCell("Mean square", 100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Between years", 100,
                HtmlStyle.Italic));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSM()), 100));
        stream.write(new HtmlTableCell(Double
                .toString(stest.getDFInterPeriod()), 100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSM()
                / stest.getDFInterPeriod()), 100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Error", 100,
                HtmlStyle.Italic));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSR()), 100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDFResidual()),
                100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSR()
                / stest.getDFResidual()), 100));
        stream.close(HtmlTag.TABLE).newLines(2);

        stream.write("Value: " + stest.getValue()).newLine();
        stream.write(
                "Distribution: " + stest.getDistribution().getDescription())
                .newLine();
        stream.write("PValue: " + df4.format(stest.getPValue()),
                RPValue(stest.getPValue(), 0.01, 0.05)).newLine();
        if (stest.getPValue() > 0.2) {
            stream
                    .write(
                    "No evidence of moving seasonality at the 20 per cent level")
                    .newLines(2);
        } else if (stest.getPValue() < 0.05) {
            stream.write("Moving seasonality present at the 5 per cent level",
                    HtmlStyle.Bold).newLines(2);
        }
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void FKWTests(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1,
                "Non parametric tests for stable seasonality").newLine();
        stream.write(HtmlTag.HEADER2, h2, "Friedman test").newLine();
        stream.write(
                "Friedman statistic = " + df4.format(m_ftest.getValue()))
                .newLine();
        stream.write("Distribution: " + m_ftest.getDistribution().toString())
                .newLine();
        stream.write("P-Value: ").write(df4.format(m_ftest.getPValue()),
                PValue(m_ftest.getPValue())).newLines(2);
        if (m_ftest.getPValue() < 0.01) {
            stream.write("Stable seasonality present at the 1 per cent level")
                    .newLines(2);
        } else if (m_ftest.getPValue() > 0.05) {
            stream
                    .write(
                    "No evidence of stable seasonality at the 5 per cent level")
                    .newLines(2);
        }

        stream.write(HtmlTag.HEADER2, h2, "Kruskall-Wallis test").newLine();
        KruskalWallisTest kw = m_stests
                .getNonParametricTestForStableSeasonality();
        stream
                .write(
                "Kruskall-Wallis statistic = "
                + Double.toString(kw.getValue())).newLine();
        stream.write("Distribution: " + kw.getDistribution().toString())
                .newLine();
        stream.write("P-Value: ").write(df4.format(kw.getPValue()),
                PValue(kw.getPValue())).newLine();
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

    private HtmlStyle PValue(double val) {
        if (val > m_badthreshold) {
            return HtmlStyle.Danger;
        } else if (val > m_goodthresohold) {
            return HtmlStyle.Warning;
        } else {
            return HtmlStyle.Success;
        }
    }

    private HtmlStyle RPValue(double val, double lb, double ub) {
        if (val < ub) {
            return HtmlStyle.Danger;
        } else if (val < lb) {
            return HtmlStyle.Warning;
        } else {
            return HtmlStyle.Success;
        }
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void StableSeasonalityTest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Test for the presence of seasonality assuming stability")
                .newLine();
        SeasonalityTest stest = m_stests.getStableSeasonality();

        stream.open(new HtmlTable(0, 400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Sum of squares", 100));
        stream.write(new HtmlTableCell("Degrees of freedom", 100));
        stream.write(new HtmlTableCell("Mean square", 100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Between periods", 100,
                HtmlStyle.Italic));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSM()), 100));
        stream.write(new HtmlTableCell(Double
                .toString(stest.getDFInterPeriod()), 100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSM()
                / stest.getDFInterPeriod()), 100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Residual", 100,
                HtmlStyle.Italic));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSR()), 100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDFResidual()),
                100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSR()
                / stest.getDFResidual()), 100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Total", 100,
                HtmlStyle.Italic));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSQ()), 100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDFTot()), 100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSSQ()
                / stest.getDFTot()), 100));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();

        stream.write("Value: " + stest.getValue()).newLine();
        stream.write(
                "Distribution: " + stest.getDistribution().getDescription())
                .newLine();
        stream.write("PValue: " + df4.format(stest.getPValue()),
                PValue(stest.getPValue())).newLine();
        if (stest.getPValue() < 0.01) {
            stream.write("Seasonality present at the 1 per cent level")
                    .newLines(2);
        } else if (stest.getPValue() > 0.05) {
            stream.write("No evidence of seasonality at the 5 per cent level",
                    HtmlStyle.Bold).newLines(2);
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
