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

import ec.tstoolkit.arima.ArimaException;
import ec.tstoolkit.arima.estimation.IGlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class IterativeGlsSarimaMonitor extends IGlsArimaMonitor<SarimaModel>
{

    private final IarimaInitializer m_initializer;

    private boolean m_all;

    /**
     *
     */
    public IterativeGlsSarimaMonitor() {
	m_initializer = new SarimaInitializer();
	m_all = true;
    }

    /**
     * 
     * @param checkAll
     */
    public IterativeGlsSarimaMonitor(boolean checkAll) {
	m_initializer = new SarimaInitializer();
	m_all = checkAll;
    }

    /**
     * 
     * @param checkAll
     * @param initializer
     */
    public IterativeGlsSarimaMonitor(boolean checkAll, IarimaInitializer initializer) {
	m_initializer = initializer;
	m_all = checkAll;
    }

    /**
     * 
     * @param regs
     * @return
     */
    @Override
    public SarimaModel initialize(RegArimaModel<SarimaModel> regs) {

	if (m_initializer != null)
	    return m_initializer.initialize(regs);
	else
	    return null;
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(
	    RegArimaModel<SarimaModel> regs, SarimaModel start) {
	if (start != null && !start.isStationary())
	    throw new ArimaException(ArimaException.NonStationary);
        boolean nomapper=getMapping() == null;
	if (nomapper) {
	    setMapping(new SarimaMapping(regs.getArima().getSpecification(),
		    m_all));
	}
	RegArimaEstimation<SarimaModel> rslt= super.optimize(regs, start);
 	SarimaModel sarima = rslt.model.getArima();
	if (SarimaMapping.stabilize(sarima))
	    rslt.model.setArima(sarima);
       if (nomapper)
            setMapping(null);
        return rslt;
    }
}
