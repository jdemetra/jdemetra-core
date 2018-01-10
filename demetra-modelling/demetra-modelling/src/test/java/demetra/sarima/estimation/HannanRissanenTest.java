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
package demetra.sarima.estimation;

import demetra.arima.ArimaSeriesGenerator;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarmaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
public class HannanRissanenTest {

    static final DoubleSequence airlineData, data;

    static {
        SarmaSpecification spec = new SarmaSpecification();
        spec.setPeriod(12);
        spec.setQ(1);
        spec.setBq(1);
        SarimaModel arima = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();
        ArimaSeriesGenerator generator = new ArimaSeriesGenerator();
        airlineData = DoubleSequence.ofInternal(generator.generate(arima, 120));
        spec.setP(3);
        arima = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .phi(-.2, -.3, .4)
                .build();
        data = DoubleSequence.ofInternal(generator.generate(arima, 120));
    }

    public HannanRissanenTest() {
    }

    @Test
    @Ignore
    public void testAirline() {
        HannanRissanen hr = new HannanRissanen();
        SarmaSpecification spec = new SarmaSpecification();
        spec.setPeriod(12);
        spec.setQ(1);
        spec.setBq(1);
        hr.process(airlineData, spec);
        System.out.println("New airline");
        System.out.println(hr.getModel());
    }

    @Test
    @Ignore
    public void test3101() {
        HannanRissanen hr = new HannanRissanen();
        SarmaSpecification spec = new SarmaSpecification();
        spec.setPeriod(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBq(1);
        hr.process(data, spec);
        System.out.println("New 3101");
        System.out.println(hr.getModel());
    }

    @Test
    @Ignore
    public void test3101_burg() {
        HannanRissanen hr = HannanRissanen.builder().finalCorrection(false).initialization(HannanRissanen.Initialization.Burg).build();
        SarmaSpecification spec = new SarmaSpecification();
        spec.setPeriod(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBq(1);
        hr.process(data, spec);
        System.out.println("New 3101, Burg");
        System.out.println(hr.getModel());
    }

    @Test
    @Ignore
    public void testLegacyAirline() {
        ec.tstoolkit.sarima.estimation.HannanRissanen hr = new ec.tstoolkit.sarima.estimation.HannanRissanen();
        ec.tstoolkit.sarima.SarmaSpecification spec = new ec.tstoolkit.sarima.SarmaSpecification(12);
        spec.setQ(1);
        spec.setBQ(1);
        hr.process(new ec.tstoolkit.data.ReadDataBlock(airlineData.toArray()), spec);
        System.out.println("Legacy airline");
        System.out.println(hr.getModel());
    }

    @Test
    @Ignore
    public void testLegacy3101() {
        ec.tstoolkit.sarima.estimation.HannanRissanen hr = new ec.tstoolkit.sarima.estimation.HannanRissanen();
        ec.tstoolkit.sarima.SarmaSpecification spec = new ec.tstoolkit.sarima.SarmaSpecification(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBQ(1);
        hr.process(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), spec);
        System.out.println("Legacy 3101");
        System.out.println(hr.getModel());
    }

    @Test
    @Ignore
    public void stressTest() {
        int K = 100000;
        for (int q = 0; q < 2; ++q) {
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < (q == 0 ? 100 : K); ++i) {
                ec.tstoolkit.sarima.estimation.HannanRissanen hr = new ec.tstoolkit.sarima.estimation.HannanRissanen();
                ec.tstoolkit.sarima.SarmaSpecification spec = new ec.tstoolkit.sarima.SarmaSpecification(12);
                spec.setQ(1);
                spec.setBQ(1);
                hr.process(new ec.tstoolkit.data.ReadDataBlock(airlineData.toArray()), spec);
            }
            long t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            for (int i = 0; i < (q == 0 ? 100 : K); ++i) {
                HannanRissanen hr = new HannanRissanen();
                SarmaSpecification spec = new SarmaSpecification();
                spec.setPeriod(12);
                spec.setQ(1);
                spec.setBq(1);
                hr.process(airlineData, spec);
            }
            t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
        }
    }
}
