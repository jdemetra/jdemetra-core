/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved 
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
package demetra.dstats;

import demetra.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class DStatException extends RuntimeException {

    private final String origin;

    /**
         *
         */
    public static final String 
            ERR_INV_SMALL = "Can't compute probability inverse (value is too near 0 or 1)",
	    ERR_ITER = "Too many iterations in search procedure", 
            ERR_PARAM = "Invalid definition",
            ERR_UNDEFINED = "Undefined";

    /**
     *
     */
    public DStatException() {
        origin=null;
    }

    /**
     * 
     * @param msg
     */
    public DStatException(final String msg) {
	super(msg);
        this.origin=null;
    }

    /**
     * 
     * @param msg
     */
    public DStatException(final String msg, final String origin) {
	super(msg);
        this.origin=origin;
    }

    public String getOrigin(){
        return origin;
    }
}
