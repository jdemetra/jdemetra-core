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

package ec.tstoolkit.data;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.modelling.arima.tramo.spectrum.TPeaks;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class BlackmanTukeySpectrumTest {

    // some data...
    // start in jan-1995
    static double[] g_exports = {
        9568.3, 9920.3, 11353.5, 9247.5, 10114.2, 10763.1, 8456.1, 8071.6, 10328, 10551.4, 10186.1, 8821.6,
        9841.3, 10233.6, 10794.6, 10289.3, 10513.4, 10607.6, 9707.4, 8103.5, 10982.6, 11836.9, 10517.5, 9810.5,
        10374.8, 10855.3, 11671.3, 11901.2, 10846.4, 11917.5, 11362.8, 9314.5, 12605.9, 12815.1, 11254.5, 11111.8,
        11282.9, 11554.5, 12935.6, 12146.3, 11615.3, 13214.8, 11735.5, 9522.3, 12694.8, 12317.6, 11450, 11380.9,
        10604.6, 10972.2, 13331.5, 11733.1, 11284.7, 13295.8, 11881.4, 10374.2, 13828, 13490.5, 13092.2, 13184.4,
        12398.4, 13882.3, 15861.5, 13286.1, 15634.9, 14211, 13646.8, 12224.6, 15916.4, 16535.9, 15796, 14418.6,
        15044.5, 14944.2, 16754.8, 14254, 15454.9, 15644.8, 14568.3, 12520.2, 14803, 15873.2, 14755.3, 12875.1,
        14291.1, 14205.3, 15859.4, 15258.9, 15498.6, 15106.5, 15023.6, 12083, 15761.3, 16943, 15070.3, 13659.6,
        14768.9, 14725.1, 15998.1, 15370.6, 14956.9, 15469.7, 15101.8, 11703.7, 16283.6, 16726.5, 14968.9, 14861,
        14583.3, 15305.8, 17903.9, 16379.4, 15420.3, 17870.5, 15912.8, 13866.5, 17823.2, 17872, 17420.4, 16704.4,
        15991.5, 16583.6, 19123.4, 17838.8, 17335.3, 19026.9, 16428.6, 15337.4, 19379.8, 18070.5, 19563, 18190.6,
        17658, 18437.9, 21510.4, 17111, 19732.7, 20221.8
    };

    public BlackmanTukeySpectrumTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testDefault() {
        double[] dx = new double[g_exports.length - 1];
        for (int i = 0; i < dx.length; ++i) {
            dx[i] = g_exports[i + 1] - g_exports[i];
        }
        BlackmanTukeySpectrum tukey = new BlackmanTukeySpectrum();
        tukey.setWindowLength(112);
        tukey.setWindowType(WindowType.Tukey);
        tukey.setData(dx);

        assertTrue(tukey.isValid());
        double[] spectrum = tukey.getSpectrum();
//        for (int i = 0; i < spectrum.length; ++i) {
//            System.out.println(spectrum[i]);
//        }

        TsData ts = new TsData(TsFrequency.Monthly, 1995, 0, g_exports, true);
        ts = ts.delta(1);
        DescriptiveStatistics ds = new DescriptiveStatistics(ts);
        ts = ts.minus(ds.getAverage());
        TPeaks tpeaks = new TPeaks(ts);
//        for (int i = 0; i < spectrum.length; ++i) {
//            System.out.println(tpeaks.getSpect()[i + 1]);
//        }
    }

    //@Test
    public void testRandom() {
        int N = 360, M = 500;
//        int[] D = new int[500];
        ArimaModelBuilder builder = new ArimaModelBuilder();
        BlackmanTukeySpectrum tukey = new BlackmanTukeySpectrum();
        tukey.setWindowLength(240);
//        tukey.setDefaultWindowLength(N, 12);
        tukey.setWindowType(WindowType.Tukey);

        double m = 0;
        double m2 = 0;
        int nm = 0;
        for (int i = 0; i < M; ++i) {
            double[] x = builder.generate(builder.createModel(Polynomial.ONE, Polynomial.ONE, 1), N);
            tukey.setData(x);
            //tukey.setWindowType(WindowType.Square);
            double dz = tukey.getAverageSpectrum(Periodogram.getSeasonalFrequencies(12));
            m += dz;
            m2 += dz * dz;
            ++nm;
//            double[] spec = tukey.getSpectrum();
//            for (int j = 0; j < spec.length; ++j) {
//                double dz = spec[j]-1;
//                m += dz;
//                m2 += dz * dz;
//                ++nm;
//            }
//            int z = (int) (dz * 250);
//            if (z >= 0 && z < D.length) {
//                D[z]++;
//            }
        }
        double v = (m2 - m * m / nm) / nm;
        StatisticalTest averageSpectrumTest = tukey.getAverageSpectrumTest(12);

//        for (int i = 0; i < D.length; ++i) {
//            System.out.println(1.0 / M * D[i]);
//        }
//        System.out.println(m / M);
//        System.out.println(v);
//        System.out.println(2 * (tukey.getWindowLength() - 2.0) / (N * 12));
    }
}
