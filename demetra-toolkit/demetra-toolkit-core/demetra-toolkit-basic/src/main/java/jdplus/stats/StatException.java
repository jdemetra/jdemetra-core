/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.stats;

import demetra.design.Development;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class StatException extends RuntimeException
{
    /**
     * 
     */
    public StatException()
    {
    }

    /**
     * 
     * @param msg
     */
    public StatException(final String msg)
    {
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public StatException(final String message, final Exception innerException)
    {
	super(message, innerException);
    }
    
    public static final String NO_DATA="No data", NOT_ENOUGH_DATA="Not enough data"; 

 }
