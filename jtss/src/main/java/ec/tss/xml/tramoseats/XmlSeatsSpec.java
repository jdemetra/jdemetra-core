/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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


package ec.tss.xml.tramoseats;

import ec.satoolkit.seats.SeatsSpecification;
import ec.tss.xml.IXmlConverter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlSeatsSpec.NAME)
public class XmlSeatsSpec implements IXmlConverter<SeatsSpecification> {
    static final String NAME = "seatsSpecType";

    @XmlElement
    public Double epsphi;
    public boolean isEpsphiSpecified() {
        return epsphi != null;
    }
    @XmlElement
    public Double rmod;
    public boolean isRmodSpecified() {
        return rmod != null;
    }
    @XmlElement
    public Double xl;
    public boolean isXlSpecified() {
        return xl != null;
    }
    @XmlElement
    public SeatsSpecification.ApproximationMode mode;

    @Override
    public SeatsSpecification create() {
        SeatsSpecification spec= new SeatsSpecification();
        if (isEpsphiSpecified())
            spec.setSeasTolerance(epsphi);
        if (isRmodSpecified())
            spec.setTrendBoundary(rmod);
        if (isXlSpecified())
            spec.setXlBoundary(xl);
        if (mode != null)
            spec.setApproximationMode(mode);
        return spec;
    }

    @Override
    public void copy(SeatsSpecification t) {
        if (t.getSeasTolerance() != SeatsSpecification.DEF_EPSPHI)
            epsphi = t.getSeasTolerance();
        if (t.getTrendBoundary() != SeatsSpecification.DEF_RMOD)
            rmod = t.getTrendBoundary();
        if (t.getXlBoundary() != SeatsSpecification.DEF_XL)
            xl = t.getXlBoundary();
        if (t.getApproximationMode() != SeatsSpecification.ApproximationMode.Legacy)
            mode = t.getApproximationMode();
    }
}
