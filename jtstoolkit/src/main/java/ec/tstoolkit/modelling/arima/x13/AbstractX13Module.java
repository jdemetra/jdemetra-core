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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public abstract class AbstractX13Module {

    protected IFunctionMinimizer minimizer;

    protected AbstractX13Module() {
        minimizer = new ProxyMinimizer(new LevenbergMarquardtMethod());
        minimizer.setConvergenceCriterion(1e-5);
    }

    /**
     * @return the minimizer
     */
    public IFunctionMinimizer getMinimizer() {
        return minimizer;
    }

    public GlsSarimaMonitor getMonitor() {
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        monitor.setMinimizer(minimizer.exemplar());
        return monitor;
    }

    /**
     * @param minimizer the minimizer to set
     */
    public void setMinimizer(IFunctionMinimizer minimizer) {
        this.minimizer = minimizer;
    }

}
