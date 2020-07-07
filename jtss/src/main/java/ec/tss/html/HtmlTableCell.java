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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlTableCell {

    /**
     *
     */
    public final IHtmlElement core;

    // this value is never used when generating html
    @Deprecated
    public int width = 0;

    public int colspan = 1;
    public int rowspan = 1;

    /**
     *
     */
    @Nullable
    @Deprecated
    public HtmlStyle[] styles = null;

    private HtmlClass classnames = HtmlClass.NO_CLASS;

    public HtmlTableCell(String text) {
        this.core = new HtmlFragment(text);
    }

    public HtmlTableCell(IHtmlElement core) {
        this.core = core;
    }

    /**
     *
     * @param text
     * @param styles
     */
    @Deprecated
    public HtmlTableCell(String text, HtmlStyle... styles) {
        this.core = new HtmlFragment(text);
        this.styles = styles;
    }

    @Deprecated
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
    @Deprecated
    public HtmlTableCell(String text, int width, HtmlStyle... styles) {
        this.core = new HtmlFragment(text);
        this.width = width;
        this.styles = styles;
    }

    @Deprecated
    public HtmlTableCell(IHtmlElement core, int width, HtmlStyle... styles) {
        this.core = core;
        this.width = width;
        this.styles = styles;
    }

    public HtmlTableCell withClass(@NonNull HtmlClass classname) {
        this.classnames = this.classnames.with(classname);
        return this;
    }

    @Deprecated
    public HtmlTableCell withWidth(int width) {
        this.width = width;
        return this;
    }

    public HtmlTableCell withColSpan(int colSpan) {
        this.colspan = colSpan;
        return this;
    }

    public HtmlTableCell withRowSpan(int rowSpan) {
        this.rowspan = rowSpan;
        return this;
    }

    @NonNull
    HtmlClass getClassnames() {
        return classnames;
    }
}
