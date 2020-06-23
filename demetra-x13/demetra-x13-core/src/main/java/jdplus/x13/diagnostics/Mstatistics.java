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
package jdplus.x13.diagnostics;

import demetra.data.DoubleSeq;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.x11.X11Results;
import demetra.x13.X13Finals;
import demetra.x13.X13Preadjustment;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import jdplus.stats.DescriptiveStatistics;
import jdplus.x11.SeriesEvolution;
import jdplus.x11.extremevaluecorrector.Cochran;
import jdplus.x13.X13Results;

/**
 * This class computes the M-Statistics
 *
 *
 * @author Frank Osaer, Jean Palate
 */
//public final class Mstatistics {
//
//    /**
//     *
//     * @param info
//     * @return
//     */
//    public static Mstatistics of(X13Results rslts) {
//        try {
//            X11Results x11 = rslts.getDecomposition();
//            X13Preadjustment preadj = rslts.getPreadjustment();
//            X13Finals finals = rslts.getFinals();
//            Mstatistics mstats = new Mstatistics(x11.getMode());
//            TsData s = preadj.getA1();
//
//            mstats.O = s;
//            mstats.Oc = TsData.fitToDomain(x11.getB1(), s.getDomain());
//            mstats.Cc = x11.getD12();
//            mstats.CIc = x11.getD11();
//            mstats.Sc = x11.getD10();
//            mstats.Ic = x11.getD13();
//            if (mstats.mode != DecompositionMode.PseudoAdditive) {
//                mstats.TD = finals.getD18();
//                mstats.P = preadj.getA8();
//                mstats.Pt =preadj.getA8t();
//                mstats.Ps = preadj.getA8s();
//                mstats.Pi = preadj.getA8i();
//            }
//            mstats.SI = x11.getD8();
//
//            mstats.Ome = finals.getE1(); 
//            mstats.CImod =finals.getE2(); 
//            mstats.Imod = finals.getE3();
//
//            //mstats.Oc = mstats.op(mstats.O, mstats.P);
//            mstats.Omod = mstats.op(mstats.Ome, mstats.Pt);
//            mstats.Omod = mstats.op(mstats.Omod, mstats.Ps);
//            mstats.CImod = mstats.op(mstats.CImod, mstats.Pt);
//            mstats.checkSeries();
//
//            if (mstats.TD != null) {
//                DescriptiveStatistics td = DescriptiveStatistics.of(mstats.TD.getValues());
//                if (td.isConstant()) {
//                    mstats.TD = null;
//                }
//            }
//
//            mstats.rms = x11.getD9msr();
//            if (mstats.rms != null) {
//                mstats.m[5] = 0.4 * Math.abs(mstats.rms.getGlobalMsr() - 4.0);
//            }
//            Integer slen = dtables.get(X11Kernel.D9_SLEN, Integer.class);
//            Boolean sdef = dtables.get(X11Kernel.D9_DEFAULT, Boolean.class);
//            if (slen != null) {
//                mstats.s3x5 = slen == 7;
//            }
//            if (sdef != null && sdef) {
//                mstats.s3x5 = false;
//            }
//
//            mstats.icr = dtables.get(X11Kernel.D12_IC, Double.class);
//            if (mstats.icr != null) {
//                mstats.m[2] = .5 * (mstats.icr - 1);
//            }
//
//            if (mstats.O.getLength() / mstats.O.getFrequency().intValue() < 6) // stable
//            // !!!
//            {
//                mstats.bShort = true;
//            }
//
//            mstats.calcStationaryVariances();
//            mstats.calcSNorm();
//            mstats.calcEvolutions();
//            mstats.calcM();
//            // TODO: CH: Welches Table muss hier rein?
//            mstats.calcCochran(x11.getC13(), mstats.mode, mstats);
//
//            return mstats;
//        } catch (RuntimeException err) {
//            return null;
//        }
//    }
//
//    public void checkSeries() {
//        if (mode == DecompositionMode.PseudoAdditive) {
//            valid = new boolean[Oc.length()];
//            for (int i = 0; i < valid.length; ++i) {
//                valid[i] = Oc.getValue(i) > 0 && CIc.getValue(i) > 0;
//            }
//        }
//    }
//
//    public boolean[] validObservations() {
//        return valid;
//    }
//
//    public TsData getO() {
//        return O;
//    }
//
//    public TsData getP() {
//        return P;
//    }
//
//    public TsData getOc() {
//        return Oc;
//    }
//
//    public TsData getIc() {
//        return Ic;
//    }
//
//    public TsData getCIc() {
//        return CIc;
//    }
//
//    public TsData getCc() {
//        return Cc;
//    }
//
//    public TsData getSc() {
//        return Sc;
//    }
//
//    public TsData getMCD() {
//        return MCD;
//    }
//
//    public TsData getstationaryC() {
//        return this.stC;
//    }
//
//    public TsData getstationaryO() {
//        return this.stO;
//    }
//
//    public double[] getOcChanges() {
//        return gOc;
//    }
//
//    public double[] getOmodChanges() {
//        return gOmod;
//    }
//
//    public double[] getIcChanges() {
//        return gIc;
//    }
//
//    public double[] getImodChanges() {
//        return gImod;
//    }
//
//    public double[] getCIcChanges() {
//        return gCIc;
//    }
//
//    public double[] getCImodChanges() {
//        return gCImod;
//    }
//
//    public double[] getCcChanges() {
//        return gCc;
//    }
//
//    public double[] getScChanges() {
//        return gSc;
//    }
//
//    public double[] getPChanges() {
//        return gP;
//    }
//
//    public double[] getTDChanges() {
//        return gTD;
//    }
//
//    public double[] getMCDChanges() {
//        return gMCD;
//    }
//
//    public double getVarI() {
//        return varI;
//    }
//
//    public double getVarS() {
//        return varS;
//    }
//
//    public double getVarC() {
//        return varC;
//    }
//
//    public double getVarP() {
//        return varP;
//    }
//
//    public double getVarTD() {
//        return varTD;
//    }
//
//    /**
//     *
//     * @return CriticalValue for Cochran Test
//     */
//    public double getCriticalValue() {
//        return criticalvalue;
//    }
//
//    /**
//     *
//     * @return TestValue from Cochran Test
//     */
//    public double getTestValue() {
//        return testvalue;
//
//    }
//
//    public boolean getCochranResult() {
//        return cochranTestResult;
//    }
//
//    public int getminNumberOfYears() {
//        return minNumberOfYears;
//    }
//
//    ;
//    /**
//     * Gets the average duration of run of CI
//     *
//     * @return
//     */
//    public double getAdrOfCI() {
//        return SeriesEvolution.Adr(CIc, mode.isMultiplicative());
//    }
//
//    /**
//     * Gets the average duration of run of I
//     *
//     * @return
//     */
//    public double getAdrOfI() {
//        return SeriesEvolution.adr(Ic.getValues(), mode.isMultiplicative());
//    }
//
//    /**
//     * Gets the average duration of run of C
//     *
//     * @return
//     */
//    public double getAdrOfC() {
//        return SeriesEvolution.adr(Cc.getValues(), mode.isMultiplicative());
//    }
//
//    /**
//     * Computes the auto-correlations on the irregular (without outliers)
//     *
//     * @return
//     */
//    public double[] getAutoCorrelationsOfIrregular() {
//        TsData irr = Ic;
//        if (mode.isMultiplicative()) {
//            irr = irr.minus(1);
//        }
//        AutoCorrelations ac = new AutoCorrelations(irr);
//        ac.setCorrectedForMean(false);
//        int ifreq = irr.getFrequency().intValue();
//        double[] c = new double[ifreq + 2];
//        for (int i = 0; i < c.length; ++i) {
//            c[i] = ac.autoCorrelation(i + 1);
//        }
//        return c;
//    }
//    // O=A1, CI=D11, I=D13, C=D12, S=D10, P=A2, TD=D18, Omod=E1, CImod=E2, Imod=E3;
//    private TsData O, P, TD, Ome;
//    // Corrected series for prelimiary effects
//    private TsData Oc, CIc, Cc, Sc, Ic, Omod, CImod, Imod;
//
//    // others
//    private TsData Snorm, Pt, Ps, Pi, SI, MCD;
//    // Stationary
//    private TsData stO, stC;
//    private double varC, varS, varI, varP, varTD;
//    private Double icr;
//    // For F.2.A
//    private double[] /*
//             * gO, gCI, gI, gC, gS,
//             */ gP, gTD, gOmod, gCImod, gImod, gMCD;
//    private boolean[] valid;
//    private double[] gOc, gCc, gIc, gCIc, gSc;
//    private double[] m = new double[11];
//    private boolean s3x5, bShort;
//    private DecompositionMode mode;
//    private static double[] wtFull = {10, 11, 10, 8, 11, 10, 18, 7, 7, 4, 4};
//    private static double[] wtShort = {14, 15, 10, 8, 11, 10, 32};
//
//    private MsrTable rms;
//
//    //Variables for Calendarsigma sigmavec  testvalue criticalvalue
//    private double testvalue = 0;
//    private double criticalvalue = 0;
//    private boolean cochranTestResult = true; //Default Value of Cochran Test
//    private int minNumberOfYears = 0; //min Number of values per period
//
//    private Mstatistics(DecompositionMode mode) {
//        this.mode = mode;
//        for (int i = 0; i < m.length; ++i) {
//            m[i] = -1;
//        }
//    }
//
//    private void calcEvolutions() {
//        boolean mul = mode != DecompositionMode.Additive;
////        gO = SeriesEvolution.calcAbsMeanVariations(O, null, mul);
////        gCI = SeriesEvolution.calcAbsMeanVariations(CI, null, mul);
////        gI = SeriesEvolution.calcAbsMeanVariations(I, null, mul);
////        gC = SeriesEvolution.calcAbsMeanVariations(C, null, mul);
////        gS = SeriesEvolution.calcAbsMeanVariations(S, null, mul);
//        // main results
//        gOmod = SeriesEvolution.calcAbsMeanVariations(Omod, null, mul, valid);
//        gCImod = SeriesEvolution.calcAbsMeanVariations(CImod, null, mul, valid);
//        gImod = SeriesEvolution.calcAbsMeanVariations(Imod, null, mul, valid);
//        gOc = SeriesEvolution.calcAbsMeanVariations(Oc, null, mul, valid);
//        gCIc = SeriesEvolution.calcAbsMeanVariations(CIc, null, mul, valid);
//        gCc = SeriesEvolution.calcAbsMeanVariations(Cc, null, mul, valid);
//        gSc = SeriesEvolution.calcAbsMeanVariations(Sc, null, mul, valid);
//        gIc = SeriesEvolution.calcAbsMeanVariations(Ic, null, mul, valid);
//        if (P != null) {
//            gP = SeriesEvolution.calcAbsMeanVariations(P, null, mul, valid);
//        }
//        if (TD != null) {
//            gTD = SeriesEvolution.calcAbsMeanVariations(TD, null, mul, valid);
//        }
//    }
//
//    private void calcM() {
//        int del = 1;
//        int p = O.getFrequency().intValue();
//        if (p == 12) {
//            del = 3;
//        }
//        calcM1(del);
//        calcM2();
//        calcM4();
//        calcM5();
//        calcM6();
//        calcM7();
//        calcM8();
//        calcM9();
//        calcM10();
//        calcM11();
//
//        // compute summaries
//    }
//
//    private void calcM1(int del) {
//        boolean mul = mode != DecompositionMode.Additive;
//        double mt = SeriesEvolution.calcAbsMeanVariations(Cc, null, del, mul, valid);
//        mt *= mt;
//        double mi = SeriesEvolution.calcAbsMeanVariations(Imod, null, del, mul, valid);
//        mi *= mi;
//        double ms = SeriesEvolution.calcAbsMeanVariations(Sc, null, del, mul, valid);
//        ms *= ms;
//        double mp = 0;
//        if (P != null) {
//            mp = SeriesEvolution.calcAbsMeanVariations(P, null, del, mul, valid);
//            mp *= mp;
//        }
//
//        double mtd = 0;
//        if (TD != null) {
//            mtd = SeriesEvolution.calcAbsMeanVariations(TD, null, del, mul, valid);
//            mtd *= mtd;
//        }
//
//        double mo = mt + mi + ms + mp + mtd;
//
//        m[0] = 10 * (mi / mo) / (1 - mp / mo);
//    }
//
//    private void calcM10() {
//        if (bShort) {
//            return;
//        }
//        PeriodIterator iter = new PeriodIterator(Snorm);
//        int ifreq = Snorm.getFrequency().intValue();
//        double ds = 0.0;
//        int nn = ifreq * 3;
//        while (iter.hasMoreElements()) {
//            DataBlock db = iter.nextElement().data;
//            int n = db.getLength();
//            if (n < 5) {
//                return;
//            }
//            for (int i = n - 5; i < n - 2; i++) {
//                ds += Math.abs(db.get(i) - db.get(i - 1));
//            }
//        }
//
//        m[9] = (ds / nn) * 10.0;
//    }
//
//    private void calcM11() {
//        if (bShort) {
//            return;
//        }
//        int ifreq = Snorm.getFrequency().intValue();
//        PeriodIterator iter = new PeriodIterator(Snorm);
//        double ds = 0.0;
//        int nn = ifreq * 3;
//        while (iter.hasMoreElements()) {
//            DataBlock db = iter.nextElement().data;
//            int n = db.getLength();
//            if (n < 6) {
//                return;
//            }
//            ds += Math.abs(db.get(n - 3) - db.get(n - 6));
//        }
//
//        m[10] = (ds / nn) * 10.0;
//    }
//
//    private void calcM2() {
//        // The original code is:
//        // m[1] = 10 * varI / Math.abs(1 - varP);
//        // In the case of very large varP (>=1), the result could be strange.
//        // Recall: P contains the preliminary effects, excepted the calendar effects (outliers)
//        m[1] = varP >= 1 ? 3 : 10 * varI / (1 - varP);
//    }
//
//    private void calcM4() {
//        double adr = SeriesEvolution.Adr(Ic, mode != DecompositionMode.Additive);
//        int n = Ic.getLength();
//        double lv = Math.sqrt(1.6 * n - 2.9) * 2.577;
//        double uv = Math.abs(3 * (n - 1) / adr - (2 * n - 1));
//        m[3] = uv / lv;
//    }
//
//    private void calcM5() {
//        int ifreq = O.getFrequency().intValue();
//        int c = 12 / ifreq;
//        // table F2E
//        int mcd = ifreq;
//        while (mcd > 0 && smic(mcd) < 1) {
//            --mcd;
//        }
//        if (mcd < ifreq) {
//            mcd++;
//        }
//        double rmcd;
//        if (mcd == 1) {
//            rmcd = 1 + (smic(1) - 1) / (smic(1) - smic(2));
//            if (rmcd < .5) {
//                rmcd = .5;
//            }
//            if (rmcd > 1) {
//                rmcd = 1;
//            }
//        } else {
//            double dsmic = smic(mcd - 1) - smic(mcd);
//            if (dsmic <= 0 && mcd == ifreq) {
//                rmcd = c * 15.5;
//            } else {
//                rmcd = mcd + (smic(mcd) - 1) / dsmic;
//            }
//        }
//        m[4] = (rmcd * c - 0.5) / 5.0;
//    }
//
//    private void calcM6() {
//    }
//
//    private void calcM7() {
//        CombinedSeasonalityTest test;
//        switch (mode) {
//            case LogAdditive:
//                test = new CombinedSeasonalityTest(SI.log(), false);
//                break;
//            case Additive:
//                test = new CombinedSeasonalityTest(SI, false);
//                break;
//            default:
//                test = new CombinedSeasonalityTest(SI, true);
//        }
//        m[6] = test.mvalue();
//    }
//
//    private void calcM8() {
//        if (bShort) {
//            return;
//        }
//        int ifreq = O.getFrequency().intValue();
//        TsData s = Snorm.delta(ifreq);
//        s.applyOnFinite(x -> Math.abs(x));
//        DescriptiveStatistics stats = new DescriptiveStatistics(s);
//        m[7] = 10 * stats.getSum() / s.getLength();
//    }
//
//    private void calcM9() {
//        if (bShort) {
//            return;
//        }
//        int ifreq = Snorm.getFrequency().intValue();
//        PeriodIterator iter = new PeriodIterator(Snorm);
//        double ds = 0.0;
//        while (iter.hasMoreElements()) {
//            DataBlock db = iter.nextElement().data;
//            int n = db.getLength() - 1;
//            ds += Math.abs(db.get(n) - db.get(0)) / n;
//        }
//
//        m[8] = ds / ifreq * 10.0;
//    }
//
//    private void calcSNorm() {
//        double stde = 0;
//        if (mode != DecompositionMode.Additive) {
//            DescriptiveStatistics stat = new DescriptiveStatistics(Sc.minus(1));
//            stde = Math.sqrt(stat.getSumSquare() / stat.getObservationsCount());
//        } else {
//            DescriptiveStatistics stat = new DescriptiveStatistics(Sc);
//            stde = Math.sqrt(stat.getSumSquare() / stat.getObservationsCount());
//        }
//        Snorm = Sc.div(stde);
//    }
//
//    private double variance(TsData s, boolean log, boolean zero) {
//        if (log) {
//            s = s.log();
//        }
//        int n = s.getLength(), m = 0;
//        double z = 0;
//        double mu = 0;
//        if (!zero) {
//            for (int i = 0; i < n; ++i) {
//                double x = s.get(i);
//                if ((valid == null || valid[i]) && Double.isFinite(x)) {
//                    ++m;
//                    z += x;
//                }
//            }
//            mu = z / m;
//        }
//        z = 0;
//        for (int i = 0; i < n; ++i) {
//            double x = s.get(i);
//            if ((valid == null || valid[i]) && Double.isFinite(x)) {
//                x -= mu;
//                z += x * x;
//            }
//        }
//        return z;
//    }
//
//    private void calcStationaryVariances() {
//        stC = Cc.clone();
//        stO = Ome.clone();
//
//        double[] line = new double[Cc.getLength()];
//        for (int i = 0; i < line.length; ++i) {
//            line[i] = i;
//        }
//        if (mode != DecompositionMode.Additive) {
//            Ols ols = new Ols();
//            RegModel model = new RegModel();
//            //
//            stC.applyOnFinite(x -> Math.log(x));
//            model.setY(new DataBlock(stC.internalStorage()));
//            model.setMeanCorrection(true);
//            model.addX(new DataBlock(line));
//            if (!ols.process(model)) {
//                return;
//            }
//
//            double[] b = ols.getLikelihood().getB();
//            TsData lt = new TsData(stC.getStart(), line, false);
//            lt.applyOnFinite(x -> x * b[1] + b[0]);
//
//            stC = stC.minus(lt);
//            stO.applyOnFinite(x -> Math.log(x));
//            stO = stO.minus(lt);
//
//        } else {
//            Ols ols = new Ols();
//            RegModel model = new RegModel();
//
//            //
//            model.setY(new DataBlock(stC.internalStorage()));
//            model.setMeanCorrection(true);
//            model.addX(new DataBlock(line));
//            if (!ols.process(model)) {
//                return;
//            }
//
//            double[] b = ols.getLikelihood().getB();
//            TsData lt = new TsData(stC.getStart(), line, false);
//            lt.applyOnFinite(x -> x * b[1] + b[0]);
//
//            stC = stC.minus(lt);
//            stO = stO.minus(lt);
//        }
//
//        double varO = variance(stO, false, false);
//        varC = variance(stC, false, false);
//        varS = variance(Sc, mode != DecompositionMode.Additive, true);
//        varI = variance(Imod, mode != DecompositionMode.Additive, true);
//        if (P != null) {
//            varP = variance(P, mode != DecompositionMode.Additive, false);
//        }
//        if (TD != null) {
//            varTD = variance(TD, mode != DecompositionMode.Additive, true);
//        }
//
//        varP /= varO;
//        varTD /= varO;
//        varS /= varO;
//        varC /= varO;
//        varI /= varO;
//    }
//
//    private void calcCochran(DoubleSeq s) {
//        Cochran cochranTest = new Cochran(s, mode.isMultiplicative());
//        cochranTest.calcCochranTest();
//        criticalvalue = cochranTest.getCriticalValue();
//        testvalue = cochranTest.getTestValue();
//        cochranTestResult = cochranTest.getTestResult();
//        minNumberOfYears = cochranTest.getMinNumberOfYearsPerPeriod();
//
//    }
//
//    public DecompositionMode getMode() {
//        return this.mode;
//    }
//
//    /**
//     *
//     * @param q
//     * @return
//     */
//    public double getM(int q) {
//        double x = m[q - 1];
//        if (x > 3) {
//            return 3;
//        } else if (x < 0) {
//            return 0;
//        } else {
//            return x;
//        }
//    }
//
//    public int getMCount() {
//        return m.length;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public double getQ() {
//        double q = 0, wtot = 0;
//        double[] wt = bShort ? wtShort : wtFull;
//        for (int i = 0; i < wt.length; ++i) {
//            if (m[i] != -1 && (i != 5 || s3x5)) {
//                wtot += wt[i];
//                if (m[i] > 3) {
//                    q += 3 * wt[i];
//                } else if (m[i] > 0) {
//                    q += m[i] * wt[i];
//                }
//            }
//        }
//        return q / wtot;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public double getQm2() {
//        double q = 0, wtot = 0;
//        double[] wt = bShort ? wtShort : wtFull;
//        for (int i = 0; i < wt.length; ++i) {
//            if (i != 1 && m[i] != -1 && (i != 5 || s3x5)) {
//                wtot += wt[i];
//                if (m[i] > 3) {
//                    q += 3 * wt[i];
//                } else if (m[i] > 0) {
//                    q += m[i] * wt[i];
//                }
//            }
//        }
//        return q / wtot;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public Double getIcr() {
//        return icr;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public MsrTable getRms() {
//        return rms;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public double getVarTotal() {
//        return varC + varS + varI + varP + varTD;
//    }
//
//    /**
//     *
//     * @param q
//     * @return
//     */
//    public boolean isUsedM(int q) {
//        return m[q - 1] != -1;
//    }
//
//    private TsData op(TsData l, TsData r) {
//        if (mode != DecompositionMode.Additive) {
//            return TsData.divide(l, r);
//        } else {
//            return TsData.subtract(l, r);
//        }
//    }
//
//    private double smic(int m) {
//        int i = m - 1;
//        return gIc[i] / gCc[i];
//    }
//
//    @Override
//    public boolean contains(String id) {
//        return MAPPING.contains(id);
//    }
//
//    @Override
//    public Map<String, Class> getDictionary() {
//        LinkedHashMap<String, Class> dictionary = new LinkedHashMap<>();
//        MAPPING.fillDictionary(null, dictionary, false);
//        return dictionary;
//    }
//
//    @Override
//    public <T> T getData(String id, Class<T> tclass) {
//        if (MAPPING.contains(id)) {
//            return MAPPING.getData(this, id, tclass);
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    public List<ProcessingInformation> getProcessingInformation() {
//        return Collections.emptyList();
//    }
//
//    public static final String M1 = "m1", M2 = "m2", M3 = "m3", M4 = "m4";
//    public static final String M5 = "m5", M6 = "m6", M7 = "m7", M8 = "m8";
//    public static final String M9 = "m9", M10 = "m10", M11 = "m11";
//    public static final String Q = "q", Q2 = "q-m2";
//
//    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
//        MAPPING.fillDictionary(prefix, map, compact);
//    }
//
//    // MAPPING
//    public static InformationMapping<Mstatistics> getMapping() {
//        return MAPPING;
//    }
//
//    public static <T> void setMapping(String name, Class<T> tclass, Function<Mstatistics, T> extractor) {
//        MAPPING.set(name, tclass, extractor);
//    }
//
//    public static <T> void setTsData(String name, Function<Mstatistics, TsData> extractor) {
//        MAPPING.set(name, extractor);
//    }
//
//    private static final InformationMapping<Mstatistics> MAPPING = new InformationMapping<>(Mstatistics.class
//    );
//
//    static {
//        MAPPING.set(M1, Double.class,
//                 source -> source.getM(1));
//        MAPPING.set(M2, Double.class,
//                 source -> source.getM(2));
//        MAPPING.set(M3, Double.class,
//                 source -> source.getM(3));
//        MAPPING.set(M4, Double.class,
//                 source -> source.getM(4));
//        MAPPING.set(M5, Double.class,
//                 source -> source.getM(5));
//        MAPPING.set(M6, Double.class,
//                 source -> source.getM(6));
//        MAPPING.set(M7, Double.class,
//                 source -> source.getM(7));
//        MAPPING.set(M8, Double.class,
//                 source -> source.getM(8));
//        MAPPING.set(M9, Double.class,
//                 source -> source.getM(9));
//        MAPPING.set(M10, Double.class,
//                 source -> source.getM(10));
//        MAPPING.set(M11, Double.class,
//                 source -> source.getM(11));
//        MAPPING.set(Q, Double.class,
//                 source -> source.getQ());
//        MAPPING.set(Q2, Double.class,
//                 source -> source.getQm2());
//    }
//}
