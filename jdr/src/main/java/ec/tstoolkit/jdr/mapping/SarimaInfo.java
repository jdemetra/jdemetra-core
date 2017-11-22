/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tstoolkit.jdr.mapping;

import demetra.information.InformationMapping;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SarimaInfo {
    static final InformationMapping<SarimaModel> MAPPING = new InformationMapping<>(SarimaModel.class);

    static {
        MAPPING.set("parameters", double[].class, model->
        {
            IReadDataBlock p = model.getParameters();
            double[] x=new double[p.getLength()];
            p.copyTo(x, 0);
            return x;
        });
        MAPPING.set("p", Integer.class, model->model.getRegularAROrder());
        MAPPING.set("d", Integer.class, model->model.getRegularDifferenceOrder());
        MAPPING.set("q", Integer.class, model->model.getRegularMAOrder());
        MAPPING.set("bp", Integer.class, model->model.getSeasonalAROrder());
        MAPPING.set("bd", Integer.class, model->model.getSeasonalDifferenceOrder());
        MAPPING.set("bq", Integer.class, model->model.getSeasonalMAOrder());
    }

    public InformationMapping<SarimaModel> getMapping() {
        return MAPPING;
    }
    
}
