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


package ec.tss.html;

/**
 * 
 * @author Kristof Bayens, Jean Palate
 */
public enum CssProperty {

    /**
     *
     */
    COLOR("color"),
    /**
     * 
     */
    ALIGN("text-align"),
    /**
     *
     */
    BACKGROUND_COLOR("background-color"),
    /**
     * 
     */
    BACKGROUND_IMAGE(
    "background-image"),
    /**
     *
     */
    BACKGROUND_REPEAT("background-repeat"),
    /**
     * 
     */
    FONT_FAMILY(
    "font-family"),
    /**
     *
     */
    FONT_STYLE("font-style"),
    /**
     *
     */
    FONT_SIZE("font-size"),
    /**
     * 
     */
    FONT_WEIGHT(
    "font-weight"),
    /**
     *
     */
    BORDER("border"),
    /**
     *
     */
    MARGIN_LEFT("margin-left"),
    /**
     * 
     */
    MARGIN_RIGHT(
    "margin-right"),
    /**
     *
     */
    TEXT_DECORATION("text-decoration");

    final String tag;

    private CssProperty(String str) {
	tag = str;
    }
}
