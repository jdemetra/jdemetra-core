/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.ssf.implementation;

import ec.tstoolkit.eco.Likelihood;
import ec.tstoolkit.ssf.DefaultCompositeModel;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.LikelihoodEvaluation;
import ec.tstoolkit.ssf.PredictionErrorDecomposition;
import ec.tstoolkit.ssf.SsfComposite;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.structural.Component;
import ec.tstoolkit.structural.ModelSpecification;
import ec.tstoolkit.structural.SeasonalModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PCUser
 */
public class SsfHarrisonStevensTest {

    public SsfHarrisonStevensTest() {
     }

    @Test
    public void testSomeMethod() {
       SsfLocalLinearTrend lt = new SsfLocalLinearTrend(.2, .1);
        SsfNoise n = new SsfNoise(1);
        SsfHarrisonStevens s = new SsfHarrisonStevens(12, .3);
        SsfComposite composite = new SsfComposite(new DefaultCompositeModel(lt, n, s));
        Filter f = new Filter();
        f.setSsf(composite);
        PredictionErrorDecomposition pe = new PredictionErrorDecomposition(true);
        f.process(new SsfData(data.Data.P, null), pe);
        Likelihood ll = new Likelihood();
        LikelihoodEvaluation.evaluate(pe, ll);
 
        ModelSpecification spec = new ModelSpecification();
        spec.setSeasonalModel(SeasonalModel.HarrisonStevens);

        BasicStructuralModel bsm = new BasicStructuralModel(spec, 12);
        bsm.setVariance(Component.Slope, .1);
        bsm.setVariance(Component.Level, .2);
        bsm.setVariance(Component.Seasonal, .3);
        bsm.setVariance(Component.Noise, 1);

        PredictionErrorDecomposition pebsm = new PredictionErrorDecomposition(true);
        f.setSsf(bsm);
        f.process(new SsfData(data.Data.P, null), pebsm);
        Likelihood llbsm = new Likelihood();
        LikelihoodEvaluation.evaluate(pebsm, llbsm);
        assertTrue(Math.abs(ll.getLogLikelihood()-llbsm.getLogLikelihood())<1e-6);
    }

}
