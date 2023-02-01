/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.stl.io.information;

import demetra.information.InformationSet;
import demetra.stl.LoessSpec;
import demetra.stl.SeasonalSpec;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SeasonalSpecMapping {

    public final String PERIOD = "period", LOW = "low-pass", SEAS = "seasonal";

    public InformationSet write(SeasonalSpec spec, boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(PERIOD, spec.getPeriod());
        info.set(LOW, LoessSpecMapping.write(spec.getLowPassSpec(), verbose));
        info.set(SEAS, LoessSpecMapping.write(spec.getSeasonalSpec(), verbose));
        return info;
    }

    public SeasonalSpec read(InformationSet info) {
        Integer period = info.get(PERIOD, Integer.class);
        LoessSpec low = LoessSpecMapping.read(info.getSubSet(LOW));
        LoessSpec seas = LoessSpecMapping.read(info.getSubSet(SEAS));
        return SeasonalSpec.builder()
                .period(period)
                .lowPassSpec(low)
                .seasonalSpec(seas)
                .build();
    }
}
