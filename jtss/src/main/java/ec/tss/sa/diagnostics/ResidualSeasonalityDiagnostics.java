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

import ec.tstoolkit.modelling.ModellingDictionary;
import ec.satoolkit.diagnostics.SeasonalityTest;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class ResidualSeasonalityDiagnostics implements IDiagnostics {

    private SeasonalityTest fsa_, fsa3_, firr_;
    

    private double[] sa_ = new double[] { 0.1, 0.05, 0.01 };
    private double[] sa3_ = new double[] { 0.1, 0.05, 0.01 };
    private double[] irr_ = new double[] { 0.1, 0.05, 0.01 };

    protected static ResidualSeasonalityDiagnostics create(ResidualSeasonalityDiagnosticsConfiguration config, CompositeResults rslts) {
        try {
            ResidualSeasonalityDiagnostics diag = new ResidualSeasonalityDiagnostics(config);
            if (! diag.test(rslts))
                return null;
            else
                return diag;
        }
        catch(Exception ex) {
            return null;
        }
    }

    public ResidualSeasonalityDiagnostics(ResidualSeasonalityDiagnosticsConfiguration config) {
           setSABounds(config.getSASevere(), config.getSABad(), config.getSAUncertain());
           setIrrBounds(config.getIrrSevere(), config.getIrrBad(), config.getIrrUncertain());
           setSA3Bounds(config.getSA3Severe(), config.getSA3Bad(), config.getSA3Uncertain());
    }

    public boolean test(CompositeResults rslts) {
        // computes the differences
        TsData s =rslts.getData(ModellingDictionary.SA, TsData.class);
        if (s != null) {
            int freq = s.getFrequency().intValue();
            s = s.delta(Math.max(1, freq / 4));

            // computes the F-Test on the complete series...
            fsa_ = SeasonalityTest.stableSeasonality(s);
            TsPeriodSelector sel = new TsPeriodSelector();
            sel.last(s.getFrequency().intValue() * 3);
            fsa3_ = SeasonalityTest.stableSeasonality(s.select(sel));
        }
        s = rslts.getData(ModellingDictionary.I, TsData.class);
        if (s != null)
            firr_ = SeasonalityTest.stableSeasonality(s);

        return true;
    }

    public double getSABound(ProcQuality quality) {
        return bound(sa_, quality);
    }

    public double getIrrBound(ProcQuality quality) {
        return bound(irr_, quality);
    }

    public double getSA3Bound(ProcQuality quality) {
        return bound(sa3_, quality);
    }

    private void setSABounds(double severe, double bad, double uncertain) {
        setbounds(sa_, severe, bad, uncertain);
    }

    private void setIrrBounds(double severe, double bad, double uncertain) {
        setbounds(irr_, severe, bad, uncertain);
    }

    private void setSA3Bounds(double severe, double bad, double uncertain) {
        setbounds(sa3_, severe, bad, uncertain);
    }

    private void setbounds(double[] lb, double severe, double bad, double uncertain) {
        lb[0] = uncertain;
        lb[1] = bad;
        lb[2] = severe;
    }

    private double bound(double[] lb, ProcQuality quality) {
        if (quality == ProcQuality.Severe)
            return lb[2];
        else if (quality == ProcQuality.Bad)
            return lb[1];
        else if (quality == ProcQuality.Uncertain)
            return lb[0];
        else
            return Double.NaN;
    }

    @Override
    public String getName() {
        return ResidualSeasonalityDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return ResidualSeasonalityDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        if (test.equals(ResidualSeasonalityDiagnosticsFactory.SA))
            return test(fsa_, sa_);
        else if(test.equals(ResidualSeasonalityDiagnosticsFactory.SA_LAST))
            return test(fsa3_, sa3_);
        else
            return test(firr_, irr_);
    }

    @Override
    public double getValue(String test) {
        double val = 0;
        if (test.equals(ResidualSeasonalityDiagnosticsFactory.SA) && fsa_ != null)
            val = fsa_.getPValue();
        else if(test.equals(ResidualSeasonalityDiagnosticsFactory.SA_LAST) && fsa3_ != null)
            val = fsa3_.getPValue();
        else if ( firr_ != null)
            val = firr_.getPValue();
        return val;
    }

    private static ProcQuality test(SeasonalityTest test, double[] b) {
        if (test == null)
            return ProcQuality.Undefined;
        else if (test.getPValue() > b[0])
            return ProcQuality.Good;
        else if (test.getPValue() > b[1])
            return ProcQuality.Uncertain;
        else if (test.getPValue() > b[2])
            return ProcQuality.Bad;
        else
            return ProcQuality.Severe;
    }

    @Override
    public List<String> getWarnings() {
        return null;
    }
}
