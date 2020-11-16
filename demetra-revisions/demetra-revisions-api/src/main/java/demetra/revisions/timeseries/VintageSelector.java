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

import nbbrd.design.Development;
import lombok.AccessLevel;

/**
 *
 * @author PALATEJ
 * @param <K>
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@Deprecated
public class VintageSelector<K extends Comparable> {

    private static VintageSelector ALL = new VintageSelector(VintageSelectorType.All, 0, 0, null, null);

    VintageSelectorType type;
    private int n0, n1;
    private K k0, k1;

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
        return new VintageSelector(VintageSelectorType.Excluding, n0, n1, null, null);
    }

    /**
     *
     * @param n
     * @return
     */
    public static VintageSelector first(final int n) {
        return new VintageSelector(VintageSelectorType.First, n, 0, null, null);
    }

    /**
     *
     * @param n
     * @return
     */
    public static VintageSelector last(final int n) {
        return new VintageSelector(VintageSelectorType.Last, 0, n, null, null);
    }

    /**
     * Selects vintages between k0 and k1 (included)
     *
     * @param <K>
     * @param k0
     * @param k1
     * @return
     */
    public static <K extends Comparable> VintageSelector custom(final K k0, final K k1) {
        return new VintageSelector(VintageSelectorType.Custom, 0, 0, k0, k1);
    }

    /**
     * Selects vintage at position n
     *
     * @param <K>
     * @param k
     * @return
     */
    public static <K extends Comparable> VintageSelector custom(final K k) {
        return new VintageSelector(VintageSelectorType.Custom, 0, 0, k, k);
    }

}
