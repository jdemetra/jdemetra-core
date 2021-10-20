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

package demetra.toolkit.io.xml.legacy;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Jean Palate
 * @param <X> Xml class (jaxb)
 * @param <O> Value class
 */
public class XmlConverterAdapter<X extends IXmlConverter<O>, O> extends XmlAdapter<X, O> {

    private final Class<X> xclass_;

    public XmlConverterAdapter(Class<X> xclass) {
        xclass_ = xclass;
    }

    @Override
    public O unmarshal(X v) throws Exception {
       return v.create();
    }

    @Override
    public X marshal(O v) throws Exception {
        try {
            X x = xclass_.getConstructor().newInstance();
            x.copy(v);
            return x;
        } catch (InstantiationException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        }catch(Exception ex){
            return null;
        }
   }
}
