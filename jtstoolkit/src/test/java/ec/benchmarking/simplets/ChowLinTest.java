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
package ec.benchmarking.simplets;

import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.realfunctions.DefaultDomain;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IParametersDomain;
import ec.tstoolkit.maths.realfunctions.NumericalDerivatives;
import ec.tstoolkit.maths.realfunctions.SingleParameter;
import ec.tstoolkit.maths.realfunctions.bfgs.Bfgs;
import ec.tstoolkit.maths.realfunctions.riso.Lbfgs;
import ec.tstoolkit.maths.realfunctions.riso.LbfgsMinimizer;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class ChowLinTest {

    static double[] PCRA = {
        16094.34042, 16368.12021, 15233.88966, 15370.77955, 15683.67074, 16407.23161, 16876.56839, 17424.12796,
        18206.35593, 19340.58648, 19555.69917, 19916.36579, 20482.17633, 21100.69323, 22038.76268/*,23006.35799,24479.42141
     ,26286.34352,28357.72638,30543.23084*/

    };
    static double[] IND_PCR = {
        103.29532, 102.75762, 101.91172, 102.58308, 103.07061, 102.87651, 104.22476, 95.163725, 95.500964,
        94.947974, 94.477737, 93.422194, 94.20844, 91.778527, 92.600797, 92.313678, 91.358591, 92.104858,
        90.402934, 91.4894, 90.6345, 92.098056, 91.939821, 92.283861, 92.091555, 91.701768, 92.288204,
        91.592825, 91.216143, 91.431754, 90.986469, 91.359825, 91.535054, 91.736206, 93.27483, 94.382133,
        95.025179, 97.090378, 96.715138, 98.10199, 99.148287, 99.172499, 100.04511, 99.498929, 99.061791,
        99.666563, 99.600238, 100.33003, 100.71723, 101.44431, 101.94025, 102.34955, 102.13386, 103.151,
        105.14168, 105.66517, 108.35956, 107.9938, 109.07239, 109.41624, 110.01066, 111.35785, 112.7508,
        114.25636, 115.12613, 116.84717, 116.93883, 119.16994, 121.01932, 123.56063, 125.66876, 128.84939,
        131.31864, 133.76343, 135.89239, 137.78569, 142.49302, 143.08843, 145.92117, 148.21557
    };

    public ChowLinTest() {
    }

    @Test
    public void testChowLin() {
        ChowLin cl = new ChowLin();
        TsData Y = new TsData(TsFrequency.Yearly, 1977, 0, PCRA, true);
        TsData Q = new TsData(TsFrequency.Quarterly, 1977, 0, IND_PCR, true);

        TsVariableList vars = new TsVariableList();
        vars.add(new TsVariable(Q));

        cl.setConstant(true);
        cl.process(Y, vars);

        System.out.println();
        System.out.println("1");
        System.out.println(cl.getRho());
        System.out.println(cl.getDisaggregatedSeries());
    }

    @Test
    public void testModel2() {
//        long t0 = System.currentTimeMillis();
//        for (int A = 0; A < 10; ++A) {
        TsData Y = new TsData(TsFrequency.Yearly, 1977, 0, PCRA, true);
        TsData Q = new TsData(TsFrequency.Quarterly, 1977, 0, IND_PCR, true);

//        TsVariableList vars = new TsVariableList();
//        vars.add(new TsVariable(Q));
//
//        ChowLin cl = new ChowLin();
//        cl.setConstant(true);
//        cl.process(Y, vars);
//
//        System.out.println(cl.getRho());
//        System.out.println(cl.getDisaggregatedSeries());

        // 
        TsData y = new TsData(TsFrequency.Quarterly, 1977, 0, IND_PCR.length);
        int j = 0;
        for (int i = 0; i < Y.getLength(); ++i) {
            for (int k = 0; k < 4; ++k, ++j) {
                y.set(j, Y.get(i) / 4);
            }
        }
        for (; j < y.getLength(); ++j) {
            y.set(j, y.get(j - 1));
        }
        TsData yprev;
        // EM
        int iter = 0;
        double a = 0, b = 0, rho = .5;
        SarmaSpecification spec = new SarmaSpecification(1);
        spec.setP(1);
        SarimaModel arma = new SarimaModel(spec);
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>(arma, new DataBlock(Q));
        regarima.setMeanCorrection(true);
        do {
            yprev = y.clone();
            regarima.clearX();
            regarima.addX(new DataBlock(y));
            arma.setPhi(1, -rho);
            regarima.setArima(arma);
            // M estimate a,b,rho
            ConcentratedLikelihood ll = regarima.computeLikelihood();
            a = ll.getB()[0];
            b = ll.getB()[1];
            // E estimate of the smoothed series
            TsData X = Q.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
            X = X.minus(Y.times(b));
            X = X.minus(a * 4);

            TsExpander expander = new TsExpander();
            expander.setModel(TsExpander.Model.AR1);
            TsData u = expander.expand(X, Q.getDomain());
            rho = expander.getParameter();
            u = Q.minus(u);
            u = u.minus(a);
            y = u.div(b);
            System.out.print(a);
            System.out.print('\t');
            System.out.print(b);
            System.out.print('\t');
            System.out.print(rho);
            System.out.println();
        } while (iter++ < 50 && yprev.distance(y) > 1e-6);
        System.out.println();
        System.out.println(y);
//   
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println();
//        System.out.println("2");
//        System.out.println(t1 - t0);
    }

    @Test
    public void testModel2bis() {
        long t0 = System.currentTimeMillis();
        for (int A = 0; A < 100; ++A) {
            TsData Y = new TsData(TsFrequency.Yearly, 1977, 0, PCRA, true);
            TsData Q = new TsData(TsFrequency.Quarterly, 1977, 0, IND_PCR, true);
            TsData X = Q.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
            Ols ols = new Ols();
            RegModel model = new RegModel();
            model.setY(new DataBlock(X));
            model.setMeanCorrection(true);
            model.addX(new DataBlock(Y));
            ols.process(model);

            LbfgsMinimizer bfgs = new LbfgsMinimizer();
            bfgs.minimize(new MyFunction(Q, Y), new MyFunctionInstance(ols.getLikelihood().getB()[1], Q, Y));
            MyFunctionInstance rslt = (MyFunctionInstance) bfgs.getResult();
            rslt.getValue();
//        System.out.println();
//        System.out.println("2 bis");
//        System.out.println(rslt.b);
//        System.out.println(rslt.y);
        }
        long t1 = System.currentTimeMillis();
        System.out.println();
        System.out.println("2Bis");
        System.out.println(t1 - t0);
    }
}

class MyFunctionInstance implements IFunctionInstance {

    final TsData x, Y;
    final double b;
    TsData y;

    public MyFunctionInstance(double b, TsData x, TsData Y) {
        this.b = b;
        this.x = x;
        this.Y = Y;
    }

    @Override
    public IReadDataBlock getParameters() {
        return new SingleParameter(b);
    }

    @Override
    public double getValue() {
        TsData Z = x.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        Z = Z.minus(Y.times(b));
        ChowLin cl = new ChowLin();
        cl.setConstant(true);
        cl.process(Z, x.getFrequency(), x.getLength() - Z.getLength() * 4);
        y = x.minus(cl.getDisaggregatedSeries());
        y.getValues().div(b);

        return -cl.getLikelihoodStatistics().logLikelihood;
    }
}

class MyFunction implements IFunction {

    private final TsData x, Y;

    MyFunction(TsData x, TsData Y) {
        this.x = x;
        this.Y = Y;
    }

    @Override
    public IFunctionInstance evaluate(final IReadDataBlock parameters) {
        return new MyFunctionInstance(parameters.get(0), x, Y);
    }

    @Override
    public IFunctionDerivatives getDerivatives(IFunctionInstance point) {
        return new NumericalDerivatives(this, point, true); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IParametersDomain getDomain() {
        return new DefaultDomain(1, 1e-8);
    }
}
