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
package jdplus.sa.diagnostics;

import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import demetra.processing.ProcResults;
import demetra.sa.SaDictionary;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import java.util.List;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.seasonal.StableSeasonality;

/**
 *
 * @author PALATEJ
 */
public class ResidualSeasonalityDiagnostics implements Diagnostics {


    private StatisticalTest saFTest, lastSaFTest, irregularFTest;

    private final double[] saThresholds = new double[]{0.1, 0.05, 0.01};
    private final double[] lastSaThresholds = new double[]{0.1, 0.05, 0.01};
    private final double[] irregularThresholds = new double[]{0.1, 0.05, 0.01};

    protected static ResidualSeasonalityDiagnostics create(ProcResults rslts, ResidualSeasonalityDiagnosticsConfiguration config) {
        try {
            ResidualSeasonalityDiagnostics diag = new ResidualSeasonalityDiagnostics(config);
            if (!diag.test(rslts)) {
                return null;
            } else {
                return diag;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public ResidualSeasonalityDiagnostics(ResidualSeasonalityDiagnosticsConfiguration config) {
        setSaBounds(config.getSevereThresholdForSa(), config.getBadThresholdForSa(), config.getSevereThresholdForSa());
        setIrrBounds(config.getSevereThresholdForIrregular(), config.getBadThresholdForIrregular(), config.getUncertainThresholdForIrregular());
        setLastSaBounds(config.getBadThresholdForLastSa(), config.getBadThresholdForLastSa(), config.getUncertainThresholdForLastSa());
    }

    public boolean test(ProcResults rslts) {
        if (rslts == null) {
            return false;
        }
        // computes the differences
        TsData s = rslts.getData(SaDictionary.SA, TsData.class);
        if (s != null) {
            int freq = s.getAnnualFrequency();
            s = s.delta(Math.max(1, freq / 4));
            // computes the F-Test on the complete series...
            saFTest = StableSeasonality.of(s.getValues(), freq).asTest();
            TimeSelector sel = TimeSelector.last(freq * 3);
            lastSaFTest = StableSeasonality.of(s.select(sel).getValues(),freq).asTest();
        }
        s = rslts.getData(SaDictionary.I, TsData.class);
        if (s != null) {
            int freq = s.getAnnualFrequency();
            irregularFTest = StableSeasonality.of(s.getValues(), freq).asTest();
        }
        return true;
    }

    public double getSaBound(ProcQuality quality) {
        return bound(saThresholds, quality);
    }

    public double getIrregularBound(ProcQuality quality) {
        return bound(irregularThresholds, quality);
    }

    public double getLastSaBound(ProcQuality quality) {
        return bound(lastSaThresholds, quality);
    }

    private void setSaBounds(double severe, double bad, double uncertain) {
        setbounds(saThresholds, severe, bad, uncertain);
    }

    private void setIrrBounds(double severe, double bad, double uncertain) {
        setbounds(irregularThresholds, severe, bad, uncertain);
    }

    private void setLastSaBounds(double severe, double bad, double uncertain) {
        setbounds(lastSaThresholds, severe, bad, uncertain);
    }

    private void setbounds(double[] lb, double severe, double bad, double uncertain) {
        if (severe > bad || bad > uncertain) {
            throw new IllegalArgumentException();
        }
        lb[0] = uncertain;
        lb[1] = bad;
        lb[2] = severe;
    }

    private double bound(double[] lb, ProcQuality quality) {
        if (null == quality) {
            return Double.NaN;
        } else {
            switch (quality) {
                case Severe:
                    return lb[2];
                case Bad:
                    return lb[1];
                case Uncertain:
                    return lb[0];
                default:
                    return Double.NaN;
            }
        }
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
        switch (test) {
            case ResidualSeasonalityDiagnosticsFactory.SA:
                return test(saFTest, saThresholds);
            case ResidualSeasonalityDiagnosticsFactory.SA_LAST:
                return test(lastSaFTest, lastSaThresholds);
            case ResidualSeasonalityDiagnosticsFactory.IRR:
                return test(irregularFTest, irregularThresholds);
            default:
                throw new IllegalArgumentException(test);
        }
    }

    @Override
    public double getValue(String test) {
        double val = 0;
        if (test.equals(ResidualSeasonalityDiagnosticsFactory.SA) && saFTest != null) {
            val = saFTest.getPValue();
        } else if (test.equals(ResidualSeasonalityDiagnosticsFactory.SA_LAST) && lastSaFTest != null) {
            val = lastSaFTest.getPValue();
        } else if (irregularFTest != null) {
            val = irregularFTest.getPValue();
        }
        return val;
    }

    private static ProcQuality test(StatisticalTest test, double[] b) {
        if (test == null) {
            return ProcQuality.Undefined;
        } else if (test.getPValue() > b[0]) {
            return ProcQuality.Good;
        } else if (test.getPValue() > b[1]) {
            return ProcQuality.Uncertain;
        } else if (test.getPValue() > b[2]) {
            return ProcQuality.Bad;
        } else {
            return ProcQuality.Severe;
        }
    }

    @Override
    public List<String> getWarnings() {
        return null;
    }
}
