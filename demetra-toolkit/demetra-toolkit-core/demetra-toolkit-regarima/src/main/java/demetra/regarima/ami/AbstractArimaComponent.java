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
package demetra.regarima.ami;

import jdplus.arima.IArimaModel;
import demetra.data.Parameter;
import demetra.design.Development;

/**
 * @author Jean Palate
 * @param <M>
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractArimaComponent<M extends IArimaModel> {

    private Parameter mean;

    protected void copy(AbstractArimaComponent<M> other) {
        if (other.mean != null) {
            mean = other.mean.clone();
        } else {
            mean = null;
        }
    }

    /**
     * @return the mean
     */
    public boolean isMean() {
        return mean != null;
    }

    public boolean isEstimatedMean() {
        return mean != null && !mean.isFixed();
    }

    /**
     * @param m
     */
    public void setMean(boolean m) {
        this.mean = m ? new Parameter() : null;
    }

    /**
     * @param value the mean to set
     */
    public void setMu(Parameter value) {
        this.mean = value;
    }

    public Parameter getMu() {
        return mean;
    }

    public double getMeanCorrection() {
        return mean == null ? 0 : mean.getValue();
    }

    @Override
    protected AbstractArimaComponent clone() throws CloneNotSupportedException {
        AbstractArimaComponent cl = (AbstractArimaComponent) super.clone();
        if (mean != null) {
            cl.mean = mean.clone();
        }
        return cl;
    }

    public abstract M getModel();

    public abstract void setModel(M value);
}
