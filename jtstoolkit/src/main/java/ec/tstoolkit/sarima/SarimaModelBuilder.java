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
package ec.tstoolkit.sarima;

import ec.tstoolkit.arima.*;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.StochasticRandomizer;
import ec.tstoolkit.random.XorshiftRNG;

/**
 * @author Jeremy Demortier, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaModelBuilder {

    private static IRandomNumberGenerator RNG = XorshiftRNG.fromSystemNanoTime();
    private IRandomNumberGenerator rng = RNG;

    public void setRandomNumberGenerator(IRandomNumberGenerator rng) {
        this.rng = rng;
    }

    public IRandomNumberGenerator getRandomNumberGenerator() {
        return rng;
    }

    /**
     *
     */
    public SarimaModelBuilder() {
    }

    /**
     *
     * @param frequency
     * @param th
     * @param bth
     * @return
     */
    public SarimaModel createAirlineModel(final int frequency, final double th,
            final double bth) {
        try {
            SarimaSpecification spec = new SarimaSpecification(frequency);
            spec.airline();
            SarimaModel model = new SarimaModel(spec);
            model.setTheta(1, th);
            model.setBTheta(1, bth);
            return model;
        } catch (ArimaException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param frequency
     * @param p
     * @param d
     * @param q
     * @param bp
     * @param bd
     * @param bq
     * @return
     */
    public SarimaModel createArimaModel(final int frequency, final int p,
            final int d, final int q, final int bp, final int bd, final int bq) {
        try {
            SarimaSpecification spec = new SarimaSpecification(frequency);
            spec.setP(p);
            spec.setD(d);
            spec.setQ(q);
            spec.setBP(bp);
            spec.setBD(bd);
            spec.setBQ(bq);
            return new SarimaModel(spec);
        } catch (ArimaException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized double next(double e) {
        return StochasticRandomizer.normal(rng, 0, e);
    }

    public SarimaModel randomize(SarimaModel spec, double stdev) {
        if (stdev <= 0)
            return spec;
        SarimaModel model = null;
        do {
            model = spec.clone();
            DataBlock p = new DataBlock(spec.getParameters());
            for (int i = 0; i < p.getLength(); ++i) {
                double v = p.get(i);
                v += next(stdev);
                p.set(i, v);
            }
            model.setParameters(p);
        } while (!model.isValid(true));
        return model;
    }

    /**
     *
     * @param frequency
     * @param p
     * @param q
     * @param bp
     * @param bq
     * @return
     */
    public SarimaModel createArmaModel(final int frequency, final int p,
            final int q, final int bp, final int bq) {
        try {
            SarimaSpecification spec = new SarimaSpecification(frequency);
            spec.setP(p);
            spec.setD(0);
            spec.setQ(q);
            spec.setBP(bp);
            spec.setBD(0);
            spec.setBQ(bq);
            return new SarimaModel(spec);
        } catch (ArimaException e) {
            throw new RuntimeException(e);
        }
    }
}
