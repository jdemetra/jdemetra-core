/*
 * Copyright 2016 National Bank of Belgium
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
package ec.benchmarking.simplets;

import ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor;
import ec.benchmarking.simplets.TsMultiBenchmarking.TemporalConstraintDescriptor;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsMultiBenchmarkingTest {
    
    public TsMultiBenchmarkingTest() {
    }

    @Test
    public void testTable() {
        TsMultiBenchmarking mb=new TsMultiBenchmarking();
        TsData s11=new TsData(TsFrequency.Monthly, 1980,0, 120);
        s11.randomAirline();
        mb.addInput("s11", s11);
        TsData s12=new TsData(TsFrequency.Monthly, 1980,0, 120);
        s12.randomAirline();
        mb.addInput("s12", s12);
        TsData s21=new TsData(TsFrequency.Monthly, 1980,0, 120);
        s21.randomAirline();
        mb.addInput("s21", s21);
        TsData s22=new TsData(TsFrequency.Monthly, 1980,0, 120);
        s22.randomAirline();
        mb.addInput("s22", s22);
        
        TsData s_1=new TsData(TsFrequency.Monthly, 1980,0, 120);
        s_1.randomAirline();
        mb.addInput("s_1", s_1);
        TsData s_2=new TsData(TsFrequency.Monthly, 1980,0, 120);
        s_2.randomAirline();
        mb.addInput("s_2", s_2);
        TsData s2_=new TsData(TsFrequency.Monthly, 1980,0, 120);
        s2_.randomAirline();
        mb.addInput("s2_", s2_);
        ContemporaneousConstraintDescriptor c1=ContemporaneousConstraintDescriptor.parse("s_1=s11+s21");
        mb.addContemporaneousConstraint(c1);
        ContemporaneousConstraintDescriptor c2=ContemporaneousConstraintDescriptor.parse("s_2=s12+s22");
        mb.addContemporaneousConstraint(c2);
        ContemporaneousConstraintDescriptor c3=ContemporaneousConstraintDescriptor.parse("s2_=s21+s22");
        mb.addContemporaneousConstraint(c3);
        TsData S22=new TsData(TsFrequency.Yearly, 1980,0, 12);
        S22.set(i->100+10*i);
        mb.addInput("S22", S22);
        TemporalConstraintDescriptor c4=TemporalConstraintDescriptor.parse("S22=sum(s22)");
        mb.addTemporalConstraint(c4);
        assertTrue(mb.process());
//        TsDataTable table=new TsDataTable();
//        table.add(s_1);
//        table.add(s_2);
//        table.add(s2_);
//        table.add(mb.getResult("s11"));
//        table.add(mb.getResult("s12"));
//        table.add(mb.getResult("s21"));
//        table.add(mb.getResult("s22"));
//        System.out.println(table);
    }
    
}
