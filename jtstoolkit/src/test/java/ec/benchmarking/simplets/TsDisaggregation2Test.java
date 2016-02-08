/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.benchmarking.simplets;

import data.Data;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Admin
 */
public class TsDisaggregation2Test {

    public TsDisaggregation2Test() {
    }

    @Test
    public void testChowLin() {
        ChowLin cl = new ChowLin();
        TsData Y = Data.Y;
        TsData Q = Data.Q;

        TsVariableList vars = new TsVariableList();
        vars.add(new TsVariable(Q));

        cl.setConstant(true);
        cl.process(Y, vars);
        cl.setPrecision(1e-9);

        TsDisaggregation2 disagg = new TsDisaggregation2();
        disagg.setConstant(true);
        disagg.setModel(TsDisaggregation2.Model.Ar1);
        disagg.setEpsilon(1e-9);
        disagg.process(Y, vars);

        TsData r1 = cl.getDisaggregatedSeries();
        TsData r2 = disagg.getDisaggregtedSeries();
        TsDataTable table=new TsDataTable();
        table.add(r1);
        table.add(r2);
        System.out.println(table);
        assertTrue(r1.distance(r2) < 1e-2);
    }

    @Test
    public void testFernandez() {
        Fernandez fn = new Fernandez();
        TsData Y = Data.Y;
        TsData Q = Data.Q;

        TsVariableList vars = new TsVariableList();
        vars.add(new TsVariable(Q));

        fn.process(Y, vars);

        TsDisaggregation2 disagg = new TsDisaggregation2();
        
        disagg.setModel(TsDisaggregation2.Model.Rw);
        disagg.process(Y, vars);

        TsData r1 = fn.getDisaggregatedSeries();
        TsData r2 = disagg.getDisaggregtedSeries();
        assertTrue(r1.distance(r2) < 1e-6);
//        TsDataTable table=new TsDataTable();
//        table.add(r1);
//        table.add(r2);
//        System.out.println(table);
    }
}
