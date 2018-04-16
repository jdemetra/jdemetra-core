/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.ucarima.estimation;

import ec.tstoolkit.ucarima.UcarimaModel;
import static ec.tstoolkit.ucarima.WienerKolmogorovPreliminaryEstimatorPropertiesTest.ucmAirline;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class BurmanEstimatesCTest {
    
    public BurmanEstimatesCTest() {
    }

    @Test
    public void testAirline() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        BurmanEstimatesC burman=new BurmanEstimatesC();
        burman.setUcarimaModel(ucm);
        burman.setData(data.Data.P);
        System.out.println(burman.estimates(0, true));
    }
    
}
