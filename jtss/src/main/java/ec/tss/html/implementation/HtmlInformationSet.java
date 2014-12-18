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

package ec.tss.html.implementation;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlFragment;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.information.InformationSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Jean
 */
public class HtmlInformationSet extends AbstractHtmlElement {

    public static interface IHtmlFormatter<T> {

        IHtmlElement format(T t);
    }
    public static final Map<Class, IHtmlFormatter> map_ = new HashMap<>();

    public static synchronized <T> void register(Class<T> tclass, IHtmlFormatter<T> fmt) {
        map_.put(tclass, fmt);
    }
    public static final IHtmlFormatter defFormatter = new IHtmlFormatter() {
        @Override
        public IHtmlElement format(Object t) {
            if (t instanceof Object[]) {
                Object[] a = (Object[]) t;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < a.length; ++i) {
                    builder.append(a[i]);
                    if (i != a.length - 1) {
                        builder.append(", ");
                    }
                }
                return new HtmlFragment(builder.toString());
            } else {
                return new HtmlFragment(t.toString());
            }
        }
    };

    static {
        register(String[].class, new IHtmlFormatter<String[]>() {
            @Override
            public IHtmlElement format(String[] t) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < t.length; ++i) {
                    builder.append(t[i]);
                    if (i != t.length - 1) {
                        builder.append(", ");
                    }
                }
                return new HtmlFragment(builder.toString());
            }
        });
        register(int[].class, new IHtmlFormatter<int[]>() {
            @Override
            public IHtmlElement format(int[] t) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < t.length; ++i) {
                    builder.append(t[i]);
                    if (i != t.length - 1) {
                        builder.append(", ");
                    }
                }
                return new HtmlFragment(builder.toString());
            }
        });
        register(double[].class, new IHtmlFormatter<double[]>() {
            @Override
            public IHtmlElement format(double[] t) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < t.length; ++i) {
                    builder.append(t[i]);
                    if (i != t.length - 1) {
                        builder.append(", ");
                    }
                }
                return new HtmlFragment(builder.toString());
            }
        });
        register(Parameter[].class, new IHtmlFormatter<Parameter[]>() {
            @Override
            public IHtmlElement format(Parameter[] t) {
                StringBuilder builder = new StringBuilder();
                if (Parameter.isDefault(t)) {
                    builder.append(t.length).append(" coeff.");
                } else {
                    for (int i = 0; i < t.length; ++i) {
                        builder.append(t[i]);
                        if (i != t.length - 1) {
                            builder.append(", ");
                        }
                    }
                }
                return new HtmlFragment(builder.toString());
            }
        });
    }
    private final InformationSet info_;

    public HtmlInformationSet(InformationSet info) {
        info_ = info;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        Map<String, Class> dictionary = new LinkedHashMap<>();
        info_.fillDictionary(null, dictionary);
        stream.open(new HtmlTable(0, 600));
        for (Entry<String, Class> entry : dictionary.entrySet()) {
            IHtmlFormatter fmt = map_.get(entry.getValue());
            if (fmt == null) {
                fmt = defFormatter;
            }
            String s = entry.getKey();
            IHtmlElement item = fmt.format(info_.search(s, Object.class));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(s, 200, HtmlStyle.Left));
            stream.write(new HtmlTableCell(item, 400, HtmlStyle.Left));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE).newLine();
    }
}
