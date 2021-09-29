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
package jdplus.x13;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.x11.X11Results;
import demetra.x13.X13Finals;
import demetra.x13.X13Preadjustment;
import demetra.x11.MsrTable;
import demetra.x11.SeasonalFilterOption;
import java.util.function.IntToDoubleFunction;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.stats.AutoCovariances;
import jdplus.sa.tests.CombinedSeasonality;
import jdplus.x11.X11Utility;
import jdplus.x13.X13Results;

/**
 * This class computes the M-Statistics
 *
 *
 * @author Frank Osaer, Jean Palate
 */
public final class Mstatistics {

    /**
     *
     * @param preadj
     * @param x11
     * @param finals
     * @return
     */
    public static Mstatistics of(X13Preadjustment preadj, X11Results x11, X13Finals finals) {
        try {
            
            Mstatistics mstats = new Mstatistics(x11.getMode());
            TsData s = preadj.getA1();
            TsDomain dom=s.getDomain();
            mstats.O = s;
            mstats.Oc = TsData.fitToDomain(x11.getB1(), dom);
            mstats.Cc = TsData.fitToDomain(x11.getD12(), dom);
            mstats.CIc = TsData.fitToDomain(x11.getD11(), dom);
            mstats.Sc = TsData.fitToDomain(x11.getD10(), dom);
            mstats.Ic = TsData.fitToDomain(x11.getD13(), dom);
            if (mstats.mode != DecompositionMode.PseudoAdditive) {
                mstats.TD = finals.getD18();
                mstats.P = preadj.getA8();
                mstats.Pt = preadj.getA8t();
                mstats.Ps = preadj.getA8s();
                mstats.Pi = preadj.getA8i();
            }
            mstats.SI = TsData.fitToDomain(x11.getD8(), dom);

            mstats.Ome = finals.getE1();
            mstats.CImod = finals.getE2();
            mstats.Imod = finals.getE3();

            //mstats.Oc = mstats.op(mstats.O, mstats.P);
            mstats.Omod = mstats.op(mstats.Ome, mstats.Pt);
            mstats.Omod = mstats.op(mstats.Omod, mstats.Ps);
            mstats.CImod = mstats.op(mstats.CImod, mstats.Pt);
            mstats.checkSeries();

            if (mstats.TD != null) {
                double m=mstats.TD.getValue(0);
                if (mstats.TD.getValues().allMatch(x->m == x)){
                    mstats.TD = null;
                }
            }

            mstats.rms = x11.getD9Msr();
            if (mstats.rms != null) {
                mstats.m[5] = 0.4 * Math.abs(mstats.rms.getGlobalMsr() - 4.0);
            }
            SeasonalFilterOption d9filter = x11.getD9filter();
            boolean sdef = x11.isD9default();
            mstats.s3x5 = !sdef && d9filter == SeasonalFilterOption.S3X5; // are we really selecting s3x5

            mstats.icr = x11.getICRatio();
            if (mstats.icr != null) {
                mstats.m[2] = .5 * (mstats.icr - 1);
            }

            if (mstats.O.length() / mstats.O.getAnnualFrequency() < 6) { // short series (stable filter)
                mstats.bShort = true;
            }

            mstats.calcStationaryVariances();
            mstats.calcSNorm();
            mstats.calcEvolutions();
            mstats.calcM();
            // TODO: CH: Welches Table muss hier rein?
            //mstats.calcCochran(x11.getC13(), mstats.mode, mstats);

            return mstats;
        } catch (RuntimeException err) {
            return null;
        }
    }

    public void checkSeries() {
        if (mode == DecompositionMode.PseudoAdditive) {
            valid = new boolean[Oc.length()];
            for (int i = 0; i < valid.length; ++i) {
                valid[i] = Oc.getValue(i) > 0 && CIc.getValue(i) > 0;
            }
        }
    }

    public boolean[] validObservations() {
        return valid;
    }

    public TsData getO() {
        return O;
    }

    public TsData getP() {
        return P;
    }

    public TsData getOc() {
        return Oc;
    }

    public TsData getIc() {
        return Ic;
    }

    public TsData getCIc() {
        return CIc;
    }

    public TsData getCc() {
        return Cc;
    }

    public TsData getSc() {
        return Sc;
    }

    public TsData getMCD() {
        return MCD;
    }

    public TsData getstationaryC() {
        return this.stC;
    }

    public TsData getstationaryO() {
        return this.stO;
    }

    public double[] getOcChanges() {
        return gOc;
    }

    public double[] getOmodChanges() {
        return gOmod;
    }

    public double[] getIcChanges() {
        return gIc;
    }

    public double[] getImodChanges() {
        return gImod;
    }

    public double[] getCIcChanges() {
        return gCIc;
    }

    public double[] getCImodChanges() {
        return gCImod;
    }

    public double[] getCcChanges() {
        return gCc;
    }

    public double[] getScChanges() {
        return gSc;
    }

    public double[] getPChanges() {
        return gP;
    }

    public double[] getTDChanges() {
        return gTD;
    }

    public double[] getMCDChanges() {
        return gMCD;
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
        return varTD;
    }

    /**
     *
     * @return CriticalValue for Cochran Test
     */
    public double getCriticalValue() {
        return criticalvalue;
    }

    /**
     *
     * @return TestValue from Cochran Test
     */
    public double getTestValue() {
        return testvalue;

    }

    public boolean getCochranResult() {
        return cochranTestResult;
    }

    public int getminNumberOfYears() {
        return minNumberOfYears;
    }

    ;
    /**
     * Gets the average duration of run of CI
     *
     * @return
     */
    public double getAdrOfCI() {
        return X11Utility.adr(CIc.getValues(), mode.isMultiplicative());
    }

    /**
     * Gets the average duration of run of I
     *
     * @return
     */
    public double getAdrOfI() {
        return X11Utility.adr(Ic.getValues(), mode.isMultiplicative());
    }

    /**
     * Gets the average duration of run of C
     *
     * @return
     */
    public double getAdrOfC() {
        return X11Utility.adr(Cc.getValues(), mode.isMultiplicative());
    }

    /**
     * Computes the auto-correlations on the irregular (without outliers)
     *
     * @return
     */
    public double[] getAutoCorrelationsOfIrregular() {
        DoubleSeq irr = Ic.getValues();
        if (mode.isMultiplicative()) {
            irr = irr.fastOp(a -> a - 1);
        }
        IntToDoubleFunction acf = AutoCovariances.autoCorrelationFunction(irr, 0);
        int ifreq = Ic.getAnnualFrequency();
        double[] c = new double[ifreq + 2];
        for (int i = 0; i < c.length; ++i) {
            c[i] = acf.applyAsDouble(i + 1);
        }
        return c;
    }
    // O=A1, CI=D11, I=D13, C=D12, S=D10, P=A2, TD=D18, Omod=E1, CImod=E2, Imod=E3;
    private TsData O, P, TD, Ome;
    // Corrected series for prelimiary effects
    private TsData Oc, CIc, Cc, Sc, Ic, Omod, CImod, Imod;

    // others
    private TsData Snorm, Pt, Ps, Pi, SI, MCD;
    // Stationary
    private TsData stO, stC;
    private double varC, varS, varI, varP, varTD;
    private Double icr;
    // For F.2.A
    private double[] /*
             * gO, gCI, gI, gC, gS,
             */ gP, gTD, gOmod, gCImod, gImod, gMCD;
    private boolean[] valid;
    private double[] gOc, gCc, gIc, gCIc, gSc;
    private double[] m = new double[11];
    private boolean s3x5, bShort;
    private DecompositionMode mode;
    private static double[] wtFull = {10, 11, 10, 8, 11, 10, 18, 7, 7, 4, 4};
    private static double[] wtShort = {14, 15, 10, 8, 11, 10, 32};

    private MsrTable rms;

    //Variables for Calendarsigma sigmavec  testvalue criticalvalue
    private double testvalue = 0;
    private double criticalvalue = 0;
    private boolean cochranTestResult = true; //Default Value of Cochran Test
    private int minNumberOfYears = 0; //min Number of values per period

    private Mstatistics(DecompositionMode mode) {
        this.mode = mode;
        for (int i = 0; i < m.length; ++i) {
            m[i] = -1;
        }
    }

    private void calcEvolutions() {
        boolean mul = mode != DecompositionMode.Additive;
        int period = O.getAnnualFrequency();
//        gO = SeriesEvolution.calcAbsMeanVariations(O, null, mul);
//        gCI = SeriesEvolution.calcAbsMeanVariations(CI, null, mul);
//        gI = SeriesEvolution.calcAbsMeanVariations(I, null, mul);
//        gC = SeriesEvolution.calcAbsMeanVariations(C, null, mul);
//        gS = SeriesEvolution.calcAbsMeanVariations(S, null, mul);
        // main results
        gOmod = X11Utility.calcAbsMeanVariations(Omod.getValues(), period, mul, valid);
        gCImod = X11Utility.calcAbsMeanVariations(CImod.getValues(), period, mul, valid);
        gImod = X11Utility.calcAbsMeanVariations(Imod.getValues(), period, mul, valid);
        gOc = X11Utility.calcAbsMeanVariations(Oc.getValues(), period, mul, valid);
        gCIc = X11Utility.calcAbsMeanVariations(CIc.getValues(), period, mul, valid);
        gCc = X11Utility.calcAbsMeanVariations(Cc.getValues(), period, mul, valid);
        gSc = X11Utility.calcAbsMeanVariations(Sc.getValues(), period, mul, valid);
        gIc = X11Utility.calcAbsMeanVariations(Ic.getValues(), period, mul, valid);
        if (P != null) {
            gP = X11Utility.calcAbsMeanVariations(P.getValues(), period, mul, valid);
        }
        if (TD != null) {
            gTD = X11Utility.calcAbsMeanVariations(TD.getValues(), period, mul, valid);
        }
    }

    private void calcM() {
        int del = 1;
        int p = O.getAnnualFrequency();
        if (p == 12) {
            del = 3;
        }
        calcM1(del);
        calcM2();
        calcM4();
        calcM5();
        calcM6();
        calcM7();
        calcM8();
        calcM9();
        calcM10();
        calcM11();

        // compute summaries
    }

    private void calcM1(int del) {
        boolean mul = mode != DecompositionMode.Additive;
        double mt = X11Utility.calcAbsMeanVariation(Cc.getValues(), del, mul, valid);
        mt *= mt;
        double mi = X11Utility.calcAbsMeanVariation(Imod.getValues(), del, mul, valid);
        mi *= mi;
        double ms = X11Utility.calcAbsMeanVariation(Sc.getValues(), del, mul, valid);
        ms *= ms;
        double mp = 0;
        if (P != null) {
            mp = X11Utility.calcAbsMeanVariation(P.getValues(), del, mul, valid);
            mp *= mp;
        }

        double mtd = 0;
        if (TD != null) {
            mtd = X11Utility.calcAbsMeanVariation(TD.getValues(), del, mul, valid);
            mtd *= mtd;
        }

        double mo = mt + mi + ms + mp + mtd;

        m[0] = 10 * (mi / mo) / (1 - mp / mo);
    }

    /**
     * Compute the changes in the seasonal component during the last years
     * ([n-5, n-2[)
     */
    private void calcM10() {
        if (bShort) {
            return;
        }
        DoubleSeq s = Snorm.getValues();
        int period = Snorm.getAnnualFrequency();
        double ds = 0.0;
        int nn = period * 3;
        int n = s.length();
        for (int j = 1; j <= period; ++j) {
            int last = n - j - period;
            int first = last - 3 * period;
            if (first < period) {
                return;
            }
            for (int i = first; i < last; i += period) {
                ds += Math.abs(s.get(i) - s.get(i - period));
            }
        }
        m[9] = (ds / nn) * 10.0;
    }

    private void calcM11() {
        if (bShort) {
            return;
        }
        DoubleSeq s = Snorm.getValues();
        int period = Snorm.getAnnualFrequency();
        double ds = 0.0;
        int nn = period * 3;
        int n = s.length();
        for (int j = 1; j <= period; ++j) {
            int last = n - j - 2 * period;
            int first = last - 3 * period;
            if (first < 0) {
                return;
            }
            ds += Math.abs(s.get(last) - s.get(first));
        }
        m[10] = (ds / nn) * 10.0;
    }

    private void calcM2() {
        // The original code is:
        // m[1] = 10 * varI / Math.abs(1 - varP);
        // In the case of very large varP (>=1), the result could be strange.
        // Recall: P contains the preliminary effects, excepted the calendar effects (outliers)
        m[1] = varP >= 1 ? 3 : 10 * varI / (1 - varP);
    }

    private void calcM4() {
        double adr = X11Utility.adr(Ic.getValues(), mode != DecompositionMode.Additive);
        int n = Ic.length();
        double lv = Math.sqrt(1.6 * n - 2.9) * 2.577;
        double uv = Math.abs(3 * (n - 1) / adr - (2 * n - 1));
        m[3] = uv / lv;
    }

    private void calcM5() {
        int ifreq = O.getAnnualFrequency();
        int c = 12 / ifreq;
        // table F2E
        int mcd = ifreq;
        while (mcd > 0 && smic(mcd) < 1) {
            --mcd;
        }
        if (mcd < ifreq) {
            mcd++;
        }
        double rmcd;
        if (mcd == 1) {
            rmcd = 1 + (smic(1) - 1) / (smic(1) - smic(2));
            if (rmcd < .5) {
                rmcd = .5;
            }
            if (rmcd > 1) {
                rmcd = 1;
            }
        } else {
            double dsmic = smic(mcd - 1) - smic(mcd);
            if (dsmic <= 0 && mcd == ifreq) {
                rmcd = c * 15.5;
            } else {
                rmcd = mcd + (smic(mcd) - 1) / dsmic;
            }
        }
        m[4] = (rmcd * c - 0.5) / 5.0;
    }

    private void calcM6() {
    }

    private void calcM7() {
        CombinedSeasonality test;
        int period =O.getAnnualFrequency();
        int startPeriod=O.getStart().annualPosition();
        switch (mode) {
            case LogAdditive:
                test = new CombinedSeasonality(SI.getValues().log(), period, startPeriod, 0);
                break;
            case Additive:
                test = new CombinedSeasonality(SI.getValues(), period, startPeriod, 0);
                break;
            default:
                test = new CombinedSeasonality(SI.getValues(), period, startPeriod, 1);
        }
        m[6] = test.mvalue();
    }

    private void calcM8() {
        if (bShort) {
            return;
        }
        int ifreq = O.getAnnualFrequency();
        TsData s = Snorm.delta(ifreq);
        m[7] = 10 * s.getValues().fastOp(z -> Math.abs(z)).average();
    }

    private void calcM9() {
        if (bShort) {
            return;
        }
        DoubleSeq s = Snorm.getValues();
        int period = Snorm.getAnnualFrequency();
        double ds = 0.0;
        int n = s.length();
        for (int j = 1; j <= period; ++j) {
            int last = n - j;
            int first = last%period;
            int m=(last-first)/period;
            ds += (Math.abs(s.get(last) - s.get(first)))/m;
        }
        m[8] = ds / period * 10.0;
    }

    private void calcSNorm() {
        double stde = 0;
        if (mode != DecompositionMode.Additive) {
            stde = Math.sqrt(Sc.getValues().fastOp(z -> z - 1).ssq() / Sc.length());
        } else {
            stde = Math.sqrt(Sc.getValues().ssq() / Sc.length());
        }
        Snorm = TsData.of(Sc.getStart(), Sc.getValues().times(1 / stde));
    }

    private double variance(TsData s, boolean log, boolean zero) {
        if (log) {
            s = s.log();
        }
        int n = s.length(), m = 0;
        double z = 0;
        double mu = 0;
        if (!zero) {
            for (int i = 0; i < n; ++i) {
                double x = s.getValue(i);
                if ((valid == null || valid[i]) && Double.isFinite(x)) {
                    ++m;
                    z += x;
                }
            }
            mu = z / m;
        }
        z = 0;
        for (int i = 0; i < n; ++i) {
            double x = s.getValue(i);
            if ((valid == null || valid[i]) && Double.isFinite(x)) {
                x -= mu;
                z += x * x;
            }
        }
        return z;
    }

    private void calcStationaryVariances() {
//        stC = Cc;
//        stO = Ome;
        DoubleSeq stc, sto;

        int n = Cc.length();
        if (mode != DecompositionMode.Additive) {
            LinearModel lm = LinearModel.builder()
                    .y(Cc.getValues().log())
                    .meanCorrection(true)
                    .addX(DoubleSeq.onMapping(n, i -> i))
                    .build();
            LeastSquaresResults lsr = Ols.compute(lm);
            DoubleSeq lt = lsr.regressionEffect();
            stc = DoublesMath.subtract(lm.getY(), lt);
            sto = Ome.getValues().fn(lt, (a, b) -> Math.log(a) - b);
        } else {
            LinearModel lm = LinearModel.builder()
                    .y(Cc.getValues())
                    .meanCorrection(true)
                    .addX(DoubleSeq.onMapping(n, i -> i))
                    .build();
            LeastSquaresResults lsr = Ols.compute(lm);
            DoubleSeq lt = lsr.regressionEffect();
            stc = DoublesMath.subtract(lm.getY(), lt);
            sto = DoublesMath.subtract(Ome.getValues(), lt);
        }
        stC = TsData.of(Cc.getStart(), stc);
        stO = TsData.of(Ome.getStart(), sto);

        double varO = variance(stO, false, false);
        varC = variance(stC, false, false);
        varS = variance(Sc, mode != DecompositionMode.Additive, true);
        varI = variance(Imod, mode != DecompositionMode.Additive, true);
        if (P != null) {
            varP = variance(P, mode != DecompositionMode.Additive, false);
        }
        if (TD != null) {
            varTD = variance(TD, mode != DecompositionMode.Additive, true);
        }

        varP /= varO;
        varTD /= varO;
        varS /= varO;
        varC /= varO;
        varI /= varO;
    }

//    private void calcCochran(DoubleSeq s) {
//        Cochran cochranTest = new Cochran(s, mode.isMultiplicative());
//        cochranTest.calcCochranTest();
//        criticalvalue = cochranTest.getCriticalValue();
//        testvalue = cochranTest.getTestValue();
//        cochranTestResult = cochranTest.getTestResult();
//        minNumberOfYears = cochranTest.getMinNumberOfYearsPerPeriod();
//    }
    public DecompositionMode getMode() {
        return this.mode;
    }

    /**
     *
     * @param q
     * @return
     */
    public double getM(int q) {
        double x = m[q - 1];
        if (x > 3) {
            return 3;
        } else if (x < 0) {
            return 0;
        } else {
            return x;
        }
    }

    public int getMCount() {
        return m.length;
    }

    /**
     *
     * @return
     */
    public double getQ() {
        double q = 0, wtot = 0;
        double[] wt = bShort ? wtShort : wtFull;
        for (int i = 0; i < wt.length; ++i) {
            if (m[i] != -1 && (i != 5 || s3x5)) {
                wtot += wt[i];
                if (m[i] > 3) {
                    q += 3 * wt[i];
                } else if (m[i] > 0) {
                    q += m[i] * wt[i];
                }
            }
        }
        return q / wtot;
    }

    /**
     *
     * @return
     */
    public double getQm2() {
        double q = 0, wtot = 0;
        double[] wt = bShort ? wtShort : wtFull;
        for (int i = 0; i < wt.length; ++i) {
            if (i != 1 && m[i] != -1 && (i != 5 || s3x5)) {
                wtot += wt[i];
                if (m[i] > 3) {
                    q += 3 * wt[i];
                } else if (m[i] > 0) {
                    q += m[i] * wt[i];
                }
            }
        }
        return q / wtot;
    }

    /**
     *
     * @return
     */
    public Double getIcr() {
        return icr;
    }

    /**
     *
     * @return
     */
    public MsrTable getRms() {
        return rms;
    }

    /**
     *
     * @return
     */
    public double getVarTotal() {
        return varC + varS + varI + varP + varTD;
    }

    /**
     *
     * @param q
     * @return
     */
    public boolean isUsedM(int q) {
        return m[q - 1] != -1;
    }

    private TsData op(TsData l, TsData r) {
        if (mode != DecompositionMode.Additive) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    private double smic(int m) {
        int i = m - 1;
        return gIc[i] / gCc[i];
    }

}
