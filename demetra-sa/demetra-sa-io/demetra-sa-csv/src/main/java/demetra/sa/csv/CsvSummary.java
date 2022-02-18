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
package demetra.sa.csv;

import demetra.information.Explorable;
import demetra.modelling.ModellingDictionary;
import demetra.sa.SaDictionaries;
import demetra.sa.SaDocument;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Kristof Bayens
 */
public class CsvSummary {

    public final String Name;
    private TsDomain domain_, fdomain_;
    private final Map<String, TsData> series_ = new HashMap<>();

    public CsvSummary(String[] items, String name, SaDocument document, int npred) {
        TsData s = document.getSeries().getData();
        Name = name;
        series_.put("y", s);
        domain_ = s.getDomain();
        fdomain_ = TsDomain.of(domain_.getEndPeriod(), npred);
        fillDictionary(items, document.getResults());
    }

    private void fillDictionary(String[] items, Explorable results) {
        for (String item : items) {
            if (item.equalsIgnoreCase("y")) {
                series_.put("y", results.getData(ModellingDictionary.Y, TsData.class));
            } else if (item.equalsIgnoreCase("yc")) {
                series_.put("yc", results.getData(ModellingDictionary.YC, TsData.class));
            } else if (item.equalsIgnoreCase("yl")) {
                series_.put("yl", results.getData(ModellingDictionary.L, TsData.class));
            } else if (item.equalsIgnoreCase("t")) {
                series_.put("t", results.getData(SaDictionaries.T, TsData.class));
            } else if (item.equalsIgnoreCase("sa")) {
                series_.put("sa", results.getData(SaDictionaries.SA, TsData.class));
            } else if (item.equalsIgnoreCase("s")) {
                series_.put("s", results.getData(SaDictionaries.S, TsData.class));
            } else if (item.equalsIgnoreCase("i")) {
                series_.put("i", results.getData(SaDictionaries.I, TsData.class));
            } else if (item.equalsIgnoreCase("tc")) {
                series_.put("tc", results.getData(SaDictionaries.T_CMP, TsData.class));
            } else if (item.equalsIgnoreCase("sac")) {
                series_.put("sac", results.getData(SaDictionaries.SA_CMP, TsData.class));
            } else if (item.equalsIgnoreCase("sc")) {
                series_.put("sc", results.getData(SaDictionaries.S_CMP, TsData.class));
            } else if (item.equalsIgnoreCase("ic")) {
                series_.put("ic", results.getData(SaDictionaries.I_CMP, TsData.class));
            } else if (item.equalsIgnoreCase("tl")) {
                series_.put("tc", results.getData(SaDictionaries.T_LIN, TsData.class));
            } else if (item.equalsIgnoreCase("sal")) {
                series_.put("sac", results.getData(SaDictionaries.SA_LIN, TsData.class));
            } else if (item.equalsIgnoreCase("sl")) {
                series_.put("sc", results.getData(SaDictionaries.S_LIN, TsData.class));
            } else if (item.equalsIgnoreCase("il")) {
                series_.put("ic", results.getData(SaDictionaries.I_LIN, TsData.class));
            } else if (item.equalsIgnoreCase("ycal")) {
                series_.put("ycal", results.getData(ModellingDictionary.YCAL, TsData.class));
            }
//            else {
//                TsDomain fullDomain = domain_.union(fdomain_);
//                IRegArimaSaResults regmodel = (IRegArimaSaResults) results;
//                if (regmodel != null) {
//                    if (item.equalsIgnoreCase("td"))
//                        series_.put("td", regmodel.getPreprocessingPart().regressionEffect(fullDomain));
//                }
//            }
        }
    }

    public TsData getSeries(String name) {
        if (series_.containsKey(name)) {
            return series_.get(name);
        } else {
            return null;
        }
    }
}
