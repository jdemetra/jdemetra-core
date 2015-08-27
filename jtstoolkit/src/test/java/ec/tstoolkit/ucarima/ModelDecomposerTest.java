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

package ec.tstoolkit.ucarima;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.implementation.TramoProcessingFactory;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.polynomials.AbstractRootSelector;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.ucarima.estimation.BurmanEstimatesC;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class ModelDecomposerTest {

    public ModelDecomposerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void demoStochasticTD() {
        TsData s = Data.X;
        // Tramo pre-processing without trading days
        PreprocessingModel rslt = TramoProcessingFactory.instance.generateProcessing(TramoSpecification.TR3, null).process(s);
        SarimaModel sarima = rslt.estimation.getArima();

        // Usual decomposers (Trend, Seasonal)
        TrendCycleSelector tsel = new TrendCycleSelector(.5);
        SeasonalSelector ssel = new SeasonalSelector(sarima.getSpecification().getFrequency(), 3);
        // New trading days decomposer
        FrequencySelector tdsel = new FrequencySelector(Periodogram.getTradingDaysFrequencies(12)[0]);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);
        decomposer.add(tdsel);

        UcarimaModel ucm = decomposer.decompose(ArimaModel.create(sarima));
        // Canonical decomposition
        double var = ucm.setVarianceMax(-1, true);
        // Estimate the components using Burman
        BurmanEstimatesC burman = new BurmanEstimatesC();
        burman.setUcarimaModel(ucm);
        burman.setData(rslt.linearizedSeries(false));

        double[] td = burman.estimates(2, true);
        if (td == null) {
            return;
        }
        TsData tdStoch = new TsData(s.getStart(), td, false);

        TsDataTable table = new TsDataTable();
        table.insert(-1, tdStoch);

        // Normal TD 
        CompositeResults tsrslt = TramoSeatsProcessingFactory.process(s, TramoSeatsSpecification.RSA5);
        table.insert(-1, tsrslt.getData(ModellingDictionary.TDE, TsData.class).log());

        System.out.println(table);
    }
}

class FrequencySelector extends AbstractRootSelector {

    private final double freq_;
    private double eps_ = 5 * Math.PI / 180; // 5 degrees

    /**
     *
     * @param roots
     */
    public FrequencySelector(final double freq) {
        freq_ = freq;
    }

    /**
     *
     * @param root
     * @return
     */
    @Override
    public boolean accept(final Complex root) {
        if (root.getIm() == 0) {
            return false;
        }
        Complex iroot = root.inv();
        double r = iroot.getRe(), n = iroot.absSquare();
        double f = Math.acos(r * (1 + n) / (2 * n));
        return Math.abs(f - freq_) < eps_;
    }

    /**
     *
     * @return
     */
    public double getEpsilon() {
        return eps_;
    }

    /**
     *
     * @return
     */
    public double getFrequency() {
        return freq_;
    }

    /**
     *
     * @param value
     */
    public void setEpsilon(final double value) {
        eps_ = value;
    }

    public void setEpsilonInDegree(final double deg) {
        eps_ = deg * Math.PI / 180;
    }

    @Override
    public boolean selectUnitRoots(Polynomial p) {
        m_sel = null;
        m_nsel = p;
        return false;
    }
}
