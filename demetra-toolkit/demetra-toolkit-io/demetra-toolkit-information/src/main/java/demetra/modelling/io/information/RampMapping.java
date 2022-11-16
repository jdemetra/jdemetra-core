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
import demetra.timeseries.regression.Ramp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RampMapping {

    public final String RANGE = "range", START = "start", END = "end";

    public String format(Ramp ramp) {
        Range<LocalDateTime> range = Range.of(ramp.getStart(), ramp.getEnd());
        return VariableMapping.rangeToShortString(range);
    }

    public Ramp parse(String sr) {
        Range<LocalDateTime> range = VariableMapping.rangeFromShortString(sr);
        return new Ramp(range.start(), range.end());
    }

    public Ramp parseLegacy(String sr) {
        Range<LocalDate> range = VariableMapping.rangeFromLegacyString(sr);
        return new Ramp(range.start().atStartOfDay(), range.end().atStartOfDay());
    }

    public InformationSet write(Ramp ramp) {
        InformationSet info = new InformationSet();
        info.set(START, ramp.getStart().toLocalDate().format(DateTimeFormatter.ISO_DATE));
        info.set(END, ramp.getEnd().toLocalDate().format(DateTimeFormatter.ISO_DATE));
        return info;
    }

    public Ramp read(InformationSet info) {
        String start = info.get(START, String.class);
        String end = info.get(END, String.class);
        return new Ramp(LocalDate.parse(start, DateTimeFormatter.ISO_DATE).atStartOfDay(),
                LocalDate.parse(end, DateTimeFormatter.ISO_DATE).atStartOfDay());
    }
}
