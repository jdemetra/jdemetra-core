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
public abstract class AbstractInformationConverter<S extends InformationSetSerializable>
        implements InformationConverter<S> {

    private final String contents;
    private final Class<S> sclass;

    public AbstractInformationConverter(final String contents, final Class<S> sclass) {
        this.contents = contents;
        this.sclass = sclass;
    }

    @Override
    public InformationSet encode(S s, boolean verbose) {
        InformationSet info = s.write(verbose);
        info.setContent(contents, null);
        return info;
    }

    @Override
    public Class<S> getInformationType() {
        return sclass;
    }

    @Override
    public String getTypeDescription() {
        return contents;
    }
    
    public void addTo(InformationLinker<? super S> linker){
        linker.register(contents, sclass, this);
    }
}
