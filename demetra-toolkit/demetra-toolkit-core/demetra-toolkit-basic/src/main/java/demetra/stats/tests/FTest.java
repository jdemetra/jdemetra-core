/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.tests;

import jdplus.dstats.F;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class FTest {

    private double SSM;
    private int dfm;
    private double SSR;
    private int dfr;

    public double getSSQ(){
        return SSM+SSR;
    }

    public double getdfq(){
        return dfm+dfr;
    }
    
    public StatisticalTest asTest(){
	F f = new F(dfm, dfr);
        return new StatisticalTest(f, (SSM / dfm) * (dfr / SSR), TestType.Upper, true);
        
    }
}
