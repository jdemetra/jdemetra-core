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

import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultSarimaInitializer implements IarimaInitializer {

    private final double ar_, ma_;

    public DefaultSarimaInitializer(){
        ar_=-.1;
        ma_=-.2;
    }

    public DefaultSarimaInitializer(double ar, double ma){
        ar_=ar;
        ma_=ma;
    }

    public SarimaModel initialize(RegArimaModel<SarimaModel> regs) {
        return doDefaultModel(regs.getArima().getSpecification());
    }

    public SarimaModel doDefaultModel(final SarimaSpecification spec) {
        SarimaModel m = new SarimaModel(spec);
        for (int j = 1; j <= spec.getP(); ++j) {
            m.setPhi(j, ar_);
        }
        for (int j = 1; j <= spec.getBP(); ++j) {
            m.setBPhi(j, ar_);
        }
        for (int j = 1; j <= spec.getQ(); ++j) {
            m.setTheta(j, ma_);
        }
        for (int j = 1; j <= spec.getBQ(); ++j) {
            m.setBTheta(j, ma_);
        }
        return m;
    }

    public SarimaModel doDefaultModel(final SarmaSpecification spec) {
        SarimaModel m = new SarimaModel(spec);
        for (int j = 1; j <= spec.getP(); ++j) {
            m.setPhi(j, ar_);
        }
        for (int j = 1; j <= spec.getBP(); ++j) {
            m.setBPhi(j, ar_);
        }
        for (int j = 1; j <= spec.getQ(); ++j) {
            m.setTheta(j, ma_);
        }
        for (int j = 1; j <= spec.getBQ(); ++j) {
            m.setBTheta(j, ma_);
        }
        return m;
    }
}
