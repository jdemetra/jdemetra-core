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


package ec.tss.sa.output;

import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Kristof Bayens
 */
public class DefaultCollectionSummary {
    final List<SeriesSummary> items_ = new ArrayList<>();

    void add(String[] series, SaDocument<?> rslts) {
        String name = rslts.getInput().getName();
        if (name == null)
            name = "series" + Integer.toString(items_.size() + 1);
        items_.add(new SeriesSummary(series, name, rslts));
    }

    List<String> getNames() {
        String[] names = new String[items_.size()];
        for (int i = 0; i < names.length; ++i)
            names[i] = items_.get(i).Name;
        return Arrays.asList(names);
    }
    
    List<String> getItems(){
        LinkedHashSet<String> set=new LinkedHashSet<>();
        items_.stream().forEach((summary) -> {
            summary.fill(set);
        });
        return set.stream().collect(Collectors.toList());
    }

    List<TsData> getSeries(String item) {
        TsData[] s = new TsData[items_.size()];
        for (int i = 0; i < s.length; ++i)
            s[i] = items_.get(i).getSeries(item);
        return Arrays.asList(s);
    }
}
