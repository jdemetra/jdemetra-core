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
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MatrixException extends BaseException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1679354782776944548L;
    /**
         *
         */
    public final static String IncompatibleDimensions = "m_err_dim";

    /**
     *
     */
    public final static String SquareOnly = "m_err_square";

    /**
     *
     */
    public final static String CholeskyFailed = "m_err_chol";

    /**
         *
         */
    public final static String RankError = "m_err_rank";

    /**
         *
         */
    public final static String Singular = "m_err_sing";
    /**
     *
     */
    public final static String EigenInit = "eig_init";
    /**
         *
         */
    public final static String EigenFailed = "eig_failed";
    /**
         *
         */
    public final static String NotImpl = "err_notimpl";

    /**
     *
     */
    public MatrixException() {
    }

    /**
     * 
     * @param msg
     */
    public MatrixException(final String msg) {
	// super(g_origin, g_rmgr.GetString(msg));
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public MatrixException(final String message, final Exception innerException) {
	super(message, innerException);
    }

    /**
     * 
     * @param msg
     * @param origin
     */
    public MatrixException(final String msg, final String origin) {
	// super(g_origin, g_rmgr.GetString(msg));
	super(msg, origin, 0);
    }

    // TODO: resourcemanager
    // private static readonly System.Resources.ResourceManager g_rmgr = new
    // System.Resources.ResourceManager("Nbb.Maths.Properties.Resources",
    // System.Reflection.Assembly.GetCallingAssembly());

}
