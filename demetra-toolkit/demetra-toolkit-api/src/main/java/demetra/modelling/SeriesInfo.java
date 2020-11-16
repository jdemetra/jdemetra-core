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
package demetra.modelling;

import nbbrd.design.Development;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
public class SeriesInfo implements Comparable<SeriesInfo> {
    

    /**
     * Name of the component
     */
    private String name;
    /**
     * Type of information
     */
    public ComponentInformation componentInformation;
    /**
     * Description of the component
     */
    public String description;
    public static final String F_SUFFIX = "_f", E_SUFFIX = "_e", EF_SUFFIX = "_ef",
            B_SUFFIX = "_b", EB_SUFFIX = "_eb";

    @Override
    public int compareTo(SeriesInfo o) {
        if (this == o) {
            return 0;
        }
        int cmp = name.compareTo(o.name);
        if (cmp != 0) {
            return cmp;
        }
        return componentInformation.compareTo(o.componentInformation);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (description != null && description.length() > 0) {
            builder.append(description);
        } else {
            builder.append(name);
        }

        if (componentInformation == ComponentInformation.Value
                || componentInformation == ComponentInformation.Undefined) {
            return builder.toString();
        } else {

            builder.append(" (");
            if (null != componentInformation) switch (componentInformation) {
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
        if (componentInformation == ComponentInformation.Undefined
                || componentInformation == ComponentInformation.Value) {
            return name;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        switch (componentInformation) {
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
            return new SeriesInfo(code.substring(0, len - 2), ComponentInformation.Forecast, null);
        } else if (hasSuffix(code, E_SUFFIX)) {
            return new SeriesInfo(code.substring(0, len - 2), ComponentInformation.Stdev, null);
        }
        if (hasSuffix(code, EF_SUFFIX)) {
            return new SeriesInfo(code.substring(0, len - 3), ComponentInformation.StdevForecast, null);
        } else {
            return new SeriesInfo(code, ComponentInformation.Value, null);
        }
    }

    private static boolean hasSuffix(String s, String suffix) {
        return s.endsWith(suffix);
    }
}
