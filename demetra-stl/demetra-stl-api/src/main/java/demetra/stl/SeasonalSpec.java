/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.stl;

/**
 * Contains specifications for the seasonal filter itself and for the low-pass
 * filter used to remove a possible trend in the seasonal component computed by
 * the seasonal filter
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class SeasonalSpec {

    public static SeasonalSpec createDefault(int period) {
        LoessSpec s = LoessSpec.defaultSeasonal();
        LoessSpec t = LoessSpec.defaultLowPass(period);
        return new SeasonalSpec(period, s, t);
    }

    public SeasonalSpec() {
        this.period = 12;
        this.seasonalSpec = LoessSpec.defaultSeasonal();
        this.lowPassSpec = LoessSpec.defaultLowPass(period);
    }

    private final int period;
    private final LoessSpec seasonalSpec;
    private final LoessSpec lowPassSpec;

    public SeasonalSpec(int period, int swindow) {
        this.period = period;
        this.seasonalSpec = LoessSpec.defaultSeasonal(swindow);
        this.lowPassSpec = LoessSpec.defaultLowPass(period);
    }

    public SeasonalSpec(int period, LoessSpec sspec, LoessSpec lspec) {
        this.period = period;
        this.seasonalSpec = sspec;
        this.lowPassSpec = lspec;
    }

    @Override
    public String toString() {
        return "seas-" + period;
    }
}
