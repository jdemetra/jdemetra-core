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
package demetra.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Jean Palate
 * @param <X> The type that JAXB knows how to handle
 * @param <V> The type that JAXB doesn't know how to handle (pure java class)
 */
public class DefaultXmlAdapter<X, V> extends ExtendedXmlAdapter<X, V> {

    private final XmlAdapter<X, V> adapter;
    private final Class<X> xclass;
    private final Class<V> vclass;
    //private ;

    public DefaultXmlAdapter(final Class<X> xclass, final Class<V> vclass, final XmlAdapter<X, V> adapter) {
        this.adapter = adapter;
        this.vclass = vclass;
        this.xclass = xclass;
    }

    public DefaultXmlAdapter(final Class<X> xclass, final Class<V> vclass, final IXmlMarshaller<X, V> marshaller, final IXmlUnmarshaller<X, V> unmarshaller) {
        this.adapter = new XmlAdapter<X, V>() {

            @Override
            public V unmarshal(X x) throws Exception {
                return unmarshaller.unmarshal(x);
            }

            @Override
            public X marshal(V v) throws Exception {
                return marshaller.marshal(v);
            }
        };
        this.vclass = vclass;
        this.xclass = xclass;
    }

    @Override
    public V unmarshal(X v) throws Exception {
        return adapter.unmarshal(v);
    }

    @Override
    public X marshal(V v) throws Exception {
        return adapter.marshal(v);
    }

    @Override
    public Class<X> getXmlType() {
        return xclass;
    }

    @Override
    public Class<V> getImplementationType() {
        return vclass;
    }
}
