/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
/*
 */
package ec.satoolkit.diagnostics;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PCUser
 */
public class StationaryVarianceDecompositionTest {
    
    public StationaryVarianceDecompositionTest() {
    }

    @Test
    public void testTS() {
        CompositeResults rslts = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSAfull);
        StationaryVarianceDecomposition var=new StationaryVarianceDecomposition();
        var.process(rslts);
        System.out.println("Tramo-Seats");
        System.out.println(var);
    }
    @Test
    public void testX12() {
        CompositeResults rslts = X13ProcessingFactory.process(Data.X, X13Specification.RSA5);
        StationaryVarianceDecomposition var=new StationaryVarianceDecomposition();
        var.process(rslts);
        System.out.println("X12");
        System.out.println(var);
    }
}
