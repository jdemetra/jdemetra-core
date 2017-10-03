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
package ec.tss.html;

import static ec.tss.html.HtmlClass.of;

/**
 * https://getbootstrap.com/docs/4.0
 *
 * @author Philippe Charles
 */
public final class Bootstrap4 {

    public static final HtmlClass TEXT_LEFT = of("text-left");
    public static final HtmlClass TEXT_CENTER = of("text-center");
    public static final HtmlClass TEXT_RIGHT = of("text-right");

    public static final HtmlClass TEXT_PRIMARY = of("text-primary");
    public static final HtmlClass TEXT_SECONDARY = of("text-secondary");
    public static final HtmlClass TEXT_SUCCESS = of("text-success");
    public static final HtmlClass TEXT_DANGER = of("text-danger");
    public static final HtmlClass TEXT_WARNING = of("text-warning");
    public static final HtmlClass TEXT_INFO = of("text-info");
    public static final HtmlClass TEXT_LIGHT = of("text-light");
    public static final HtmlClass TEXT_DARK = of("text-dark");
    public static final HtmlClass TEXT_WHITE = of("text-white");

    public static final HtmlClass BG_PRIMARY = of("bg-primary");
    public static final HtmlClass BG_SECONDARY = of("bg-secondary");
    public static final HtmlClass BG_SUCCESS = of("bg-success");
    public static final HtmlClass BG_DANGER = of("bg-danger");
    public static final HtmlClass BG_WARNING = of("bg-warning");
    public static final HtmlClass BG_INFO = of("bg-info");
    public static final HtmlClass BG_LIGHT = of("bg-light");
    public static final HtmlClass BG_DARK = of("bg-dark");
    public static final HtmlClass BG_WHITE = of("bg-white");

    public static final HtmlClass FONT_WEIGHT_BOLD = of("font-weight-bold");
    public static final HtmlClass FONT_WEIGHT_NORMAL = of("font-weight-normal");
    public static final HtmlClass FONT_ITALIC = of("font-italic");

    private Bootstrap4() {
        // static class
    }
}
