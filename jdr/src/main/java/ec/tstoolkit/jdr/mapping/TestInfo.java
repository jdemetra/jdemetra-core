/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.mapping;

import demetra.information.InformationMapping;
import ec.tstoolkit.information.StatisticalTest;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class TestInfo {
    static final InformationMapping<StatisticalTest> MAPPING = new InformationMapping<>(StatisticalTest.class);

    static {
        MAPPING.set("value", Double.class, test->test.value);
        MAPPING.set("pvalue", Double.class, test->test.pvalue);
        MAPPING.set("description", String.class, test->test.description);
    }

    public InformationMapping<StatisticalTest> getMapping() {
        return MAPPING;
    }
    
}
