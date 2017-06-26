/*
 * Copyright 2017 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.likelihood;

import demetra.eco.EcoException;

/**
 * Device for computing the determinantal term of likelihood functions in a safe way.
 * See the likelihood classes for further information
 * @author Jean Palate
 */
public class DeterminantalTerm {
    
    private final static double LOG2= Math.log(2.0);
    
    private double detcar, detman = 1;

    /**
     *
     */
    public DeterminantalTerm() {
    }

    /**
     * 
     * @param var
     */
    public void add(final double var) {
 	update(var);
    }

    /**
         *
         */
    public void clear() {
	detman = 1;
	detcar = 0;
    }

    /**
     * 
     * @param n
     * @return
     */
    public double factor(final int n) {
	double det = Math.pow(detman, (1.0 / n));
	det *= Math.pow(2.0, (detcar / n));
	return det;
    }

    /**
     * 
     * @return
     */
    public double getLogDeterminant() {
	return Math.log(detman) + detcar * LOG2;
    }

    /**
     * 
     * @param var
     */
    public void remove(final double var) {
	update(1 / var);
    }

    private void update(final double var) {
        if (!Double.isFinite(var)) {
            throw new EcoException(EcoException.INV_VAR);
        }
        if (var <= 0) {
            throw new EcoException(EcoException.NEG_VAR);
        }
	detman *= var;
	while (detman >= 1) {
	    detman *= .0625;
	    detcar += 4.0; // 2^4 = 16, 1/16 = 0.0625
	}

	while (detman != 0 && detman <= 0.0625) {
	    detman *= 16;
	    detcar -= 4.0;
	}
    }

}
