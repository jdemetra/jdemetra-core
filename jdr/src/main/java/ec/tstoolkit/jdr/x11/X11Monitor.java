/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.x11;

import demetra.information.InformationMapping;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Kernel;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x11.X11Toolkit;
import demetra.algorithm.IProcResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11Monitor {

    @lombok.Value
    public static class Results implements IProcResults{

        X11Specification spec;
        X11Results decomposition;
        Mstatistics mstatistics;

        static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate("diagnostics", MstatisticsInfo.getMapping(), source->source.getMstatistics());
            MAPPING.delegate("decomposition", X11DecompositionInfo.getMapping(), source->source.getDecomposition());
        }

        public InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

    }

    public static Results process(TsData s, X11Specification spec) {
        X11Toolkit toolkit = X11Toolkit.create(spec);
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results x11 = kernel.process(s);
        Mstatistics mstatistics = Mstatistics.computeFromX11(spec.getMode(), x11.getInformation());
        return new Results(spec.clone(), x11, mstatistics);
    }
}
