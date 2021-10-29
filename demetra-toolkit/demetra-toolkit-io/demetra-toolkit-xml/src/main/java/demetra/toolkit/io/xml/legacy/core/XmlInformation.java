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
package demetra.toolkit.io.xml.legacy.core;

import demetra.data.Parameter;
import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.processing.AlgorithmDescriptor;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsData;
import demetra.timeseries.TsMoniker;
import demetra.timeseries.regression.modelling.RegressionItem;
import demetra.toolkit.io.xml.legacy.DummyAdapter;
import demetra.toolkit.io.xml.legacy.XmlConverterAdapter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
//@XmlRootElement(name = XmlInformation.RNAME)
@XmlType(name = XmlInformation.NAME)
public class XmlInformation {

    static final String NAME = "informationType";
    static final String RNAME = "information";
    static final HashMap<Class, XmlAdapter> fromXmlMap = new HashMap<>();
    static final HashMap<Class, XmlAdapter> toXmlMap = new HashMap<>();

    // default mapping
    static {
        DummyAdapter dummy = new DummyAdapter();
        fromXmlMap.put(Double.class, dummy);
        fromXmlMap.put(Integer.class, dummy);
        fromXmlMap.put(String.class, dummy);
        fromXmlMap.put(Boolean.class, dummy);
        fromXmlMap.put(boolean[].class, dummy);
        //fromXmlMap.put(String[].class, dummy);
        toXmlMap.put(Double.class, dummy);
        toXmlMap.put(Integer.class, dummy);
        toXmlMap.put(String.class, dummy);
        toXmlMap.put(Boolean.class, dummy);
        toXmlMap.put(boolean[].class, dummy);
        //toXmlMap.put(String[].class, dummy);

        XmlConverterAdapter<XmlStrings, String[]> strMapper
                = new XmlConverterAdapter<>(XmlStrings.class);
        fromXmlMap.put(XmlStrings.class, strMapper);
        toXmlMap.put(String[].class, strMapper);

        XmlConverterAdapter<XmlDoubles, double[]> doublesMapper
                = new XmlConverterAdapter<>(XmlDoubles.class);
        fromXmlMap.put(XmlDoubles.class, doublesMapper);
        toXmlMap.put(double[].class, doublesMapper);

        XmlConverterAdapter<XmlIntegers, int[]> intsMapper
                = new XmlConverterAdapter<>(XmlIntegers.class);
        fromXmlMap.put(XmlIntegers.class, intsMapper);
        toXmlMap.put(int[].class, intsMapper);

        XmlConverterAdapter<XmlInformationSet, InformationSet> infoMapper
                = new XmlConverterAdapter<>(XmlInformationSet.class);
        fromXmlMap.put(XmlInformationSet.class, infoMapper);
        toXmlMap.put(InformationSet.class, infoMapper);

        XmlConverterAdapter<XmlAlgorithm, AlgorithmDescriptor> algMapper
                = new XmlConverterAdapter<>(XmlAlgorithm.class);

        fromXmlMap.put(XmlAlgorithm.class, algMapper);
        toXmlMap.put(AlgorithmDescriptor.class, algMapper);

        fromXmlMap.put(XmlTsData.class, XmlTsData.getAdapter());
        toXmlMap.put(TsData.class, XmlTsData.getAdapter());

        fromXmlMap.put(XmlPeriodSelection.class, XmlPeriodSelection.getAdapter());
        toXmlMap.put(TimeSelector.class, XmlPeriodSelection.getAdapter());

        fromXmlMap.put(XmlParameters.class, XmlParameters.getAdapter());
        toXmlMap.put(Parameter[].class, XmlParameters.getAdapter());

        fromXmlMap.put(XmlParameter.class, XmlParameter.getAdapter());
        toXmlMap.put(Parameter.class, XmlParameter.getAdapter());

        fromXmlMap.put(XmlTsMoniker.class, XmlTsMoniker.getAdapter());
        toXmlMap.put(TsMoniker.class, XmlTsMoniker.getAdapter());

        fromXmlMap.put(XmlTs.class, XmlTs.getTsAdapter());
        toXmlMap.put(Ts.class, XmlTs.getTsAdapter());

        fromXmlMap.put(XmlTsCollection.class, XmlTsCollection.getTsAdapter());
        toXmlMap.put(TsCollection.class, XmlTsCollection.getTsAdapter());

        XmlConverterAdapter<XmlRegItem, RegressionItem> regitemMapper
                = new XmlConverterAdapter<>(XmlRegItem.class);

        fromXmlMap.put(XmlRegItem.class, regitemMapper);
        toXmlMap.put(RegressionItem.class, regitemMapper);

        XmlConverterAdapter<XmlStatisticalTest, StatisticalTest> statsMapper
                = new XmlConverterAdapter<>(XmlStatisticalTest.class);

        fromXmlMap.put(XmlStatisticalTest.class, statsMapper);
        toXmlMap.put(StatisticalTest.class, statsMapper);

        fromXmlMap.put(XmlMatrix.class, XmlMatrix.getAdapter());
        toXmlMap.put(Matrix.class, XmlMatrix.getAdapter());

    }

     @XmlAttribute
    public String name;
    @XmlElements(value = {
        @XmlElement(type = Boolean.class, name = "boolean"),
        @XmlElement(type = Double.class, name = "double"),
        @XmlElement(type = Integer.class, name = "integer"),
        @XmlElement(type = String.class, name = "string"),
        @XmlElement(type = XmlIntegers.class, name = "integers"),
        @XmlElement(type = XmlDoubles.class, name = "doubles"),
        @XmlElement(type = boolean[].class, name = "booleans"),
        @XmlElement(type = XmlStrings.class, name = "strings"),
        @XmlElement(type = XmlInformationSet.class, name = "subset"),
        @XmlElement(type = XmlAlgorithm.class, name = "method"),
        @XmlElement(type = XmlTsData.class, name = "tsdata"),
        @XmlElement(type = XmlTsMoniker.class, name = "moniker"),
        @XmlElement(type = XmlTs.class, name = "ts"),
        @XmlElement(type = XmlTsCollection.class, name = "tscollection"),
        @XmlElement(type = XmlPeriodSelection.class, name = "span"),
        @XmlElement(type = XmlStatisticalTest.class, name = "test"),
//        @XmlElement(type = XmlInterventionVariable.class, name = "ivar"),
//        @XmlElement(type = XmlRamp.class, name = "ramp"),
        @XmlElement(type = XmlRegItem.class, name = "regitem"),
        @XmlElement(type = XmlParameter.class, name = "param"),
        @XmlElement(type = XmlParameters.class, name = "params"),       
        @XmlElement(type = XmlMatrix.class, name = "matrix")})
    Object information;

    public Information<Object> toInformation() throws Exception {
        if (information == null) {
            return null;
        }
        XmlAdapter mapper = fromXmlMap.get(information.getClass());
        if (mapper == null) {
            return null;
        } else {
            return new Information<>(name, mapper.unmarshal(information));
        }
    }

    public XmlInformation() {
    }

    public XmlInformation(String name, Object value) {
        this.name = name;
        this.information = value;
    }

    public static <T> XmlInformation create(Information<T> info) throws Exception {
        XmlAdapter mapper = toXmlMap.get(info.getValue().getClass());
        if (mapper != null) {
            return new XmlInformation(info.getName(), mapper.marshal(info.getValue()));
        } else if (info.getValue().getClass().isEnum()) {
            return new XmlInformation(info.getName(), info.getValue().toString());
        } else {
            // try to find a compatible mapping
            for (Entry<Class, XmlAdapter> entry : toXmlMap.entrySet()) {
                if (entry.getKey().isInstance(info.getValue())) {
                    return new XmlInformation(info.getName(), entry.getValue().marshal(info.getValue()));
                }
            }
            return null;
        }
    }
}
