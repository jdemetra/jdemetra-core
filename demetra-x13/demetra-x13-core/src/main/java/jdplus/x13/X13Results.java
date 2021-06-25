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
package jdplus.x13;

import demetra.x11.X11Results;
import demetra.x13.X13Finals;
import demetra.x13.X13Preadjustment;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.x13.extractors.X13Extractor;
import demetra.information.Explorable;

/**
 *
 * @author palatej
 */
@lombok.Value
public class X13Results implements Explorable {

    private RegSarimaModel preprocessing;
    private X13Preadjustment preadjustment;
    private X11Results decomposition;
    private X13Finals finals;

}
