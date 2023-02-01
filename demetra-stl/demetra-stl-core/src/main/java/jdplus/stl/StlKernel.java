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
package jdplus.stl;

import demetra.stl.StlSpec;
import demetra.timeseries.TsData;

/**
 *
 * @author palatej
 */
public class StlKernel {

    private final StlSpec spec;

    private StlKernel(StlSpec spec) {
        this.spec = spec;
    }

    public static StlKernel of(StlSpec spec) {
        return new StlKernel(spec);
    }

    public StlResults process(TsData s) {
        if (spec == null) {
            StlSpec nspec = StlSpec.createDefault(s.getAnnualFrequency(), false, true);
            return StlToolkit.process(s, nspec);
        } else {
            return StlToolkit.process(s, spec);
        }
    }
}
