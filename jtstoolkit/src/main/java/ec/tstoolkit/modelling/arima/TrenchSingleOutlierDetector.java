/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.ConcentratedLikelihoodEstimation;
import ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.LevelShiftFactory;
import ec.tstoolkit.timeseries.regression.SeasonalOutlierFactory;
import ec.tstoolkit.timeseries.regression.TransitoryChangeFactory;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TrenchSingleOutlierDetector<T extends IArimaModel> extends AbstractSingleOutlierDetector<T>
{

    private TrenchSolver m_solver;

    private double m_threshold = 1e-6;

    /**
     *
     * @return
     */
    @Override
    protected boolean calc() {
	m_solver = new TrenchSolver(getModel());
	calcYL();
	processOutliers();
	return true;
    }

    private void calcYL() {
	/*
	 * ConcentratedLikelihood cll = getModel().Likelihood(); double[] yl =
	 * getModel().DModel.CalcRes(cll.B);
	 * 
	 * ArmaKF kf = new ArmaKF(getModel().Arma); Likelihood ll = new
	 * Likelihood(); kf.Process(new RC(yl), ll); m_yl = ll.Residuals;
	 * calcMAD(ll.Residuals);
	 */
	ConcentratedLikelihoodEstimation estimator = new ConcentratedLikelihoodEstimation(
		new ModifiedLjungBoxFilter());
	estimator.estimate(getModel());
	calcMAD(new DataBlock(estimator.getResiduals(), getModel().getArma()
		.getARCount()
		+ getModel().getArma().getMACount(),
		estimator.getResiduals().length, 1));
    }

    private void processdefOutlier(int io) {
	int n = getModel().getY().getLength();
	int d = getModel().getDifferencingFilter().getDegree();
	double[] xo = new double[n + d];
	IOutlierFactory fac = getOutlierFactory(io);
	IOutlierVariable o = fac.create(getDomain().get(d));
	o.data(getDomain().getStart(), new DataBlock(xo));
	double[] dao = new double[n];
	getModel().getDifferencingFilter().filter(new DataBlock(xo),
		new DataBlock(dao));
	int ni = 0;
	for (int i = 0; i < n; ++i)
	    if (Math.abs(dao[i]) > m_threshold)
		++ni;

	int[] idx = new int[ni];
	for (int i = 0, j = 0; i < n; ++i)
	    if (Math.abs(dao[i]) > m_threshold)
		idx[j++] = i;

	for (int a = 0; a < n; ++a)
	    if (isDefined(a, io))
		setT(a, io, m_solver.TStat(d, a, dao, idx) / getMAD());
    }

    private void processOutliers() {
	int d = getModel().getDifferencingFilter().getDegree();
	//
	TrenchSolver dsolver = null;
	for (int io = 0; io < getOutlierFactoriesCount(); ++io) {
	    IOutlierFactory o = getOutlierFactory(io);
	    if (o instanceof TransitoryChangeFactory) {
		TransitoryChangeFactory tc = (TransitoryChangeFactory) o;
		processtcOutlier(io, tc.getCoefficient());
	    } else if (d > 0)
		processdefOutlier(io);
	    else {
		LevelShiftFactory ls = null;
		if (o instanceof LevelShiftFactory)
		    ls = (LevelShiftFactory) o;
		SeasonalOutlierFactory s = null;
		if (o instanceof SeasonalOutlierFactory)
		    s = (SeasonalOutlierFactory) o;
		if (ls != null || s != null) {
		    if (dsolver == null) {
			dsolver = m_solver.clone();
			dsolver.difference();
		    }
		    processstOutlier(dsolver, io);
		} else
		    processdefOutlier(io);
	    }
	}
    }

    private void processstOutlier(TrenchSolver solver, int io) {
	int n = getModel().getY().getLength();
	double[] xo = new double[n];
	IOutlierFactory fac = getOutlierFactory(io);
	IOutlierVariable o = fac.create(getDomain().getStart());
	o.data(getDomain().getStart(), new DataBlock(xo));
	double[] dao = new double[n];
	DataBlock DAO = new DataBlock(dao);
	getModel().getDifferencingFilter().filter(new DataBlock(xo), DAO);
	DAO.difference();
	int ni = 0;
	for (int i = 0; i < n; ++i)
	    if (Math.abs(dao[i]) > m_threshold)
		++ni;

	int[] idx = new int[ni];
	for (int i = 0, j = 0; i < n; ++i)
	    if (Math.abs(dao[i]) > m_threshold)
		idx[j++] = i;
	for (int a = 0; a < n; ++a)
	    if (isDefined(a, io))
		setT(a, io, solver.TStat(0, a, dao, idx) / getMAD());
    }

    private void processtcOutlier(int io, double delta) {
	int n = getModel().getY().getLength();
	int d = getModel().getDifferencingFilter().getDegree();
	double[] xo = new double[n + d];
	IOutlierFactory fac = getOutlierFactory(io);
	TrenchSolver solver = m_solver.clone();

	solver.difference(delta);
	int[] idx = null;
	double[] dao = null;

	for (int a = 0; a < n; ++a) {
	    if (a == d || (isDefined(a, io) && a < d)) {
		IOutlierVariable o = fac.create(getDomain().get(a));
		o.data(getDomain().getStart(), new DataBlock(xo));
		dao = new double[n];
		DataBlock DAO = new DataBlock(dao);
		getModel().getDifferencingFilter().filter(new DataBlock(xo),
			DAO);
		DAO.difference(delta);
		int ni = 0;
		for (int i = 0; i < n; ++i)
		    if (Math.abs(dao[i]) > m_threshold)
			++ni;

		idx = new int[ni];
		for (int i = 0, j = 0; i < n; ++i)
		    if (Math.abs(dao[i]) > m_threshold)
			idx[j++] = i;
	    }
	    if (isDefined(a, io))
		if (a <= d)
		    setT(a, io, solver.TStat(0, 0, dao, idx) / getMAD());
		else
		    setT(a, io, solver.TStat(d, a, dao, idx) / getMAD());
	}
    }
}
