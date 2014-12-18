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


package ec.tstoolkit.maths.matrices.lapack;

import ec.tstoolkit.design.Development;

/**
 * 
 * @author PCuser
 */
@Development(status = Development.Status.Exploratory)
public class Xerbla extends java.lang.RuntimeException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 4030556585412103077L;
    private String m_fn;

    private int m_info;

    /**
     * 
     * @param fn
     * @param info
     */
    public Xerbla(String fn, int info) {
	m_fn = fn;
	m_info = info;
    }

    /**
     * 
     * @return
     */
    public String getFn()
    {
	return m_fn;
    }

    /**
     * 
     * @return
     */
    public int getInfo() {
	return m_info;
    }
}
