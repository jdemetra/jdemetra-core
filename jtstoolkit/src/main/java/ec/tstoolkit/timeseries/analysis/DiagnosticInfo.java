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
package ec.tstoolkit.timeseries.analysis;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsDataFunction;

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
                return (ref, s, pos) -> ref.get(pos) - s.get(pos);
            case RelativeDifference:
                return (ref, s, pos) -> {
                    double T = ref.get(pos);
                    return (T - s.get(pos)) / T;
                };
            case PeriodToPeriodGrowthDifference:
                return (ref, s, pos) -> ref.get(pos) / ref.get(pos - 1) - s.get(pos) / s.get(pos - 1);
            case PeriodToPeriodDifference:
                return (ref, s, pos) -> (ref.get(pos) - ref.get(pos - 1)) - (s.get(pos) - s.get(pos - 1));
            case AnnualGrowthDifference:
                return (ref, s, pos) -> {
                    int lag = ref.getFrequency().intValue();
                    return ref.get(pos) / ref.get(pos - lag) - s.get(pos) / s.get(pos - lag);
                };
            case AnnualDifference:
                return (ref, s, pos) -> {
                    int lag = ref.getFrequency().intValue();
                    return (ref.get(pos) - ref.get(pos - lag)) - (s.get(pos) - s.get(pos - lag));
                };
            default:
                return (ref, s, pos) -> Double.NaN;
        }

    }

    public TsDataFunction asTsDataFunction() {
        switch (this) {

            case AbsoluteDifference:
            case RelativeDifference:
                return (s, pos) -> s.get(pos);
            case PeriodToPeriodGrowthDifference:
                return (s, pos) -> s.get(pos) / s.get(pos - 1) - 1;
            case PeriodToPeriodDifference:
                return (s, pos) -> (s.get(pos) - s.get(pos - 1));
            case AnnualGrowthDifference:
                return (s, pos) -> s.get(pos) / s.get(pos - s.getFrequency().intValue()) - 1;
            case AnnualDifference:
                return (s, pos) -> s.get(pos) - s.get(pos - s.getFrequency().intValue());
            default:
                return (s, pos) -> Double.NaN;
        }

    }

    public DiagnosticInfo adaptForNegativeValues() {
        switch (this) {

            case RelativeDifference:
                return AbsoluteDifference;
            case PeriodToPeriodGrowthDifference:
                return PeriodToPeriodDifference;
            case AnnualGrowthDifference:
                return AnnualDifference;
            default:
                return this;
        }

    }
}
