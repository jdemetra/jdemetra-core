/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarmaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ArmaModuleTest {

    public ArmaModuleTest() {
    }

    @Test
    public void testSomeMethod() {
        PreprocessingModel model = TramoSpecification.TRfull.build().process(Data.P, null);
        SarimaComponent arima = model.description.getArimaComponent();
        DataBlock res = model.estimation.getLinearizedData();
        DataBlock dres = new DataBlock(res.getLength() - arima.getDifferencingOrder());
        arima.getDifferencingFilter().filter(res, dres);
        int freq = model.description.getFrequency();
        ec.tstoolkit.modelling.arima.x13.ArmaModule x13 = new ec.tstoolkit.modelling.arima.x13.ArmaModule();
        x13.setMixed(false);
        SarmaSpecification spec = x13.select(new DataBlock(res), freq, 3,1, arima.getD(), arima.getBD());
        assertTrue(spec != null);
    }

}
