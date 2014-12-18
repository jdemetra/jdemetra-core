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
package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.StationaryTransformation;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;

/**
 * 
 * @param <M>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class RegArimaModel<M extends IArimaModel> extends AbstractRegArimaModel
	implements Cloneable {

    private M m_arima, m_arma;

    /**
     *
     */
    public RegArimaModel() {
    }

    /**
     * 
     * @param m
     */
    protected RegArimaModel(final AbstractRegArimaModel m) {
	super(m, false);
    }

    /**
     * 
     * @param arima
     */
    public RegArimaModel(final M arima) {
	m_arima = arima;
    }

    /**
     * 
     * @param arima
     * @param y
     */
    public RegArimaModel(final M arima, final DataBlock y) {
	super(y);
	m_arima = arima;
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void calstationarymodel() {
	StationaryTransformation st = m_arima.stationaryTransformation();
	m_ur = st.unitRoots;
	m_arma = (M) st.stationaryModel;
    }

    @Override
    public RegArimaModel<M> clone() {
	return (RegArimaModel<M>) super.clone();
    }

    /**
     * 
     * @return
     */
    public M getArima() {
	return m_arima;
    }

    /**
     * 
     * @return
     */
    public M getArma() {
	if (m_arma == null)
	    calstationarymodel();
	return m_arma;
    }

    /**
     * 
     * @return
     */
    public ConcentratedLikelihood computeLikelihood() {
	ConcentratedLikelihood ll = new ConcentratedLikelihood();
	M stmodel = getArma();
	RegModel dregs = getDModel();
	ArmaKF kf = new ArmaKF(stmodel);
	Matrix dvars = dregs.variables();
	if (dvars == null)
	    if (kf.process(dregs.getY(), ll))
		return ll;
	    else
		return null;
	else
        {
            SubArrayOfInt ao=null;
            int nm=this.getMissingsCount();
            if (nm>0){
                int[] o=new int[nm];
                int del=this.isMeanCorrection() ? 1 : 0;
                for (int i=0; i<nm; ++i)
                    o[i]=del+i;
                ao=SubArrayOfInt.create(o);
            } 
            if (kf.process(dregs.getY(), ao, dvars
		.subMatrix(), ll))
	    return ll;
	else
	    return null;}
    }

    /**
     * 
     * @param value
     */
    @SuppressWarnings("unchecked")
    public void setArima(final M value) {
	m_arima = value;
	// optimization: dregs are not modified if the unit toors are similar...
	StationaryTransformation st = m_arima.stationaryTransformation();

	BackFilter ur = st.unitRoots;
	M arma = (M) st.stationaryModel;
	if (m_ur != null && !m_ur.equals(ur))
	    m_dregs = null;
	m_ur = ur;
	m_arma = arma;
    }
}
