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
import demetra.information.InformationException;
import demetra.information.InformationSet;
import demetra.timeseries.regression.InterventionVariable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class InterventionVariableMapping {

    public final String NAME = "name",
            DELTA = "delta",
            DELTAS = "deltas",
            SEQS = "sequences";

    private final String INVALID = "Invalid intervention variable";

    public void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, SEQS), String[].class);
        dic.put(InformationSet.item(prefix, NAME), String.class);
        dic.put(InformationSet.item(prefix, DELTA), Double.class);
        dic.put(InformationSet.item(prefix, DELTAS), Double.class);
    }

    public String toShortString(Range<LocalDateTime> seq) {
        StringBuilder builder = new StringBuilder();
        builder.append(seq.start().toLocalDate().format(DateTimeFormatter.ISO_DATE))
                .append(InformationSet.SEP).append(seq.end().toLocalDate().format(DateTimeFormatter.ISO_DATE));
        return builder.toString();
    }

    public Range<LocalDateTime> fromShortString(String s) {
        String[] ss = InformationSet.split(s);
        if (ss.length == 1) {
            LocalDate start = LocalDate.parse(ss[0], DateTimeFormatter.ISO_DATE);
            if (start != null) {
                return Range.of(start.atStartOfDay(), start.atStartOfDay());
            }
        }
        if (ss.length != 2) {
            throw new InformationException(INVALID);
        }
        LocalDate start = LocalDate.parse(ss[0], DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(ss[1], DateTimeFormatter.ISO_DATE);
        return Range.of(start.atStartOfDay(), end.atStartOfDay());
    }

    public InformationSet write(InterventionVariable var, boolean verbose) {
        InformationSet info = new InformationSet();
        if (verbose || var.getDelta() != 0) {
            info.add(DELTA, var.getDelta());
        }
        if (verbose || var.getDeltaSeasonal() != 0) {
            info.add(DELTAS, var.getDeltaSeasonal());
        }
        Range<LocalDateTime>[] sequences = var.getSequences();
        String[] seqs = new String[sequences.length];
        for (int i = 0; i < sequences.length; ++i) {
            seqs[i] = toShortString(sequences[i]);
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
                Range<LocalDateTime> cur = fromShortString(seqs[i]);
                if (cur != null) {
                    builder.add(cur.start(), cur.end());
                }
            }
        }
        return builder.build();
    }
}
