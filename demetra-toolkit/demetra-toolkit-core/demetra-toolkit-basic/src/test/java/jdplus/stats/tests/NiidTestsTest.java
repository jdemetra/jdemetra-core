/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stats.tests;

import jdplus.data.DataBlock;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class NiidTestsTest {

    public NiidTestsTest() {
    }

    @Test
    public void testRandom() {
        double[] data = new double[120];
        DataBlock X = DataBlock.of(data);
        Random rnd = new Random(0);
        X.set(rnd::nextDouble);

        ec.tstoolkit.stats.NiidTests oldTests = new ec.tstoolkit.stats.NiidTests(
                new ec.tstoolkit.data.ReadDataBlock(data), 12, 2, true);

        NiidTests newTests = NiidTests.builder()
                .data(X)
                .period(12)
                .hyperParametersCount(2)
                .defaultTestsLength()
                .build();

        assertEquals(oldTests.getMeanTest().getValue(), newTests.meanTest().getValue(), 1e-9);
        assertEquals(oldTests.getLjungBox().getValue(), newTests.ljungBox().getValue(), 1e-9);
        assertEquals(oldTests.getLjungBoxOnSquare().getValue(), newTests.ljungBoxOnSquare().getValue(), 1e-9);
        assertEquals(oldTests.getSeasonalLjungBox().getValue(), newTests.seasonalLjungBox().getValue(), 1e-9);
        assertEquals(oldTests.getBoxPierce().getValue(), newTests.boxPierce().getValue(), 1e-9);
        assertEquals(oldTests.getBoxPierceOnSquare().getValue(), newTests.boxPierceOnSquare().getValue(), 1e-9);
        assertEquals(oldTests.getSeasonalBoxPierce().getValue(), newTests.seasonalBoxPierce().getValue(), 1e-9);
        assertEquals(oldTests.getNormalityTest().getValue(), newTests.normalityTest().getValue(), 1e-9);
        assertEquals(oldTests.getSkewness().getValue(), newTests.skewness().getValue(), 1e-9);
        assertEquals(oldTests.getKurtosis().getValue(), newTests.kurtosis().getValue(), 1e-9);
        assertEquals(oldTests.getRuns().getValue(), newTests.runsNumber().getValue(), 1e-9);
        assertEquals(oldTests.getUpAndDownRuns().getValue(), newTests.upAndDownRunsNumbber().getValue(), 1e-9);
    }

    @Test
    public void testRandomWithMissing() {
        double[] data = new double[120];
        DataBlock X = DataBlock.of(data);
        Random rnd = new Random(0);
        X.set(rnd::nextDouble);
        X.set(2, Double.NaN);

        for (int i = 35; i < 50; ++i) {
            X.set(i, Double.NaN);
        }

        ec.tstoolkit.stats.NiidTests oldTests = new ec.tstoolkit.stats.NiidTests(
                new ec.tstoolkit.data.ReadDataBlock(data), 12, 2, true);

        NiidTests newTests = NiidTests.builder()
                .data(X)
                .period(12)
                .hyperParametersCount(2)
                .defaultTestsLength()
                .build();

        assertEquals(oldTests.getMeanTest().getValue(), newTests.meanTest().getValue(), 1e-9);
//  Old implementation is not correct in case of missing values. No impact in the software        
        
//        assertEquals(oldTests.getLjungBox().getValue(), newTests.ljungBox().getValue(), 1e-9);
//        assertEquals(oldTests.getLjungBoxOnSquare().getValue(), newTests.ljungBoxOnSquare().getValue(), 1e-9);
//        assertEquals(oldTests.getSeasonalLjungBox().getValue(), newTests.seasonalLjungBox().getValue(), 1e-9);
//        assertEquals(oldTests.getBoxPierce().getValue(), newTests.boxPierce().getValue(), 1e-9);
//        assertEquals(oldTests.getBoxPierceOnSquare().getValue(), newTests.boxPierceOnSquare().getValue(), 1e-9);
//        assertEquals(oldTests.getSeasonalBoxPierce().getValue(), newTests.seasonalBoxPierce().getValue(), 1e-9);
        assertEquals(oldTests.getNormalityTest().getValue(), newTests.normalityTest().getValue(), 1e-9);
        assertEquals(oldTests.getSkewness().getValue(), newTests.skewness().getValue(), 1e-9);
        assertEquals(oldTests.getKurtosis().getValue(), newTests.kurtosis().getValue(), 1e-9);
        assertEquals(oldTests.getRuns().getValue(), newTests.runsNumber().getValue(), 1e-9);
        assertEquals(oldTests.getUpAndDownRuns().getValue(), newTests.upAndDownRunsNumbber().getValue(), 1e-9);
    }
}
