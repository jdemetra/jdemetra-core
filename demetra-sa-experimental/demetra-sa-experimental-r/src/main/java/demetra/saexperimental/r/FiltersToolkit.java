/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.saexperimental.r;

import demetra.information.InformationMapping;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.IFilter;
import jdplus.maths.linearfilters.IFiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import demetra.likelihood.DiffuseConcentratedLikelihood;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import demetra.processing.ProcResults;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class FiltersToolkit {

    @lombok.Value
    @lombok.Builder
    public static class FiniteFilters implements ProcResults {

        private SymmetricFilter filter;
        private IFiniteFilter[] afilters;

        private static final InformationMapping<FiniteFilters> MAPPING = new InformationMapping<>(FiniteFilters.class);

        public static final InformationMapping<FiniteFilters> getMapping() {
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

        static double[] gain(IFilter filter) {
            DoubleUnaryOperator gainFunction = filter.gainFunction();
            int RES = 600;
            double[] g = new double[RES + 1];
            for (int i = 0; i <= RES; ++i) {
                g[i] = gainFunction.applyAsDouble(i*Math.PI / 600);
            }
            return g;
        }

        static double varianceReduction(IFiniteFilter filter) {
            double s=0;
            int l=filter.getLowerBound(), u=filter.getUpperBound();
            IntToDoubleFunction weights = filter.weights();
            for (int i=l; i<=u; ++i){
                double w=weights.applyAsDouble(i);
                s+=w*w;
            }
            return s;
        }
        
        static double bias0(IFiniteFilter filter) {
            double s=0;
            int l=Math.max(filter.getLowerBound(), -2), u=Math.min(filter.getUpperBound(), 2);
            IntToDoubleFunction weights = filter.weights();
            for (int i=l; i<=u; ++i){
                double w=weights.applyAsDouble(i);
                s+=w;
            }
            return s;
        }

        static double bias1(IFiniteFilter filter) {
            double s=0;
            int l=filter.getLowerBound(), u=filter.getUpperBound();
            IntToDoubleFunction weights = filter.weights();
            for (int i=l; i<=u; ++i){
                double w=i*weights.applyAsDouble(i);
                s+=w;
            }
            return s;
        }

        static double bias2(IFiniteFilter filter) {
            double s=0;
            int l=filter.getLowerBound(), u=filter.getUpperBound();
            IntToDoubleFunction weights = filter.weights();
            for (int i=l; i<=u; ++i){
                double w=i*i*weights.applyAsDouble(i);
                s+=w;
            }
            return s;
        }

        static double[] phase(IFilter filter) {
            DoubleUnaryOperator phaseFunction = filter.phaseFunction();
            int RES = 600;
            double[] g = new double[RES + 1];
            for (int i = 0; i <= RES; ++i) {
                g[i] = phaseFunction.applyAsDouble(i*Math.PI / 600);
            }
            return g;
        }

        static {
            MAPPING.set("svariancereduction", Double.class, source -> varianceReduction(source.filter));
            MAPPING.setArray("avariancereduction", 0, Double.class, (source, i) -> 
                    i < source.afilters[i].length() ? varianceReduction(source.afilters[i]) : Double.NaN);
            MAPPING.set("sbias2", Double.class, source -> bias2(source.filter));
            MAPPING.setArray("abias0", 0, Double.class, (source, i) -> 
                    i < source.afilters[i].length() ? bias0(source.afilters[i]) : Double.NaN);
            MAPPING.setArray("abias1", 0, Double.class, (source, i) -> 
                    i < source.afilters[i].length() ? bias1(source.afilters[i]) : Double.NaN);
            MAPPING.setArray("abias2", 0, Double.class, (source, i) -> 
                    i < source.afilters[i].length() ? bias2(source.afilters[i]) : Double.NaN);
            MAPPING.set("sweights", double[].class, source -> source.filter.weightsToArray());
            MAPPING.setArray("aweights", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? source.afilters[i].weightsToArray() : null);
            MAPPING.set("sgain", double[].class, source -> gain(source.filter));
            MAPPING.setArray("again", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? gain(source.afilters[i]) : null);
            MAPPING.setArray("aphase", 0, double[].class, (source, i) -> i < source.afilters[i].length() ? phase(source.afilters[i]) : null);
        }
    }
}
