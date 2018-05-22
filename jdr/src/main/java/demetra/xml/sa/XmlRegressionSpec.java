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


package demetra.xml.sa;

import demetra.xml.regression.XmlInterventionVariable;
import demetra.xml.regression.XmlRamp;
import demetra.xml.regression.XmlTsVariableDescriptor;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.utilities.Arrays2;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlRegressionSpec.NAME)
public class XmlRegressionSpec {
    static final String NAME = "regressionSpecType";

    @XmlElementWrapper(name="preSpecifiedOutliers")
    @XmlElements(value={
        @XmlElement(name="ao", type=XmlAoDefinition.class),
        @XmlElement(name="ls", type=XmlLsDefinition.class),
        @XmlElement(name="tc", type=XmlTcDefinition.class),
        @XmlElement(name="so", type=XmlSoDefinition.class)
    })
    public AbstractXmlOutlierDefinition[] preSpecifiedOutlier;
    @XmlElement(name="variable")
    public XmlTsVariableDescriptor[] variables;
    @XmlElement(name="intervention")
    public XmlInterventionVariable[] interventions;
    @XmlElement(name="ramp")
    public XmlRamp[] ramps;

    public XmlRegressionSpec() { }

    public boolean isEmpty() {
        return preSpecifiedOutlier == null && variables == null && ramps == null && interventions == null;
    }

    public void add(OutlierDefinition[] def) {
        if (Arrays2.isNullOrEmpty(def))
            return;
        preSpecifiedOutlier = new AbstractXmlOutlierDefinition[def.length];
        for (int i = 0; i < def.length; ++i)
            preSpecifiedOutlier[i] = AbstractXmlOutlierDefinition.create(def[i]);
    }

    public void add(TsVariableDescriptor[] def) {
        if (Arrays2.isNullOrEmpty(def))
            return;
        variables = new XmlTsVariableDescriptor[def.length];
        for (int i = 0; i < def.length; ++i) {
            variables[i] = new XmlTsVariableDescriptor();
            variables[i].copy(def[i]);
        }
    }

    public void add(InterventionVariable[] def) {
        if (Arrays2.isNullOrEmpty(def))
            return;
        interventions = new XmlInterventionVariable[def.length];
        for (int i = 0; i < def.length; ++i) {
            interventions[i] = new XmlInterventionVariable();
            interventions[i].copy(def[i]);
        }
    }

    public void add(Ramp[] def) {
        if (Arrays2.isNullOrEmpty(def))
            return;
        ramps = new XmlRamp[def.length];
        for (int i = 0; i < def.length; ++i) {
            ramps[i] = new XmlRamp();
            ramps[i].copy(def[i]);
        }
    }

    public OutlierDefinition[] createOutliers() {
        if (preSpecifiedOutlier == null)
            return null;
        OutlierDefinition[] x = new OutlierDefinition[preSpecifiedOutlier.length];
        for (int i = 0; i < x.length; ++i)
            x[i] = preSpecifiedOutlier[i].getDefinition();
        return x;
    }

    public TsVariableDescriptor[] createVariables() {
        if (variables == null)
            return null;
        TsVariableDescriptor[] x = new TsVariableDescriptor[variables.length];
        for (int i = 0; i < variables.length; ++i)
            x[i] = variables[i].create();
        return x;
     }

    public InterventionVariable[] createInterventions() {
        if (interventions == null)
            return null;
        InterventionVariable[] x = new InterventionVariable[interventions.length];
        for (int i = 0; i < interventions.length; ++i)
            x[i] = interventions[i].create();
        return x;
     }

    public Ramp[] createRamps() {
        if (ramps == null)
            return null;
        Ramp[] x = new Ramp[ramps.length];
        for (int i = 0; i < ramps.length; ++i)
            x[i] = ramps[i].create();
         return x;
    }
}
