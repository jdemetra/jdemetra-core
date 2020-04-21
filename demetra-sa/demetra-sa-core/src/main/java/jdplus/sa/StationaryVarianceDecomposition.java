/*
 * Copyright 2015 National Bank of Belgium
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
 /*
 */
package jdplus.sa;

import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import demetra.timeseries.TsData;
import java.text.DecimalFormat;
import jdplus.arima.ArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.UnitRoots;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.CompositeSsf;
import jdplus.ssf.univariate.SsfData;
import jdplus.stats.DescriptiveStatistics;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.ssf.SsfUcarima;

/**
 *
 * @author Jean Palate
 */
public class StationaryVarianceDecomposition {

    public static interface ILongTermTrendComputer {

        TsData calcLongTermTrend(TsData s);
    }

    public static class LinearTrendComputer implements ILongTermTrendComputer {

        @Override
        public TsData calcLongTermTrend(TsData s) {
            if (s == null) {
                return null;
            }
            DataBlock x = DataBlock.make(s.length());
            x.set(1);
            x.cumul();
            LinearModel model = LinearModel.builder()
                    .y(s.getValues())
                    .meanCorrection(true)
                    .addX(x)
                    .build();
            LeastSquaresResults ls = Ols.compute(model);
            DoubleSeq b = ls.getCoefficients();
            x.mul(b.get(1));
            x.add(b.get(0));
            return TsData.ofInternal(s.getStart(), x);
        }

        @Override
        public String toString() {
            return "Linear trend computed by Ols";
        }
    }

    public static class HPTrendComputer implements ILongTermTrendComputer {

        private final double cyclelen;

        public HPTrendComputer() {
            cyclelen = 8;
        }

        public HPTrendComputer(double len) {
            cyclelen = len;
        }

        public static double defaultLambda(double ylen, int freq) {
            double w = 2 * Math.PI / (freq * ylen);
            double x = 1 - Math.cos(w);
            return .75 / (x * x);
        }

        @Override
        public TsData calcLongTermTrend(TsData s) {
            Polynomial D = UnitRoots.D(1), D2 = D.times(D);
            double lambda = defaultLambda(cyclelen, s.getAnnualFrequency());
            ArimaModel i2 = new ArimaModel(BackFilter.ONE, new BackFilter(D2), BackFilter.ONE, 1);
            ArimaModel wn = new ArimaModel(BackFilter.ONE, BackFilter.ONE, BackFilter.ONE, lambda);
            UcarimaModel ucm = UcarimaModel.builder()
                    .add(i2)
                    .add(wn)
                    .build();
            CompositeSsf ssf = SsfUcarima.of(ucm);
            DataBlockStorage ss = DkToolkit.fastSmooth(ssf, new SsfData(s.getValues()));
            return TsData.of(s.getStart(), Doubles.of(ss.item(0)));
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Trend computed by Hodrick-Prescott filter (cycle length = ")
                    .append(cyclelen).append(" years)");
            return builder.toString();
        }
    }

    private double varC, varS, varI, varP, varCal;
    private final ILongTermTrendComputer trendComputer;

    public StationaryVarianceDecomposition() {
        trendComputer = new HPTrendComputer();
    }

    public StationaryVarianceDecomposition(final ILongTermTrendComputer trendComputer) {
        this.trendComputer = trendComputer;
    }

    public ILongTermTrendComputer getTrendComputer() {
        return this.trendComputer;
    }

    /**
     * Computes the stationary variance decomposition
     *
     * @param O The original series
     * @param T The linearized trend
     * @param S The linearized seasonal component
     * @param I The linearized irregular component
     * @param Cal The calendar effect
     * @param others The other deterministic effects
     * @param mul Indicates that the decomposition is multiplicative or not
     * @return True if the decomposition was successful, false otherwise
     */
    public boolean process(final TsData O, final TsData T, final TsData S, final TsData I, final TsData Cal, final TsData others, final boolean mul) {
        if (O == null) {
            return false;
        }
        TsData stOc, stCc, Sc, Ic, Calc, Pc;
        if (mul) {
            // use logs
            stOc = O.log();
            stCc = T != null ? T.log() : null;
            Sc = S != null ? S.log() : null;
            Ic = I != null ? I.log() : null;
            Calc = Cal != null ? Cal.log() : null;
            Pc = others != null ? others.log() : null;

        } else {
            stOc = O;
            stCc = T;
            Sc = S;
            Ic = I;
            Calc = Cal;
            Pc = others;
        }

        TsData lt = trendComputer.calcLongTermTrend(stCc);
        stCc = TsData.subtract(stCc, lt);
        stOc = TsData.subtract(stOc, lt);

        //
        DescriptiveStatistics stats = DescriptiveStatistics.of(stOc.getValues());
        double varO = stats.getVar();

        if (stCc != null) {
            stats = DescriptiveStatistics.of(stCc.getValues());
            varC = stats.getVar();
        } else {
            varC = 0;
        }

        if (Sc != null) {
            stats = DescriptiveStatistics.of(Sc.getValues());
            varS = stats.getVar();
        } else {
            varS = 0;
        }
        if (Ic != null) {
            stats = DescriptiveStatistics.of(Ic.getValues());
            varI = stats.getVar();
        } else {
            varI = 0;
        }
        if (Calc != null) {
            stats = DescriptiveStatistics.of(Calc.getValues());
            varCal = stats.getVar();
        } else {
            varCal = 0;
        }
        if (Pc != null) {
            stats = DescriptiveStatistics.of(Pc.getValues());
            varP = stats.getVar();
        } else {
            varP = 0;
        }

        varP /= varO;
        varCal /= varO;
        varS /= varO;
        varC /= varO;
        varI /= varO;

        return true;
    }

//    public boolean process(IProcResults results) {
//        TsData O = results.getData(ModellingDictionary.YC, TsData.class);
//        TsData T = results.getData(ModellingDictionary.T_CMP, TsData.class);
//        TsData S = results.getData(ModellingDictionary.S_CMP, TsData.class);
//        TsData I = results.getData(ModellingDictionary.I_CMP, TsData.class);
//        TsData Cal = results.getData(ModellingDictionary.CAL, TsData.class);
//        TsData D = results.getData(ModellingDictionary.DET, TsData.class);
//        DecompositionMode m = results.getData(ModellingDictionary.MODE, DecompositionMode.class);
//        boolean mul = m != null ? m != DecompositionMode.Additive : false;
//        TsData P = mul ? TsData.divide(D, Cal) : TsData.subtract(D, Cal);
//        return process(O, T, S, I, Cal, P, mul);
//    }
//
    public double getVarI() {
        return varI;
    }

    public double getVarS() {
        return varS;
    }

    public double getVarC() {
        return varC;
    }

    public double getVarP() {
        return varP;
    }

    public double getVarTD() {
        return varCal;
    }

    /**
     *
     * @return
     */
    public double getVarTotal() {
        return varC + varS + varI + varP + varCal;
    }
    private static final String nl = System.lineSeparator();

    @Override
    public String toString() {
        DecimalFormat dg1 = new DecimalFormat("0.#");
        StringBuilder builder = new StringBuilder();
        builder.append("C: ").append(dg1.format(varC * 100)).append(nl);
        builder.append("S: ").append(dg1.format(varS * 100)).append(nl);
        builder.append("I: ").append(dg1.format(varI * 100)).append(nl);
        builder.append("TD+Hol: ").append(dg1.format(varCal * 100)).append(nl);
        builder.append("P: ").append(dg1.format(varP * 100)).append(nl);
        return builder.toString();
    }

}
