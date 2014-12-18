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
 * @author Kristof Bayens
 */
public class HtmlTableCell {

    /**
     *
     */
    public final IHtmlElement core;

    /**
     *
     */
    public int width = 0;
    public int colspan = 1;
    public int rowspan = 1;

    /**
     *
     */
    public HtmlStyle[] styles = new HtmlStyle[]{};

    /**
     *
     * @param text
     * @param styles
     */
    public HtmlTableCell(String text, HtmlStyle... styles) {
        this.core = new HtmlFragment(text);
        this.styles = styles;
    }

    public HtmlTableCell(IHtmlElement core, HtmlStyle... styles) {
        this.core = core;
        this.styles = styles;
    }

    /**
     *
     * @param text
     * @param width
     * @param styles
     */
    public HtmlTableCell(String text, int width, HtmlStyle... styles) {
        this.core = new HtmlFragment(text);
        this.width = width;
        this.styles = styles;
    }

    public HtmlTableCell(IHtmlElement core, int width, HtmlStyle... styles) {
        this.core = core;
        this.width = width;
        this.styles = styles;
    }

}
