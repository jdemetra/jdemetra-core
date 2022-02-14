/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package demetra.information.formatters;

import demetra.arima.SarimaOrders;
import demetra.data.Parameter;
import demetra.information.Explorable;
import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.math.Complex;
import demetra.processing.ProcDiagnostic;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.RegressionItem;
import demetra.util.MultiLineNameUtil;
import demetra.util.NamedObject;
import demetra.util.WildCards;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 */
@lombok.experimental.UtilityClass
public class CsvInformationFormatter {

    private final HashMap<Type, InformationFormatter> DICTIONARY = new HashMap<>();
    private final String NEWLINE = System.lineSeparator();
    private volatile Character csvSeparator;

    static {
        DecimalFormat fmt = (DecimalFormat) DecimalFormat.getNumberInstance();
        fmt.setMaximumFractionDigits(BasicConfiguration.getFractionDigits());
        fmt.setGroupingUsed(false);
        char sep = fmt.getDecimalFormatSymbols().getDecimalSeparator();
        if (sep == ',') {
            csvSeparator = ';';
        } else {
            csvSeparator = ',';
        }

        DICTIONARY.put(double.class, new DoubleFormatter());
        DICTIONARY.put(int.class, new IntegerFormatter());
        DICTIONARY.put(long.class, new LongFormatter());
        DICTIONARY.put(boolean.class, new BooleanFormatter("1", "0"));
        DICTIONARY.put(Double.class, new DoubleFormatter());
        DICTIONARY.put(Integer.class, new IntegerFormatter());
        DICTIONARY.put(Long.class, new LongFormatter());
        DICTIONARY.put(Boolean.class, new BooleanFormatter("1", "0"));
        DICTIONARY.put(Complex.class, new ComplexFormatter());
        DICTIONARY.put(String.class, new StringFormatter());
        DICTIONARY.put(SarimaOrders.class, new SarimaFormatter());
        DICTIONARY.put(Parameter.class, new ParameterFormatter());
        DICTIONARY.put(TsPeriod.class, new PeriodFormatter());
        DICTIONARY.put(RegressionItem.class, new RegressionItemFormatter(true));
        DICTIONARY.put(StatisticalTest.class, new StatisticalTestFormatter());
        DICTIONARY.put(ProcDiagnostic.class, new DiagnosticFormatter());
    }

    public char getCsvSeparator() {
        return csvSeparator;
    }

    public void setCsvSeparator(Character c) {
        csvSeparator = c;
    }

    public Set<Type> formattedTypes(){
        return DICTIONARY.keySet();
    }

    // preparing the matrix:
    // for each record, for each name, we search for the length of an item, the actual items (in case of
    // wildcards) and the corresponding result
    private static class MatrixItem {

        private static final Object[] EMPTY = new Object[0];
        private static final String[] SEMPTY = new String[0];

        int length;
        String[] items;
        Object[] results = EMPTY;

        boolean isHomogeneous() {
            if (results.length <= 1) {
                return true;
            }
            Class c = null;
            for (int i = 0; i < results.length; ++i) {
                if (results[i] != null) {
                    if (c == null) {
                        c = results[i].getClass();
                    } else if (!results[i].getClass().equals(c)) {
                        return false;
                    }
                }
            }
            return true;
        }

        void fill(final String id, InformationSet record, boolean shortname) {
            int l = id.indexOf(':');
            String sid = id;
            if (l >= 0) {
                sid = id.substring(0, l);
                String s1 = id.substring(l + 1);
                try {
                    length = Integer.parseInt(s1);
                } catch (Exception ex) {
                    length = 1;
                }
            }
            if (WildCards.hasWildCards(sid)) {
                List<Information<Object>> sel = record.select(sid);
                if (!sel.isEmpty()) {
                    int n = sel.size();
                    results = new Object[n];
                    items = new String[n];
                    for (int i = 0; i < n; ++i) {
                        Information<Object> cur = sel.get(i);
                        results[i] = record.search(cur.getName(), Object.class);
                        items[i] = shortId(cur.getName(), shortname);
                    }
                    if (length == 0 && isHomogeneous()) {
                        updateLength();
                    }
                } else {
                    results = EMPTY;
                    items = SEMPTY;
                }
            } else {
                results = new Object[]{record.search(sid, Object.class)};
                items = new String[]{shortId(sid, shortname)};
                if (length == 0 && results[0] != null) {
                    updateLength();
                }
            }
        }

        void fill(final String id, Explorable record, boolean shortname) {
            if (record == null) {
                results = EMPTY;
                items = SEMPTY;
                return;
            }
            // we search for a pre-specified length
            int l = id.indexOf(':');
            String sid = id;
            if (l >= 0) {
                sid = id.substring(0, l);
                String s1 = id.substring(l + 1);
                try {
                    length = Integer.parseInt(s1);
                } catch (Exception ex) {
                    length = 1;
                }
            }
            // request with wild cards
            if (WildCards.hasWildCards(sid)) {
                try{
                Map<String, Object> sel = record.searchAll(sid, Object.class);
                if (!sel.isEmpty()) {
                    List<String> ids = new ArrayList<>();
                    List<Object> objs = new ArrayList<>();
                    sel.forEach((s, o) -> {
                        if (o != null) {
                            ids.add(shortId(s, shortname));
                            objs.add(o);
                        }
                    });
                    // update unspecified length
                    int n = ids.size();
                    results = objs.toArray(EMPTY);
                    items = ids.toArray(SEMPTY);

                    if (length == 0 && isHomogeneous()) {
                        updateLength();
                    }
                } else {
                    results = EMPTY;
                    items = SEMPTY;
                }
                }catch(Exception ex){
                    results = EMPTY;
                    items = SEMPTY;
                    
                }
            } else {
                try{
                results = new Object[]{record.getData(sid, Object.class)};
                items = new String[]{shortId(sid, shortname)};
                if (length == 0 && results[0] != null) {
                    updateLength();
                }
               }catch(Exception ex){
                    results = EMPTY;
                    items = SEMPTY;
                    
                }
            }
        }

        void updateLength() {
            if (results.length == 0) {
                return;
            }
            InformationFormatter fmt = DICTIONARY.get(results[0].getClass());
            if (fmt != null) {
                length = fmt.getDefaultRepresentationLength();
            } else {
                length = 1;
            }
        }

        String shortId(String id, boolean shortname) {
            if (!shortname) {
                return id;
            } else {
                int last = id.lastIndexOf(InformationSet.SEP);
                if (last < 0) {
                    return id;
                } else {
                    return id.substring(last + 1);
                }
            }
        }

        void fillDictionary(Set<String> ids) {
            for (int i = 0; i < items.length; ++i) {
                ids.add(items[i]);
            }
        }

        Object search(String id) {
            for (int i = 0; i < items.length; ++i) {
                if (items[i].equals(id)) {
                    return results[i];
                }
            }
            return null;
        }
    }

    public void format(Writer writer, List<InformationSet> records, List<String> names, boolean shortname) {
        // STEP 1: we retrieve all information for all records/names
        List<MatrixItem[]> items = new ArrayList<>();
        LinkedHashSet<String> dic = new LinkedHashSet<>();
        records.forEach(record -> {
            MatrixItem[] m = new MatrixItem[names.size()];
            for (int i = 0; i < m.length; ++i) {
                m[i] = new MatrixItem();
                m[i].fill(names.get(i), record, shortname);
                m[i].fillDictionary(dic);
                items.add(m);
            }
        });
        String[] nnames = dic.toArray(new String[dic.size()]);
        format(writer, items, names.size(), null, false);
    }

    private void format(Writer writer, List<MatrixItem[]> items, int nnames, List<String> rowheaders, boolean fullName) {
        // STEP 2: for each name, we find the set of items/length
        List<LinkedHashMap<String, Integer>> wnames = new ArrayList<>();
        for (int cur = 0; cur < nnames; ++cur) {
            LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
            for (MatrixItem[] mis : items) {
                MatrixItem m = mis[cur];
                if (m.items != null) {
                    for (int j = 0; j < m.items.length; ++j) {
                        Integer l = map.get(m.items[j]);
                        if (l == null || l < m.length) {
                            map.put(m.items[j], m.length);
                        }
                    }
                }
            }
            wnames.add(map);
        }
        // STEP 3: write the output
        try {
            // columns headers
            if (rowheaders != null) {
                writer.write(csvSeparator);
            }
            writeColumnsHeaders(writer, wnames, nnames);
            int cur = 0;
            for (MatrixItem[] item : items) {
                if (rowheaders != null) {
                    String rh = rowheaders.get(cur++);
                    if (rh != null) {
                        writeHeader(writer, rh, fullName);
                    }
                    writer.write(csvSeparator);
                }
                writeLine(writer, item, wnames);
            }
            writer.close();
        } catch (IOException ex) {
            String msg = ex.getMessage();
        }
    }

    private void writeLine(Writer writer, MatrixItem[] item, List<LinkedHashMap<String, Integer>> wnames) throws IOException {
        for (int k = 0; k < item.length;) {
            final MatrixItem citem = item[k];
            LinkedHashMap<String, Integer> map = wnames.get(k);
            int nmax = map.size();
            int i = 0;
            for (Entry<String, Integer> ccur : map.entrySet()) {
                String c = ccur.getKey();
                int n = ccur.getValue();
                Object obj = citem.search(c);
                if (obj != null) {
                    if (n == 1) {
                        write(writer, format(obj, 0));
                    } else {
                        for (int j = 1; j <= n; ++j) {
                            write(writer, format(obj, j));
                            if (j < n) {
                                writer.write(csvSeparator);
                            }
                        }
                    }
                } else {
                    for (int j = 1; j < n; ++j) {
                        writer.write(csvSeparator);
                    }
                }
                if (++i < nmax) {
                    writer.write(csvSeparator);
                }
            }
            if (++k < item.length) {
                writer.write(csvSeparator);
            } else {
                writer.write(NEWLINE);
            }
        }
    }

    private void writeColumnsHeaders(Writer writer, List<LinkedHashMap<String, Integer>> wnames, int n) throws IOException {
        int cur = 0;
        for (LinkedHashMap<String, Integer> map : wnames) {
            int ncur = 0;
            int nmax = map.size();
            for (Entry<String, Integer> entry : map.entrySet()) {
                String c = entry.getKey();
                int i = entry.getValue();
                try {
                    write(writer, c);
                    for (int j = 1; j < i; ++j) {
                        writer.write(csvSeparator);
                    }
                    if (++ncur < nmax) {
                        writer.write(csvSeparator);
                    }
                } catch (IOException ex) {
                }
            }
            if (++cur < n) {
                writer.write(csvSeparator);
            } else {
                writer.write(NEWLINE);
            }
        }

    }

    public void formatResults(Writer writer, List<NamedObject<Explorable>> records, List<String> names, boolean shortColName, boolean fullRowName) {
        // STEP 1: we retrieve all information for all records/names
        List<MatrixItem[]> items = new ArrayList<>();
        LinkedHashSet<String> dic = new LinkedHashSet<>();
        List<String> rowheaders = new ArrayList<>();
        records.forEach(record -> {
            MatrixItem[] m = new MatrixItem[names.size()];
            for (int i = 0; i < m.length; ++i) {
                m[i] = new MatrixItem();
                m[i].fill(names.get(i), record.getObject(), shortColName);
                m[i].fillDictionary(dic);
            }
            items.add(m);
            rowheaders.add(record.getName());
        });
        String[] nnames = dic.toArray(new String[dic.size()]);
        format(writer, items, names.size(), rowheaders, fullRowName);
    }

    private String format(Object obj, int item) {

        try {
            InformationFormatter fmt = DICTIONARY.get(obj.getClass());
            if (fmt != null) {
                return fmt.format(obj, item);
            } else if (item == 0) {
                return obj.toString();
            } else {
                return "";
            }
        } catch (Exception ex) {
            String msg = ex.getMessage();
            return "";
        }
    }

    private void writeHeader(Writer writer, String txt, boolean fullName) throws IOException {

        if (txt == null) {
            return;
        }
        if (fullName) {
            txt = MultiLineNameUtil.join(txt, " * ");
        } else {
            txt = MultiLineNameUtil.last(txt);
        }
        txt = StringFormatter.cleanup(txt);

        if (txt.indexOf(csvSeparator) >= 0) {
            if (txt.indexOf('\"') >= 0) {
                writer.write("\"\"");
                writer.write(txt);
                writer.write("\"\"");
            } else {
                writer.write('\"');
                writer.write(txt);
                writer.write('\"');
            }
        } else if (txt.indexOf('\"') >= 0) {
            writer.write("\"\"");
            writer.write(txt);
            writer.write("\"\"");
        } else {
            writer.write(txt);
        }
    }

    private void write(Writer writer, String txt) throws IOException {

        if (txt == null) {
            return;
        }

        if (txt.indexOf(csvSeparator) >= 0) {
            if (txt.indexOf('\"') >= 0) {
                writer.write("\"\"");
                writer.write(txt);
                writer.write("\"\"");
            } else {
                writer.write('\"');
                writer.write(txt);
                writer.write('\"');
            }
        } else if (txt.indexOf('\"') >= 0) {
            writer.write("\"\"");
            writer.write(txt);
            writer.write("\"\"");
        } else {
            writer.write(txt);
        }
    }
}
