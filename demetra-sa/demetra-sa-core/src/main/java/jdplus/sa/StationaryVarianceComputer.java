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
import demetra.sa.StationaryVarianceDecomposition;
import demetra.timeseries.TsData;
import jdplus.arima.ArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.stats.linearmodel.LeastSquaresResults;
import jdplus.stats.linearmodel.LinearModel;
import jdplus.stats.linearmodel.Ols;
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
public class StationaryVarianceComputer {


    public static interface ILongTermTrendComputer {

        TsData calcLongTermTrend(TsData s);
    }
    
    public static final ILongTermTrendComputer LINEARTREND=new LinearTrendComputer(), HP=new HPTrendComputer();
    
    private static class LinearTrendComputer implements ILongTermTrendComputer {

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
            return TsData.of(s.getStart(), x);
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
            return TsData.ofInternal(s.getStart(), ss.item(0).toArray());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Trend computed by Hodrick-Prescott filter (cycle length = ")
                    .append(cyclelen).append(" years)");
            return builder.toString();
        }
    }
    
    public final ILongTermTrendComputer trendComputer;

    public StationaryVarianceComputer(){
        trendComputer=LINEARTREND;
    }
            
    public StationaryVarianceComputer(ILongTermTrendComputer trendComputer){
        this.trendComputer=trendComputer;
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
    public StationaryVarianceDecomposition build(final TsData O, final TsData T, final TsData S, final TsData I, final TsData Cal, final TsData others, final boolean mul) {
        if (O == null) {
            return null;
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
        Calc = cleanup(Calc);
        Pc = cleanup(Pc);

        TsData lt = trendComputer.calcLongTermTrend(stCc);
        stCc = TsData.subtract(stCc, lt);
        stOc = TsData.subtract(stOc, lt);

        //
        DescriptiveStatistics stats = DescriptiveStatistics.of(stOc.getValues());
        double varO = stats.getVar();

        double varC, varS, varI, varP, varCal; 
        
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

        return StationaryVarianceDecomposition.builder()
                .trendType(type())
                .C(varC)
                .S(varS)
                .I(varI)
                .P(varP)
                .Calendar(varCal)
                .build();
    }

    private StationaryVarianceDecomposition.TrendType type() {
        if (trendComputer instanceof LinearTrendComputer)
            return StationaryVarianceDecomposition.TrendType.Linear;
        else if (trendComputer instanceof HPTrendComputer)
            return StationaryVarianceDecomposition.TrendType.HodrickPrescott;
        else
            return StationaryVarianceDecomposition.TrendType.Other;
     }

    private TsData cleanup(TsData s) {
        if (s.getValues().allMatch(x -> Math.abs(x) < 1e-12)) {
            return null;
        } else {
            return s;
        }
    }
}
