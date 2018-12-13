/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DoubleSequence;
import demetra.likelihood.ILikelihood;
import demetra.maths.matrices.Matrix;
import demetra.ssf.StateInfo;
import demetra.ssf.StateStorage;
import demetra.ssf.akf.AkfToolkit;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.StateFilteringResults;

/**
 *
 * @author palatej
 */
public class CompositeModelEstimation {

    public static CompositeModelEstimation estimationOf(CompositeModel model, Matrix data, double eps, boolean marginal, boolean concentrated, double[] parameters) {
        CompositeModelEstimation rslt = new CompositeModelEstimation();
        rslt.data = data;
        MstsMonitor monitor = MstsMonitor.builder()
                .marginalLikelihood(marginal)
                .concentratedLikelihood(concentrated)
                .precision(eps)
                .build();
        monitor.process(data, model.getMapping(), parameters == null ? null : DoubleSequence.ofInternal(parameters));
        rslt.likelihood = monitor.getLikelihood();
        rslt.ssf = monitor.getSsf();
        rslt.cmpPos = rslt.getSsf().componentsPosition();
        rslt.parameters = monitor.getParameters().toArray();
        rslt.fullParameters = monitor.fullParameters().toArray();
        rslt.parametersName = model.getMapping().parametersName();
        return rslt;
    }

    public static CompositeModelEstimation computationOf(CompositeModel model, Matrix data, DoubleSequence fullParameters, boolean marginal, boolean concentrated) {
        CompositeModelEstimation rslt = new CompositeModelEstimation();
        rslt.data = data;
        rslt.fullParameters = fullParameters.toArray();
        DoubleSequence fp = model.getMapping().functionParameters(fullParameters);
        rslt.parameters = fp.toArray();
        rslt.ssf = model.getMapping().map(fp);
        rslt.cmpPos = rslt.getSsf().componentsPosition();
        rslt.parametersName = model.getMapping().parametersName();
        if (marginal) {
            rslt.likelihood = AkfToolkit.marginalLikelihoodComputer(concentrated).
                    compute(M2uAdapter.of(rslt.getSsf()), M2uAdapter.of(new SsfMatrix(data)));
        } else {
            rslt.likelihood = DkToolkit.likelihood(rslt.getSsf(), new SsfMatrix(data));
        }
        return rslt;
    }

    private ILikelihood likelihood;
    private MultivariateCompositeSsf ssf;
    private int[] cmpPos;
    private Matrix data;
    private double[] fullParameters, parameters;
    private String[] parametersName;
    private StateStorage smoothedStates, filteredStates, filteringStates;

    public StateStorage getSmoothedStates() {
        if (smoothedStates == null) {
            smoothedStates = DkToolkit.smooth(getSsf(), new SsfMatrix(getData()), true);
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
            // TO BE CORRECTED
            int nd = ussf.getDiffuseDim()/ m;
            for (int i = 0; i < nd; ++i) {
                ss.a(i).set(Double.NaN);
                ss.P(i).set(Double.NaN);
            }
            ss.rescaleVariances(likelihood.sigma());
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
            ss.rescaleVariances(likelihood.sigma());
            filteringStates = ss;
        }
        return filteringStates;
    }

    /**
     * @return the likelihood
     */
    public ILikelihood getLikelihood() {
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
     * @return the data
     */
    public Matrix getData() {
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
