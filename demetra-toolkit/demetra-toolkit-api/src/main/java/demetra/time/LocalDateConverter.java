/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public enum LocalDateConverter implements IsoConverter<LocalDate> {

    LOCAL_DATE(DateTimeFormatter.ISO_DATE),
    BASIC_DATE(DateTimeFormatter.BASIC_ISO_DATE),
    ORDINAL_DATE(DateTimeFormatter.ISO_ORDINAL_DATE),
    WEEK_DATE(DateTimeFormatter.ISO_WEEK_DATE);

    private final DateTimeFormatter formatter;

    @Override
    public CharSequence format(LocalDate value) {
        return formatter.format(value);
    }

    @Override
    public LocalDate parse(CharSequence text) throws DateTimeParseException {
        return formatter.parse(text, LocalDate::from);
    }
}
