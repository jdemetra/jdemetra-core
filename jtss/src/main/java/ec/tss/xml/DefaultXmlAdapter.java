/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tss.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Jean Palate
 * @param <ValueType> The type that JAXB knows how to handle 
 * @param <BoundType> The type that JAXB doesn't know how to handle (pure java class)
 */
public class DefaultXmlAdapter<ValueType, BoundType> extends XmlAdapter<ValueType, BoundType>{
    private final IXmlMarshaller<ValueType, BoundType> marshaller;
    private final IXmlUnmarshaller<ValueType, BoundType> unmarshaller;
    public DefaultXmlAdapter(final IXmlMarshaller<ValueType, BoundType> marshaller, final IXmlUnmarshaller<ValueType, BoundType> unmarshaller){
        this.marshaller=marshaller;
        this.unmarshaller=unmarshaller;
    }

    @Override
    public BoundType unmarshal(ValueType v) throws Exception {
        return unmarshaller.unmarshal(v);
    }

    @Override
    public ValueType marshal(BoundType v) throws Exception {
        return marshaller.marshal(v);
    }
    
}
