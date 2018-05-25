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

package demetra.x12;

import demetra.modelling.ChangeOfRegimeSpec;
import demetra.modelling.RegressionTestSpec;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class MovingHolidaySpec{


    public final static int DEF_EASTERDUR = 8;
    
    public static enum Type {

        None, Easter, Thank, SCEaster, Labor, JulianEaster
    }
    private ChangeOfRegimeSpec changeofregime;
    private int w;
    private RegressionTestSpec test = RegressionTestSpec.None;
    private Type type;
    
    public static MovingHolidaySpec easterSpec(boolean pretest) {
        return easterSpec(pretest, false);
    }
     
    public static MovingHolidaySpec easterSpec(boolean pretest, boolean julian) {
      MovingHolidaySpec easter = new MovingHolidaySpec();
        easter.test = RegressionTestSpec.Add;
        easter.type = julian ? Type.JulianEaster : Type.Easter;
        easter.w = DEF_EASTERDUR;
        easter.test = pretest ? RegressionTestSpec.Add : RegressionTestSpec.None;
        return easter;
    }
    

    public MovingHolidaySpec() {
    }
    
    public MovingHolidaySpec(MovingHolidaySpec other) {
        this.changeofregime=other.changeofregime;
        this.test=other.test;
        this.type=other.type;
        this.w=other.w;
    }
    
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type value) {
        type = value;
    }
    
    public int getW() {
        return w;
    }
    
    public void setW(int value) {
        if (value <= 0 || value > 25)
            throw new IllegalArgumentException("Should be in [1,25]");
        w = value;
    }
    
    public ChangeOfRegimeSpec getChangeOfRegime() {
        return changeofregime;
    }
    
    public void setChangeOfRegime(ChangeOfRegimeSpec value) {
        changeofregime = value;
    }
    
    public RegressionTestSpec getTest() {
        return test;
    }
    
    public void setTest(RegressionTestSpec value) {
        test = value;
    }
   
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof MovingHolidaySpec && equals((MovingHolidaySpec) obj));
    }
    
    private boolean equals(MovingHolidaySpec other) {
        return Objects.equals(changeofregime, other.changeofregime)
                && test == other.test && type == other.type && w == other.w;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.changeofregime);
        hash = 53 * hash + this.w;
        hash = 53 * hash + Objects.hashCode(this.test);
        hash = 53 * hash + Objects.hashCode(this.type);
        return hash;
    }
    
}
