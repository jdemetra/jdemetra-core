package demetra.saexperimental.r;

import jdplus.filters.ISymmetricFiltering;
import jdplus.filters.SpectralDensity;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.dfa.DFAFilterFactory;
import jdplus.dfa.DFAFilterSpec;

@lombok.experimental.UtilityClass
public class DFAFilters {
	public FiltersToolkit.FiniteFilters filterProperties(double[] target,
			int nlags, int pdegree, boolean rwdensity, double passband, 
			double waccuracy, double wsmoothness, double wtimeliness) {
        // Creates the filters
    	DFAFilterSpec tspec=new DFAFilterSpec();
    	tspec.setW0(0);
    	tspec.setW1(passband);
    	tspec.setAccuracyWeight(passband);
    	tspec.setPolynomialPreservationDegree(pdegree);
        tspec.setLags(nlags);
        tspec.setTarget(SymmetricFilter.ofInternal(target));
        tspec.setAccuracyWeight(waccuracy);
        tspec.setSmoothnessWeight(wsmoothness);
        tspec.setTimelinessWeight(wtimeliness);
        tspec.setDensity(rwdensity ? SpectralDensity.RandomWalk : SpectralDensity.Undefined);

        ISymmetricFiltering dfafilter= DFAFilterFactory.of(tspec);
       
        return new FiltersToolkit.FiniteFilters(dfafilter.symmetricFilter(),
        		dfafilter.endPointsFilters());
    }
}
