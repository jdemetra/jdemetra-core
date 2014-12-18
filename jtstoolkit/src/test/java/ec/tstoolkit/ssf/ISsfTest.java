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
package ec.tstoolkit.ssf;

import data.Data;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.arima.SsfRw;
import ec.tstoolkit.ssf.implementation.SsfNoise;
import ec.tstoolkit.ucarima.UcarimaModel;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class ISsfTest {

    public ISsfTest() {
    }

    //@Test
    public void testComposite() {
        // we consider the following model
        // y(t) = u(t) + e(t)
        // u(t) = u(t-1) + n(t)
        // n(t) ~ N(0,1)
        // e(t) ~ N(0,v)
        double v = 10;
        ArimaModel e = new ArimaModel(v);
        ArimaModel u = new ArimaModel(BackFilter.ONE, BackFilter.D1, BackFilter.ONE, 1);
        UcarimaModel ucm = new UcarimaModel(null, new ArimaModel[]{u, e});
        SsfArima ssfarima = new SsfArima(ucm.sum());

        DataBlock x = new DataBlock(300);
        x.randomize();

        SsfData data = new SsfData(x, null);

        Filter<ISsf> filter = new Filter<>();
        filter.setSsf(ssfarima);
        long t0 = System.currentTimeMillis();
        PredictionErrorDecomposition decomp1, decomp2, decomp3;
        for (int i = 0; i < 1000; ++i) {
            decomp1 = new PredictionErrorDecomposition(true);
            filter.process(data, decomp1);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        SsfRw rw = new SsfRw();
        SsfNoise noise = new SsfNoise(v);
        DefaultCompositeModel composite = new DefaultCompositeModel(rw, noise);
        SsfComposite cssf = new SsfComposite(composite);
        filter.setSsf(cssf);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            decomp2 = new PredictionErrorDecomposition(true);
            filter.process(data, decomp2);
        }
        DefaultTimeInvariantSsf tissf = DefaultTimeInvariantSsf.of(cssf);
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        filter.setSsf(tissf);
        for (int i = 0; i < 1000; ++i) {
            decomp3 = new PredictionErrorDecomposition(true);
            filter.process(data, decomp3);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

   // @Test
    public void testArima() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        SarimaModel sarima = new SarimaModel(spec);
        SsfArima ssfarima = new SsfArima(sarima);

        DataBlock x = new DataBlock(300);
        x.randomize();

        SsfData data = new SsfData(x, null);

        Filter<ISsf> filter = new Filter<>();
        filter.setSsf(ssfarima);
        long t0 = System.currentTimeMillis();
        PredictionErrorDecomposition decomp1, decomp2;
        for (int i = 0; i < 100; ++i) {
            decomp1 = new PredictionErrorDecomposition(true);
            filter.process(data, decomp1);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        DefaultTimeInvariantSsf tissf = DefaultTimeInvariantSsf.of(ssfarima);
        t0 = System.currentTimeMillis();
        filter.setSsf(tissf);
        for (int i = 0; i < 100; ++i) {
            decomp2 = new PredictionErrorDecomposition(true);
            filter.process(data, decomp2);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testSsfArima() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        SarimaModel sarima = new SarimaModel(spec);
        SsfArima ssfarima = new SsfArima(sarima);

        SsfData data = new SsfData(Data.X, null);

        Filter<ISsf> filter = new Filter<>();
        filter.setSsf(ssfarima);
        SsfModel<SsfArima> model = new SsfModel<>(ssfarima, data, null, null);
        SM mapping = new SM(spec);
        SsfFunction<SsfArima> fn = new SsfFunction<>(model, mapping, new SsfAlgorithm());
        ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod lm = new ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod();
        lm.minimize(fn, mapping.map(ssfarima));
        SsfFunctionInstance rslt=(SsfFunctionInstance<SsfArima>) lm.getResult();
        ssfarima=(SsfArima) rslt.ssf;
        System.out.println(ssfarima.getModel());
    }
}

class SM implements IParametricMapping<SsfArima> {

    private final SarimaMapping mapping_;

    SM(SarimaSpecification spec) {
        mapping_ = new SarimaMapping(spec, true);
    }

    @Override
    public SsfArima map(IReadDataBlock p) {
        return new SsfArima(mapping_.map(p));
    }

    @Override
    public IReadDataBlock map(SsfArima t) {
        SarimaModel arima = (SarimaModel) t.getModel();
        return mapping_.map(arima);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return mapping_.checkBoundaries(inparams);
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return mapping_.epsilon(inparams, idx);
    }

    @Override
    public int getDim() {
        return mapping_.getDim();
    }

    @Override
    public double lbound(int idx) {
        return mapping_.lbound(idx);
    }

    @Override
    public double ubound(int idx) {
        return mapping_.ubound(idx);
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        return mapping_.validate(ioparams);
    }

    @Override
    public String getDescription(int idx) {
        return mapping_.getDescription(idx);
    }

}
