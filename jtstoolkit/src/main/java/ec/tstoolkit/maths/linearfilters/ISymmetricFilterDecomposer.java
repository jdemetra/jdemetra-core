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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.design.Development;

/**
 * Generic interface that describe the following factorization problem: Given a
 * symmetric filter S(B, F), find D(B), D(F), v such that S(B, F) = v * D(B) *
 * D(F), D(0) = 1
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface ISymmetricFilterDecomposer {

    /**
     * Decomposes the given symmetric filter
     *
     * @param sf The filter being factorized
     * @return True if the decomposition was successful. A decomposition is
     * considered as successful when the scaling factor v is strictly positive.
     * However, implementations should provide computable decompositions even if
     * they are not valid.
     * Uncomputable decompositions should be identified by a 0 scaling factor
     * and polynomials in B/F set to null.
     */
    boolean decompose(SymmetricFilter sf);

    /**
     * Gets the polynomial in B of the factorization.
     *
     * @return The polynomial in B of the factorization. Can be null.
     */
    BackFilter getBFilter();

    /**
     * Gets the polynomial in F of the factorization.
     *
     * @return The polynomial in F of the factorization. Can be null.
     */
    ForeFilter getFFilter();

    /**
     * Gets the scaling factor.
     *
     * @return A strictly positive factor if the decomposition was successful.
     */
    double getFactor();
}
