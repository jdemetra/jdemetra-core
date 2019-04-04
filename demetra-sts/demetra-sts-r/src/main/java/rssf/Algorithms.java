/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;
import demetra.msts.CompositeModel;
import demetra.msts.MstsMonitor;
import demetra.ssf.StateStorage;
import demetra.ssf.dk.DefaultDiffuseFilteringResults;
import demetra.ssf.likelihood.DiffuseLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;

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
            DiffuseLikelihood dll = DkToolkit.likelihood(model, s);
            return dll.logLikelihood();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    public StateStorage smooth(ISsf model, double[] data, boolean all) {
        SsfData s = new SsfData(data);
        return DkToolkit.sqrtSmooth(model, s, all, true);
    }

    public double diffuseLikelihood(IMultivariateSsf model, Matrix data) {
        try {
            SsfMatrix s = new SsfMatrix(data);
            DiffuseLikelihood dll = DkToolkit.likelihood(model, s);
            return dll.logLikelihood();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    public StateStorage smooth(IMultivariateSsf model, Matrix data, boolean all) {
        SsfMatrix s = new SsfMatrix(data);
        return DkToolkit.smooth(model, s, all, true);
    }
    
    public double diffuseLikelihood(CompositeModel model, Matrix data, double[] parameters){
        MultivariateCompositeSsf mssf = model.getMapping().map(DoubleSeq.copyOf(parameters));
        DiffuseLikelihood likelihood = DkToolkit.likelihood(mssf, new SsfMatrix(data));
        return likelihood.logLikelihood();
    }
    
    public double[] estimate(CompositeModel model, Matrix data){
        MstsMonitor monitor=MstsMonitor.builder()
                .build();
        monitor.process(data, model.getMapping(), null);
        return monitor.fullParameters().toArray();
    }
}
