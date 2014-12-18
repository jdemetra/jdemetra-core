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


package ec.tstoolkit;

import ec.tstoolkit.design.Development;

/**
 * Base class for all exceptions of the framework
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String m_origin;

    private int m_id;

    /**
     * Default unspecified exception
     */
    public BaseException() {
    }

    /**
     * Exception with message
     * @param msg The message that explains the exception
     */
    public BaseException(final String msg) {
	super(msg);
    }

    /**
     * Exception with message and inner exception
     * @param message The message
     * @param innerException The inner exception
     */
    public BaseException(final String message, final Exception innerException) {
	super(message, innerException);
    }

    /**
     * Exception with message, origin and identifier
     * @param msg The message
     * @param origin The origin of the exception
     * @param id The identifier of the exception
     */
    public BaseException(final String msg, final String origin, final int id) {
	super(msg);
	m_origin = origin;
	m_id = id;
    }

    /**
     * Gets the identifier
     * @return The identifier. 0 by default.
     */
    public int getId() {
	return m_id;
    }

    // / <summary>Origin (module or class) of the exception</summary>
    /**
     * Gets the origin of the exception. 
     * @return The origin of the exception. May be null
     */
    public String getOrigin() {
	return m_origin;
    }

    /**
     * Set information for this exception
     * @param origin Origin of the exception
     * @param id Identifier of the exception
     */
    public void setInfo(final String origin, final int id) {
	m_origin = origin;
	m_id = id;
    }
}
