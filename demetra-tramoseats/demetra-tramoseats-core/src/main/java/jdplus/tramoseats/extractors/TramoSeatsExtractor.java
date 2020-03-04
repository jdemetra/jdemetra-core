/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.extractors;

import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.tramoseats.TramoSeatsResults;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class TramoSeatsExtractor {
    private final InformationMapping<TramoSeatsResults> MAPPING = new InformationMapping<>(TramoSeatsResults.class);

    static {
    }

    public InformationMapping<TramoSeatsResults> getMapping() {
        return MAPPING;
    }
}
