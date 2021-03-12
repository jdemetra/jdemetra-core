/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stl.r;

import demetra.information.InformationMapping;
import jdplus.stl.IDataGetter;
import jdplus.stl.IDataSelector;
import jdplus.stl.LoessFilter;
import jdplus.stl.LoessSpecification;
import jdplus.stl.StlPlus;
import jdplus.stl.StlPlusSpecification;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.processing.ProcResults;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class StlDecomposition {

    @lombok.Value
    @lombok.Builder
    public static class Results implements ProcResults {

        boolean multiplicative;
        DoubleSeq y;
        DoubleSeq t, s, i;

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

        static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa", MUL = "mul";

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.set(Y, double[].class, source -> source.getY().toArray());
            MAPPING.set(T, double[].class, source -> source.getT().toArray());
            MAPPING.set(S, double[].class, source -> source.getS().toArray());
            MAPPING.set(I, double[].class, source -> source.getI().toArray());
            MAPPING.set(SA, double[].class, source
                    -> {
                DoubleSeq y = source.getY();
                DoubleSeq s = source.getS();
                if (source.isMultiplicative()) {
                    return DoubleSeq.onMapping(y.length(), i -> y.get(i) / s.get(i)).toArray();
                } else {
                    return DoubleSeq.onMapping(y.length(), i -> y.get(i) - s.get(i)).toArray();
                }
            }
            );
            MAPPING.set(MUL, Boolean.class, source -> source.isMultiplicative());
        }
    }

    public Results process(double[] data, int period, boolean mul, int swindow, int twindow, boolean robust) {
        StlPlusSpecification spec = StlPlusSpecification.createDefault(period, swindow, robust);
        if (twindow != 0) {
            spec.setTrendSpec(LoessSpecification.of(twindow, 1));
        }
        spec.setMultiplicative(mul);
        StlPlus stl = spec.build();
        DoubleSeq y = DoubleSeq.of(data);
        stl.process(y);

        return Results.builder()
                .y(y)
                .t(DoubleSeq.of(stl.getTrend()))
                .s(DoubleSeq.of(stl.getSeason(0)))
                .i(DoubleSeq.of(stl.getIrr()))
                .multiplicative(mul)
                .build();
    }

    public double[] loess(double[] y, int window, int degree, int jump) {
        LoessSpecification spec = LoessSpecification.of(window, degree, jump, null);
        LoessFilter filter = new LoessFilter(spec);
        double[] z = new double[y.length];
        filter.filter(IDataGetter.of(y), null, IDataSelector.of(z));
        return z;
    }

}
