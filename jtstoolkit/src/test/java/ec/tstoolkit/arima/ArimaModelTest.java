/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima;

import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class ArimaModelTest {

    public ArimaModelTest() {
    }

    @Test
    public void testAddSubtractWhiteNoise() {
        SarimaModelBuilder builder = new SarimaModelBuilder();
        SarimaModel tmp = builder.createAirlineModel(7, -.6, -.4);
        ArimaModel model = ArimaModel.create(tmp);
        ArimaModel m1 = model.plus(1);
        ArimaModel m2 = m1.minus(1);
        assertTrue(ArimaModel.same(tmp, m2, 1e-6));
    }

    @Test
    public void testAddSubtractModels() {
        SarimaModelBuilder builder = new SarimaModelBuilder();
        SarimaModel tmp1 = builder.createAirlineModel(11, -.36, -.4);
        SarimaModel tmp2 = builder.createAirlineModel(9, -.61, -.47);
        ArimaModel model = ArimaModel.add(tmp1, ArimaModel.create(tmp2));
        ArimaModel m = model.minus(ArimaModel.create(tmp1));
        m.simplifyUr();
        assertTrue(ArimaModel.same(m, tmp2, 1e-6));
    }

    @Test
    public void testVariance() {
        SarimaModelBuilder builder = new SarimaModelBuilder();
        SarimaModel tmp1 = builder.createAirlineModel(11, -.36, -.4);
        ArimaModel arima = ArimaModel.create((IArimaModel) tmp1.stationaryTransformation().stationaryModel);
        double var = arima.getAutoCovarianceFunction().get(0);
        ArimaModel narima = arima.scaleVariance(1 / var);
        double nvar = narima.getAutoCovarianceFunction().get(0);
        assertEquals(nvar, 1, 1e-9);
    }
}
