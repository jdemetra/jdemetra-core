/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.tramoseats.io.information;

import demetra.information.InformationSet;
import demetra.tramo.EasterSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EasterSpecMapping {

    public final String DURATION = "duration", TYPE = "type", TEST = "test", JULIAN = "julian";

    public void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, TYPE), String.class);
        dic.put(InformationSet.item(prefix, DURATION), Integer.class);
        dic.put(InformationSet.item(prefix, TEST), String.class);
        dic.put(InformationSet.item(prefix, JULIAN), Boolean.class);
    }

    public InformationSet write(EasterSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getDuration() != EasterSpec.DEF_IDUR) {
            info.add(DURATION, spec.getDuration());
        }
        if (verbose || spec.getType() != EasterSpec.Type.Unused) {
            info.add(TYPE, spec.getType().name());
        }
        if (verbose || spec.isTest()) {
            info.add(TEST, spec.isTest());
        }
        if (verbose || spec.isJulian() != EasterSpec.DEF_JULIAN) {
            info.add(JULIAN, spec.isJulian());
        }
        return info;
    }

    public EasterSpec read(InformationSet info) {
        if (info == null) {
            return EasterSpec.DEFAULT;
        }
        EasterSpec.Builder builder = EasterSpec.builder();
        Integer d = info.get(DURATION, Integer.class);
        if (d != null) {
            builder = builder.duration(d);
        }
        String type = info.get(TYPE, String.class);
        if (type != null) {
            builder = builder.type(EasterSpec.Type.valueOf(type));
        }
        Boolean test = info.get(TEST, Boolean.class);
        if (test != null) {
            builder = builder.test(test);
        }
        Boolean jul = info.get(JULIAN, Boolean.class);
        if (jul != null) {
            builder = builder.julian(jul);
        }
        return builder.build();
    }
}
