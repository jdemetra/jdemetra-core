/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.data.DoubleSequence;
import demetra.information.InformationMapping;
import demetra.processing.IProcResults;
import demetra.sa.DecompositionMode;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.X11Context;
import demetra.x11.X11Kernel;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class X11Decomposition {
    
    @lombok.Value
    @lombok.Builder
    public static class Results implements IProcResults {

        boolean multiplicative;
        DoubleSequence y;
        X11Kernel kernel;

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
            MAPPING.set("b1", double[].class, source -> source.getKernel().getBstep().getB1().toArray());
            MAPPING.set("b2", double[].class, source -> source.getKernel().getBstep().getB2().toArray());
            MAPPING.set("b3", double[].class, source -> source.getKernel().getBstep().getB3().toArray());
            MAPPING.set("b4", double[].class, source -> source.getKernel().getBstep().getB4().toArray());
            MAPPING.set("b5", double[].class, source -> source.getKernel().getBstep().getB5().toArray());
            MAPPING.set("b6", double[].class, source -> source.getKernel().getBstep().getB6().toArray());
            MAPPING.set("b7", double[].class, source -> source.getKernel().getBstep().getB7().toArray());
            MAPPING.set("b8", double[].class, source -> source.getKernel().getBstep().getB8().toArray());
            MAPPING.set("b9", double[].class, source -> source.getKernel().getBstep().getB9().toArray());
            MAPPING.set("b10", double[].class, source -> source.getKernel().getBstep().getB10().toArray());
            MAPPING.set("b11", double[].class, source -> source.getKernel().getBstep().getB11().toArray());
            MAPPING.set("b13", double[].class, source -> source.getKernel().getBstep().getB13().toArray());
            MAPPING.set("b17", double[].class, source -> source.getKernel().getBstep().getB17().toArray());
            MAPPING.set("b20", double[].class, source -> source.getKernel().getBstep().getB20().toArray());
            MAPPING.set("c1", double[].class, source -> source.getKernel().getCstep().getC1().toArray());
            MAPPING.set("c2", double[].class, source -> source.getKernel().getCstep().getC2().toArray());
            MAPPING.set("c4", double[].class, source -> source.getKernel().getCstep().getC4().toArray());
            MAPPING.set("c5", double[].class, source -> source.getKernel().getCstep().getC5().toArray());
            MAPPING.set("c6", double[].class, source -> source.getKernel().getCstep().getC6().toArray());
            MAPPING.set("c7", double[].class, source -> source.getKernel().getCstep().getC7().toArray());
            MAPPING.set("c9", double[].class, source -> source.getKernel().getCstep().getC9().toArray());
            MAPPING.set("c10", double[].class, source -> source.getKernel().getCstep().getC10().toArray());
            MAPPING.set("c11", double[].class, source -> source.getKernel().getCstep().getC11().toArray());
            MAPPING.set("c12", double[].class, source -> source.getKernel().getCstep().getC12().toArray());
            MAPPING.set("c13", double[].class, source -> source.getKernel().getCstep().getC13().toArray());
            MAPPING.set("c17", double[].class, source -> source.getKernel().getCstep().getC17().toArray());
            MAPPING.set("c20", double[].class, source -> source.getKernel().getCstep().getC20().toArray());
            MAPPING.set("d1", double[].class, source -> source.getKernel().getDstep().getD1().toArray());
            MAPPING.set("d2", double[].class, source -> source.getKernel().getDstep().getD2().toArray());
            MAPPING.set("d4", double[].class, source -> source.getKernel().getDstep().getD4().toArray());
            MAPPING.set("d5", double[].class, source -> source.getKernel().getDstep().getD5().toArray());
            MAPPING.set("d6", double[].class, source -> source.getKernel().getDstep().getD6().toArray());
            MAPPING.set("d7", double[].class, source -> source.getKernel().getDstep().getD7().toArray());
            MAPPING.set("d8", double[].class, source -> source.getKernel().getDstep().getD8().toArray());
            MAPPING.set("d9", double[].class, source -> source.getKernel().getDstep().getD9().toArray());
            MAPPING.set("d10", double[].class, source -> source.getKernel().getDstep().getD10().toArray());
            MAPPING.set("d11", double[].class, source -> source.getKernel().getDstep().getD11().toArray());
            MAPPING.set("d12", double[].class, source -> source.getKernel().getDstep().getD12().toArray());
            MAPPING.set("d13", double[].class, source -> source.getKernel().getDstep().getD13().toArray());
            MAPPING.set(MUL, Boolean.class, source -> source.isMultiplicative());
        }
    }

    public Results process(double[] data, double period, boolean mul, int henderson, String seas0, String seas1) {
        int iperiod=(int) period;
        Number P;
        if (Math.abs(period-iperiod)<1e-9){
            P=Integer.valueOf(iperiod);
        }else
            P=Double.valueOf(period);
        X11Context context=X11Context.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(P)
                .hendersonFilterLength(henderson)
                .initialSeasonalFilter(SeasonalFilterOption.valueOf(seas0))
                .finalSeasonalFilter(SeasonalFilterOption.valueOf(seas1))
                .build();
        X11Kernel kernel=new X11Kernel();
        DoubleSequence y = DoubleSequence.ofInternal(data);
        kernel.process(y, context);

        return Results.builder()
                .y(y)
                .kernel(kernel)
                .multiplicative(mul)
                .build();

    }

}
