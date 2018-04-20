/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import data.Data;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.jdr.mapping.SarimaInfo;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ProcessorTest {
    
    public ProcessorTest() {
    }
    
    @Test
    public void testTramoSeats(){
        TramoSeatsResults rslts = Processor.tramoseats(Data.P, TramoSeatsSpecification.RSAfull, null);
        System.out.println(rslts.getData("preprocessing.model.bcasts(-2)", TsData.class));
    }
    
    @Test
    public void testX13(){
        X13Results rslts = Processor.x13(Data.P, X13Specification.RSA5, null);
//        System.out.println(new DataBlock(rslts.getData("preprocessing.arima.parameters", double[].class)));
//        System.out.println(rslts.getData("decomposition.d8", TsData.class));
        System.out.println(rslts.getData("t", TsData.class));
    }

}
