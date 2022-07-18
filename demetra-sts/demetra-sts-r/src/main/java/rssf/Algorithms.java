/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.data.Doubles;
import jdplus.msts.CompositeModel;
import jdplus.msts.MstsMonitor;
import jdplus.ssf.StateStorage;
import jdplus.ssf.dk.DefaultDiffuseFilteringResults;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.ssf.composite.MultivariateCompositeSsf;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.SsfMatrix;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.SsfData;
import jdplus.math.matrices.FastMatrix;
import jdplus.msts.MstsMapping;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Algorithms {

    public double[] filter(ISsf model, double[] data) {
        SsfData s = new SsfData(data);
        DefaultDiffuseFilteringResults rslt = DkToolkit.filter(model, s, false);
        return rslt.errors().toArray();
    }

    public double[] sqrtFilter(ISsf model, double[] data) {
        SsfData s = new SsfData(data);
        DefaultDiffuseSquareRootFilteringResults rslt = DkToolkit.sqrtFilter(model, s, false);
        return rslt.errors().toArray();
    }

    public double diffuseLikelihood(ISsf model, double[] data) {
        try {
            SsfData s = new SsfData(data);
            DiffuseLikelihood dll = DkToolkit.likelihood(model, s, true, false);
            return dll.logLikelihood();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    public StateStorage smooth(ISsf model, double[] data, boolean all) {
        SsfData s = new SsfData(data);
        return DkToolkit.sqrtSmooth(model, s, all, true);
    }

    public double diffuseLikelihood(IMultivariateSsf model, FastMatrix data) {
        try {
            SsfMatrix s = new SsfMatrix(data);
            DiffuseLikelihood dll = DkToolkit.likelihood(model, s, true, false);
            return dll.logLikelihood();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    public StateStorage smooth(IMultivariateSsf model, FastMatrix data, boolean all) {
        SsfMatrix s = new SsfMatrix(data);
        return DkToolkit.smooth(model, s, all, true);
    }
    
    public double diffuseLikelihood(CompositeModel model, FastMatrix data, double[] parameters){
        MstsMapping mapping = model.mapping();
        MultivariateCompositeSsf mssf = mapping.map(Doubles.of(parameters));
        DiffuseLikelihood likelihood = DkToolkit.likelihood(mssf, new SsfMatrix(data), true, false);
        return likelihood.logLikelihood();
    }
    
    public double[] estimate(CompositeModel model, FastMatrix data){
        MstsMapping mapping = model.mapping();
        MstsMonitor monitor=MstsMonitor.builder()
                .build();
        monitor.process(data, mapping, null);
        return monitor.fullParameters().toArray();
    }
}
