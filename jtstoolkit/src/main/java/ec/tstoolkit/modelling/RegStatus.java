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


package ec.tstoolkit.modelling;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public enum RegStatus {
    Undefined,
    /**
     * Pre-specified regression variable. Always belongs to the regression model
     */
    Prespecified,

    /**
     * Regression variable that had to be tested (ToAdd / ToRemove) and that has
     * been accepted.
     */
    Accepted,

    /**
     * Regression variable that had to be tested (ToAdd / ToRemove) and that has
     * been rejected.
     */
    Rejected,

     /**
     * Regression variable that has to be tested.
     * Doesn't belong to the current model
     */
   ToAdd,

     /**
     * Regression variable that has to be tested.
     * Belongs to the current model
     */
   ToRemove,

     /**
     * Regression variable that could be added/removed
     * Belongs to the current model
     */
   Volatile,
    /**
     * Regression variable that has been excluded for any unexpected reason
     */
    Excluded;

   public boolean isSelected(){
       if (this == ToAdd || this == Rejected || this == Excluded)
           return false;
       else
           return true;
   }

   public boolean needTesting(){
       return this != Excluded && this != Prespecified;
   }

   public boolean isDefined(){
       return this != ToAdd && this != ToRemove;
   }
}
