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
package ec.satoolkit.diagnostics;

import ec.businesscycle.simplets.TsHodrickPrescott;
import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import java.text.DecimalFormat;

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
            RegModel model = new RegModel();
            model.setY(TsDataBlock.all(s).data);
            model.setMeanCorrection(true);
            DataBlock x = new DataBlock(s.getLength());
            x.set(1);
            x.cumul();
            model.addX(x);
            Ols ols = new Ols();
            if (ols.process(model)) {
                double[] b = ols.getLikelihood().getB();
                x.mul(b[1]);
                x.add(b[0]);
                return new TsData(s.getStart(), x);
            } else {
                return null;
            }
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

        @Override
        public TsData calcLongTermTrend(TsData s) {
            TsHodrickPrescott hp = new TsHodrickPrescott();
            hp.setCycleLength(cyclelen);
            if (hp.process(s)) {
                return hp.getTrend();
            } else {
                return null;
            }
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
        DescriptiveStatistics stats = new DescriptiveStatistics(stOc);
        double varO = stats.getVar();

        if (stCc != null) {
            stats = new DescriptiveStatistics(stCc);
            varC = stats.getVar();
        } else {
            varC = 0;
        }

        if (Sc != null) {
            stats = new DescriptiveStatistics(Sc);
            varS = stats.getVar();
        } else {
            varS = 0;
        }
        if (Ic != null) {
            stats = new DescriptiveStatistics(Ic);
            varI = stats.getVar();
        } else {
            varI = 0;
        }
        if (Calc != null) {
            stats = new DescriptiveStatistics(Calc);
            varCal = stats.getVar();
        } else {
            varCal = 0;
        }
        if (Pc != null) {
            stats = new DescriptiveStatistics(Pc);
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

    public boolean process(IProcResults results) {
        TsData O = results.getData(ModellingDictionary.YC, TsData.class);
        TsData T = results.getData(ModellingDictionary.T_CMP, TsData.class);
        TsData S = results.getData(ModellingDictionary.S_CMP, TsData.class);
        TsData I = results.getData(ModellingDictionary.I_CMP, TsData.class);
        TsData Cal = results.getData(ModellingDictionary.CAL, TsData.class);
        TsData D = results.getData(ModellingDictionary.DET, TsData.class);
        DecompositionMode m = results.getData(ModellingDictionary.MODE, DecompositionMode.class);
        boolean mul = m != null ? m != DecompositionMode.Additive : false;
        TsData P = mul ? TsData.divide(D, Cal) : TsData.subtract(D, Cal);
        return process(O, T, S, I, Cal, P, mul);
    }

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
    private static final String nl = "\r\n";

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
