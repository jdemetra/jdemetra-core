/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.ssf.implementation;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.eco.Likelihood;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.ssf.DefaultCompositeModel;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.ICompositeModel;
import ec.tstoolkit.ssf.LikelihoodEvaluation;
import ec.tstoolkit.ssf.PredictionErrorDecomposition;
import ec.tstoolkit.ssf.RegSsf;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfAlgorithm;
import ec.tstoolkit.ssf.SsfComposite;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfFunction;
import ec.tstoolkit.ssf.SsfFunctionInstance;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.structural.Component;
import ec.tstoolkit.structural.ModelSpecification;
import ec.tstoolkit.structural.SeasonalModel;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
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
        assertTrue(Math.abs(ll.getLogLikelihood() - llbsm.getLogLikelihood()) < 1e-6);
    }

    @Test
    @Ignore
    public void testProd() {
        DataBlock p = new DataBlock(new double[]{.2, 0, 1, .003, .003});
        HSMapping mapping = new HSMapping(new int[]{2,6,11});
        SsfComposite ssf = mapping.map(p);
        SsfModel<SsfComposite> model = new SsfModel(ssf, new SsfData(data.Data.P, null), null, null);
        SsfFunction<SsfComposite> fn = new SsfFunction<>(
                model,
                mapping,
                new SsfAlgorithm());

        LevenbergMarquardtMethod lm = new LevenbergMarquardtMethod();
        lm.minimize(fn, p);
        SsfFunctionInstance<SsfComposite> xfn = (SsfFunctionInstance<SsfComposite>) lm.getResult();
        IReadDataBlock np = mapping.map(xfn.ssf);
        DiffuseConcentratedLikelihood ll = xfn.getLikelihood();
        System.out.println("HS");
        System.out.println(ll.getUncorrectedLogLikelihood());
        
        Smoother smoother=new Smoother();
        SmoothingResults sr=new SmoothingResults();
        smoother.setSsf(xfn.ssf);
        smoother.process(new SsfData(data.Data.P, null), sr);
        
        DataBlock s=new DataBlock(data.Data.P.getLength());
        DataBlock z=new DataBlock(ssf.getStateDim());
        for (int i=0; i<s.getLength(); ++i){
            z.set(0);
            xfn.ssf.Z(i, z);
            z.range(0, 3).set(0);
            s.set(i, z.dot(sr.A(i)));
        }
        System.out.println(s);
        System.out.println(xfn.getParameters());
    }
}

class HSMapping implements IParametricMapping<SsfComposite> {

    private int[] noisySeasons;

    HSMapping(int[] noisy) {
        this.noisySeasons = noisy;
    }

    @Override
    public SsfComposite map(IReadDataBlock p) {
        SsfLocalLinearTrend lt = new SsfLocalLinearTrend(p.get(0) * p.get(0), p.get(1) * p.get(1));
        SsfNoise n = new SsfNoise(p.get(2) * p.get(2));
        double[] var = new double[12];
        double p0 = p.get(3), p1 = p.get(4);
        for (int i = 0; i < 12; ++i) {
            var[i] = p0 * p0;
        }
        for (int i = 0; i < noisySeasons.length; ++i) {
            var[noisySeasons[i]] = p1 * p1;
        }
        SsfHarrisonStevens s = new SsfHarrisonStevens(var);
        SsfComposite composite = new SsfComposite(new DefaultCompositeModel(lt, n, s));
        return composite;
    }

    private double getVar0(double[] var) {
        for (int i = 0; i < 12; ++i) {
            if (!isNoisy(i)) {
                return var[i];
            }
        }
        return 0;
    }

    private double getVar1(double[] var) {
        for (int i = 0; i < 12; ++i) {
            if (isNoisy(i)) {
                return var[i];
            }
        }
        return 0;
    }

    private boolean isNoisy(int p) {
        for (int j = 0; j < noisySeasons.length; ++j) {
            if (p == noisySeasons[j]) {
                return true;
            }
        }
        return false;

    }

    @Override
    public IReadDataBlock map(SsfComposite t) {
        double[] p = new double[5];
        ICompositeModel cm = t.getCompositeModel();
        SsfLocalLinearTrend lt = (SsfLocalLinearTrend) cm.getComponent(0);
        SsfNoise n = (SsfNoise) cm.getComponent(1);
        SsfHarrisonStevens s = (SsfHarrisonStevens) cm.getComponent(2);
        p[0] = Math.sqrt(lt.getVariance());
        p[1] = Math.sqrt(lt.getSlopeVariance());
        p[2] = Math.sqrt(n.getVariance());
        p[3] = Math.sqrt(getVar0(s.getVariances()));
        p[4] = Math.sqrt(getVar1(s.getVariances()));
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        for (int i = 0; i < inparams.getLength(); ++i) {
            if (Math.abs(inparams.get(i)) > 10) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        double p = inparams.get(idx);
        if (p < 0) {
            if (p < -1) {
                return p * .001;
            } else {
                return .001;
            }
        } else if (p > 1) {
            return -p * .001;
        } else {
            return -.001;
        }
    }

    @Override
    public int getDim() {
        return 5;
    }

    @Override
    public double lbound(int idx) {
        return -10;
    }

    @Override
    public double ubound(int idx) {
        return 10;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        boolean changed = false;
        for (int i = 0; i < ioparams.getLength(); ++i) {
            double p = ioparams.get(i);
            if (p > 10) {
                ioparams.set(i, 10 - 1 / p);
                changed = true;
            } else if (p < -10) {
                ioparams.set(i, -10 - 1 / p);
                changed = true;
            }
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    @Override
    public String getDescription(int idx) {
        return "stde" + (idx + 1);
    }
}
