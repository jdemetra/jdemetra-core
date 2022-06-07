/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats.samples;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class PopulationTest {
    
    public PopulationTest() {
    }

    @Test
    public void testSomeMethod() {
        
        Population pop = Population.builder()
                .size(1000).build();
        System.out.println(pop.toString());
       
    }
    
}
