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
package jdplus.seats;

import demetra.data.DoubleSeq;
import demetra.processing.ProcResults;
import demetra.sa.SeriesDecomposition;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.sarima.SarimaModel;
import jdplus.tramoseats.extractors.SeatsExtractor;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SeatsResults implements ProcResults {

    private SarimaModel originalModel;
    private SarimaModel finalModel;
    private boolean meanCorrection;
    private UcarimaModel ucarimaModel;
    private SeriesDecomposition<DoubleSeq> initialComponents, finalComponents;

    @Override
    public boolean contains(String id) {
        return SeatsExtractor.getMapping().contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        SeatsExtractor.getMapping().fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return SeatsExtractor.getMapping().getData(this, id, tclass);
    }

}
