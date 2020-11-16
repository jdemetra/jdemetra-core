/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.extractors;

import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.x11.X11Results;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class X11Extractor {
    private final InformationMapping<X11Results> MAPPING = new InformationMapping<>(X11Results.class);

    static {
    }

    public InformationMapping<X11Results> getMapping() {
        return MAPPING;
    }
}
