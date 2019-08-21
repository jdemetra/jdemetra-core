/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts;

import jdplus.ssf.StateInfo;
import jdplus.ssf.StateStorage;
import jdplus.ssf.akf.AkfToolkit;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.ssf.implementations.MultivariateCompositeSsf;
import jdplus.ssf.multivariate.M2uAdapter;
import jdplus.ssf.multivariate.SsfMatrix;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.StateFilteringResults;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import demetra.likelihood.Likelihood;
import demetra.maths.Optimizer;
import demetra.ssf.LikelihoodType;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
public class CompositeModelEstimation {

    public static CompositeModelEstimation estimationOf(CompositeModel model, FastMatrix data, double eps, LikelihoodType lt, Optimizer optimizer,boolean concentrated, double[] parameters) {
        CompositeModelEstimation rslt = new CompositeModelEstimation();
        rslt.data = data;
        MstsMonitor monitor = MstsMonitor.builder()
                .likelihood(lt)
                .optimizer(optimizer)
                .concentratedLikelihood(concentrated)
                .precision(eps)
                .build();
        monitor.process(data, model.getMapping(), parameters == null ? null : DoubleSeq.of(parameters));
        rslt.likelihood = monitor.getLikelihood();
        rslt.ssf = monitor.getSsf();
        rslt.cmpPos = rslt.getSsf().componentsPosition();
        rslt.parameters = monitor.getParameters().toArray();
        rslt.fullParameters = monitor.fullParameters().toArray();
        rslt.parametersName = model.getMapping().parametersName();
        rslt.cmpName=model.getCmpsName();
        return rslt;
    }

    public static CompositeModelEstimation computationOf(CompositeModel model, FastMatrix data, DoubleSeq fullParameters, boolean marginal, boolean concentrated) {
        CompositeModelEstimation rslt = new CompositeModelEstimation();
        rslt.data = data;
        rslt.fullParameters = fullParameters.toArray();
        model.getMapping().fixModelParameters(p->true, fullParameters);
        rslt.parameters = DoubleSeq.EMPTYARRAY;
        rslt.ssf = model.getMapping().map(Doubles.EMPTY);
        rslt.cmpPos = rslt.getSsf().componentsPosition();
        rslt.cmpName=model.getMapping().parametersName();
        rslt.parametersName = model.getMapping().parametersName();
        if (marginal) {
            rslt.likelihood = AkfToolkit.marginalLikelihoodComputer(concentrated).
                    compute(M2uAdapter.of(rslt.getSsf()), M2uAdapter.of(new SsfMatrix(data)));
        } else {
            rslt.likelihood = DkToolkit.likelihood(rslt.getSsf(), new SsfMatrix(data), true, false);
        }
        return rslt;
    }

    private Likelihood likelihood;
    private MultivariateCompositeSsf ssf;
    private int[] cmpPos;
    private FastMatrix data;
    private double[] fullParameters, parameters;
    private String[] parametersName, cmpName;
    private StateStorage smoothedStates, filteredStates, filteringStates;

    public StateStorage getSmoothedStates() {
        if (smoothedStates == null) {
            StateStorage ss = AkfToolkit.smooth(getSsf(), new SsfMatrix(getData()), true, false);
//            StateStorage ss = DkToolkit.smooth(getSsf(), new SsfMatrix(getData()), true, false);
            if (likelihood.isScalingFactor()) {
                ss.rescaleVariances(likelihood.sigma());
            }
            smoothedStates = ss;
        }
        return smoothedStates;
    }

    public StateStorage getFilteredStates() {
        if (filteredStates == null) {

            ISsf ussf = M2uAdapter.of(ssf);
            ISsfData udata = M2uAdapter.of(new SsfMatrix(data));
            StateFilteringResults fr = new StateFilteringResults(StateInfo.Concurrent, true);
            int m = data.getColumnsCount(), n = data.getRowsCount();
            fr.prepare(ussf.getStateDim(), 0, udata.length());
            DkToolkit.sqrtFilter(ussf, udata, fr, true);
            StateStorage ss = StateStorage.full(StateInfo.Forecast);
            ss.prepare(ussf.getStateDim(), 0, n);
            for (int i = 1; i <= n; ++i) {
                ss.save(i - 1, fr.a(i * m - 1), fr.P(i * m - 1));
            }
            if (likelihood.isScalingFactor()) {
                ss.rescaleVariances(likelihood.sigma());
            }
            filteredStates = ss;
        }
        return filteredStates;
    }

    public StateStorage getFilteringStates() {
        if (filteringStates == null) {

            ISsf ussf = M2uAdapter.of(ssf);
            ISsfData udata = M2uAdapter.of(new SsfMatrix(data));
            DefaultDiffuseSquareRootFilteringResults fr = DkToolkit.sqrtFilter(ussf, udata, true);
            StateStorage ss = StateStorage.full(StateInfo.Forecast);
            int m = data.getColumnsCount(), n = data.getRowsCount();
            ss.prepare(ussf.getStateDim(), 0, n);
            int nd = fr.getEndDiffusePosition() / m;
            if (fr.getEndDiffusePosition() % m != 0) {
                ++nd;
            }
            for (int i = 0; i < n; ++i) {
                ss.save(i, fr.a(i * m), fr.P(i * m));
            }
            for (int i = 0; i < nd; ++i) {
                ss.a(i).set(Double.NaN);
                ss.P(i).set(Double.NaN);
            }
            if (likelihood.isScalingFactor()) {
                ss.rescaleVariances(likelihood.sigma());
            }
            filteringStates = ss;
        }
        return filteringStates;
    }

    /**
     * @return the likelihood
     */
    public Likelihood getLikelihood() {
        return likelihood;
    }

    /**
     * @return the ssf
     */
    public MultivariateCompositeSsf getSsf() {
        return ssf;
    }

    /**
     * @return the cmpPos
     */
    public int[] getCmpPos() {
        return cmpPos;
    }

    /**
     * @return the cmpPos
     */
    public String[] getCmpName() {
        return cmpName;
    }
    /**
     * @return the data
     */
    public FastMatrix getData() {
        return data;
    }

    /**
     * @return the fullParameters
     */
    public double[] getFullParameters() {
        return fullParameters;
    }

    /**
     * @return the parameters
     */
    public double[] getParameters() {
        return parameters;
    }

    public String[] getParametersName() {
        return parametersName;
    }
}
