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
package ec.tstoolkit.information;

/**
 *
 * @author pcuser
 */
public class DefaultInformationConverter<S extends InformationSetSerializable>
        extends AbstractInformationConverter<S> {


    public DefaultInformationConverter(final String contents, final Class<S> sclass) {
        super(contents, sclass);
    }

    @Override
    public S decode(InformationSet info) {
        if (!info.getContentType().equals(info.getContentType())) {
            return null;
        }
        try {
            S s = this.getInformationType().newInstance();
            if (!s.read(info)) {
                return null;
            }
            return s;
        } catch (InstantiationException | IllegalAccessException ex) {
            return null;
        }
    }
}
