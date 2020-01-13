/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import demetra.data.DoubleSeq;
import jdplus.math.linearfilters.IFiniteFilter;

/**
 *
 * @author Jean Palate
 */
public class FSTFilterFactory {

    public static IFiltering of(FSTFilterSpec spec) {
        return new Filter(spec);
    }

    private static class Filter implements IFiltering {

        private final IFiniteFilter cf;
        private final IFiniteFilter[] lf, rf;

        private Filter(FSTFilterSpec spec) {
            // central filter
            FSTFilter.Builder builder = FSTFilter.builder()
                    .degreeOfSmoothness(spec.getSmoothnessDegree())
                    .polynomialPreservation(spec.getPolynomialPreservationDegree())
                    .nlags(spec.getLags())
                    .nleads(spec.getLeads())
                    .timelinessAntiphaseCriterion(spec.isAntiphase())
                    .timelinessLimits(spec.getW0(), spec.getW1());
                    
            FSTFilter.Results rslt = builder.build().make(spec.getSmoothnessWeight(), spec.getTimelinessWeight());
            cf=rslt.getFilter();
            lf=new IFiniteFilter[spec.getLags()];
            rf=new IFiniteFilter[spec.getLeads()];
            int del=spec.getLags()+spec.getLeads();
            for (int i=0; i<lf.length; ++i){
                rslt = builder.nlags(i).nleads(del-i).build().make(spec.getSmoothnessWeight(), spec.getTimelinessWeight());
                lf[lf.length-i-1]=rslt.getFilter();
            }
            builder.nlags(spec.getLags());
            builder.nleads(spec.getLeads());
            for (int i=0; i<rf.length; ++i){
                rslt = builder.nleads(i).nlags(del-i).build().make(spec.getSmoothnessWeight(), spec.getTimelinessWeight());
               rf[rf.length-i-1]=rslt.getFilter();
            }
        }

        @Override
        public DoubleSeq process(DoubleSeq in) {
            return jdplus.math.linearfilters.FilterUtility.filter(in, cf, lf, rf);
        }

        @Override
        public IFiniteFilter centralFilter() {
            return cf;
        }

        @Override
        public IFiniteFilter[] leftEndPointsFilters() {
            return lf;
        }

        @Override
        public IFiniteFilter[] rightEndPointsFilters() {
            return rf;
        }

    }

}
