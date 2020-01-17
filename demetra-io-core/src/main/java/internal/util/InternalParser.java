/*
 * Copyright 2017 National Bank of Belgium
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
package internal.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalParser {

    public <T> T parseTemporalAccessor(DateTimeFormatter formatter, TemporalQuery<T>[] queries, CharSequence input) {
        if (input != null) {
            try {
                switch (queries.length) {
                    case 0:
                        throw new IllegalArgumentException("At least one query must be specified");
                    case 1:
                        return formatter.parse(input, queries[0]);
                    default:
                        return (T) formatter.parseBest(input, queries);
                }
            } catch (DateTimeParseException ex) {
            }
        }
        return null;
    }

    public Boolean parseBoolean(CharSequence input) {
        if (input != null) {
            switch (input.toString()) {
                case "true":
                case "TRUE":
                case "1":
                    return Boolean.TRUE;
                case "false":
                case "FALSE":
                case "0":
                    return Boolean.FALSE;
            }
        }
        return null;
    }

    public static Character parseCharacter(CharSequence input) {
        return input != null && input.length() == 1 ? input.charAt(0) : null;
    }

    public double[] parseDoubleArray(CharSequence input) {
        if (input != null) {
            String tmp = input.toString();
            try {
                int beginIndex = tmp.indexOf('[');
                int endIndex = tmp.lastIndexOf(']');
                if (beginIndex == -1 || endIndex == -1) {
                    return null;
                }
                String[] values = tmp.substring(beginIndex + 1, endIndex).split("\\s*,\\s*");
                double[] result = new double[values.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = Double.parseDouble(values[i].trim());
                }
                return result;
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public String[] parseStringArray(CharSequence input) {
        if (input != null) {
            String tmp = input.toString();
            try {
                int beginIndex = tmp.indexOf('[');
                int endIndex = tmp.lastIndexOf(']');
                if (beginIndex == -1 || endIndex == -1) {
                    return null;
                }
                String[] values = tmp.substring(beginIndex + 1, endIndex).split("\\s*,\\s*");
                String[] result = new String[values.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = values[i].trim();
                }
                return result;
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public Integer parseInteger(CharSequence input) {
        if (input != null) {
            try {
                return Integer.valueOf(input.toString());
            } catch (NumberFormatException ex) {
            }
        }
        return null;
    }

    public Long parseLong(CharSequence input) {
        if (input != null) {
            try {
                return Long.valueOf(input.toString());
            } catch (NumberFormatException ex) {
            }
        }
        return null;
    }

    public Double parseDouble(CharSequence input) {
        if (input != null) {
            try {
                return Double.valueOf(input.toString());
            } catch (NumberFormatException ex) {
            }
        }
        return null;
    }

    public Charset parseCharset(CharSequence input) {
        if (input != null) {
            try {
                return Charset.forName(input.toString());
            } catch (UnsupportedCharsetException ex) {
            }
        }
        return null;
    }

    public File parseFile(CharSequence input) {
        return input != null ? new File(input.toString()) : null;
    }

    public Date parseDate(DateFormat dateFormat, CharSequence input) {
        if (input != null) {
            String source = input.toString();
            ParsePosition pos = new ParsePosition(0);
            Date result = dateFormat.parse(source, pos);
            return pos.getIndex() == input.length() ? result : null;
        }
        return null;
    }

    public Number parseNumber(NumberFormat numberFormat, CharSequence input) {
        return input != null ? NumberFormats.parseAll(numberFormat, NumberFormats.simplify(numberFormat, input)) : null;
    }

    public <T extends Enum<T>> T parseEnum(Class<T> enumClass, CharSequence input) {
        if (input != null) {
            try {
                return Enum.valueOf(enumClass, input.toString());
            } catch (IllegalArgumentException ex) {
            }
        }
        return null;
    }

    public String parseString(CharSequence input) {
        return input != null ? input.toString() : null;
    }

    public <T> T parseConstant(T constant, CharSequence input) {
        return constant;
    }

    public <T> T parseNull(CharSequence input) {
        return null;
    }

    public List<String> parseStringList(Function<CharSequence, Stream<String>> splitter, CharSequence input) {
        return input != null ? splitter.apply(input).collect(Collectors.toList()) : null;
    }

    /**
     * <p>
     * Converts a String to a Locale.</p>
     *
     * <p>
     * This method takes the string format of a locale and creates the locale
     * object from it.</p>
     *
     * <pre>
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>
     * (#) The behaviour of the JDK variant constructor changed between JDK1.3
     * and JDK1.4. In JDK1.3, the constructor upper cases the variant, in
     * JDK1.4, it doesn't. Thus, the result from getVariant() may vary depending
     * on your JDK.</p>
     *
     * <p>
     * This method validates the input strictly. The language code must be
     * lowercase. The country code must be uppercase. The separator must be an
     * underscore. The length must be correct. </p>
     *
     * @param input the locale String to convert, null returns null
     * @return a Locale, null if invalid locale format
     * @see
     * http://www.java2s.com/Code/Java/Data-Type/ConvertsaStringtoaLocale.htm
     */
    public Locale parseLocale(CharSequence input) {
        if (input == null) {
            return null;
        }
        String str = input.toString();
        int len = str.length();
        if (len != 2 && len != 5 && len < 7) {
            return null;
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            return null;
        }
        if (len == 2) {
            return new Locale(str, "");
        } else {
            if (str.charAt(2) != '_') {
                return null;
            }
            char ch3 = str.charAt(3);
            if (ch3 == '_') {
                return new Locale(str.substring(0, 2), "", str.substring(4));
            }
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                return null;
            }
            if (len == 5) {
                return new Locale(str.substring(0, 2), str.substring(3, 5));
            } else {
                if (str.charAt(5) != '_') {
                    return null;
                }
                return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
            }
        }
    }
}
