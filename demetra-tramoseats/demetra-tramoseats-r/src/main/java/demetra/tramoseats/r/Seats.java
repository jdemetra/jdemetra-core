/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.r;

import demetra.arima.SarimaSpec;
import demetra.data.DoubleSeq;
import demetra.information.InformationMapping;
import demetra.processing.ProcResults;
import demetra.processing.ProcessingLog;
import demetra.seats.DecompositionSpec;
import demetra.seats.SeatsModelSpec;
import demetra.timeseries.TsData;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.seats.SeatsKernel;
import jdplus.seats.SeatsResults;
import jdplus.seats.SeatsToolkit;
import jdplus.tramoseats.extractors.SeatsExtractor;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Seats {

    @lombok.Value
    public static class Results implements ProcResults {

        private SeatsResults core;

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
            return SeatsExtractor.getMapping().getData(core, id, tclass);
        }

        public static InformationMapping getMapping() {
            return SeatsExtractor.getMapping();
        }
    }

    public Results process(TsData data, boolean log, int[] order, int[] seasonal, boolean mean, int nb, int nf) {

        SarimaSpec arima = SarimaSpec.builder()
                .p(order[0])
                .d(order[1])
                .q(order[2])
                .bp(seasonal == null ? 0 : seasonal[0])
                .bd(seasonal == null ? 0 : seasonal[1])
                .bq(seasonal == null ? 0 : seasonal[2])
                .build();

        SeatsModelSpec model = SeatsModelSpec.builder()
                .series(data)
                .sarimaSpec(arima)
                .log(log)
                .meanCorrection(mean)
                .build();

        DecompositionSpec dspec = DecompositionSpec.builder()
                .forecastCount(nf)
                .backcastCount(nb)
                .build();

        SeatsToolkit toolkit = SeatsToolkit.of(dspec);
        SeatsKernel kernel = new SeatsKernel(toolkit);
        ProcessingLog plog = new ProcessingLog();
        return new Results(kernel.process(model, plog));
    }

}
