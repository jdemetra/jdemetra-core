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
public enum HtmlTag {

    /**
     *
     */
    HEADER1("h1"),
    /**
     * 
     */
    HEADER2("h2"),
    /**
     *
     */
    HEADER3("h3"),
    /**
     * 
     */
    HEADER4("h4"),
    /**
     *
     */
    HEADER5("h1"),
    /**
     * 
     */
    HEADER6(
    "h6"),
    /**
     *
     */
    LINEBREAK("hr"),
    /**
     * 
     */
    DIV("div"),
    /**
     * 
     */
    TABLE("table"),
    /**
     *
     */
    TABLEROW("tr"),
    /**
     * 
     */
    TABLEHEADER("th"),
    /**
     *
     */
    TABLECELL("td"),
    SUMMARY("summary")
    ;

    final String tag;

    private HtmlTag(String str) {
	tag = str;
    }
}
