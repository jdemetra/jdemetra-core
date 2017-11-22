/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.x11;

import demetra.information.InformationMapping;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class DecompositionInfo {
    
    static final InformationMapping<X11Results> MAPPING = new InformationMapping<>(X11Results.class);

    static {
        MAPPING.set("b1", TsData.class, source->source.getData("b-tables.b1", TsData.class));
        MAPPING.set("d8", TsData.class, source->source.getData("d-tables.d8", TsData.class));
        MAPPING.set("d10", TsData.class, source->source.getData("d-tables.d10", TsData.class));
        MAPPING.set("d11", TsData.class, source->source.getData("d-tables.d11", TsData.class));
        MAPPING.set("d12", TsData.class, source->source.getData("d-tables.d12", TsData.class));
        MAPPING.set("d13", TsData.class, source->source.getData("d-tables.d13", TsData.class));
    }

    public InformationMapping<X11Results> getMapping() {
        return MAPPING;
    }
    
}
