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

package ec.tstoolkit.sarima.estimation;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SarimaFixedInitializer implements IarimaInitializer {

    private boolean m_failhr;

    private boolean[] fixedItems;

    private double[] parameters;

    /**
     * 
     * @param params
     * @param fixed
     */
    public SarimaFixedInitializer(double[] params, boolean[] fixed)
    {
	this.parameters = params;
	this.fixedItems = fixed;
    }

    /**
     *
     * @param spec
     * @param parameters
     * @param fixed
     */
    public SarimaFixedInitializer(SarimaSpecification spec,
	    IReadDataBlock parameters, boolean[] fixed) {
	this.parameters = new double[parameters.getLength()];
	parameters.copyTo(this.parameters, 0);
	this.fixedItems = fixed;
    }

    /**
     * 
     * @return
     */
    public boolean getfailHR() {
	return m_failhr;
    }

    @Override
    public SarimaModel initialize(RegArimaModel<SarimaModel> regs) {
	try {
	    SarimaModel sarima = regs.getArima();
	    if (sarima.getParametersCount() == 0)
		return sarima;
	    SarimaSpecification spec = sarima.getSpecification();

	    HannanRissanen hr = new HannanRissanen();
	    SarmaSpecification dspec = spec.doStationary();
	    DataBlock dy = null;
	    if (regs.getDModel().getVarsCount() > 0) {
		Ols ols = new Ols();
		if (!ols.process(regs.getDModel()))
		    return null;
		dy = ols.getResiduals();
	    } else
		dy = regs.getDModel().getY();
	    if (!hr.process(dy, dspec) && m_failhr)
		return null;
	    SarimaModel m = hr.getModel();
	    if (!m.isStable(true) && m_failhr)
		return null;
	    else {
		DataBlock tmp = new DataBlock(m.getParameters());
		int i = 0;
		// check if the new set is valid...
		while (++i <= 5) {
		    for (int j = 0; j < parameters.length; ++j)
			if (fixedItems[j])
			    tmp.set(j, parameters[j]);
		    m.setParameters(tmp);
		    if (!SarimaMapping.stabilize(m))
			break;
		}
		if (i > 5)
		    return null;
	    }
	    return m;
	} catch (BaseException ex) {
	    return null;
	}
    }

    /**
     * 
     * @param value
     */
    public void setFailHR(boolean value) {
	m_failhr = value;
    }
}
