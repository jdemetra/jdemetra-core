/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.ssf.ckms.CkmsToolkit;
import demetra.ssf.dk.DefaultDiffuseFilteringResults;
import demetra.ssf.dk.DkConcentratedLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Algorithms {
    public double[] filter(ISsf model, double[] data){
        SsfData s=new SsfData(data);
        DefaultDiffuseFilteringResults rslt = DkToolkit.filter(model, s, false);
        return rslt.errors().toArray();
    }

    public double[] sqrtFilter(ISsf model, double[] data){
        SsfData s=new SsfData(data);
        DefaultDiffuseSquareRootFilteringResults rslt = DkToolkit.sqrtFilter(model, s, false);
        return rslt.errors().toArray();
    }
    
    public double concentratedLikelihood(ISsf model, double[] data){
        SsfData s=new SsfData(data);
        DkConcentratedLikelihood dll = DkToolkit.concentratedLikelihoodComputer().compute(model, s);
        return dll.logLikelihood();
    }
}
