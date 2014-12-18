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

package ec.tstoolkit.modelling;

import ec.tstoolkit.design.Development;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SeriesInfo implements Comparable<SeriesInfo> {

    /**
     *
     */
    public final String name;
    /**
     *
     */
    public final ComponentType component;
    /**
     *
     */
    public final ComponentInformation info;
    /**
     *
     */
    public final boolean isSystem;
    /**
     *
     */
    public String description;
    /**
     *
     */
    public String comment;
    public static final String F_SUFFIX = "_f", E_SUFFIX = "_e", EF_SUFFIX = "_ef", 
            B_SUFFIX="_b";

    /**
     * 
     * @param name
     * @param type
     */
    public SeriesInfo(String name, ComponentType type) {
        this.name = name;
        this.component = type;
        this.description = null;
        this.info = ComponentInformation.Value;
        this.isSystem = false;
    }

    /**
     * 
     * @param name
     * @param desc
     * @param type
     */
    /**
     *
     * @param name
     * @param desc
     * @param type
     */
    public SeriesInfo(String name, String desc) {
        this.name = name;
        this.component = ComponentType.Undefined;
        this.description = desc;
        this.info = ComponentInformation.Value;
        this.isSystem = false;
    }

    public SeriesInfo(String name, String desc, ComponentType type) {
        this.name = name;
        this.component = type;
        this.description = desc;
        this.info = ComponentInformation.Value;
        this.isSystem = false;
    }

    SeriesInfo(String name, String desc, ComponentType type, boolean system) {
        this.name = name;
        this.component = type;
        this.description = desc;
        this.info = ComponentInformation.Value;
        this.isSystem = system;
    }

    /**
     *
     * @param name
     * @param description
     * @param type
     * @param step
     * @param info
     */
    public SeriesInfo(String name, String description, ComponentType type,
            ComponentInformation info) {
        this.name = name;
        this.component = type;
        this.description = description;
        this.info = info;
        this.isSystem = false;
    }

    SeriesInfo(String name, String description, ComponentType type,
            ComponentInformation info, boolean system) {
        this.name = name;
        this.component = type;
        this.description = description;
        this.info = info;
        this.isSystem = system;
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
            if (info == ComponentInformation.Forecast) {
                builder.append("forecasts)");
            } else if (info == ComponentInformation.Stdev) {
                builder.append("std error)");
            } else if (info == ComponentInformation.StdevForecast) {
                builder.append("forecast errors)");
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
            return new SeriesInfo(code.substring(0, len - 2), null, ComponentType.Undefined, ComponentInformation.Forecast);
        } else if (hasSuffix(code, E_SUFFIX)) {
            return new SeriesInfo(code.substring(0, len - 2), null, ComponentType.Undefined, ComponentInformation.Stdev);
        }
        if (hasSuffix(code, EF_SUFFIX)) {
            return new SeriesInfo(code.substring(0, len - 3), null, ComponentType.Undefined, ComponentInformation.StdevForecast);
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
