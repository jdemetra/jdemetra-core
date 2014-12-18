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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.eco.CoefficientEstimation;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
public class OutlierEstimation extends CoefficientEstimation {

    public OutlierEstimation(CoefficientEstimation e, IOutlierVariable outlier) {
        super(e.getValue(), e.getStdev());
        position_ = outlier.getPosition();
        code_ = outlier.getOutlierType();
    }

    //    public OutlierDefinition getOutlierDefinition() {
    //        return new OutlierDefinition(position_, code_);
    //    }
    public TsPeriod getPosition() {
        return position_;
    }

    public OutlierType getCode() {
        return code_;
    }
    
    private TsPeriod position_;
    private OutlierType code_;
    
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(position_).append(": ").append(code_).append("=").append(super.toString());
        return builder.toString();
    }
}
