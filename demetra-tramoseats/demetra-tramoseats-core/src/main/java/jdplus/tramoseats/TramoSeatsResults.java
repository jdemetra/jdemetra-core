/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramoseats;

import demetra.processing.ProcResults;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.seats.SeatsResults;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.tramoseats.extractors.TramoSeatsExtractor;

/**
 *
 * @author palatej
 */
@lombok.Value
public class TramoSeatsResults implements ProcResults {

    private ModelEstimation preprocessing;
    private SeatsResults decomposition;
    private SeriesDecomposition<TsData> finals;

    @Override
    public boolean contains(String id) {
        return TramoSeatsExtractor.getMapping().contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        TramoSeatsExtractor.getMapping().fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return TramoSeatsExtractor.getMapping().getData(this, id, tclass);
    }

}
