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
package demetra.tramo;

import demetra.design.Development;
import demetra.modelling.TransformationType;
import demetra.timeseries.TimeSelector;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Data
public final class TransformSpec implements Cloneable{

    public static final double DEF_FCT = 0.95;

    @lombok.NonNull
    private TimeSelector span = TimeSelector.all();
    private double fct = DEF_FCT;
    private boolean preliminaryCheck = true;
    private TransformationType function = TransformationType.None;
    
    private static final TransformSpec DEFAULT=new TransformSpec();

    public TransformSpec() {
    }

    @Override
    public TransformSpec clone() {
        try {
            return (TransformSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public void reset() {
        span=TimeSelector.all();
        fct = DEF_FCT;
        preliminaryCheck = true;
        function = TransformationType.None;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

}
