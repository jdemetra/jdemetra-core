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

package demetra.ucarima;

import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.ComplexUtility;
import demetra.maths.polynomials.Polynomial;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractRootSelector implements IRootSelector {

    protected Polynomial selected, notSelected;

    /**
     *
     */
    public AbstractRootSelector() {
    }

    /**
     * 
     * @param root
     * @return
     */
    public abstract boolean accept(final Complex root);

    @Override
    public Polynomial getOutofSelection() {
	return notSelected;
    }

    @Override
    public Polynomial getSelection() {
	return selected;
    }

    @Override
    public boolean select(final Polynomial p) {
	if (p.degree() == 0) {
	    notSelected = p;
	    selected = null;
	    return false;
	} else {
	    Complex[] roots = p.roots();
	    List<Complex> sel = new ArrayList<>(), nsel = new ArrayList<>();
	    for (int i = 0; i < roots.length; ++i)
		if (accept(roots[i]))
		    sel.add(roots[i]);
		else
		    nsel.add(roots[i]);
	    if (sel.isEmpty()) {
		selected = null;
		notSelected = p;
		return false;
	    } else if (nsel.isEmpty()) {
		notSelected = null;
		selected = p;
		return true;
	    } else {
		Complex[] rs =sel.toArray(new Complex[sel.size()]);
		Complex[] rn = nsel.toArray(new Complex[sel.size()]);

		ComplexUtility.lejaOrder(rs);
		ComplexUtility.lejaOrder(rn);
		selected = Polynomial.fromComplexRoots(rs);
		selected = selected.times(p.get(0) / selected.get(0));
		notSelected = Polynomial.fromComplexRoots(rn);
		notSelected = notSelected.divide(notSelected.get(0));
		return true;
	    }
	}
    }
}
