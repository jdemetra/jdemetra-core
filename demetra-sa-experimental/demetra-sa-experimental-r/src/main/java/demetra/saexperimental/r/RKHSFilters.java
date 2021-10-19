/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.saexperimental.r;

import jdplus.rkhs.CutAndNormalizeFilters;
import jdplus.rkhs.HighOrderKernels;
import jdplus.rkhs.KernelsUtility;
import jdplus.rkhs.RKHSFilterFactory;
import jdplus.rkhs.RKHSFilterSpec;
import jdplus.stats.Kernels;
import jdplus.filters.SpectralDensity;
import jdplus.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.filters.ISymmetricFiltering;
import jdplus.filters.KernelOption;

import java.util.function.DoubleUnaryOperator;

import jdplus.filters.AsymmetricCriterion;



/**
 *
 * @author Alain QLT
 */
@lombok.experimental.UtilityClass
public class RKHSFilters {


    public FiltersToolkit.FiniteFilters filterProperties(int horizon, int degree, String kernel, 
            boolean optimalbw, String criterion, boolean rwdensity, double passband, double bandwidth) {
        // Creates the filters
    	RKHSFilterSpec tspec=new RKHSFilterSpec();
        tspec.setFilterLength(horizon);
        tspec.setPolynomialDegree(degree);
        tspec.setKernel(KernelOption.valueOf(kernel));
        tspec.setOptimalBandWidth(optimalbw);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.valueOf(criterion));
        tspec.setDensity(rwdensity ? SpectralDensity.RandomWalk : SpectralDensity.Undefined);
        tspec.setPassBand(passband);
        tspec.setBandWidth(bandwidth);
        tspec.setMinBandWidth(horizon);
        tspec.setMaxBandWidth(3*horizon);
        ISymmetricFiltering rkhsfilter= RKHSFilterFactory.of(tspec);
       
        return new FiltersToolkit.FiniteFilters(rkhsfilter.symmetricFilter(),
        		rkhsfilter.endPointsFilters());
    }
    public FiltersToolkit.FiniteFilters filterProperties(int horizon, int degree, String kernel, 
            boolean optimalbw, String criterion, boolean rwdensity, double passband,
            double bandwidth, double minbandwidth, double maxbandwidth) {
        // Creates the filters
    	RKHSFilterSpec tspec=new RKHSFilterSpec();
        tspec.setFilterLength(horizon);
        tspec.setPolynomialDegree(degree);
        tspec.setKernel(KernelOption.valueOf(formattingKernel(kernel)));
        tspec.setOptimalBandWidth(optimalbw);
        tspec.setAsymmetricBandWith(AsymmetricCriterion.valueOf(criterion));
        tspec.setDensity(rwdensity ? SpectralDensity.RandomWalk : SpectralDensity.Undefined);
        tspec.setPassBand(passband);
        tspec.setBandWidth(bandwidth);
        tspec.setMinBandWidth(minbandwidth);
        tspec.setMaxBandWidth(maxbandwidth);
        ISymmetricFiltering rkhsfilter= RKHSFilterFactory.of(tspec);
       
        return new FiltersToolkit.FiniteFilters(rkhsfilter.symmetricFilter(),
        		rkhsfilter.endPointsFilters());
    }
    
    public DoubleUnaryOperator optimalCriteria(int horizon, int leads, int degree, String kernel, 
            String criterion, boolean rwdensity, double passband) {

        DoubleUnaryOperator density = rwdensity ? SpectralDensity.RandomWalk.asFunction() : SpectralDensity.Undefined.asFunction();

        DoubleUnaryOperator kernel_fun = kernel(formattingKernel(kernel), degree, horizon);
        AsymmetricFiltersFactory.Distance distance;
        switch (criterion) {
	        case "FrequencyResponse":
	        	distance = AsymmetricFiltersFactory.frequencyResponseDistance(density);
	            break;
	        case "Accuracy":
	        	distance = AsymmetricFiltersFactory.accuracyDistance(density, passband);
	            break;
	        case "Smoothness":
	        	distance = AsymmetricFiltersFactory.smoothnessDistance(density, passband);
	            break;
	        case "Timeliness":
	        	distance = AsymmetricFiltersFactory.timelinessDistance(density, passband);
	            break;
	        default:
	        	distance = null;
        }
        SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), horizon + 1, horizon);
        return (bandWidth -> distance.compute(H, CutAndNormalizeFilters.of(kernel_fun, bandWidth, horizon, leads)));
        
    }
    public double[] optimalBandwidth(int horizon, int degree, String kernelFun, 
            String criterion, boolean rwdensity, double passBand,
            double minbandwidth, double maxbandwidth) {
    	double[] optimalBw = new double[horizon];
    	
        int len = horizon;
        DoubleUnaryOperator kernel = kernel(formattingKernel(kernelFun), degree, horizon);
        DoubleUnaryOperator density = rwdensity ? SpectralDensity.RandomWalk.asFunction() : SpectralDensity.Undefined.asFunction();
       
        for (int i = 0; i < len; ++i) {
        	switch (criterion) {
                case "FrequencyResponse":
                	optimalBw[i] = CutAndNormalizeFilters.optimalBandWidth(len, i,
                			AsymmetricFiltersFactory.frequencyResponseDistance(density),
                            kernel, minbandwidth, maxbandwidth);
                    break;
                case "Accuracy":
                	optimalBw[i] = CutAndNormalizeFilters.optimalBandWidth(len, i,
                			AsymmetricFiltersFactory.accuracyDistance(density, passBand),
                            kernel, minbandwidth, maxbandwidth);
                    break;
                case "Smoothness":
                	optimalBw[i] = CutAndNormalizeFilters.optimalBandWidth(len, i,
                			AsymmetricFiltersFactory.smoothnessDistance(density, passBand),
                            kernel, minbandwidth, maxbandwidth);
                    break;
                case "Timeliness":
                	optimalBw[i] = CutAndNormalizeFilters.optimalBandWidth(len, i,
                			AsymmetricFiltersFactory.timelinessDistance(density, passBand),
                            kernel, minbandwidth, maxbandwidth);
                    break;
                default:
                	optimalBw[i] = len + 1;
        	}
        }
        return optimalBw;
        
    }
    public static DoubleUnaryOperator kernel(String kernel, int deg, int len) {
        switch (kernel) {
            case "BiWeight":
                return HighOrderKernels.kernel(Kernels.BIWEIGHT, deg);
            case "TriWeight":
                return HighOrderKernels.kernel(Kernels.TRIWEIGHT, deg);
            case "Uniform":
                return HighOrderKernels.kernel(Kernels.UNIFORM, deg);
            case "Triangular":
                return HighOrderKernels.kernel(Kernels.TRIANGULAR, deg);
            case "Epanechnikov":
                return HighOrderKernels.kernel(Kernels.EPANECHNIKOV, deg);
            case "Henderson":
                return HighOrderKernels.kernel(Kernels.henderson(len), deg);
            default:
                return null;
        }
    }
    private static String formattingKernel(String kernel) {
        switch (kernel.toLowerCase()) {
            case "biweight":
                return "BiWeight";
            case "triweight":
                return "TriWeight";
            case "uniform":
                return "Uniform";
            case "triangular":
                return "Triangular";
            case "epanechnikov":
                return "Epanechnikov";
            case "henderson":
                return "Henderson";
            default:
                return null;
        }
    }
    
}
