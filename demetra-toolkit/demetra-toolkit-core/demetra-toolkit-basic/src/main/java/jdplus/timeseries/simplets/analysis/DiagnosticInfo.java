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
package jdplus.timeseries.simplets.analysis;

import demetra.timeseries.TsData;
import java.util.function.ToDoubleFunction;
import nbbrd.design.Development;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public enum DiagnosticInfo {

    /**
     *
     */
    AbsoluteDifference,
    /**
     *
     */
    RelativeDifference,
    /**
     *
     */
    PeriodToPeriodGrowthDifference,
    /**
     *
     */
    PeriodToPeriodDifference,
    /**
     *
     */
    AnnualGrowthDifference,
    /**
     *
     */
    AnnualDifference;

    public DiagnosticTsFunction asFunction() {
        switch (this) {

            case AbsoluteDifference:
                return (ref, s, pos) -> ref.getValue(pos) - s.getValue(pos);
            case RelativeDifference:
                return (ref, s, pos) -> {
                    double T = ref.getValue(pos);
                    return 100*(T - s.getValue(pos)) / T; // percent
                };
            case PeriodToPeriodGrowthDifference:
                return (ref, s, pos) -> 100*(ref.getValue(pos) / ref.getValue(pos - 1) - s.getValue(pos) / s.getValue(pos - 1));
            case PeriodToPeriodDifference:
                return (ref, s, pos) -> (ref.getValue(pos) - ref.getValue(pos - 1)) - (s.getValue(pos) - s.getValue(pos - 1));
            case AnnualGrowthDifference:
                return (ref, s, pos) -> {
                    int lag = ref.getAnnualFrequency();
                    return 100*(ref.getValue(pos) / ref.getValue(pos - lag) - s.getValue(pos) / s.getValue(pos - lag));
                };
            case AnnualDifference:
                return (ref, s, pos) -> {
                    int lag = ref.getAnnualFrequency();
                    return (ref.getValue(pos) - ref.getValue(pos - lag)) - (s.getValue(pos) - s.getValue(pos - lag));
                };
            default:
                return (ref, s, pos) -> Double.NaN;
        }

    }

    public TsDataFunction asTsDataFunction() {
        return switch (this) {
            case AbsoluteDifference, RelativeDifference -> (s, pos) -> s.getValue(pos);
            case PeriodToPeriodGrowthDifference -> (s, pos) -> 100*(s.getValue(pos) / s.getValue(pos - 1) - 1);
            case PeriodToPeriodDifference -> (s, pos) -> (s.getValue(pos) - s.getValue(pos - 1));
            case AnnualGrowthDifference -> (s, pos) -> 100*(s.getValue(pos) / s.getValue(pos - s.getAnnualFrequency()) - 1);
            case AnnualDifference -> (s, pos) -> s.getValue(pos) - s.getValue(pos - s.getAnnualFrequency());
            default -> (s, pos) -> Double.NaN;
        };

    }

    public DiagnosticInfo adaptForNegativeValues() {
        return switch (this) {
            case RelativeDifference -> AbsoluteDifference;
            case PeriodToPeriodGrowthDifference -> PeriodToPeriodDifference;
            case AnnualGrowthDifference -> AnnualDifference;
            default -> this;
        };

    }
    
    public boolean isRelative(){
        return this==RelativeDifference || this == PeriodToPeriodGrowthDifference || this == AnnualGrowthDifference;
    }
    
    @Override
    public String toString(){
        return switch (this) {
            case AbsoluteDifference -> "Absolute differences";
            case RelativeDifference -> "Relative differences (%)";
            case PeriodToPeriodGrowthDifference -> "Period to period growth differences (%)";
            case PeriodToPeriodDifference -> "Period to period differences";
            case AnnualGrowthDifference -> "Annual growth differences (%)";
            case AnnualDifference -> "Annual differences";
            default -> null;
        };
        
    }
}
