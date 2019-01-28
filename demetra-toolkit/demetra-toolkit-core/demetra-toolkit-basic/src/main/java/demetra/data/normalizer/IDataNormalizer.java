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
package demetra.data.normalizer;

import demetra.data.DataBlock;
import demetra.design.Algorithm;


/**
 * 
 * @author Jean Palate
 */
@Algorithm
public interface IDataNormalizer {
    /**
     * 
     * @param data On exit, data contains data*factor; 
     * @return The scaling factor
     */
    double normalize(DataBlock data);
}
