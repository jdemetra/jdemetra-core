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

package demetra.sa;

import demetra.design.Development;
import demetra.modelling.ComponentInformation;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.AllArgsConstructor
public class SeriesInfo implements Comparable<SeriesInfo> {
    
    private static final String EMPTY="";

    /**
     *
     */
    @lombok.NonNull
    private String name;
    /**
     *
     */
    @lombok.NonNull
    private ComponentType component;
    /**
     *
     */
    @lombok.NonNull
    private ComponentInformation info;
    /**
     *
     */
    @lombok.NonNull
    public String description;

    public static final String F_SUFFIX = "_f", E_SUFFIX = "_e", EF_SUFFIX = "_ef", 
            B_SUFFIX="_b";

    /**
     * 
     * @param name
     * @param type
     */
    public SeriesInfo(String name, ComponentType type) {
        this(name, type, ComponentInformation.Value, EMPTY);
    }

    @Override
    public int compareTo(SeriesInfo o) {
        if (this == o) {
            return 0;
        }
        int cmp = name.compareTo(o.name);
        if (cmp != 0) {
            return cmp;
        }
        cmp = component.compareTo(o.component);
        if (cmp != 0) {
            return cmp;
        }
        return info.compareTo(o.info);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (description != null && description.length() > 0) {
            builder.append(description);
        } else {
            builder.append(name);
        }

        if (info == ComponentInformation.Value
                || info == ComponentInformation.Undefined) {
            return builder.toString();
        } else {
            builder.append(" (");
            if (null != info) switch (info) {
                case Forecast:
                    builder.append("forecasts)");
                    break;
                case Stdev:
                    builder.append("std error)");
                    break;
                case StdevForecast:
                    builder.append("forecast errors)");
                    break;
                default:
                    break;
            }
            return builder.toString();
        }
    }

    public String getCode() {
        if (info == ComponentInformation.Undefined
                || info == ComponentInformation.Value) {
            return name;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        switch (info) {
            case Forecast:
                builder.append("_f");
                break;
            case Stdev:
                builder.append("_e");
                break;
            case StdevForecast:
                builder.append("_ef");
        }
        return builder.toString();
    }

    public static SeriesInfo split(String code) {
        int len = code.length();
        if (hasSuffix(code, F_SUFFIX)) {
            return new SeriesInfo(code.substring(0, len - 2), ComponentType.Undefined, ComponentInformation.Forecast, EMPTY);
        } else if (hasSuffix(code, E_SUFFIX)) {
            return new SeriesInfo(code.substring(0, len - 2), ComponentType.Undefined, ComponentInformation.Stdev, EMPTY);
        }
        if (hasSuffix(code, EF_SUFFIX)) {
            return new SeriesInfo(code.substring(0, len - 3), ComponentType.Undefined, ComponentInformation.StdevForecast, EMPTY);
        } else {
            return new SeriesInfo(code, ComponentType.Undefined);
        }
    }
    
    private static boolean hasSuffix(String s, String suffix) {
        int len = s.length(), slen = suffix.length();
        if (len <= slen) {
            return false;
        }
        for (int i = len - slen, j = 0; j < slen; ++i, ++j) {
            if (s.charAt(i) != suffix.charAt(j)) {
                return false;
            }
        }
        return true;
    }
}
