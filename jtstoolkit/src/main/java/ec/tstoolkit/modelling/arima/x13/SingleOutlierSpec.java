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

import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class SingleOutlierSpec implements Cloneable {

    private OutlierType type_;
    private double cv_;

    public SingleOutlierSpec() {
    }

    public SingleOutlierSpec(OutlierType type) {
        type_ = type;
    }

    public SingleOutlierSpec(OutlierType type, double cv) {
        type_ = type;
        cv_=cv;
    }
    
    public OutlierType getType() {
        return type_;
    }

    public void setType(OutlierType value) {
        type_ = value;
    }

    public double getCriticalValue() {
        return cv_;
    }

    public void setCriticalValue(double value) {
        cv_ = value;
    }
    
    @Override
    public SingleOutlierSpec clone(){
        try {
            return (SingleOutlierSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.type_);
        hash = 79 * hash + Jdk6.Double.hashCode(this.cv_);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SingleOutlierSpec && equals((SingleOutlierSpec) obj));
    }
    
    private boolean equals(SingleOutlierSpec other) {
        return other.cv_ == cv_ && other.type_ == type_;
    }

}
