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

import demetra.information.InformationSet;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class OutlierDefinition {

    private final LocalDate position;
    private final String code;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(InformationSet.concatenate(code, position.format(DateTimeFormatter.ISO_DATE)));
        return builder.toString();
    }

    public String toString(int freq) {
        if (freq == 0) {
            return toString();
        } else {
            TsPeriod p = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), position);
            StringBuilder builder = new StringBuilder();
            builder.append(code).append(InformationSet.SEP).append(write(p));
            return builder.toString();
        }
    }

    public static OutlierDefinition fromString(String s) {
        String[] ss = InformationSet.split(s);
        if (ss.length != 2 && ss.length != 3) {
            return null;
        }
        String type = ss[0];
        boolean p = false;
        if (ss.length == 3) {
            if (!ss[2].equals("f")) {
                return null;
            } else {
                p = true;
            }
        }
        if (p) {
            TsPeriod period = readPeriod(ss[1]);
            if (period != null) {
                return new OutlierDefinition(period.start().toLocalDate(), type);
            } else {
                return null;
            }
        }
        try {
            LocalDate day = LocalDate.parse(ss[1], DateTimeFormatter.ISO_DATE);
            return new OutlierDefinition(day, type);
        } catch (DateTimeParseException err) {
            return null;
        }
    }

    public static String write(TsPeriod period) {
        if (period == null) {
            return null;

        }
        StringBuilder builder = new StringBuilder();
        builder.append(period.year());
        switch (period.getUnit().getAnnualFrequency()) {
            case 1:
                return builder.toString();
            case 2:
                builder.append('H');
                break;
            case 3:
                builder.append('T');
                break;
            case 4:
                builder.append('Q');
                break;
            case 6:
                builder.append('B');
                break;
            case 12:
                builder.append('M');
                break;
        }
        builder.append(period.annualPosition() + 1);
        return builder.toString();
    }

    public static TsPeriod readPeriod(String s) {
        if (s == null) {
            return null;

        }
        int pos = -1;

        for (int i = 0; i < s.length(); ++i) {
            if (Character.isLetter(s.charAt(i))) {
                pos = i;
                break;
            }

        }
        if (pos == -1) {
            try {
                int year = Integer.parseInt(s);
                return TsPeriod.yearly(year);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        int year = Integer.parseInt(s.substring(0, pos));
        int period = Integer.parseInt(s.substring(pos + 1));
        switch (s.charAt(pos)) {
            case 'H':
            case 'h':
                return TsPeriod.of(TsUnit.HALF_YEAR, LocalDate.of(year, (period - 1) * 6 + 1, 1));
            case 'T':
            case 't':
                return TsPeriod.of(TsUnit.of(4, ChronoUnit.MONTHS), LocalDate.of(year, (period - 1) * 4 + 1, 1));
            case 'Q':
            case 'q':
                return TsPeriod.quarterly(year, period);
            case 'B':
            case 'b':
                return TsPeriod.of(TsUnit.of(2, ChronoUnit.MONTHS), LocalDate.of(year, (period - 1) * 2 + 1, 1));
            case 'M':
            case 'm':
                return TsPeriod.quarterly(year, period);

            default:
                return null;
        }
    }

}
