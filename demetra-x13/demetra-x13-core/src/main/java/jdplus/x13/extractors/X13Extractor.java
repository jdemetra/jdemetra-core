/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.extractors;

import nbbrd.design.Development;
import demetra.information.InformationMapping;
import jdplus.x13.X13Results;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class X13Extractor {
    private final InformationMapping<X13Results> MAPPING = new InformationMapping<>(X13Results.class);

    static {
    }

    public InformationMapping<X13Results> getMapping() {
        return MAPPING;
    }
}
