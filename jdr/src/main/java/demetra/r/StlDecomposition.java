/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.data.DoubleSequence;
import demetra.information.InformationMapping;
import demetra.processing.IProcResults;
import demetra.stl.IDataGetter;
import demetra.stl.IDataSelector;
import demetra.stl.LoessFilter;
import demetra.stl.LoessSpecification;
import demetra.stl.StlPlus;
import demetra.stl.StlPlusSpecification;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class StlDecomposition {

    @lombok.Value
    @lombok.Builder
    public static class Results implements IProcResults {

        boolean multiplicative;
        DoubleSequence y, t, s, i;

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
                DoubleSequence y = source.getY(), s = source.getS();
                if (source.isMultiplicative()) {
                    return DoubleSequence.of(y.length(), i -> y.get(i) / s.get(i)).toArray();
                } else {
                    return DoubleSequence.of(y.length(), i -> y.get(i) - s.get(i)).toArray();
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
        DoubleSequence y = DoubleSequence.ofInternal(data);
        stl.process(y);

        return Results.builder()
                .y(y)
                .t(DoubleSequence.ofInternal(stl.getTrend()))
                .s(DoubleSequence.ofInternal(stl.getSeason(0)))
                .i(DoubleSequence.ofInternal(stl.getIrr()))
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
