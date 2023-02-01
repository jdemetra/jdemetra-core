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

import demetra.data.WeightFunction;
import demetra.information.InformationSet;
import demetra.stl.LoessSpec;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class LoessSpecMapping {

    public static final String WIN = "window", DEG = "degree", JUMPS = "jumps", WEIGHTS = "weights";

    public LoessSpec read(InformationSet info) {

        Integer win = info.get(WIN, Integer.class);
        Integer deg = info.get(DEG, Integer.class);
        Integer jump = info.get(JUMPS, Integer.class);

        String w = info.get(WEIGHTS, String.class);

        WeightFunction fn = w == null ? LoessSpec.DEF_WEIGHTS : WeightFunction.valueOf(w);

        int iwin = win, ideg = deg == null ? 1 : deg, ijump = jump == null ? 0 : jump;

        return LoessSpec.of(iwin, ideg, ijump, fn);
    }

    public InformationSet write(LoessSpec spec, boolean verbose) {
        InformationSet info = new InformationSet();

        info.set(WIN, spec.getWindow());
        if (verbose || spec.getDegree() != 1) {
            info.set(DEG, spec.getDegree());
        }
        if (verbose || spec.getJump() != 0) {
            info.set(JUMPS, spec.getJump());
        }
        if (verbose || spec.getLoessFunction() != LoessSpec.DEF_WEIGHTS) {
            info.set(WEIGHTS, spec.getLoessFunction().name());
        }
        return info;
    }

}
