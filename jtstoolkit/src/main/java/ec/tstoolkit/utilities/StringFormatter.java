/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

package ec.tstoolkit.utilities;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author pcuser
 */
public final class StringFormatter {

    public static Day convertDay(String str) {
        return convertDay(str, new Day(new Date()));
    }

    public static Day convertDay(String str, Day defaultDay) {
        try {
            return Day.fromString(str);
        } catch (ParseException err) {
            return defaultDay;
        }
//        if (str != null && str.length() == 10) {
//            String[] split = str.split("-");
//            if (split.length == 3) {
//                return new Day(Integer.getInteger(split[0]), Month.valueOf(Integer.getInteger(split[1])-1), Integer.getInteger(split[2])-1);
//            }
//        }
//        return defaultDay;
    }

    public static String convert(Day day) {
        return day.toString();
    }

    public static String convert(Day day, Day defaultDay) {
        if (day == defaultDay) {
            return null;
        } else {
            return day.toString();
        }
    }

    public static Day yearMonth(String str) {
        if (str != null && str.length() == 7) {
            String[] split = str.split("-");
            if (split.length == 2) {
                return new Day(Integer.getInteger(split[0]), Month.valueOf(Integer.getInteger(split[1])-1), 0);
            }
        }
        return null;
    }

    public static String yearMonth(Day day) {
        return day.getYear() + "-" + (1+day.getMonth());
    }

    public static boolean read(IntList c, String str) {
        Tokenizer tokens = new Tokenizer(str);
        while (tokens.hasNextToken()) {
            String next = tokens.nextToken();
            int val;
            try {
                val = Integer.parseInt(next);
            } catch (Exception ex) {
                return false;
            }
            c.add(val);
        }
        return true;
    }

    private StringFormatter() {
    }

    public static String write(TsPeriod period) {
        if (period == null) {
            return null;

        }
        StringBuilder builder = new StringBuilder();
        builder.append(period.getYear());
        switch (period.getFrequency()) {
            case Yearly:
                return builder.toString();
            case HalfYearly:
                builder.append('H');
                break;
            case QuadriMonthly:
                builder.append('T');
                break;
            case Quarterly:
                builder.append('Q');
                break;
            case BiMonthly:
                builder.append('B');
                break;
            case Monthly:
                builder.append('M');
                break;
        }
        builder.append(period.getPosition() + 1);
        return builder.toString();
    }

    public static TsPeriod readPeriod(String s) {
        if (s == null) {
            return null;

        }
        int pos = -1;
        int year, freq, period;

        for (int i = 0; i < s.length(); ++i) {
            if (Character.isLetter(s.charAt(i))) {
                pos = i;
                break;
            }

        }
        if (pos == -1) {
            try {
                year = Integer.parseInt(s);
                return new TsPeriod(TsFrequency.Yearly, year, 0);
            } catch (NumberFormatException ex) {
                return null;
            }
        }


        switch (s.charAt(pos)) {
            case 'H':
                freq = 2;
                break;
            case 'T':
                freq = 3;
                break;
            case 'Q':
                freq = 4;
                break;
            case 'B':
                freq = 6;
                break;
            case 'M':
                freq = 12;
                break;
            case 'h':
                freq = 2;
                break;
            case 't':
                freq = 3;
                break;
            case 'q':
                freq = 4;
                break;
            case 'b':
                freq = 6;
                break;
            case 'm':
                freq = 12;
                break;

            default:
                return null;
        }
        try {
            year = Integer.parseInt(s.substring(0, pos));
            period = Integer.parseInt(s.substring(pos + 1));
            if (period <= 0 || period > freq) {
                return null;

            }
            return new TsPeriod(TsFrequency.valueOf(freq), year, period - 1);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    private static final NumberFormat fmt = java.text.NumberFormat.getNumberInstance(Locale.ROOT);

    static {
        fmt.setMaximumFractionDigits(9);
    }

    public static String write(Parameter[] p) {
        if (p == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < p.length; ++i) {
            if (p[i] != null) {
                builder.append(fmt.format(p[i].getValue()));
                if (p[i].getType() == ParameterType.Fixed) {
                    builder.append('f');
                }
            } else {
                builder.append(0);
            }
            if (i < p.length - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    public static Parameter[] readParameters(String s) {
        if (s == null) {
            return null;
        }
        ArrayList<Parameter> p = new ArrayList<>();
        Tokenizer tokenizer = new Tokenizer(s);
        while (tokenizer.hasNextToken()) {
            String token = tokenizer.nextToken();
            boolean f = token.charAt(token.length() - 1) == 'f';
            if (f) {
                token = token.substring(0, token.length() - 1);
            }
            Number val;
            try {
                val = fmt.parse(token);
            } catch (ParseException ex) {
                return null;
            }
            p.add(new Parameter(val.doubleValue(), f ? ParameterType.Fixed : ParameterType.Estimated));
        }
        if (p.isEmpty()) {
            return null;
        } else {
            return Jdk6.Collections.toArray(p, Parameter.class);
        }
    }
}
