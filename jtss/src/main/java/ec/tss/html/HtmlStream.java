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

import ec.tstoolkit.utilities.Arrays2;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Kristof Bayens
 * @author Jean Palate
 * @author Mats Maggi
 */
public class HtmlStream implements Closeable {

    /**
     *
     */
    public final Writer writer;

    /**
     *
     * @param writer
     */
    public HtmlStream(Writer writer) {
        this.writer = writer;
    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.writer.append("</body></html>");
    }

    /**
     *
     * @param html
     * @return
     * @throws IOException
     */
    public HtmlStream close(HtmlTag html) throws IOException {
        writer.write("</");
        writer.write(html.tag);
        writer.write('>');
        return this;
    }

    /**
     *
     * @return @throws IOException
     */
    public HtmlStream newLine() throws IOException {
        return write("<br>");
    }

    /**
     *
     * @param n
     * @return
     * @throws IOException
     */
    public HtmlStream newLines(int n) throws IOException {
        for (int i = 0; i < n; ++i) {
            newLine();
        }
        return this;
    }

    /**
     *
     * @throws IOException
     */
    public void open() throws IOException {
        this.writer.append("<html><body>");
    }

    /**
     *
     * @param table
     * @return
     * @throws IOException
     */
    public HtmlStream open(HtmlTable table) throws IOException {
        writer.write("<table");
        if (table.width != 0) {
            writer.write(" width=\'");
            writer.write(Integer.toString(table.width));
            writer.append('\'');
        }
        if (table.border != 0) {
            writer.write(" border=\'");
            writer.write(Integer.toString(table.border));
            writer.append('\'');
        }
        writer.write('>');
        return this;
    }

    /**
     *
     * @param html
     * @return
     * @throws IOException
     */
    public HtmlStream open(HtmlTag html) throws IOException {
        writer.write('<');
        writer.write(html.tag);
        writer.write('>');
        return this;
    }

    /**
     *
     * @param html
     * @return
     * @throws IOException
     */
    public HtmlStream open(HtmlTag html, String attribute, String val) throws IOException {
        writer.write('<');
        writer.write(html.tag);
        writer.write(' ');
        writer.write(attribute);
        writer.write("=\'");
        writer.write(val);
        writer.write("\'>");
        return this;
    }

    /**
     *
     * @param html
     * @return
     * @throws IOException
     */
    public HtmlStream open(HtmlTag html, Map<String, String> attributes) throws IOException {
        writer.write('<');
        writer.write(html.tag);
        for (Entry<String, String> entry : attributes.entrySet()) {
            writer.write(' ');
            writer.write(entry.getKey());
            writer.write("=\'");
            writer.write(entry.getValue());
            writer.append('\'');
        }
        writer.write('>');
        return this;
    }

    /**
     *
     * @param html
     * @param style
     * @return
     * @throws IOException
     */
    public HtmlStream open(HtmlTag html, CssStyle style) throws IOException {
        writer.write('<');
        writer.write(html.tag);
        style.write(this);
        writer.write('>');
        return this;
    }

    public HtmlStream open(HtmlTag html, CssStyle style, String attribute, String val) throws IOException {
        writer.write('<');
        writer.write(html.tag);
        style.write(this);
        writer.write(' ');
        writer.write(attribute);
        writer.write("=\'");
        writer.write(val);
        writer.write("'\'>");
        return this;
    }

    /**
     *
     * @param c
     * @return
     * @throws IOException
     */
    public HtmlStream write(char c) throws IOException {
        writer.write(c);
        return this;
    }

    /**
     *
     * @param d
     * @return
     * @throws IOException
     */
    public HtmlStream write(double d) throws IOException {
        writer.write(Double.toString(d));
        return this;
    }

    /**
     *
     * @param cell
     * @return
     * @throws IOException
     */
    public HtmlStream write(HtmlTableCell cell) throws IOException {
        return write(cell, null);
    }

    /**
     *
     * @param cell
     * @param cellBg
     * @return
     * @throws IOException
     */
    public HtmlStream write(HtmlTableCell cell, String cellBg) throws IOException {
        writer.write("<td");
        if (cellBg != null && !cellBg.isEmpty()) {
            writer.write(" bgcolor=\"" + cellBg + "\"");
        }

        if (cell.colspan > 1) {
            writer.write(" colspan=\"" + cell.colspan + "\"");
        }

        if (cell.rowspan > 1) {
            writer.write(" rowspan=\"" + cell.rowspan + "\"");
        }

        if (cellBg != null && !cellBg.isEmpty()) {
            writer.write(" bgcolor=\"" + cellBg + "\"");
        }

        if (cell.styles.length > 0) {
            writer.write(" style=\'");
            for (int i = 0; i < cell.styles.length; ++i) {
                writer.write(cell.styles[i].tag);
                if (i < cell.styles.length - 1) {
                    writer.write("; ");
                }
            }

            writer.write("\'");
        }
        writer.write('>');
        cell.core.write(this);
        writer.write("</td>");
        return this;
    }

    /**
     *
     * @param header
     * @return
     * @throws IOException
     */
    public HtmlStream write(HtmlTableHeader header) throws IOException {
        writer.write("<th");

        if (header.colspan > 1) {
            writer.write(" colspan=\"" + header.colspan + "\"");
        }

        if (header.rowspan > 1) {
            writer.write(" rowspan=\"" + header.rowspan + "\"");
        }

        if (header.styles.length > 0) {
            writer.write(" style=\'");
            for (int i = 0; i < header.styles.length; ++i) {
                writer.write(header.styles[i].tag);
                if (i < header.styles.length - 1) {
                    writer.write("; ");
                }
            }
            writer.write("\'");
        }
        writer.write('>');

        header.core.write(this);

        writer.write("</th>");
        return this;
    }

    /**
     *
     * @param html
     * @return
     * @throws IOException
     */
    public HtmlStream write(HtmlTag html) throws IOException {
        writer.write('<');
        writer.write(html.tag);
        writer.write(" />");
        return this;
    }

    /**
     *
     * @param html
     * @param style
     * @param text
     * @return
     * @throws IOException
     */
    public HtmlStream write(HtmlTag html, CssStyle style, String text)
            throws IOException {
        open(html, style);
        writer.write(text);
        close(html);
        return this;
    }

    /**
     *
     * @param html
     * @param text
     * @return
     * @throws IOException
     */
    public HtmlStream write(HtmlTag html, String text) throws IOException {
        open(html);
        writer.write(text);
        close(html);
        return this;
    }

    // public HTMLStream write(Object obj) throws IOException
    /**
     *
     * @param obj
     * @return
     * @throws IOException
     */
    public HtmlStream write(IHtmlElement obj) throws IOException {
        obj.write(this);
        // if (obj instanceof IHTMLElement)
        // {
        // IHTMLElement iobj = (IHTMLElement) obj;
        // iobj.write(this);
        // }
        // else
        // writer.write(obj.toString());
        return this;
    }

    /**
     *
     * @param i
     * @return
     * @throws IOException
     */
    public HtmlStream write(int i) throws IOException {
        writer.write(Integer.toString(i));
        return this;
    }

    /**
     *
     * @param txt
     * @return
     * @throws IOException
     */
    public HtmlStream write(String txt) throws IOException {
        writer.write(txt);
        return this;
    }

    /**
     *
     * @param txt
     * @param styles
     * @return
     * @throws IOException
     */
    public HtmlStream write(String txt, HtmlStyle... styles) throws IOException {
        if (Arrays2.isNullOrEmpty(styles)) {
            return write(txt);
        }
        writer.write("<span");
        writer.write(" style=\'");
        for (int i = 0; i < styles.length; ++i) {
            writer.write(styles[i].tag);
            writer.write("; ");
        }
        writer.write("\'>");
        writer.write(txt);
        writer.write("</span>");
        return this;
    }
}
