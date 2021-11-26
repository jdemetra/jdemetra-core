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
package demetra.html.core;

import demetra.data.Parameter;
import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlElement;
import demetra.html.HtmlFragment;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTag;
import demetra.information.InformationSet;
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

    public static interface HtmlFormatter<T> {

        HtmlElement format(T t);
    }
    public static final Map<Class, HtmlFormatter> map_ = new HashMap<>();

    public static synchronized <T> void register(Class<T> tclass, HtmlFormatter<T> fmt) {
        map_.put(tclass, fmt);
    }
    public static final HtmlFormatter defFormatter = new HtmlFormatter() {
        @Override
        public HtmlElement format(Object t) {
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
        register(String[].class, new HtmlFormatter<String[]>() {
            @Override
            public HtmlElement format(String[] t) {
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
        register(int[].class, new HtmlFormatter<int[]>() {
            @Override
            public HtmlElement format(int[] t) {
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
        register(double[].class, new HtmlFormatter<double[]>() {
            @Override
            public HtmlElement format(double[] t) {
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
        register(Parameter[].class, new HtmlFormatter<Parameter[]>() {
            @Override
            public HtmlElement format(Parameter[] t) {
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
        stream.open(new HtmlTable().withWidth(600));
        for (Entry<String, Class> entry : dictionary.entrySet()) {
            HtmlFormatter fmt = map_.get(entry.getValue());
            if (fmt == null) {
                fmt = defFormatter;
            }
            String s = entry.getKey();
            HtmlElement item = fmt.format(info_.search(s, Object.class));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(s).withWidth(200).withClass(Bootstrap4.TEXT_LEFT));
            stream.write(new HtmlTableCell(item).withWidth(400).withClass(Bootstrap4.TEXT_LEFT));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE).newLine();
    }
}
