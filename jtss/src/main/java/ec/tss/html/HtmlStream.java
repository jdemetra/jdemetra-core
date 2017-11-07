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

import com.google.common.base.Strings;
import static ec.tss.html.HtmlTag.TABLE;
import static ec.tss.html.HtmlTag.TABLECELL;
import static ec.tss.html.HtmlTag.TABLEHEADER;
import ec.tstoolkit.utilities.Arrays2;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 *
 * @author Kristof Bayens
 * @author Jean Palate
 * @author Mats Maggi
 */
public class HtmlStream implements Closeable {

    public final Writer writer;

    public HtmlStream(Writer writer) {
        this.writer = writer;
    }

    public void open() throws IOException {
        this.writer.append("<html><body>");
    }

    @Override
    public void close() throws IOException {
        this.writer.append("</body></html>");
    }

    public HtmlStream newLine() throws IOException {
        writer.write("<br/>");
        return this;
    }

    public HtmlStream newLines(int n) throws IOException {
        for (int i = 0; i < n; ++i) {
            newLine();
        }
        return this;
    }

    public HtmlStream open(HtmlTag tag) throws IOException {
        return open(tag, HtmlClass.NO_CLASS);
    }

    public HtmlStream open(HtmlTag tag, HtmlClass classNames) throws IOException {
        writeBeg();
        writeTag(tag);
        writeAttr(CLASS_ATTR, classNames, HtmlClass::isEmpty);
        writeEnd();
        return this;
    }

    public HtmlStream close(HtmlTag tag) throws IOException {
        writeBeg();
        writeSlash();
        writeTag(tag);
        writeEnd();
        return this;
    }

    public HtmlStream write(HtmlTag tag) throws IOException {
        writeBeg();
        writeTag(tag);
        writeSlash();
        writeEnd();
        return this;
    }

    public HtmlStream write(HtmlTag tag, String text) throws IOException {
        return open(tag).write(text).close(tag);
    }

    public HtmlStream write(HtmlTag tag, String text, HtmlClass classNames) throws IOException {
        return open(tag, classNames).write(text).close(tag);
    }

    public HtmlStream write(String text, HtmlClass classNames) throws IOException {
        return classNames.isEmpty() ? write(text) : write(HtmlTag.SPAN, text, classNames);
    }

    public HtmlStream write(char c) throws IOException {
        writer.write(c);
        return this;
    }

    public HtmlStream write(double d) throws IOException {
        writeText(Double.toString(d));
        return this;
    }

    public HtmlStream write(int i) throws IOException {
        writeText(Integer.toString(i));
        return this;
    }

    public HtmlStream write(String txt) throws IOException {
        writeText(txt);
        return this;
    }

    public HtmlStream open(HtmlTable table) throws IOException {
        writeBeg();
        writeTag(TABLE);
//        writeAttr(WIDTH_ATTR, table.width, o -> o == 0);
//        writeAttr(BORDER_ATTR, table.border, o -> o == 0);
        writeEnd();
        return this;
    }

    public HtmlStream write(HtmlTableCell cell) throws IOException {
        writeBeg();
        writeTag(TABLECELL);
        writeAttr(COLSPAN_ATTR, cell.colspan, HtmlStream::isDefaultSpan);
        writeAttr(ROWSPAN_ATTR, cell.rowspan, HtmlStream::isDefaultSpan);
        writeAttr(CLASS_ATTR, cell.getClassnames(), HtmlClass::isEmpty);
        writeStyleAttribute(cell.styles);
        writeEnd();

        cell.core.write(this);

        writeBeg();
        writeSlash();
        writeTag(TABLECELL);
        writeEnd();
        return this;
    }

    public HtmlStream write(HtmlTableHeader header) throws IOException {
        writeBeg();
        writeTag(TABLEHEADER);
        writeAttr(COLSPAN_ATTR, header.colspan, HtmlStream::isDefaultSpan);
        writeAttr(ROWSPAN_ATTR, header.rowspan, HtmlStream::isDefaultSpan);
        writeAttr(CLASS_ATTR, header.getClassnames(), HtmlClass::isEmpty);
        writeStyleAttribute(header.styles);
        writeEnd();

        header.core.write(this);

        writeBeg();
        writeSlash();
        writeTag(TABLEHEADER);
        writeEnd();
        return this;
    }

    public HtmlStream write(IHtmlElement obj) throws IOException {
        obj.write(this);
        return this;
    }

    @Deprecated
    public HtmlStream write(String text, HtmlStyle... styles) throws IOException {
        if (Arrays2.isNullOrEmpty(styles)) {
            return write(text);
        }
        writer.write("<span");
        writeStyleAttribute(styles);
        writer.write(">");
        writeText(text);
        writer.write("</span>");
        return this;
    }

    @Deprecated
    public HtmlStream open(HtmlTag tag, String attrName, String attrValue) throws IOException {
        writeBeg();
        writeTag(tag);
        writeAttr(attrName, attrValue, String::isEmpty);
        writeEnd();
        return this;
    }

    @Deprecated
    public HtmlStream open(HtmlTag tag, CssStyle style) throws IOException {
        writeBeg();
        writeTag(tag);
        writeStyle(style);
        writeEnd();
        return this;
    }

    @Deprecated
    public HtmlStream open(HtmlTag tag, CssStyle style, String attrName, String attrValue) throws IOException {
        writeBeg();
        writeTag(tag);
        writeStyle(style);
        writeAttr(attrName, attrValue, String::isEmpty);
        writeEnd();
        return this;
    }

    @Deprecated
    public HtmlStream open(HtmlTag tag, Map<String, String> attributes) throws IOException {
        writeBeg();
        writeTag(tag);
        for (Entry<String, String> entry : attributes.entrySet()) {
            writeAttr(entry.getKey(), entry.getValue(), String::isEmpty);
        }
        writeEnd();
        return this;
    }

    @Deprecated
    public HtmlStream write(HtmlTag tag, CssStyle style, String text) throws IOException {
        open(tag, style);
        writeText(text);
        close(tag);
        return this;
    }

    @Deprecated
    public HtmlStream write(HtmlTableCell cell, String cellBg) throws IOException {
        writeBeg();
        writeTag(TABLECELL);
        writeAttr(COLSPAN_ATTR, cell.colspan, o -> o <= 1);
        writeAttr(ROWSPAN_ATTR, cell.rowspan, o -> o <= 1);
        writeAttr(BGCOLOR_ATTR, cellBg, Strings::isNullOrEmpty);
        writeAttr(CLASS_ATTR, cell.getClassnames(), HtmlClass::isEmpty);
        writeStyleAttribute(cell.styles);
        writeEnd();

        cell.core.write(this);

        writeBeg();
        writeSlash();
        writeTag(TABLECELL);
        writeEnd();
        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private void writeBeg() throws IOException {
        writer.append('<');
    }

    private void writeEnd() throws IOException {
        writer.append('>');
    }

    private void writeSlash() throws IOException {
        writer.append('/');
    }

    private void writeTag(HtmlTag tag) throws IOException {
        writer.append(tag.tag);
    }

    private void writeText(String text) throws IOException {
        writer.append(text);
    }

    private void writeStyle(CssStyle style) throws IOException {
        style.write(this);
    }

    private void writeStyleAttribute(@Nullable HtmlStyle[] styles) throws IOException {
        if (styles != null && styles.length > 0) {
            writeAttr(STYLE_ATTR, Stream.of(styles).map(o -> o.tag).collect(Collectors.joining("; ")), String::isEmpty);
        }
    }

    private <T> void writeAttr(String name, T value, Predicate<? super T> isDefaultValue) throws IOException {
        if (isDefaultValue.test(value)) {
            return;
        }
        writer.write(' ');
        writer.write(name);
        writer.write("=\"");
        writer.write(value.toString());
        writer.write("\"");
    }

    private static boolean isDefaultSpan(int span) {
        return span <= 1;
    }

    private static final String CLASS_ATTR = "class";
    private static final String STYLE_ATTR = "style";
    private static final String WIDTH_ATTR = "width";
    private static final String BORDER_ATTR = "border";
    private static final String COLSPAN_ATTR = "colspan";
    private static final String ROWSPAN_ATTR = "rowspan";
    private static final String BGCOLOR_ATTR = "bgcolor";
    //</editor-fold>
}
