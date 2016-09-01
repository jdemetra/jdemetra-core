/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.eco;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.PeriodicDummies;
import ec.tstoolkit.data.TrigonometricSeries;
import ec.tstoolkit.data.WindowType;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RobustCovarianceMatrixComputerTest {

    public RobustCovarianceMatrixComputerTest() {
    }

    @Test
    public void testTrig() {
        DataBlock y = new DataBlock(Data.P.log().delta(1));
        RegModel model = new RegModel();
        model.setY(y);
        model.setMeanCorrection(true);
        TrigonometricSeries vars = TrigonometricSeries.regular(12);
        Matrix m = vars.matrix(y.getLength());
        for (int i = 0; i < m.getColumnsCount(); ++i) {
            model.addX(m.column(i));
        }
        Ols ols = new Ols();
        ols.process(model);
        DataBlock e = ols.getResiduals();
        RobustCovarianceMatrixComputer har = new RobustCovarianceMatrixComputer();
        Matrix rvar = har.compute(m.all(), e);
        System.out.println(rvar);
    }

    @Test
    public void testDummies() {
        DataBlock y = new DataBlock(Data.US_UNEMPL);
        RegModel model = new RegModel();
        model.setY(y.drop(1, 0));
        model.addX(y.drop(0, 1));
        PeriodicDummies vars = new PeriodicDummies(4, 1);

        Matrix m = vars.matrix(y.getLength() - 1);
        for (int i = 0; i < m.getColumnsCount(); ++i) {
            model.addX(m.column(i));
        }
        Ols ols = new Ols();
        ols.process(model);
        DataBlock e = ols.getResiduals();
        RobustCovarianceMatrixComputer har = new RobustCovarianceMatrixComputer();
        har.setWindowType(WindowType.Bartlett);
        Matrix rvar = har.compute(m.all(), e);
        System.out.println(rvar);
    }
}
