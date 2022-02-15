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
package demetra.x13;

import demetra.sa.SaDictionaries;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X13Dictionary {

    // Preadjustment
    public final String PREADJUST = "preadjustment";

    public final String[] A_TABLE = new String[]{"a1", "a1a", "a1b", "a6",
        "a7", "a8", "a8t", "a8s", "a8i", "a9", "a9u", "a9sa", "a9ser"};

    // Decomposition
    public final String X11 = SaDictionaries.DECOMPOSITION;

    public final String[] B_TABLE = new String[]{"b1", "b2", "b3", "b4", "b5",
        "b6", "b7", "b8", "b9", "b10", "b11", "b13", "b17", "b20"};
    public final String[] C_TABLE = new String[]{"c1", "c2", "c4",
        "c6", "c7", "c9", "c10", "c11", "c13", "c17", "c20"};
    public final String[] D_TABLE = new String[]{"d1", "d2", "d4",
        "d5", "d6", "d7", "d8", "d9", "d10", "d11", "d12", "d13"};

    // finals
    public final String FINAL = "";

    public final String[] D_TABLE_FINAL = new String[]{"d10final", "d11final", "d12final", "d13final", "d16", "d18",
        "d10a", "d11a", "d12a", "d16a", "d18a"};
    public final String[] E_TABLE = new String[]{"e1", "e2", "e3", "e11"};
}
