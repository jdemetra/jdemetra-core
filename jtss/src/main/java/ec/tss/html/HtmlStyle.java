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
 * These styles apply exclusively on text items.
 * 
 * @author Kristof Bayens
 * @author Mats Maggi
 */
public enum HtmlStyle {

    /**
     * HTML's default emphasis.
     */
    Bold("font-weight:bold"),
    Italic("font-style:italic"),
    Underline("text-decoration:underline"),

    /**
     * Alignment: Easily realign text to components.
     */
    Right("text-align:right"),
    Center("text-align:center"),
    Left("text-align:left"),
    
    /**
     * Basic colors.
     */
    Black("color: #000000"),
    Red("color: #FF0000"),
    DarkOrange("color: #FF8A30"),
    Orange("color: #ff9900"),
    Green("color: #32cd32"),
    Blue("color: #0000FF"),
    White("color: #FFFFFF"),
    Yellow("color: #FFCC00"),

    CustomDark("color: #323232"),
    CustomLight("color: #E6E6E6"),

    /**
     * Emphasis styles: convey meaning through color.
     * @see http://getbootstrap.com/css/#type-emphasis
     */
    Muted("color: #999999"), 
    Primary("color: #428bca"), 
    Success("color: #468847"), 
    Info("color: #3a87ad"), 
    Warning("color: #c09853"), 
    Danger("color: #b94a48");
    
    final String tag;

    private HtmlStyle(String str) {
	tag = str;
    }
}
