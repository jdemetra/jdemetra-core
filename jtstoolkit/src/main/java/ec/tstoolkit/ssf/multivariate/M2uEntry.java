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
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class M2uEntry
{

    /**
     *
     */
    /**
     *
     */
    public int it, ivar;

    /**
     * 
     */
    public M2uEntry()
    {
	this(0, 0);
    }

    /**
     * 
     * @param pos
     * @param var
     */
    public M2uEntry(final int pos, final int var)
    {
	it = pos;
	ivar = var;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof M2uEntry && equals((M2uEntry) obj));
    }
    
    private boolean equals(M2uEntry other) {
        return other.it == it && other.ivar == ivar;
    }

    @Override
    public int hashCode() {
	return (it >> 4) + ivar;
    }
}
