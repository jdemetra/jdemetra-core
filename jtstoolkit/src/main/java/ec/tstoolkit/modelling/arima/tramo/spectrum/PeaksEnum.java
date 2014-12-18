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

package ec.tstoolkit.modelling.arima.tramo.spectrum;

/**
 *
 * @author gianluca
 */
public class PeaksEnum {

    static public enum AR {

        A, a, none, undef;
        public boolean isPresent(){
            return this == A || this == a;
        }
    }

    static public enum Tukey {

        T, t, none, undef;
        
        public boolean isPresent(){
            return this == T || this == t;
        }
    }
    public final AR ar;
    public final Tukey tu;
    
    public static final PeaksEnum NONE=new PeaksEnum(AR.none, Tukey.none);
    public static final PeaksEnum UNDEF=new PeaksEnum(AR.undef, Tukey.undef);
    public static final PeaksEnum ALL=new PeaksEnum(AR.A, Tukey.T);

    public PeaksEnum(AR ar, Tukey tu) {
        this.ar = ar;
        this.tu = tu;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.ar != null ? this.ar.hashCode() : 0);
        hash = 73 * hash + (this.tu != null ? this.tu.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof PeaksEnum && equals((PeaksEnum) obj));
    }

    public boolean equals(PeaksEnum peaks) {
        return this.ar == peaks.ar && this.tu == peaks.tu;
    }

    public String artoString() {
        if (this.ar == AR.A) {
            return "A";
        } else if (this.ar == AR.a) {
            return "a";
        } else if (this.ar == AR.none) {
            return "-";
        } else {
            return "n";
        }
    }

    public String tutoString() {
        if (this.tu == Tukey.T) {
            return "T";
        } else if (this.tu == Tukey.t) {
            return "t";
        } else if (this.tu == Tukey.none) {
            return "-";
        } else {
            return "c";
        }
    }

    @Override
    public String toString() {
        return this.artoString() + this.tutoString();
    }
}
