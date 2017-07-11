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

package ec.tstoolkit.ssf;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.*;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.ssf.arima.SsfRwAr1;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.ucarima.UcarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class RwExtendedSsfTest {

    public static TsData rnd = TsData.random(TsFrequency.Monthly).delta(1);

    public RwExtendedSsfTest() {
    }

    //@Test
    public void demo0() {
        TsData s = Data.X.log();
        double[] x = new double[]{0, 0};

        TestFunctionInstance rslt = new TestFunctionInstance(s, new DataBlock(x));
        rslt.compute();
        System.out.println("X0");
        printRslt(rslt);
    }

    //@Test
    public void demo1() {
        TsData s = Data.X.log();
        TestFunction fn = new TestFunction(s);
        ISsqFunctionMinimizer min = new ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod();
//        ISsqFunctionMinimizer min = new ec.tstoolkit.maths.realfunctions.minpack.LevenbergMarquardtMinimizer();
//        min.setConvergenceCriterion(1e-9);
        min.setMaxIter(10);
        min.minimize(fn, new TestFunctionInstance(s));

        TestFunctionInstance rslt = (TestFunctionInstance) min.getResult();
        double[] gradient = fn.getDerivatives(rslt).getGradient();
        System.out.println("X");
        printRslt(rslt);
    }

    //@Test
    public void demoRnd0() {
        TsData s = rnd;
        double[] x = new double[]{0};

        TestFunctionInstance rslt = new TestFunctionInstance(s, new DataBlock(x));
        rslt.compute();
        System.out.println("Rnd0");
        printRslt(rslt);
    }

    //@Test
    public void demoRnd1() {
        TsData s = rnd;
        TestFunction fn = new TestFunction(s);
        ISsqFunctionMinimizer min = new LevenbergMarquardtMethod();
        min.minimize(fn, new TestFunctionInstance(s));
        TestFunctionInstance rslt = (TestFunctionInstance) min.getResult();
        System.out.println("Rnd1");
        printRslt(rslt);
    }

    void printRslt(TestFunctionInstance rslt) {
        System.out.println(rslt.getParameters());
        System.out.println(rslt.ll.getLikelihood().getLogLikelihood());
        System.out.println();
        double[] scmp = rslt.scomponent();
        for (int i = 0; i < scmp.length; ++i) {
            System.out.println(scmp[i]);
        }
        System.out.println();
    }

    // time-varying trading days
    //@Test
    public void demoTD() {
        TsData s = Data.X;
        CompositeResults rslts = TramoSeatsProcessingFactory.process(s, TramoSeatsSpecification.RSA5);
        PreprocessingModel regarima = rslts.get("preprocessing", PreprocessingModel.class);
        SeatsResults seats = rslts.get("decomposition", SeatsResults.class);
        assertTrue(seats != null && regarima != null);

        if (regarima.isMultiplicative()) {
            s = s.log();
        }
        int[] calPos = regarima.description.getRegressionVariablePositions(ComponentType.CalendarEffect);
        UcarimaModel ucm = seats.getUcarimaModel();
        // compute the full decomposition...
        SsfUcarima stoch = new SsfUcarima(ucm);
        ExtendedSsfData xdata = new ExtendedSsfData(new SsfData(s, null));
        xdata.setForecastsCount(s.getFrequency().intValue());
        Matrix x = regarima.description.buildRegressionVariables().all().matrix(new TsDomain(s.getStart(), xdata.getCount()));
        RegSsf xssf = new RegSsf(stoch, x.subMatrix());

        Filter filter = new Filter();
        filter.setInitializer(new DiffuseSquareRootInitializer());
        filter.setSsf(xssf);
        DiffuseFilteringResults fr = new DiffuseFilteringResults(true);
        fr.getVarianceFilter().setSavingP(true);
        fr.getFilteredData().setSavingA(true);
        filter.process(xdata, fr);
        Smoother smoother = new Smoother();
        smoother.setSsf(xssf);
        smoother.setCalcVar(true);
        SmoothingResults sm = new SmoothingResults();
        smoother.process(xdata, fr, sm);

        Smoother lsmoother = new Smoother();
        lsmoother.setSsf(stoch);
        lsmoother.setCalcVar(true);
        SmoothingResults lsm = new SmoothingResults();
        ExtendedSsfData xldata = new ExtendedSsfData(new SsfData(regarima.linearizedSeries(false), null));
        xldata.setForecastsCount(s.getFrequency().intValue());
        lsmoother.process(xldata, lsm);

        int spos = stoch.cmpPos(1);
        DataBlock Z = new DataBlock(xssf.getStateDim());
        double[] v = new double[xdata.getCount()];
        double[] c = new double[xdata.getCount()];
        double[] svar = sm.componentVar(spos);
        double[] slvar = lsm.componentVar(spos);
        int start = regarima.description.getRegressionVariablesStartingPosition();
        for (int i = 0; i < v.length; ++i) {
        Z.set(spos, 1);
            for (int j = 0; j < calPos.length; ++j) {
                Z.set(stoch.getStateDim() + calPos[j], x.get(i, calPos[j]));
            }
            v[i] = sm.zvariance(i, Z);
            Z.set(spos,0);
            c[i] = sm.zvariance(i, Z);
            System.out.print(svar[i]);
            System.out.print('\t');
            System.out.print(slvar[i]);
            System.out.print('\t');
            System.out.print(c[i]);
            System.out.print('\t');
            System.out.println(v[i]);
        }
        System.out.println(sm.P(50));
        System.out.println(sm.P(svar.length - 1));
        System.out.println(regarima.estimation.getLikelihood().getBVar());
    }
}

class TestDomain implements IParametersDomain {

    TestDomain() {
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return 1e-6;
    }

    @Override
    public int getDim() {
        return 1;
    }

    @Override
    public double lbound(int idx) {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double ubound(int idx) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        return ParamValidation.Valid;
    }

    @Override
    public String getDescription(int idx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class TestDomain2 implements IParametersDomain {

    TestDomain2() {
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        double ro = inparams.get(0), var = inparams.get(1);
        return ro > -1 && ro < 1;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return 1e-6;
    }

    @Override
    public int getDim() {
        return 2;
    }

    @Override
    public double lbound(int idx) {
        return idx == 0 ? -1 + 1e-6 : Double.NEGATIVE_INFINITY;
    }

    @Override
    public double ubound(int idx) {
        return idx == 0 ? 1 - 1e-6 : Double.POSITIVE_INFINITY;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        double ro = ioparams.get(0), var = ioparams.get(1);
        boolean changed = false;
        if (ro <= -10) {
            ro = -1 + 1e-6;
            changed = true;
        }
        if (ro >= 1 - 1e-6) {
            ro = 1 - 1e-6;
            changed = true;
        }
//        if (var < 0) {
//            var = 0;
//            changed = true;
//        }
        if (changed) {
            ioparams.set(0, ro);
            //           ioparams.set(1, var);
            return ParamValidation.Changed;
        } else {
            return ParamValidation.Valid;
        }
    }

    @Override
    public String getDescription(int idx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class TestFunction implements ISsqFunction {

    private final TsData data;

    TestFunction(TsData s) {
        data = s;
    }

    @Override
    public ISsqFunctionDerivatives getDerivatives(ISsqFunctionInstance point) {
        return new SsqNumericalDerivatives(this, point);
    }

    @Override
    public IParametersDomain getDomain() {
        return new TestDomain2();
    }

    @Override
    public ISsqFunctionInstance ssqEvaluate(IReadDataBlock parameters) {
        return new TestFunctionInstance(data, parameters);
    }
}

class TestFunctionInstance implements ISsqFunctionInstance {

    TestFunctionInstance(TsData data) {
        this(data, null);
    }

    TestFunctionInstance(TsData data, IReadDataBlock parameters) {
        this.data = data;
        if (parameters != null) {
            params[0] = parameters.get(0);
            params[1] = parameters.get(1);
        }
        int n = data.getLength();
        int freq = data.getFrequency().intValue();
        S = new Matrix(n, freq - 1);
        SeasonalDummies s = new SeasonalDummies(data.getFrequency());
        s.data(data.getDomain(), S.columnList());
        ssfdata = new SsfData(data.internalStorage(), null);
        SsfRwAr1 rwar1 = new SsfRwAr1();
        rwar1.setRho(params[0]);
        ssf = new RwExtendedSsf(rwar1, S.subMatrix(), params[1] * params[1]);
    }

    void compute() {
        if (ll != null) {
            return;
        }
        Filter filter = new Filter();
        filter.setSsf(ssf);
        DiffuseFilteringResults dfr = new DiffuseFilteringResults();
        filter.process(ssfdata, dfr);
        DiffuseConcentratedLikelihood dll = new DiffuseConcentratedLikelihood();
        LikelihoodEvaluation.evaluate(dfr, dll);
        ll = new DefaultLikelihoodEvaluation<>(dll);
    }

    double[] scomponent() {

        DisturbanceSmoother smoother = new DisturbanceSmoother();
        smoother.setSsf(ssf);
        smoother.process(ssfdata);
        SmoothingResults sstates = smoother.calcSmoothedStates();
        double[] cmp = new double[ssfdata.getCount()];
        DataBlock z = new DataBlock(ssf.getStateDim());
        DataBlock zc = z.drop(2, 0);
        for (int i = 0; i < cmp.length; ++i) {
            zc.copy(S.row(i));
            cmp[i] = sstates.zcomponent(i, z);
        }
        return cmp;
    }
    final double[] params = new double[]{-.5, 1}; // ro=0, var=0;
    final TsData data;
    final Matrix S;
    DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ll;
    final ISsf ssf;
    final ISsfData ssfdata;

    @Override
    public IReadDataBlock getParameters() {
        return new ReadDataBlock(params);
    }

    @Override
    public double[] getE() {
        compute();
        return ll.getE();
    }

    @Override
    public double getSsqE() {
        compute();
        return ll.getSsqValue();
    }
}
