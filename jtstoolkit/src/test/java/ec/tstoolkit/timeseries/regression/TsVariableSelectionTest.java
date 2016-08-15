/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.timeseries.regression;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TsVariableSelectionTest {
    
    public TsVariableSelectionTest() {
    }

    @Test
    public void testItems() {
        TsVariableList list=new TsVariableList();
        TsVariableSelection<ICalendarVariable> sel = list.select(ICalendarVariable.class);
        TsVariableSelection.Item<ICalendarVariable>[] elements = sel.elements();
       // TsVariableSelection.Item<ITsVariable>[] elements2 = sel.elements();
    }
    
}
