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
package demetra.x13.r;

import demetra.information.InformationMapping;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.x11.X11Results;
import demetra.x11.X11Spec;
import demetra.x13.io.protobuf.X13Protos;
import demetra.x13.io.protobuf.X13ProtosUtility;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.x11.X11Kernel;
import jdplus.x13.extractors.X11Extractor;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X11 {

    @lombok.Value
    public static class Results implements ProcResults {

        private X11Results core;
        
        public X11ResultsBuffer buffer(){
            return new X11ResultsBuffer(core);
        }

        @Override
        public boolean contains(String id) {
            return X11Extractor.getMapping().contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            X11Extractor.getMapping().fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return X11Extractor.getMapping().getData(core, id, tclass);
        }

        public static InformationMapping getMapping() {
            return X11Extractor.getMapping();
        }
    }

    public Results process(TsData series, X11Spec spec) {
        X11Kernel kernel = new X11Kernel();
        return new Results(kernel.process(series.cleanExtremities(), spec));
    }
    
    public byte[] toBuffer(X11Spec spec){
        return X13ProtosUtility.toBuffer(spec);
    }

}
