/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DifferencingModuleTest {
    
    public DifferencingModuleTest() {
    }

    @Test
    public void testBAR() {
        SarimaSpecification xspec=new SarimaSpecification(12);
        xspec.setBP(1);
        SarimaModel model=new SarimaModel(xspec);
        model.setBPhi(1, -.9);
        ArimaModelBuilder builder=new ArimaModelBuilder();
        double[] data = builder.generate(model, 240);
        DifferencingModule diff=new DifferencingModule();
        diff.setSeas(true);
        diff.process(new ReadDataBlock(data), 12);
        System.out.println(diff.getD());
        System.out.println(diff.getBD());
        System.out.println(diff.getTMean());
        System.out.println(diff.isMeanCorrection());
    }
    
}
