/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StlPlusSpecification {
    
    private boolean multiplicative;
    private LoessSpecification tspec;
    private final List<SeasonalSpecification> sspecs=new ArrayList<>(); 
    private int ni=2, no=0;
    private double rwthreshold = 0.001;

    private DoubleUnaryOperator rwfn = x -> {
        double t = 1 - x * x;
        return t * t;
    };
    
    /**
     * Creates a default specification for a series that has a given periodicity
     * @param period The periodicity of the series
     * @param robust True for robust filtering, false otherwise.
     * @return 
     */
    public static StlPlusSpecification createDefault(int period, boolean robust){
        return createDefault(period, 7, robust);
    }
    
    /**
     * Given the length of the seasonal window, creates a default specification 
     * for a series that has a given periodicity
     * 
     * @param period
     * @param swindow
     * @param robust
     * @return 
     */
    public static StlPlusSpecification createDefault(int period, int swindow, boolean robust){
        StlPlusSpecification spec=new StlPlusSpecification(robust);
        spec.tspec=LoessSpecification.defaultTrend(period, swindow);
        spec.sspecs.add(new SeasonalSpecification(period, swindow));
        return spec;
    }

    /**
     * Creates the skeleton for a stl specification (the different filters have to be
     * specified
     * @param robust 
     */
    public StlPlusSpecification(boolean robust){
        if (robust){
            ni=1;
            no=15;
        }else{
            ni=2;
            no=0;
        }
    }

    /**
     * @return the multiplicative
     */
    public boolean isMultiplicative() {
        return multiplicative;
    }

    /**
     * @param multiplicative the multiplicative to set
     */
    public void setMultiplicative(boolean multiplicative) {
        this.multiplicative = multiplicative;
    }

    /**
     * @return the tspec
     */
    public LoessSpecification getTrendSpec() {
        return tspec;
    }

    /**
     * @param tspec the tspec to set
     */
    public void setTrendSpec(LoessSpecification tspec) {
        this.tspec = tspec;
    }

    /**
     * @return the sspecs
     */
    public List<SeasonalSpecification> getSeasonalSpecs() {
        return Collections.unmodifiableList(sspecs);
    }
    
    public void add(SeasonalSpecification spec){
        sspecs.add(spec);
    }

    /**
     * @return the ni
     */
    public int getNumberOfInnerIterations() {
        return ni;
    }

    /**
     * @param ni the ni to set
     */
    public void setNumberOfInnerIterations(int ni) {
        this.ni = ni;
    }

    /**
     * @return the no
     */
    public int getNumberOfOuterIterations() {
        return no;
    }

    /**
     * @param no the no to set
     */
    public void setNumberOfOuterIterations(int no) {
        this.no = no;
    }

    /**
     * @return the rwthreshold
     */
    public double getRobustWeightsThreshold() {
        return rwthreshold;
    }

    /**
     * @param rwthreshold the rwthreshold to set
     */
    public void setRobustWeightsThreshold(double rwthreshold) {
        this.rwthreshold = rwthreshold;
    }

    /**
     * @return the rwfn
     */
    public DoubleUnaryOperator getRobustWeightsFunction() {
        return rwfn;
    }

    /**
     * @param rwfn the rwfn to set
     */
    public void setRobustWeightsFunction(DoubleUnaryOperator rwfn) {
        this.rwfn = rwfn;
    }

    public StlPlus build(){
        LoessFilter tf=new LoessFilter(tspec);
        SeasonalFilter[] sf=new SeasonalFilter[sspecs.size()];
        for (int i=0; i<sf.length; ++i){
            SeasonalSpecification cur = sspecs.get(i);
            sf[i]=new SeasonalFilter(cur.getSeasonalSpec(),cur.getLowPassSpec(), cur.getPeriod());
        }
        StlPlus stl=new StlPlus(tf, sf);
        stl.setNi(ni);
        stl.setNo(no);
        stl.wfn=this.rwfn;
        stl.setWthreshold(rwthreshold);
        stl.setMultiplicative(this.multiplicative);
        return stl;
   }
}
