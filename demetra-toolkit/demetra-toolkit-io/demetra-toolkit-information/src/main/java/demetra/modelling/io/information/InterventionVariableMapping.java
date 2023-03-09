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

import demetra.data.Range;
import demetra.information.InformationSet;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Variable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class InterventionVariableMapping {

    public final String DELTA = "delta",
            DELTAS = "deltas",
            SEQS = "sequences",
            NAME_LEGACY = "name";


    public void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, SEQS), String[].class);
        dic.put(InformationSet.item(prefix, DELTA), Double.class);
        dic.put(InformationSet.item(prefix, DELTAS), Double.class);
    }
    
    public InformationSet writeLegacy(Variable<InterventionVariable> var, boolean verbose){
        InformationSet info = write(var.getCore(), verbose);
        info.set(NAME_LEGACY, var.getName());
        return info;
    }

    public InformationSet write(InterventionVariable var, boolean verbose) {
        InformationSet info = new InformationSet();
        if (verbose || var.getDelta() != 0) {
            info.add(DELTA, var.getDelta());
        }
        if (verbose || var.getDeltaSeasonal() != 0) {
            info.add(DELTAS, var.getDeltaSeasonal());
        }
        List<Range<LocalDateTime>> sequences = var.getSequences();
        String[] seqs = new String[sequences.size()];
        int i=0;
        for (Range<LocalDateTime> seq:sequences) {
            seqs[i++] = VariableMapping.rangeToShortString(seq);
        }
        info.add(SEQS, seqs);
        return info;
    }

    public InterventionVariable read(InformationSet info) {
        InterventionVariable.Builder builder = InterventionVariable.builder();
        Double delta = info.get(DELTA, Double.class);
        if (delta != null) {
            builder.delta(delta);
        }
        Double deltas = info.get(DELTAS, Double.class);
        if (deltas != null) {
            builder.deltaSeasonal(deltas);
        }
        String[] seqs = info.get(SEQS, String[].class);
        if (seqs != null) {
            for (int i = 0; i < seqs.length; ++i) {
                Range<LocalDateTime> cur = VariableMapping.rangeFromShortString(seqs[i]);
                if (cur != null) {
                    builder.sequence(cur);
                }
            }
        }
        return builder.build();
    }
}
