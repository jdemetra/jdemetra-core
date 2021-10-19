package jdplus.dfa;

import java.util.function.DoubleUnaryOperator;

import org.junit.Test;

import demetra.data.DoubleSeq;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.filters.FSTFilter;
import jdplus.filters.IFiltering;
import jdplus.filters.ISymmetricFiltering;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.filters.LocalPolynomialFilterFactory;
import jdplus.filters.LocalPolynomialFilterSpec;
import jdplus.filters.SpectralDensity;

public class DFAFilterTest {
	
	public DFAFilterTest() {
    }

    @Test
	 public void testSymmetric() {
    	int len = 6;
    	LocalPolynomialFilterSpec spec = new LocalPolynomialFilterSpec();
    	spec.setFilterLength(len);
        IFiltering lf = LocalPolynomialFilterFactory.of(spec);
        SymmetricFilter sf = ((ISymmetricFiltering) lf).symmetricFilter();
    	DFAFilter ff = DFAFilter.builder()
                .nlags(len)
                .nleads(len)
                .symetricFilter(sf)
                .density(SpectralDensity.WhiteNoise.asFunction())
                .build();
        DFAFilter.Results rslt = ff.make(0.33, 0.33, 0.33);
        System.out.println(DoubleSeq.of(rslt.getFilter().weightsToArray()));
        DoubleUnaryOperator sd = x -> 1;
        MSEDecomposition.of(sd, sf.frequencyResponseFunction(), rslt.getFilter().frequencyResponseFunction(), Math.PI/12);
        MSEDecomposition d = MSEDecomposition.of(sd, sf.frequencyResponseFunction(), rslt.getFilter().frequencyResponseFunction(), Math.PI/12);
        System.out.println(DoubleSeq.of(d.getAccuracy(), d.getSmoothness(), d.getTimeliness(), d.getResidual(), d.getTotal()));
        }
}
