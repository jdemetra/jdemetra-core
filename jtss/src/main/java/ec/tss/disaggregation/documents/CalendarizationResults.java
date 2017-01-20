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
package ec.tss.disaggregation.documents;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Results of the calendarization process. Contains smoothed data, aggregated
 * data by the given frequency and their respective standard deviation data.
 *
 * @author Mats Maggi
 */
public class CalendarizationResults implements IProcResults {

    public static final String SMOOTH = "smooth", SMOOTH_DEV = "smoothstdev", AGGREGATED = "aggregated", AGGREGATED_DEV = "aggregatedstdev";

    private InformationSet info = new InformationSet();

    public void set(double[] smooth, double[] smoothStdev, TsData aggregated, TsData aggregatedStdev) {
        info.set(SMOOTH, smooth);
        info.set(SMOOTH_DEV, smoothStdev);
        info.set(AGGREGATED, aggregated);
        info.set(AGGREGATED_DEV, aggregatedStdev);
    }

    @Override
    public boolean contains(String id) {
        return info.search(id, Object.class) != null;
    }

    @Override
    public Map<String, Class> getDictionary(boolean compact) {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, compact);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return info.search(id, tclass);
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
        dic.put(InformationSet.item(prefix, SMOOTH), double[].class);
        dic.put(InformationSet.item(prefix, SMOOTH_DEV), double[].class);
        dic.put(InformationSet.item(prefix, AGGREGATED), TsData.class);
        dic.put(InformationSet.item(prefix, AGGREGATED_DEV), TsData.class);
    }

}
