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


package demetra.x11plus;

import demetra.design.Development;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public enum SeasonalFilterOption {

    S3X1,
    /**
     *
     */
    S3X3,
    /**
     * 
     */
    S3X5,
    /**
     *
     */
    S3X9,
    /**
     * 
     */
    S3X15,
    /**
     *
     */
    Stable,
    /**
     * 
     */
    X11Default,
    /**
     *
     */
    Msr;
}
