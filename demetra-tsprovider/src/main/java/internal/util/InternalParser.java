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

import demetra.util.Parser;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalParser {

    public Boolean parseBoolean(CharSequence input) {
        switch (input.toString()) {
            case "true":
            case "TRUE":
            case "1":
                return Boolean.TRUE;
            case "false":
            case "FALSE":
            case "0":
                return Boolean.FALSE;
            default:
                return null;
        }
    }

    public static Character parseCharacter(CharSequence input) {
        return input.length() == 1 ? input.charAt(0) : null;
    }

    public double[] parseDoubleArray(CharSequence input) {
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
            return null;
        }
    }

    public String[] parseStringArray(CharSequence input) {
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
            return null;
        }
    }

    public Integer parseInteger(CharSequence input) {
        try {
            return Integer.valueOf(input.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Long parseLong(CharSequence input) {
        try {
            return Long.valueOf(input.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Double parseDouble(CharSequence input) {
        try {
            return Double.valueOf(input.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Charset parseCharset(CharSequence input) {
        try {
            return Charset.forName(input.toString());
        } catch (UnsupportedCharsetException ex) {
            return null;
        }
    }

    public File parseFile(CharSequence input) {
        return new File(input.toString());
    }

    public <T> T unmarshal(Unmarshaller unmarshaller, CharSequence input) {
        try {
            return (T) unmarshaller.unmarshal(new StringReader(input.toString()));
        } catch (JAXBException ex) {
            return null;
        }
    }

    public <T> Unmarshaller newUnmarshaller(Class<T> classToBeParsed) {
        try {
            return JAXBContext.newInstance(classToBeParsed).createUnmarshaller();
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Unmarshaller newUnmarshaller(JAXBContext context) {
        try {
            return context.createUnmarshaller();
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Date parseDate(DateFormat dateFormat, CharSequence input) {
        try {
            return dateFormat.parse(input.toString());
        } catch (ParseException ex) {
            return null;
        }
    }

    public Number parseNumber(NumberFormat numberFormat, CharSequence input) {
        try {
            return numberFormat.parse(input.toString());
        } catch (ParseException ex) {
            return null;
        }
    }

    public <T extends Enum<T>> T parseEnum(Class<T> enumClass, CharSequence input) {
        try {
            return Enum.valueOf(enumClass, input.toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public <T> T parseConstant(T constant, CharSequence input) {
        Objects.requireNonNull(input);
        return constant;
    }

    public <T> T parseNull(CharSequence input) {
        Objects.requireNonNull(input);
        return null;
    }

    public List<String> parseStringList(Function<CharSequence, Stream<String>> splitter, CharSequence input) {
        Objects.requireNonNull(input);
        return splitter.apply(input).collect(Collectors.toList());
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
     * @throws IllegalArgumentException if the string is an invalid format
     * @see
     * http://www.java2s.com/Code/Java/Data-Type/ConvertsaStringtoaLocale.htm
     */
    public Locale parseLocale(CharSequence input) {
        Objects.requireNonNull(input);
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

    public static final class StrictDatePatternParser implements Parser<Date> {

        private final DateFormat dateFormat;

        public StrictDatePatternParser(String datePattern, Locale locale) {
            this.dateFormat = new SimpleDateFormat(datePattern, locale);
            dateFormat.setLenient(false);
        }

        @Override
        public Date parse(CharSequence input) {
            String inputAsString = input.toString();
            try {
                Date result = dateFormat.parse(inputAsString);
                return result != null && inputAsString.equals(dateFormat.format(result)) ? result : null;
            } catch (ParseException ex) {
                return null;
            }
        }
    }
}
