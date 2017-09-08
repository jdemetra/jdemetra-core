/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.tsprovider.grid;

import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.ObsGathering;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
@lombok.Value(staticConstructor = "of")
public class GridImport {

    public static final GridImport DEFAULT = new GridImport(ObsFormat.DEFAULT, ObsGathering.DEFAULT);

    private ObsFormat obsFormat;
    private ObsGathering obsGathering;
}
