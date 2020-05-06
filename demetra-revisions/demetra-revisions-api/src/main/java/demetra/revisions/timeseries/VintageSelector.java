/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.revisions.timeseries;

import demetra.design.Development;
import lombok.AccessLevel;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VintageSelector {

    private static VintageSelector ALL = new VintageSelector(VintageSelectorType.All, 0, 0);

    VintageSelectorType type;
    private int n0, n1;

    /**
     *
     * @return
     */
    public static VintageSelector all() {
        return ALL;
    }

    /**
     *
     * @param n0
     * @param n1
     * @return
     */
    public static VintageSelector excluding(final int n0, final int n1) {
        return new VintageSelector(VintageSelectorType.Excluding, n0, n1);
    }

    /**
     *
     * @param n
     * @return
     */
    public static VintageSelector first(final int n) {
        return new VintageSelector(VintageSelectorType.First, n, 0);
    }

    /**
     *
     * @param n
     * @return
     */
    public static VintageSelector last(final int n) {
        return new VintageSelector(VintageSelectorType.Last, 0, n);
    }

    /**
     * Selects vintages between positions n0 and n1 (included)
     *
     * @param n0
     * @param n1
     * @return
     */
    public static VintageSelector custom(final int n0, final int n1) {
        return new VintageSelector(VintageSelectorType.Custom, n0, n1);
    }

    /**
     * Selects vintage at position n
     *
     * @param n
     * @return
     */
    public static VintageSelector custom(final int n) {
        return new VintageSelector(VintageSelectorType.Custom, n, n);
    }

}
