/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.algorithm.implementation;

import ec.satoolkit.special.StmSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author PCUser
 */
public class StmProcessingFactoryTest {
    
    public StmProcessingFactoryTest() {
    }

    @Test
    @Ignore
    public void demoProcessing() {
        StmSpecification spec=new StmSpecification();
        //spec.getPreprocessingSpec().transform=DefaultTransformationType.None;
        CompositeResults stm = StmProcessingFactory.process(data.Data.P, spec);
        TsDataTable table=new TsDataTable();
        table.insert(-1, stm.getData("y", TsData.class));
        table.insert(-1, stm.getData("sa", TsData.class));
        table.insert(-1, stm.getData("t", TsData.class));
        table.insert(-1, stm.getData("s", TsData.class));
        table.insert(-1, stm.getData("i", TsData.class));
        System.out.println(table);
    }
    
 }
