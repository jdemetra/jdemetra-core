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
package ec.benchmarking.ssf;

import data.Data;
import ec.benchmarking.Cumulator;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.eco.DiffuseLikelihood;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.ssf.AkfAlgorithm;
import ec.tstoolkit.ssf.DiffusePredictionErrorDecomposition;
import ec.tstoolkit.ssf.DiffuseSquareRootInitializer;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.LikelihoodEvaluation;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfAlgorithm;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfFunction;
import ec.tstoolkit.ssf.SsfFunctionInstance;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class SsfDisaggregationTest {

    public SsfDisaggregationTest() {
    }

    //@Test
    public void testLLAirline() {
        SarimaModel sarima = new SarimaModelBuilder().createAirlineModel(12, -.5, -.4);
        SsfArima ssf = new SsfArima(sarima);
        SsfDisaggregation disagg = new SsfDisaggregation(3, ssf);
        Cumulator cumul = new Cumulator(3);
        double[] x = Data.P.getValues().internalStorage().clone();
        cumul.transform(x);
        for (int i = 0; i < x.length / 2; ++i) {
            if (i % 3 != 2) {
                x[i] = Double.NaN;
            }
        }
        int K = 100;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            DiffusePredictionErrorDecomposition pred = new DiffusePredictionErrorDecomposition(false);
            Filter filter = new Filter<>();
            filter.setSsf(disagg);
            filter.setInitializer(new DiffuseSquareRootInitializer());
            filter.process(new SsfData(x, null), pred);
            DiffuseLikelihood ll = new DiffuseLikelihood();
            LikelihoodEvaluation.evaluate(pred, ll);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testAirline() {
        TsData X = Data.X;
        double[] x = new double[X.getLength()];
        X.copyTo(x, 0);

        Cumulator cumul = new Cumulator(3);
        cumul.transform(x);
        for (int i = 0; i < x.length; ++i) {
            if (i < x.length / 4 && i % 3 != 2) {
                x[i] = Double.NaN;
            } else {
                int q = 1 + i % 3;
                x[i] = q * Math.log(x[i]) - q * Math.log(q);
            }
        }
        SarimaModel sarima = new SarimaModelBuilder().createAirlineModel(12, -.6, -.8);
        SsfArima ssf = new SsfArima(sarima);
        SsfDisaggregation disagg = new SsfDisaggregation(3, ssf);
        SsfModel<SsfDisaggregation> model = new SsfModel<>(disagg, new SsfData(x, null), null, null);
        AkfAlgorithm<SsfDisaggregation> alg0 = new AkfAlgorithm<>();
        alg0.useDiffuseInitialization(true);
        DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ell0 = alg0.evaluate(model);
        DiffuseConcentratedLikelihood likelihood0 = ell0.getLikelihood();
        SsfAlgorithm<SsfDisaggregation> alg1 = new SsfAlgorithm<>();
        DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ell1 = alg1.evaluate(model);
        DiffuseConcentratedLikelihood likelihood1 = ell1.getLikelihood();
        assertTrue(Math.abs(likelihood0.getLogLikelihood() - likelihood1.getLogLikelihood()) < 1e-7);

    }

    @Test
    public void testAirlineDecomposition() {
        Cumulator cumul = new Cumulator(3);
        TsData X = Data.P;
        double[] x = X.getValues().internalStorage().clone();
        cumul.transform(x);
        for (int i = 0; i < x.length; ++i) {
            if (i < x.length / 4 && i % 3 != 2) {
                x[i] = Double.NaN;
            } else {
                int q = 1 + i % 3;
                x[i] = q * Math.log(x[i]) - q * Math.log(q);
            }
        }
        SarimaModel arima = optimize(x);
        System.out.println(arima);
        SsfUcarima ussf = new SsfUcarima(ucmAirline(arima.theta(1), arima.btheta(1)));
        SsfDisaggregation disagg = new SsfDisaggregation(3, ussf);
        // System.out.println(s);
        Smoother smoother = new Smoother();
        smoother.setCalcVar(true);
        smoother.setSsf(disagg);
        SmoothingResults srslts = new SmoothingResults(true, true);
        smoother.process(new SsfData(x, null), srslts);
        DataBlock p = new DataBlock(X.log());
        DataBlock c0 = new DataBlock(srslts.component(0));
        DataBlock c1 = new DataBlock(srslts.component(1));
        DataBlock c3 = new DataBlock(srslts.component(4));
        DataBlock e3 = new DataBlock(srslts.componentStdev(4));
        System.out.println(p);
        System.out.println(c0);
        System.out.println(c1);
        System.out.println(c3);

        smoother.setSsf(ussf);
        srslts = new SmoothingResults(true, true);
        smoother.process(new SsfData(p, null), srslts);
        DataBlock d3 = new DataBlock(srslts.component(3));
        System.out.println(d3);
        DataBlock f3 = new DataBlock(srslts.componentStdev(3));
        System.out.println(e3);
        System.out.println(f3);
    }

    static UcarimaModel ucmAirline(double th, double bth) {
        SarimaModel sarima = new SarimaModelBuilder().createAirlineModel(12, th, bth);
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(ArimaModel.create(sarima));
        double var = ucm.setVarianceMax(-1, false);
        return ucm;
    }

    private SarimaModel optimize(double[] x) {
        SarimaModel sarima = new SarimaModelBuilder().createAirlineModel(12, -.2, -.2);
        SsfArima ssf = new SsfArima(sarima);
        AkfAlgorithm<SsfDisaggregation> alg = new AkfAlgorithm<>();
        alg.useDiffuseInitialization(true);
        SsfDisaggregation disagg = new SsfDisaggregation(3, ssf);
        SsfModel<SsfDisaggregation> model = new SsfModel<>(disagg, new SsfData(x, null), null, null);
        SsfFunction<SsfDisaggregation> fn = new SsfFunction<>(model, new AirlineMapping(),
                new SsfAlgorithm(), true, true);
        LevenbergMarquardtMethod opt = new LevenbergMarquardtMethod();
        //opt.setConvergenceCriterion(1e-7);
        opt.minimize(fn, sarima.getParameters());
        SsfFunctionInstance<SsfDisaggregation> rslt = (SsfFunctionInstance<SsfDisaggregation>) opt.getResult();
        ssf = (SsfArima) rslt.ssf.getInternalSsf();
        return (SarimaModel) ssf.getModel();
    }
}

class AirlineMapping implements IParametricMapping<SsfDisaggregation> {

    AirlineMapping() {
    }

    @Override
    public SsfDisaggregation map(IReadDataBlock p) {
        SarimaModel sarima = new SarimaModelBuilder().createAirlineModel(12, p.get(0), p.get(1));
        SsfArima ssf = new SsfArima(sarima);
        return new SsfDisaggregation(3, ssf);
    }

    @Override
    public IReadDataBlock map(SsfDisaggregation t) {
        SsfArima ssf = (SsfArima) t.getInternalSsf();
        SarimaModel sarima = (SarimaModel) ssf.getModel();
        return sarima.getParameters();
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        for (int i = 0; i < inparams.getLength(); ++i) {
            if (Math.abs(inparams.get(i)) > .99) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return 1e-8;
    }

    @Override
    public int getDim() {
        return 2;
    }

    @Override
    public double lbound(int idx) {
        return -.99;
    }

    @Override
    public double ubound(int idx) {
        return .99;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        ParamValidation rslt = ParamValidation.Valid;
        for (int i = 0; i < ioparams.getLength(); ++i) {
            if (Math.abs(ioparams.get(i)) > .99) {
                ioparams.set(i, 1 / ioparams.get(i));
                rslt = ParamValidation.Changed;
            }
        }
        return rslt;
    }

    @Override
    public String getDescription(int idx) {
        return idx == 0 ? "th" : "bth";
    }

}
