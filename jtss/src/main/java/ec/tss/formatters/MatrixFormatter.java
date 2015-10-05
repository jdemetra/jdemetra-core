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
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcDiagnostic;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class MatrixFormatter {

    private static HashMap<Class, IStringFormatter> dictionary = new HashMap<>();

    public MatrixFormatter() {

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
        dictionary.put(RegressionItem.class, new RegressionItemFormatter());
        dictionary.put(StatisticalTest.class, new StatisticalTestFormatter());
        dictionary.put(ProcDiagnostic.class, new DiagnosticFormatter());
        dictionary.put(Complex.class, new ComplexFormatter());
    }

    public String[] formatInformation(List<InformationSet> records, List<String> names, boolean shortname) {

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
        ArrayList<String> rslt = new ArrayList<>();
        int i = 0;
        for (String[] cnames : snames) {

            if (shortname) {
                rslt.add(cnames[cnames.length - 1]);
            } else {
                rslt.add(names.get(i));
            }
            for (int j = 1; j < witems[i]; ++j) {
                rslt.add("");
            }
        }

        for (InformationSet record : records) {

            i = 0;
            for (String[] cnames : snames) {

                int n = witems[i];
                Object obj = record.search(cnames, Object.class);
                if (obj != null) {

                    if (n == 1) {
                        rslt.add(format(obj, 0));
                    } else if (n<0){
                        rslt.add(format(obj, -n));
                     }
                    else{
                        for (int j = 1; j <= n; ++j) {
                            rslt.add(format(obj, j));
                        }
                    }
                } else {
                    for (int j = 0; j < n; ++j) {
                        rslt.add("");
                    }
                }
            }
        }
        String[] srslt=new String[rslt.size()];
        return rslt.toArray(srslt);
    }

    public String[] formatProcResults(List<IProcResults> records, List<String> names, boolean shortname) {

        // TODOT. inefficient. Should be improved
        
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
        ArrayList<String> rslt = new ArrayList<>();
        int i = 0;
        for (String[] cnames : snames) {

            if (shortname) {
                rslt.add(cnames[cnames.length - 1]);
            } else {
                rslt.add(names.get(i));
            }
            for (int j = 1; j < witems[i]; ++j) {
                rslt.add("");
            }
        }

        for (IProcResults record : records) {

            i = 0;
            for (String[] cnames : snames) {

                int n = witems[i];
                Object obj = record.getData(InformationSet.concatenate(cnames), Object.class);
                if (obj != null) {

                    if (n == 1) {
                        rslt.add(format(obj, 0));
                    } else {
                        for (int j = 1; j <= n; ++j) {
                            rslt.add(format(obj, j));
                        }
                    }
                } else {
                    for (int j = 0; j < n; ++j) {
                        rslt.add("");
                    }
                }
            }
        }
        String[] srslt=new String[rslt.size()];
        return rslt.toArray(srslt);
    }

    private String format(Object obj, int item) {

        IStringFormatter fmt;
        try {
            fmt = dictionary.get(obj.getClass());
            return fmt.format(obj, item);
        } catch (Exception ex) {
            String msg = ex.getMessage();
        }

        if (item == 0) {
            return obj.toString();
        } else {
            return "";
        }
    }
}
