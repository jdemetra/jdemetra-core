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
package ec.satoolkit.x11;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.stats.AutoCorrelations;
import ec.tstoolkit.stats.CochranTest;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class computes the M-Statistics
 *
 *
 * @author Frank Osaer, Jean Palate
 */
public final class Mstatistics implements IProcResults {

    /**
     *
     * @param mode
     * @param info
     * @return
     */
    public static Mstatistics computeFromX11(DecompositionMode mode,
            InformationSet info) {
        // get D-Tables...
        InformationSet dtables = info.getSubSet(X11Kernel.D);
        InformationSet atables = info.getSubSet(X11Kernel.A);
        InformationSet etables = info.getSubSet(X11Kernel.E);
        InformationSet ctables = info.getSubSet(X11Kernel.C);
        if (dtables == null) {
            return null;
        }
        try {
            Mstatistics mstats = new Mstatistics(mode);
            mstats.O = etables.get(X11Kernel.E1, TsData.class);

            mstats.Oc = dtables.get(X11Kernel.D1, TsData.class);
            mstats.Cc = dtables.get(X11Kernel.D12, TsData.class);
            mstats.CIc = dtables.get(X11Kernel.D11, TsData.class);
            mstats.Sc = dtables.get(X11Kernel.D10, TsData.class);
            mstats.Ic = dtables.get(X11Kernel.D13, TsData.class);
            mstats.TD = dtables.get(X11Kernel.D18, TsData.class);
            mstats.P = atables.get(X11Kernel.A8, TsData.class);
            mstats.Pt = atables.get(X11Kernel.A8t, TsData.class);
            mstats.Ps = atables.get(X11Kernel.A8s, TsData.class);
            mstats.Pi = atables.get(X11Kernel.A8i, TsData.class);
            mstats.SI = dtables.get(X11Kernel.D8, TsData.class);

            mstats.Omod = etables.get(X11Kernel.E1, TsData.class);
            mstats.CImod = etables.get(X11Kernel.E2, TsData.class);
            mstats.Imod = etables.get(X11Kernel.E3, TsData.class);

            //mstats.Oc = mstats.op(mstats.O, mstats.P);
            mstats.Cc = mstats.op(mstats.Cc, mstats.Pt);
            mstats.Sc = mstats.op(mstats.Sc, mstats.Ps);
            mstats.Ic = mstats.op(mstats.Ic, mstats.Pi);
            mstats.CIc = mstats.op(mstats.CIc, mstats.Pt);
            mstats.CIc = mstats.op(mstats.CIc, mstats.Pi);
            //
            mstats.Omod = mstats.op(mstats.Omod, mstats.Pt);
            mstats.Omod = mstats.op(mstats.Omod, mstats.Ps);
            mstats.CImod = mstats.op(mstats.CImod, mstats.Pt);

            if (mstats.TD != null) {
                DescriptiveStatistics td = new DescriptiveStatistics(mstats.TD);
                if (td.isConstant()) {
                    mstats.TD = null;
                }
            }

            MsrTable rms = dtables.get(X11Kernel.D9_RMS, MsrTable.class);
            if (rms != null) {
                mstats.m[5] = 0.4 * Math.abs(rms.getGlobalMsr() - 4.0);
            }
            Integer slen = dtables.get(X11Kernel.D9_SLEN, Integer.class);
            Boolean sdef = dtables.get(X11Kernel.D9_DEFAULT, Boolean.class);
            if (slen != null) {
                mstats.s3x5 = slen == 7;
            }
            if (sdef != null && sdef) {
                mstats.s3x5 = false;
            }

            Double icr = dtables.get(X11Kernel.D12_IC, Double.class);
            if (icr != null) {
                mstats.m[2] = .5 * (icr - 1);
            }

            if (mstats.O.getLength() / mstats.O.getFrequency().intValue() < 6) // stable
            // !!!
            {
                mstats.bShort = true;
            }

            mstats.calcStationaryVariances();
            mstats.calcSNorm();
            mstats.calcEvolutions();
            mstats.calcM();
            // TODO: CH: Welches Table muss hier rein?         
            mstats.calcCochran(ctables.get(X11Kernel.C13, TsData.class), mode, mstats);

            return mstats;
        } catch (RuntimeException err) {
            return null;
        }
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
        return SeriesEvolution.Adr(CIc, mode.isMultiplicative());
    }

    /**
     * Gets the average duration of run of I
     *
     * @return
     */
    public double getAdrOfI() {
        return SeriesEvolution.Adr(Ic, mode.isMultiplicative());
    }

    /**
     * Gets the average duration of run of C
     *
     * @return
     */
    public double getAdrOfC() {
        return SeriesEvolution.Adr(Cc, mode.isMultiplicative());
    }

    /**
     * Computes the auto-correlations on the irregular (without outliers)
     *
     * @return
     */
    public double[] getAutoCorrelationsOfIrregular() {
        TsData irr = Ic;
        if (mode.isMultiplicative()) {
            irr = irr.minus(1);
        }
        AutoCorrelations ac = new AutoCorrelations(irr);
        ac.setCorrectedForMean(false);
        int ifreq = irr.getFrequency().intValue();
        double[] c = new double[ifreq + 2];
        for (int i = 0; i < c.length; ++i) {
            c[i] = ac.autoCorrelation(i + 1);
        }
        return c;
    }
    // O=A1, CI=D11, I=D13, C=D12, S=D10, P=A2, TD=D18, Omod=E1, CImod=E2, Imod=E3;
    private TsData O,/*
             * E1, CI, I, C, S,
             */ P, TD, Omod, CImod, Imod;
    // Corrected series for prelimiary effects
    private TsData Oc, CIc, Cc, Sc, Ic;
    // others
    private TsData Snorm, Pt, Ps, Pi, SI, MCD;
    // Stationary
    private TsData stO, stC;
    private double varC, varS, varI, varP, varTD;
    // For F.2.A
    private double[] /*
             * gO, gCI, gI, gC, gS,
             */ gP, gTD, gOmod, gCImod, gImod, gMCD;
    private double[] gOc, gCc, gIc, gCIc, gSc;
    private double[] m = new double[11];
    private boolean s3x5, bShort;
    private DecompositionMode mode;
    private static double[] wtFull = {10, 11, 10, 8, 11, 10, 18, 7, 7, 4, 4};
    private static double[] wtShort = {14, 15, 10, 8, 11, 10, 32};

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
//        gO = SeriesEvolution.calcAbsMeanVariations(O, null, mul);
//        gCI = SeriesEvolution.calcAbsMeanVariations(CI, null, mul);
//        gI = SeriesEvolution.calcAbsMeanVariations(I, null, mul);
//        gC = SeriesEvolution.calcAbsMeanVariations(C, null, mul);
//        gS = SeriesEvolution.calcAbsMeanVariations(S, null, mul);
        // main results
        gOmod = SeriesEvolution.calcAbsMeanVariations(Omod, null, mul);
        gCImod = SeriesEvolution.calcAbsMeanVariations(CImod, null, mul);
        gImod = SeriesEvolution.calcAbsMeanVariations(Imod, null, mul);
        gOc = SeriesEvolution.calcAbsMeanVariations(Oc, null, mul);
        gCIc = SeriesEvolution.calcAbsMeanVariations(CIc, null, mul);
        gCc = SeriesEvolution.calcAbsMeanVariations(Cc, null, mul);
        gSc = SeriesEvolution.calcAbsMeanVariations(Sc, null, mul);
        gIc = SeriesEvolution.calcAbsMeanVariations(Ic, null, mul);
        if (P != null) {
            gP = SeriesEvolution.calcAbsMeanVariations(P, null, mul);
        }
        if (TD != null) {
            gTD = SeriesEvolution.calcAbsMeanVariations(TD, null, mul);
        }
    }

    private void calcM() {
        int del = 1;
        int p = O.getFrequency().intValue();
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
        double mt = SeriesEvolution.calcAbsMeanVariations(Cc, null, del, mul);
        mt *= mt;
        double mi = SeriesEvolution.calcAbsMeanVariations(Imod, null, del, mul);
        mi *= mi;
        double ms = SeriesEvolution.calcAbsMeanVariations(Sc, null, del, mul);
        ms *= ms;
        double mp = 0;
        if (P != null) {
            mp = SeriesEvolution.calcAbsMeanVariations(P, null, del, mul);
            mp *= mp;
        }

        double mtd = 0;
        if (TD != null) {
            mtd = SeriesEvolution.calcAbsMeanVariations(TD, null, del, mul);
            mtd *= mtd;
        }

        double mo = mt + mi + ms + mp + mtd;

        m[0] = 10 * (mi / mo) / (1 - mp / mo);
    }

    private void calcM10() {
        if (bShort) {
            return;
        }
        PeriodIterator iter = new PeriodIterator(Snorm);
        int ifreq = Snorm.getFrequency().intValue();
        double ds = 0.0;
        int nn = ifreq * 3;
        while (iter.hasMoreElements()) {
            DataBlock db = iter.nextElement().data;
            int n = db.getLength();
            if (n < 5) {
                return;
            }
            for (int i = n - 5; i < n - 2; i++) {
                ds += Math.abs(db.get(i) - db.get(i - 1));
            }
        }

        m[9] = (ds / nn) * 10.0;
    }

    private void calcM11() {
        if (bShort) {
            return;
        }
        int ifreq = Snorm.getFrequency().intValue();
        PeriodIterator iter = new PeriodIterator(Snorm);
        double ds = 0.0;
        int nn = ifreq * 3;
        while (iter.hasMoreElements()) {
            DataBlock db = iter.nextElement().data;
            int n = db.getLength();
            if (n < 6) {
                return;
            }
            ds += Math.abs(db.get(n - 3) - db.get(n - 6));
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
        double adr = SeriesEvolution.Adr(Ic, mode != DecompositionMode.Additive);
        int n = Ic.getLength();
        double lv = Math.sqrt(1.6 * n - 2.9) * 2.577;
        double uv = Math.abs(3 * (n - 1) / adr - (2 * n - 1));
        m[3] = uv / lv;
    }

    private void calcM5() {
        int ifreq = O.getFrequency().intValue();
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
        CombinedSeasonalityTest test = new CombinedSeasonalityTest(SI,
                mode == DecompositionMode.Multiplicative);
        m[6] = test.mvalue();
    }

    private void calcM8() {
        if (bShort) {
            return;
        }
        int ifreq = O.getFrequency().intValue();
        TsData s = Snorm.delta(ifreq);
        s.getValues().abs();
        DescriptiveStatistics stats = new DescriptiveStatistics(s.getValues());
        m[7] = 10 * stats.getSum() / s.getLength();
    }

    private void calcM9() {
        if (bShort) {
            return;
        }
        int ifreq = Snorm.getFrequency().intValue();
        PeriodIterator iter = new PeriodIterator(Snorm);
        double ds = 0.0;
        while (iter.hasMoreElements()) {
            DataBlock db = iter.nextElement().data;
            int n = db.getLength() - 1;
            ds += Math.abs(db.get(n) - db.get(0)) / n;
        }

        m[8] = ds / ifreq * 10.0;
    }

    private void calcSNorm() {
        double stde = 0;
        if (mode != DecompositionMode.Additive) {
            DescriptiveStatistics stat = new DescriptiveStatistics(Sc.minus(1).getValues());
            stde = Math.sqrt(stat.getSumSquare() / stat.getObservationsCount());
        } else {
            DescriptiveStatistics stat = new DescriptiveStatistics(Sc.getValues());
            stde = Math.sqrt(stat.getSumSquare() / stat.getObservationsCount());
        }
        Snorm = Sc.div(stde);
    }

    private void calcStationaryVariances() {
        stC = Cc.clone();
        stO = O.clone();

        double[] line = new double[Cc.getLength()];
        for (int i = 0; i < line.length; ++i) {
            line[i] = i;
        }
        if (mode != DecompositionMode.Additive) {
            Ols ols = new Ols();
            RegModel model = new RegModel();
            //
            stC.getValues().log();
            model.setY(new DataBlock(stC.getValues().internalStorage()));
            model.setMeanCorrection(true);
            model.addX(new DataBlock(line));
            if (!ols.process(model)) {
                return;
            }

            double[] b = ols.getLikelihood().getB();
            TsData lt = new TsData(stC.getStart(), line, false).times(b[1]);
            lt.getValues().add(b[0]);

            stC = stC.minus(lt);
            stO.getValues().log();
            stO = stO.minus(lt);

        } else {
            Ols ols = new Ols();
            RegModel model = new RegModel();

            //
            model.setY(new DataBlock(stC.getValues().internalStorage()));
            model.setMeanCorrection(true);
            model.addX(new DataBlock(line));
            if (!ols.process(model)) {
                return;
            }

            double[] b = ols.getLikelihood().getB();
            TsData lt = new TsData(stC.getStart(), line, false).times(b[1]);
            lt.getValues().add(b[0]);

            stC = stC.minus(lt);
            stO = stO.minus(lt);
        }

        //
        DescriptiveStatistics stats = new DescriptiveStatistics(stO.getValues());
        double varO = stats.getVar();

        stats = new DescriptiveStatistics(stC.getValues());
        varC = stats.getVar();

        if (mode != DecompositionMode.Additive) {
            stats = new DescriptiveStatistics(Sc.log().getValues());
        } else {
            stats = new DescriptiveStatistics(Sc.getValues());
        }
        varS = stats.getSumSquare() / stats.getObservationsCount();

        if (mode != DecompositionMode.Additive) {
            stats = new DescriptiveStatistics(Imod.log().getValues());
        } else {
            stats = new DescriptiveStatistics(Imod.getValues());
        }
        varI = stats.getSumSquare() / stats.getObservationsCount();

        if (P != null) {
            if (mode != DecompositionMode.Additive) {
                stats = new DescriptiveStatistics(P.log().getValues());
            } else {
                stats = new DescriptiveStatistics(P.getValues());
            }

            varP = stats.getVar();
        }
        if (TD != null) {
            if (mode != DecompositionMode.Additive) {
                stats = new DescriptiveStatistics(TD.log().getValues());
            } else {
                stats = new DescriptiveStatistics(TD.getValues());
            }
            varTD = stats.getSumSquare() / stats.getObservationsCount();
        }

        varP /= varO;
        varTD /= varO;
        varS /= varO;
        varC /= varO;
        varI /= varO;
    }

    private void calcCochran(TsData ts, DecompositionMode mode, Mstatistics mstats) {
      // die folgenden müssen mit dem chochran Test in Abhängigkeit von den Sigmavec Einstellungen berechnet werden

        CochranTest cochranTest = new CochranTest(ts, mode.isMultiplicative());
        cochranTest.calcCochranTest();
        mstats.criticalvalue = cochranTest.getCriticalValue();
        mstats.testvalue = cochranTest.getTestValue();
        mstats.cochranTestResult = cochranTest.getTestResult();
        mstats.minNumberOfYears = cochranTest.getMinNumberOfYearsPerPeriod();

    
    }

    ;
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

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            return mapper.contains(id);
        }
    }

    @Override
    public Map<String, Class> getDictionary() {
        synchronized (mapper) {
            LinkedHashMap<String, Class> dictionary = new LinkedHashMap<>();
            mapper.fillDictionary(null, dictionary);
            return dictionary;
        }
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        synchronized (mapper) {
            if (mapper.contains(id)) {
                return mapper.getData(this, id, tclass);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static final String M1 = "m1", M2 = "m2", M3 = "m3", M4 = "m4";
    public static final String M5 = "m5", M6 = "m6", M7 = "m7", M8 = "m8";
    public static final String M9 = "m9", M10 = "m10", M11 = "m11";
    public static final String Q = "q", Q2 = "q-m2";

    public static void fillDictionary(String prefix, Map<String, Class> map) {
        mapper.fillDictionary(prefix, map);
    }

    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<Mstatistics, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<Mstatistics> mapper = new InformationMapper<>();

    static {
        mapper.add(M1, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
        ) {
            @Override
            public Double retrieve(Mstatistics source) {
                return source.getM(1);
            }
        }
        );
        mapper
                .add(M2, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(2);
                            }
                        }
                );
        mapper
                .add(M3, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(3);
                            }
                        }
                );
        mapper
                .add(M4, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(4);
                            }
                        }
                );
        mapper
                .add(M5, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(5);
                            }
                        }
                );
        mapper
                .add(M6, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(6);
                            }
                        }
                );
        mapper
                .add(M7, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(7);
                            }
                        }
                );
        mapper
                .add(M8, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(8);
                            }
                        }
                );
        mapper
                .add(M9, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(9);
                            }
                        }
                );
        mapper
                .add(M10, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(10);
                            }
                        }
                );
        mapper
                .add(M11, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getM(11);
                            }
                        }
                );
        mapper
                .add(Q, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getQ();
                            }
                        }
                );
        mapper
                .add(Q2, new InformationMapper.Mapper<Mstatistics, Double>(Double.class
                        ) {
                            @Override
                            public Double retrieve(Mstatistics source) {
                                return source.getQm2();
                            }
                        }
                );
    }
}
