/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tss.html.implementation;

import ec.satoolkit.diagnostics.SeasonalityTest;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.Bootstrap4;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;

/**
 *
 * @author palatej
 */
public class HtmlResidualSeasonalityTest extends AbstractHtmlElement {

    private TsData m_sa;
    private double m_badthreshold = 0.1;
    private double m_goodthresohold = 0.01;
    private ec.satoolkit.diagnostics.ResidualSeasonalityDiagnostics.Configuration m_config = ec.satoolkit.diagnostics.ResidualSeasonalityDiagnostics.defaultConfiguration
            .clone();

    public HtmlResidualSeasonalityTest(TsData sa) {
        m_sa = sa;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void ResidualSeasonality(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Residual seasonality test")
                .newLine();
        if (m_sa == null || m_sa.getFrequency() == TsFrequency.Yearly) {
            stream.write("Series can't be tested");
            return;
        }
        int freq = m_sa.getFrequency().intValue();
        TsData s = m_sa.delta(Math.max(1, freq / 4));

        SeasonalityTest ftest = SeasonalityTest.stableSeasonality(s);
        double val = ftest.getPValue();
        if (val < m_config.getSASevere()) {
            stream.write(HtmlTag.IMPORTANT_TEXT,
                    "Residual seasonality present in the entire series at the "
                    + (100 * m_config.getSASevere())
                    + " per cent level: F="
                    + df4.format(ftest.getValue()),
                    Bootstrap4.TEXT_DANGER)
                    .newLine();
        } else if (val < m_config.getSABad()) {
            stream.write(
                    "Residual seasonality present in the entire series at the "
                    + (100 * m_config.getSABad())
                    + " per cent level: F="
                    + df4.format(ftest.getValue()),
                    Bootstrap4.TEXT_DANGER).newLine();
        } else if (val < m_config.getSAUncertain()) {
            stream.write(
                    "Residual seasonality present in the entire series at the "
                    + (100 * m_config.getSAUncertain())
                    + " per cent level: F="
                    + df4.format(ftest.getValue()),
                    Bootstrap4.TEXT_WARNING).newLine();
        } else {
            stream.write(
                    "No evidence of residual seasonality in the entire series at the "
                    + (100 * m_config.getSAUncertain())
                    + " per cent level: F="
                    + df4.format(ftest.getValue()),
                    Bootstrap4.TEXT_SUCCESS).newLine();
        }

        TsPeriodSelector sel = new TsPeriodSelector();
        sel.last(freq * 3);

        ftest = SeasonalityTest.stableSeasonality(s.select(sel));
        val = ftest.getPValue();
        if (val < m_config.getSA3Severe()) {
            stream.write(HtmlTag.IMPORTANT_TEXT,
                    "Residual seasonality present in the last 3 years at the "
                    + (100 * m_config.getSA3Severe())
                    + " per cent level: F="
                    + df4.format(ftest.getValue()),
                    Bootstrap4.TEXT_DANGER)
                    .newLine();
        } else if (val < m_config.getSA3Bad()) {
            stream.write(
                    "Residual seasonality present in the last 3 years at the "
                    + (100 * m_config.getSA3Bad())
                    + " per cent level: F="
                    + df4.format(ftest.getValue()),
                    Bootstrap4.TEXT_DANGER).newLine();
        } else if (val < m_config.getSA3Uncertain()) {
            stream.write(
                    "Residual seasonality present in the last 3 years at the "
                    + (100 * m_config.getSA3Uncertain())
                    + " per cent level: F="
                    + df4.format(ftest.getValue()),
                    Bootstrap4.TEXT_WARNING).newLine();
        } else {
            stream.write(
                    "No evidence of residual seasonality in the last 3 years at the "
                    + (100 * m_config.getSA3Uncertain())
                    + " per cent level: F="
                    + df4.format(ftest.getValue()),
                    Bootstrap4.TEXT_SUCCESS).newLine();
        }

        stream.newLine();
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        ResidualSeasonality(stream);
    }
}
