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

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.ChangeOfRegimeSpec;
import ec.tstoolkit.modelling.RegressionTestSpec;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class MovingHolidaySpec implements Cloneable, InformationSetSerializable {

    public static final String TYPE = "type", PARAM = "param", TEST = "test", CHANGEOFREGIME = "changeofregime";

    public final static int DEF_EASTERDUR = 8;
    
    public static void fillDictionary(String prefix, Map<String, Class> dic) {
          dic.put(InformationSet.item(prefix, TYPE), String.class);
          dic.put(InformationSet.item(prefix, PARAM), Integer.class);
          dic.put(InformationSet.item(prefix, TEST), String.class);
          dic.put(InformationSet.item(prefix, CHANGEOFREGIME), String.class);
   }
   public static enum Type {

        None, Easter, Thank, SCEaster, Labor, JulianEaster
    }
    private ChangeOfRegimeSpec changeofregime_;
    private int w_;
    private RegressionTestSpec test_ = RegressionTestSpec.None;
    private Type type_;
    
    public static MovingHolidaySpec easterSpec(boolean pretest) {
        return easterSpec(pretest, false);
    }
     
    public static MovingHolidaySpec easterSpec(boolean pretest, boolean julian) {
      MovingHolidaySpec easter = new MovingHolidaySpec();
        easter.test_ = RegressionTestSpec.Add;
        easter.type_ = julian ? Type.JulianEaster : Type.Easter;
        easter.w_ = DEF_EASTERDUR;
        easter.test_ = pretest ? RegressionTestSpec.Add : RegressionTestSpec.None;
        return easter;
    }
    

    public MovingHolidaySpec() {
    }
    
    public Type getType() {
        return type_;
    }
    
    public void setType(Type value) {
        type_ = value;
    }
    
    public int getW() {
        return w_;
    }
    
    public void setW(int value) {
        w_ = value;
    }
    
    public ChangeOfRegimeSpec getChangeOfRegime() {
        return changeofregime_;
    }
    
    public void setChangeOfRegime(ChangeOfRegimeSpec value) {
        changeofregime_ = value;
    }
    
    public RegressionTestSpec getTest() {
        return test_;
    }
    
    public void setTest(RegressionTestSpec value) {
        test_ = value;
    }
    
    @Override
    public MovingHolidaySpec clone() {
        try {
            MovingHolidaySpec rslt = (MovingHolidaySpec) super.clone();
            if (changeofregime_ != null) {
                rslt.changeofregime_ = changeofregime_.clone();
            }
            return rslt;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof MovingHolidaySpec && equals((MovingHolidaySpec) obj));
    }
    
    private boolean equals(MovingHolidaySpec other) {
        return Objects.equals(changeofregime_, other.changeofregime_)
                && test_ == other.test_ && type_ == other.type_ && w_ == other.w_;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.changeofregime_);
        hash = 53 * hash + this.w_;
        hash = 53 * hash + Objects.hashCode(this.test_);
        hash = 53 * hash + Objects.hashCode(this.type_);
        return hash;
    }
    
    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(TYPE, type_.name());
        if (verbose || w_ != 0) {
            info.add(PARAM, w_);
        }
        if (verbose || test_ != RegressionTestSpec.None) {
            info.add(TEST, test_.name());
        }
        if (changeofregime_ != null){
            info.add(CHANGEOFREGIME, changeofregime_.toString());
        }
        return info;
    }
    
    @Override
    public boolean read(InformationSet info) {
        try {
            String type=info.get(TYPE, String.class);
            if (type == null)
                return false;
            type_=Type.valueOf(type);
            Integer w = info.get(PARAM, Integer.class);
            if (w != null) {
                w_ = w;
            }
            String test=info.get(TEST, String.class);
            if (test != null) {
                test_ = RegressionTestSpec.valueOf(test);
            }
            String cr=info.get(CHANGEOFREGIME, String.class);
            if (cr != null) {
                changeofregime_=ChangeOfRegimeSpec.fromString(cr);
                if (changeofregime_ == null)
                    return false;
            }
           return true;
        } catch (Exception err) {
            return false;
        }
    }
    
}
