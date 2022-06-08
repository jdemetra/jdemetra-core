/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.benchmarking.multivariate;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class MultivariateCholetteSpecTest {
    
    public MultivariateCholetteSpecTest() {
    }

    @Test
    public void testInvalid1() throws Exception {
        try{
        MultivariateCholetteSpec.builder()
                .rho(1.1)
                .build();
        throw new Exception();
        }catch (IllegalArgumentException err){
            
        }
    }
    
    @Test
    public void testValid() {
        MultivariateCholetteSpec.builder()
                .rho(1.0)
                .build();
    }
    
}
