/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.modelling.regression;

import demetra.data.DoubleSeq;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import jdplus.math.matrices.FastMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class GenericTradingDaysFactoryTest {
    
    public GenericTradingDaysFactoryTest() {
    }

    @Test
    public void testMeanMDays() {
        
        TsDomain m=TsDomain.of(TsPeriod.monthly(1980,3), 36);
        DoubleSeq M=DoubleSeq.of(GenericTradingDaysFactory.meanDays(m));
        assertEquals(M.range(0,12).sum(), 365.25/7, 1e-9);
        assertEquals(M.range(5,17).sum(), 365.25/7, 1e-9);
        assertEquals(M.range(24,36).sum(), 365.25/7, 1e-9);
    }
    
    @Test
    public void testMeanQDays() {
        TsDomain m=TsDomain.of(TsPeriod.quarterly(1980,3), 12);
        DoubleSeq M=DoubleSeq.of(GenericTradingDaysFactory.meanDays(m));
        assertEquals(M.range(0,4).sum(), 365.25/7, 1e-9);
        assertEquals(M.range(5,9).sum(), 365.25/7, 1e-9);
        assertEquals(M.range(8,12).sum(), 365.25/7, 1e-9);
    }
    
    @Test
    public void testTDM() {
        FastMatrix M=FastMatrix.make(12*28, 7);
        GenericTradingDaysFactory.fillTradingDaysMatrix(TsPeriod.monthly(1980, 1), true, M);
        for (int i=0; i<7; ++i){
            assertEquals(M.column(i).sum(), 0, 1e-9);
        }
        GenericTradingDaysFactory.fillTradingDaysMatrix(TsPeriod.monthly(1980, 1), false, M);
        for (int i=0; i<7; ++i){
            assertEquals(M.column(i).sum(), 1461, 1e-9);
        }
        FastMatrix Q=FastMatrix.make(4*28, 7);
        GenericTradingDaysFactory.fillTradingDaysMatrix(TsPeriod.quarterly(1980, 1), true, Q);
        for (int i=0; i<7; ++i){
            assertEquals(Q.column(i).sum(), 0, 1e-9);
        }
        GenericTradingDaysFactory.fillTradingDaysMatrix(TsPeriod.quarterly(1980, 1), false, Q);
        for (int i=0; i<7; ++i){
            assertEquals(Q.column(i).sum(), 1461, 1e-9);
        }
    }    
}
