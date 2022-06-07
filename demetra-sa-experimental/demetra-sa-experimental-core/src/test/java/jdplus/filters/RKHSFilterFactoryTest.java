/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import jdplus.rkhs.RKHSFilterSpec;
import jdplus.rkhs.RKHSFilterFactory;
import demetra.data.Data;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RKHSFilterFactoryTest {
    
    public RKHSFilterFactoryTest() {
    }

  
    public static void test1(DoubleSeq seq, KernelOption option) {
        RKHSFilterSpec spec=new RKHSFilterSpec();
        spec.setFilterLength(6);
        spec.setKernel(option);
        spec.setAsymmetricBandWith(AsymmetricCriterion.Undefined);
        spec.setDensity(SpectralDensity.Undefined);
        IFiltering filtering = RKHSFilterFactory.of(spec);
        System.out.println(filtering.process(seq));
    }
    
    public static void main(String[] arg){
        DoubleSeq seq=DoubleSeq.of(Data.NILE);            
        System.out.println(seq);
        test1(seq, KernelOption.BiWeight);
        test1(seq, KernelOption.TriWeight);
        test1(seq, KernelOption.Henderson);
    }
    
}
