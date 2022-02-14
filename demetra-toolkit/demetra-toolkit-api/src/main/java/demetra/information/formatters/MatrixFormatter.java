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

package demetra.information.formatters;

import demetra.data.Parameter;
import demetra.information.Explorable;
import demetra.information.InformationSet;
import demetra.math.Complex;
import demetra.processing.ProcDiagnostic;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.modelling.RegressionItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class MatrixFormatter {

    private static final HashMap<Class, InformationFormatter> DICTIONARY = new HashMap<>();
    
    static{
        DICTIONARY.put(double.class, new DoubleFormatter());
        DICTIONARY.put(int.class, new IntegerFormatter());
        DICTIONARY.put(long.class, new LongFormatter());
        DICTIONARY.put(boolean.class, new BooleanFormatter("1", "0"));
        DICTIONARY.put(Double.class, new DoubleFormatter());
        DICTIONARY.put(Integer.class, new IntegerFormatter());
        DICTIONARY.put(Long.class, new LongFormatter());
        DICTIONARY.put(Boolean.class, new BooleanFormatter("1", "0"));
        DICTIONARY.put(String.class, new StringFormatter());
//        DICTIONARY.put(SarimaModel.class, new SarimaFormatter());
        DICTIONARY.put(Parameter.class, new ParameterFormatter());
//        DICTIONARY.put(ParameterInfo.class, new ParameterInfoFormatter());
//        DICTIONARY.put(TsMoniker.class, new MonikerFormatter());
        DICTIONARY.put(TsPeriod.class, new PeriodFormatter());
        DICTIONARY.put(RegressionItem.class, new RegressionItemFormatter());
        DICTIONARY.put(StatisticalTest.class, new StatisticalTestFormatter());
        DICTIONARY.put(ProcDiagnostic.class, new DiagnosticFormatter());
        DICTIONARY.put(Complex.class, new ComplexFormatter());
    }

    public boolean canProcess(Class cl){
        return DICTIONARY.containsKey(cl);
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

    public String[] formatProcResults(List<Explorable> records, List<String> names, boolean shortname) {

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

        for (Explorable record : records) {

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

        InformationFormatter fmt;
        try {
            fmt = DICTIONARY.get(obj.getClass());
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
