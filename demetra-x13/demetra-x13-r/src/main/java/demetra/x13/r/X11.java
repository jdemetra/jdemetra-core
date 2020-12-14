/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.arima.SarimaSpec;
import demetra.information.InformationMapping;
import demetra.processing.DefaultProcessingLog;
import demetra.processing.ProcResults;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.x11.X11Results;
import demetra.x11.X11Spec;
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
        ProcessingLog plog = new DefaultProcessingLog();
        return new Results(kernel.process(series, spec));
    }

}
