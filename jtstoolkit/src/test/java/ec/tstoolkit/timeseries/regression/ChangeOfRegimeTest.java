/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PCUser
 */
public class ChangeOfRegimeTest {

    static TsVariable var;

    static {
        DataBlock x = new DataBlock(120);
        x.set(1);
        x.cumul();
        TsPeriod start = new TsPeriod(TsFrequency.Monthly, 1980, 0);
        TsData s = new TsData(start, x);
        var = new TsVariable(s);
    }

    public ChangeOfRegimeTest() {
    }

    @Test
    public void test() {
        TsPeriod p = var.getDefinitionDomain().getStart().plus(var.getDefinitionDomain().getLength() / 2);
        ChangeOfRegime x1 = new ChangeOfRegime(var, ChangeOfRegimeType.ZeroStarted, p.firstday());
        ChangeOfRegime x2 = new ChangeOfRegime(var, ChangeOfRegimeType.ZeroEnded, p.firstday());
        TsVariableGroup group = new TsVariableGroup("test", new ITsVariable[]{var, x1, x2});
        Matrix matrix = RegressionUtilities.matrix(group, var.getDefinitionDomain());
        DataBlock c=matrix.column(0).deepClone();
        c.sub(matrix.column(1));
        c.sub(matrix.column(2));
        assertTrue(c.isZero());
    }
}
