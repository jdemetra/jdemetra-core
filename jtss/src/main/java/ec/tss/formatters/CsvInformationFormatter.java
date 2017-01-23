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
package ec.tss.formatters;

import ec.tss.TsMoniker;
import ec.tss.sa.output.BasicConfiguration;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcDiagnostic;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.NamedObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class CsvInformationFormatter {

    private static final HashMap<Type, IStringFormatter> dictionary = new HashMap<>();
    private final char comma;
    private static final String newLine = System.lineSeparator();
    private boolean fullName;

    public void setFullName(boolean fullName) {
        this.fullName = fullName;
    }

    public CsvInformationFormatter() {

        comma = BasicConfiguration.getCsvSeparator();
        DecimalFormat fmt = (DecimalFormat) DecimalFormat.getNumberInstance();
        fmt.setMaximumFractionDigits(BasicConfiguration.getFractionDigits());
        fmt.setGroupingUsed(false);

        dictionary.put(double.class, new DoubleFormatter());
        dictionary.put(int.class, new IntegerFormatter());
        dictionary.put(long.class, new LongFormatter());
        dictionary.put(boolean.class, new BooleanFormatter("1", "0"));
        dictionary.put(Double.class, new DoubleFormatter());
        dictionary.put(Integer.class, new IntegerFormatter());
        dictionary.put(Long.class, new LongFormatter());
        dictionary.put(Boolean.class, new BooleanFormatter("1", "0"));
        dictionary.put(String.class, new StringFormatter());
        dictionary.put(SarimaModel.class, new SarimaFormatter());
        dictionary.put(Parameter.class, new ParameterFormatter());
        dictionary.put(TsMoniker.class, new MonikerFormatter());
        dictionary.put(TsPeriod.class, new PeriodFormatter());
        dictionary.put(RegressionItem.class, new RegressionItemFormatter(true));
        dictionary.put(StatisticalTest.class, new StatisticalTestFormatter());
        dictionary.put(ProcDiagnostic.class, new DiagnosticFormatter());
    }
    
    // preparing the matrix:
    // for each record, for each name, we search for the length of an item, the actual items (in case of
    // wildcards) and the corresponding result
    private static class MatrixItem
    {
        private static final Object[] EMPTY=new Object[0], SEMPTY=new String[0];
        
        int length;
        String name;
        String[] items;
        Object[] results=EMPTY;
        
        boolean isHomogeneous(){
            if ( results.length <= 1)
                return true;
            Class c=results[0].getClass();
            for (int i=1; i<results.length; ++i)
                if (!results[i].getClass().equals(c))
                    return false;
            return true;
        }
        
        void fill(final String id, InformationSet record, boolean shortname){
            int l = id.indexOf(':');
            length=1;
            String sid=id;
            if (l >= 0) {
                sid = id.substring(0, l);
                String s1 = id.substring(l + 1);
                int w = 0;
                try {
                    length = Integer.parseInt(s1);
                } catch (Exception ex) {
                }
            }
            List<Information<Object>> sel = record.select(sid);
            if (sel.isEmpty()){
                
            }
        }
    }

    public void format(Writer writer, List<InformationSet> records, List<String> names, boolean shortname) {

        List<String[]> snames = new ArrayList<>();
        for (String name : names) {
            snames.add(InformationSet.split(name));
        }
        int[] witems = new int[snames.size()];
        int icur = 0;
        for (String[] sname : snames) {

            String slast = sname[sname.length - 1];
            int l = slast.indexOf(':');
            witems[icur] = 1;
            if (l >= 0) {

                String s0 = slast.substring(0, l);
                String s1 = slast.substring(l + 1);
                int w = 0;
                try {
                    w = Integer.parseInt(s1);
                    sname[sname.length - 1] = s0;
                    witems[icur] = w;
                } catch (Exception ex) {
                }
            }
            icur++;
        }

        try {
            int nitems = names.size();
            int i = 0;
            for (String[] cnames : snames) {

                if (shortname) {
                    write(cnames[cnames.length - 1], writer);
                } else {
                    write(names.get(i), writer);
                }
                for (int j = 1; j < witems[i]; ++j) {
                    writer.write(comma);
                }
                if (++i < nitems) {
                    writer.write(comma);
                } else {
                    writer.write(newLine);
                }
            }
            for (InformationSet record : records) {

                i = 0;
                for (String[] cnames : snames) {

                    int n = witems[i];
                    Object obj = record == null ? null : record.search(cnames, Object.class);

                    if (obj != null) {

                        if (n == 1) {
                            write(format(obj, 0), writer);
                        } else {
                            for (int j = 1; j <= n; ++j) {

                                write(format(obj, j), writer);
                                if (j < n) {
                                    writer.write(comma);
                                }
                            }
                        }
                    } else {
                        for (int j = 1; j < n; ++j) {
                            writer.write(comma);
                        }
                    }
                    if (++i == nitems) {
                        writer.write(newLine);
                    } else {
                        writer.write(comma);
                    }
                }
            }
            writer.close();
        } catch (IOException ex) {
            String msg = ex.getMessage();
        }
    }

    public void formatResults(Writer writer, List<NamedObject<IProcResults>> records, List<String> names, boolean shortname) {

        List<String[]> snames = new ArrayList<>();
        for (String name : names) {
            snames.add(InformationSet.split(name));
        }
        int[] witems = new int[snames.size()];
        int icur = 0;
        for (String[] sname : snames) {

            String slast = sname[sname.length - 1];
            int l = slast.indexOf(':');
            if (l >= 0) {

                String s0 = slast.substring(0, l);
                String s1 = slast.substring(l + 1);
                int w = 0;
                try {
                    w = Integer.parseInt(s1);
                    sname[sname.length - 1] = s0;
                    witems[icur] = w;
                } catch (Exception ex) {
                }
            } else {
                witems[icur] = 1;
            }
            icur++;
        }

        try {
            int nitems = names.size();
            int i = 0;
            writer.write("series");
            writer.write(comma);
            for (String[] cnames : snames) {
                if (shortname) {
                    write(cnames[cnames.length - 1], writer);
                } else {
                    write(names.get(i), writer);
                }
                for (int j = 1; j < witems[i]; ++j) {
                    writer.write(comma);
                }
                if (++i < nitems) {
                    writer.write(comma);
                } else {
                    writer.write(newLine);
                }
            }
            for (NamedObject<IProcResults> nrecord : records) {
                i = 0;
                write(nrecord.name, writer);
                IProcResults record = nrecord.object;
                writer.write(comma);
                for (String[] cnames : snames) {

                    int n = witems[i];
                    Object obj = record == null ? null : record.getData(InformationSet.concatenate(cnames), Object.class);

                    if (obj != null) {

                        if (n == 1) {
                            write(format(obj, 0), writer);
                        } else {
                            for (int j = 1; j <= n; ++j) {

                                write(format(obj, j), writer);
                                if (j < n) {
                                    writer.write(comma);
                                }
                            }
                        }
                    } else {
                        for (int j = 1; j < n; ++j) {
                            writer.write(comma);
                        }
                    }
                    if (++i == nitems) {
                        writer.write(newLine);
                    } else {
                        writer.write(comma);
                    }
                }
            }
            writer.close();
        } catch (IOException ex) {
            String msg = ex.getMessage();
        }
    }

    private String format(Object obj, int item) {

        try {
            IStringFormatter fmt = dictionary.get(obj.getClass());
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

    private void write(String txt, Writer writer) throws IOException {

        if (txt == null) {
            return;
        }
        if (fullName) {
            txt = MultiLineNameUtil.join(txt, " * ");
        } else {
            txt = MultiLineNameUtil.last(txt);
        }

        if (txt.indexOf(comma) >= 0) {
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
