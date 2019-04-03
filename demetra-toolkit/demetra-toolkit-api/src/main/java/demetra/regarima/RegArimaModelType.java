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

import demetra.arima.ArimaType;
import demetra.design.Development;
import demetra.linearmodel.LinearModelType;
import demetra.maths.MatrixType;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder(toBuilder=true)
public class RegArimaModelType {
    
    @lombok.NonNull
    private LinearModelType model;
    
    @lombok.NonNull
    private ArimaType arima;
    
    //<editor-fold defaultstate="collapsed" desc="delegate to model">
    public DoubleSeq getY() {
        return model.getY();
    }
    
    public boolean isMeanCorrection() {
        return model.isMeanCorrection();
    }
    
    public MatrixType getX() {
        return model.getX();
    }
    //</editor-fold>
}
