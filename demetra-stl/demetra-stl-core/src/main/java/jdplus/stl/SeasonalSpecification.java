/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder=true, builderClassName="Builder")
public class SeasonalSpecification {
    private final int period;
    private final LoessSpecification sspec;
    private final LoessSpecification lspec;
    
    public SeasonalSpecification(int period, int swindow){
        this.period=period;
        this.sspec=LoessSpecification.defaultSeasonal(swindow);
        this.lspec=LoessSpecification.defaultLowPass(period);
    }
    
    public SeasonalSpecification(int period, LoessSpecification sspec, LoessSpecification lspec){
        this.period=period;
        this.sspec=sspec;
        this.lspec=lspec;
    }

    /**
     * @return the period
     */
    public int getPeriod() {
        return period;
    }

    /**
     * @return the sspec
     */
    public LoessSpecification getSeasonalSpec() {
        return sspec;
    }

    /**
     * @return the lspec
     */
    public LoessSpecification getLowPassSpec() {
        return lspec;
    }
}
