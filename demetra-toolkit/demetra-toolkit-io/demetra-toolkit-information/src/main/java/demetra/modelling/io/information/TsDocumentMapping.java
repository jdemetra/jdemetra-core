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
package demetra.modelling.io.information;

import demetra.information.Explorable;
import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.processing.ProcSpecification;
import demetra.timeseries.Ts;
import demetra.timeseries.TsDocument;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TsDocumentMapping {

    public static final String INPUT = "input", SPEC = "specification", RESULTS = "results", METADATA = "metadata";
    public static final String SERIES = "series";

    public <S extends ProcSpecification, R extends Explorable> InformationSet write(TsDocument<S, R> doc, InformationSetSerializer<S> ispec, boolean verbose, boolean legacy) {

        InformationSet info = new InformationSet();

        S spec = doc.getSpecification();
        Ts s = doc.getInput();
        if (s != null) {
            info.subSet(INPUT).set(SERIES, s);
        }
        info.set(SPEC, ispec.write(spec, verbose));
        if (legacy) {
            info.set(ProcSpecification.ALGORITHM, spec.getAlgorithmDescriptor());
        }
        Map<String, String> meta = doc.getMetadata();
        if (!meta.isEmpty()) {
            InformationSet minfo = info.subSet(METADATA);
            meta.entrySet().forEach(entry -> {
                minfo.set(entry.getKey(), entry.getValue());
            });
        }
        return info;
    }

    public <S extends ProcSpecification, R extends Explorable> void read(InformationSet info, InformationSetSerializer<S> ispec, TsDocument<S, R> doc) {
        InformationSet spec = info.getSubSet(SPEC);
        S sp = ispec.read(spec);
        InformationSet input = info.getSubSet(INPUT);
        if (input != null) {
            Ts s = input.get(SERIES, Ts.class);
            doc.set(sp, s);
        } else {
            doc.set(sp);
        }
        InformationSet minfo = info.subSet(METADATA);
        if (minfo != null) {
            Map<String, String> map = new HashMap<>();
            List<Information<String>> all = minfo.select(String.class);
            all.forEach(entry -> {
                map.put(entry.getName(), entry.getValue());
            });
            if (!map.isEmpty()) {
                doc.setMetadata(map);
            }
        }

    }

}
