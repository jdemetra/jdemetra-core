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


package ec.tss.sa.diagnostics;

import ec.satoolkit.GenericSaResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.stats.DoornikHansenTest;
import ec.tstoolkit.stats.LjungBoxTest;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class ResidualsDiagnostics implements IDiagnostics {

    public static final String NORMALITY = "normality", INDEPENDENCE = "independence",
        TD_PEAK = "spectral td peaks", S_PEAK = "spectral seas peaks";

    public static final String NAME = "regarima residuals";

    private List<String> tests_ = Arrays.asList(NORMALITY, INDEPENDENCE, TD_PEAK, S_PEAK);

    private double N0_ = 0.1, N1_ = .01;
    private double tdPeriodogram0_ = 0.1, tdPeriodogram1_ = 0.01, tdPeriodogram2_ = 0.001;
    private double sPeriodogram0_ = 0.1, sPeriodogram1_ = 0.01, sPeriodogram2_ = 0.001;
    private List<String> warnings_ = new ArrayList<>();

    private NiidTests stats_;
    private Periodogram periodogram_;
    private int freq_;

    static ResidualsDiagnostics create(ResidualsDiagnosticsConfiguration config, CompositeResults crslts) {
        try {
            PreprocessingModel regarima=GenericSaResults.getPreprocessingModel(crslts);
            if (regarima instanceof PreprocessingModel) {
                    return new ResidualsDiagnostics(config, regarima);
            } else
                return null;
        }
        catch(Exception ex) {
            return null;
        }
    }

    public ResidualsDiagnostics(ResidualsDiagnosticsConfiguration config, PreprocessingModel rslts) {
        sPeriodogram2_ = config.getSpecSeasSevere();
        sPeriodogram1_ = config.getSpecSeasBad();
        sPeriodogram0_ = config.getSpecSeasUncertain();

        tdPeriodogram2_ = config.getSpecTDSevere();
        tdPeriodogram1_ = config.getSpecTDBad();
        tdPeriodogram0_ = config.getSpecTDUncertain();

        N0_ = config.getNIIDUncertain();
        N1_ = config.getNIIDBad();

        testRegarima(rslts);
    }

    private boolean testRegarima(PreprocessingModel regarima) {
        try {
            TsData res = regarima.getFullResiduals();
            freq_ = res.getFrequency().intValue();
            stats_ = new NiidTests(res, freq_, 0, true);
            periodogram_ = new Periodogram(res);
            return true;
        }
        catch(Exception ex) {
            return false;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getTests() {
        return tests_;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        double pval = 0;
        if (test.equals(tests_.get(0))) {
            if (stats_ == null)
                return ProcQuality.Undefined;
            DoornikHansenTest dht = stats_.getNormalityTest();
            if (dht == null)
                return ProcQuality.Undefined;
            pval = dht.getPValue();
            if (pval > N0_)
                return ProcQuality.Good;
            else if (pval < N1_)
                return ProcQuality.Bad;
            else
                return ProcQuality.Uncertain;
        }
        else if (test.equals(tests_.get(1))) {
            if (stats_ == null)
                return ProcQuality.Undefined;
            LjungBoxTest lbt = stats_.getLjungBox();
            if (lbt == null)
                return ProcQuality.Undefined;
            pval = lbt.getPValue();
            if (pval > N0_)
                return ProcQuality.Good;
            else if (pval < N1_)
                return ProcQuality.Bad;
            else
                return ProcQuality.Uncertain;
        }
        else if (test.equals(tests_.get(2))) {
            if (periodogram_ == null)
                return ProcQuality.Undefined;
            double[] tdfreqs = Periodogram.getTradingDaysFrequencies(freq_);
            double[] p = periodogram_.getS();
            double xmax = 0;
            double dstep = periodogram_.getIntervalInRadians();
            for (int i = 0; i < tdfreqs.length; ++i) {
                int i0 = (int)(tdfreqs[i] / dstep);
                double xcur = p[i0];
                if (xcur > xmax)
                    xmax = xcur;
                xcur = p[i0 + 1];
                if (xcur > xmax)
                    xmax = xcur;
            }
            pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), tdfreqs.length);
            if (pval < tdPeriodogram2_)
                return ProcQuality.Severe;
            if (pval < tdPeriodogram1_)
                return ProcQuality.Bad;
            else if (pval > tdPeriodogram0_)
                return ProcQuality.Good;
            else
                return ProcQuality.Uncertain;
        }
        else if (test.equals(tests_.get(3))) {
            if (periodogram_ == null)
                return ProcQuality.Undefined;
            double[] seasfreqs = new double[(freq_ - 1) / 2];
            // seas freq in radians...
            for (int i = 0; i < seasfreqs.length; ++i)
                seasfreqs[i] = (i + 1) * 2 * Math.PI / freq_;

            double[] p = periodogram_.getS();
            double xmax = 0;
            double dstep = periodogram_.getIntervalInRadians();
            for (int i = 0; i < seasfreqs.length; ++i) {
                int i0 = (int)(seasfreqs[i] / dstep);
                double xcur = p[i0];
                if (xcur > xmax)
                    xmax = xcur;
                xcur = p[i0 + 1];
                if (xcur > xmax)
                    xmax = xcur;
            }
            pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), seasfreqs.length);
            if (pval < sPeriodogram2_)
                return ProcQuality.Severe;
            if (pval < sPeriodogram1_)
                return ProcQuality.Bad;
            else if (pval > sPeriodogram0_)
                return ProcQuality.Good;
            else
                return ProcQuality.Uncertain;
        }
        return ProcQuality.Undefined;
    }

    @Override
    public double getValue(String test) {
        try{
        double pval = 0;
        if (test.equals(tests_.get(0))) {
            if (stats_ != null) {
                DoornikHansenTest dht = stats_.getNormalityTest();
                pval = dht.getPValue();
            }
        }
        else if (test.equals(tests_.get(1))) {
            if (stats_ != null) {
                LjungBoxTest lbt = stats_.getLjungBox();
                pval = lbt.getPValue();
            }
        }
        else if (test.equals(tests_.get(2))) {
            if (periodogram_ != null) {
                double[] tdfreqs = Periodogram.getTradingDaysFrequencies(freq_);
                double[] p = periodogram_.getS();
                double xmax = 0;
                double dstep = periodogram_.getIntervalInRadians();
                for (int i = 0; i < tdfreqs.length; ++i) {
                    int i0 = (int)(tdfreqs[i] / dstep);
                    double xcur = p[i0];
                    if (xcur > xmax)
                        xmax = xcur;
                    xcur = p[i0 + 1];
                    if (xcur > xmax)
                        xmax = xcur;
                }
                pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), tdfreqs.length);
            }
        }
        else if (test.equals(tests_.get(3))) {
            if (periodogram_ != null) {
                double[] seasfreqs = new double[(freq_ - 1) / 2];
                // seas freq in radians...
                for (int i = 0; i < seasfreqs.length; ++i)
                    seasfreqs[i] = (i + 1) * 2 * Math.PI / freq_;

                double[] p = periodogram_.getS();
                double xmax = 0;
                double dstep = periodogram_.getIntervalInRadians();
                for (int i = 0; i < seasfreqs.length; ++i) {
                    int i0 = (int)(seasfreqs[i] / dstep);
                    double xcur = p[i0];
                    if (xcur > xmax)
                        xmax = xcur;
                    xcur = p[i0 + 1];
                    if (xcur > xmax)
                        xmax = xcur;
                }
                pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), seasfreqs.length);
            }
        }
        return pval;
        }
        catch (Exception err){
            return Double.NaN;
        }
    }

    @Override
    public List<String> getWarnings() {
        return warnings_;
    }

    public double getNIIDBound(ProcQuality quality) {
        switch (quality) {
            case Bad:
                return N1_;
            case Uncertain:
                return N0_;
            default:
                return Double.NaN;
        }
    }

    public double getTDPeriodogram(ProcQuality quality) {
        switch (quality) {
            case Severe:
                return tdPeriodogram2_;
            case Bad:
                return tdPeriodogram1_;
            case Uncertain:
                return tdPeriodogram0_;
            default:
                return Double.NaN;
        }
    }

    public double getSPeriodogram(ProcQuality quality) {
        switch (quality) {
            case Severe:
                return sPeriodogram2_;
            case Bad:
                return sPeriodogram1_;
            case Uncertain:
                return sPeriodogram0_;
            default:
                return Double.NaN;
        }
    }
}
