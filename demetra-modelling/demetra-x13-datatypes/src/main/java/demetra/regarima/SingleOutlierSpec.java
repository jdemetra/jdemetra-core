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

package demetra.regarima;

import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class SingleOutlierSpec {

    private final String type;
    private final double cv;

    public SingleOutlierSpec(String type, double cv) {
        this.type = type;
        this.cv=cv;
    }
    
    public String getType() {
        return type;
    }

    public double getCriticalValue() {
        return cv;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.type);
        hash = 79 * hash + Double.hashCode(this.cv);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SingleOutlierSpec && equals((SingleOutlierSpec) obj));
    }
    
    private boolean equals(SingleOutlierSpec other) {
        return other.cv == cv && other.type.equals(type);
    }

}
