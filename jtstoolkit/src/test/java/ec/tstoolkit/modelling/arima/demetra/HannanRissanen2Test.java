/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.demetra;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.random.MersenneTwister;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;
import ec.tstoolkit.sarima.estimation.HannanRissanen;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class HannanRissanen2Test {

    public HannanRissanen2Test() {
    }

    @Test
    public void testSomeMethod() {
        SarmaSpecification spec = new SarmaSpecification(12);
        spec.setP(3);
        spec.setBP(0);
        spec.setQ(1);
        spec.setBQ(1);
        SarimaModelBuilder builder = new SarimaModelBuilder();
        //builder.setRandomNumberGenerator(new MersenneTwister(1));
        SarimaModel model = new SarimaModel(spec);
        SarimaModel arima = builder.randomize(model, 1);
        System.out.println(arima);
        ArimaModelBuilder gen = new ArimaModelBuilder();
        double[] data = gen.generateStationary(arima, 240);
        HannanRissanen hr = new HannanRissanen();
        hr.process(new ReadDataBlock(data), spec);
        System.out.println(hr.getModel());
        HannanRissanen2 hr2 = new HannanRissanen2();
        hr2.process(new ReadDataBlock(data), spec);
        System.out.println(hr2.getModel());
    }

}
