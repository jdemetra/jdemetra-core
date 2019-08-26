/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import demetra.data.DoubleSeq;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import jdplus.filters.ISymmetricFiltering;
import jdplus.maths.linearfilters.AsymmetricFilters;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.IFiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.stats.Kernels;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class RKHSFilterFactory {

    private static final Map<RKHSFilterSpec, ISymmetricFiltering> dictionary = new HashMap<>();

    public static ISymmetricFiltering of(RKHSFilterSpec spec) {
        synchronized (dictionary) {
            ISymmetricFiltering filtering = dictionary.get(spec);
            if (filtering == null) {
                filtering = new Filter(spec);
                dictionary.put(spec, filtering);
            }
            return filtering;
        }
    }

    private static class Filter implements ISymmetricFiltering {

        private final SymmetricFilter symmetricFilter;
        private final FiniteFilter[] asymmetricFilters;

        private Filter(RKHSFilterSpec spec) {
            int len = spec.getFilterLength();
            DoubleUnaryOperator kernel = kernel(spec);
            symmetricFilter = KernelsUtility.symmetricFilter(kernel, len + 1, len);
            asymmetricFilters = new FiniteFilter[len];
            double passBand = spec.getPassBand();
            DoubleUnaryOperator density = spec.getDensity().asFunction();
            for (int i = 0, j=len-1; i < len; ++i, --j) {
                double bandWidth;
                switch (spec.getAsymmetricBandWith()) {
                    case FrequencyResponse:
                        bandWidth = CutAndNormalizeFilters.optimalBandWidth(len, j,
                                AsymmetricFilters.frequencyResponseDistance(density));
                        break;
                    case Accuracy:
                        bandWidth = CutAndNormalizeFilters.optimalBandWidth(len, j,
                                AsymmetricFilters.accuracyDistance(density, passBand));
                        break;
                    case Smoothness:
                        bandWidth = CutAndNormalizeFilters.optimalBandWidth(len, j,
                                AsymmetricFilters.smoothnessDistance(density, passBand));
                        break;
                    case Timeliness:
                        bandWidth = CutAndNormalizeFilters.optimalBandWidth(len, j,
                                AsymmetricFilters.timelinessDistance(density, passBand));
                        break;
                    default:
                        bandWidth = len + 1;
                }
                asymmetricFilters[i] = CutAndNormalizeFilters.of(kernel, bandWidth, len, j);
            }
        }

        private static DoubleUnaryOperator kernel(RKHSFilterSpec spec) {
            int deg = spec.getPolynomialDegree();
            switch (spec.getKernel()) {
                case BiWeight:
                    return HighOrderKernels.kernel(Kernels.BIWEIGHT, deg);
                case TriWeight:
                    return HighOrderKernels.kernel(Kernels.TRIWEIGHT, deg);
                case Uniform:
                    return HighOrderKernels.kernel(Kernels.UNIFORM, deg);
                case Triangular:
                    return HighOrderKernels.kernel(Kernels.TRIANGULAR, deg);
                case Epanechnikov:
                    return HighOrderKernels.kernel(Kernels.EPANECHNIKOV, deg);
                case Henderson:
                    return HighOrderKernels.kernel(Kernels.henderson(spec.getFilterLength()), deg);
                default:
                    return null;
            }
        }

        @Override
        public DoubleSeq process(DoubleSeq in) {
            return jdplus.maths.linearfilters.FilterUtility.filter(in, symmetricFilter, asymmetricFilters);
        }
        
        @Override
        public SymmetricFilter symmetricFilter(){
            return symmetricFilter;
        }
        
        @Override
        public IFiniteFilter[] endPointsFilters(){
            return asymmetricFilters;
        }
        
    }

}
