/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.x11;

import demetra.information.InformationMapping;
import ec.satoolkit.x11.Mstatistics;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class MstatisticsInfo {
    
    static final InformationMapping<Mstatistics> MAPPING = new InformationMapping<>(Mstatistics.class);

    static {
        MAPPING.setArray("M", 1, 11, Double.class, (source, i)->source.getM(i));
        MAPPING.set("Q", Double.class, source->source.getQ());
        MAPPING.set("Q-M2", Double.class, source->source.getQm2());
    }

    public InformationMapping<Mstatistics> getMapping() {
        return MAPPING;
    }
    
}
