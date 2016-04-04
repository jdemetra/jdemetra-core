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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class InformationLinker<T> {

    private class ConverterNode<S extends T> {

        ConverterNode(final InformationConverter<S> converter, final String description, final Class<S> type) {
            this.converter = converter;
            this.description = description;
            this.type = type;
        }
        final InformationConverter<S> converter;
        final String description;
        final Class<S> type;
    }
    private final List<ConverterNode<? extends T>> nodes = new ArrayList<>();

    public <S extends T> void register(String desc, Class<S> sclass, InformationConverter<S> converter) {
        if (this.getNode(sclass) != null)
            return;
        nodes.add(new ConverterNode(converter, desc, sclass));
    }

    public T decode(InformationSet info) {
        String contentType = info.getContentType();
        InformationConverter<? extends T> decoder = getConverter(contentType);
        if (decoder == null) {
            return null;
        } else {
            return decoder.decode(info);
        }
    }

    public InformationConverter<? extends T> getConverter(String s) {
        for (ConverterNode<? extends T> node : nodes) {
            if (node.description.equals(s)) {
                return node.converter;
            }
        }
        return null;
    }

   public <S extends T> InformationConverter<S> getConverter(Class<S> sclass) {
        for (ConverterNode<? extends T> node : nodes) {
            if (node.type.equals(sclass)) {
                return (InformationConverter<S>) node.converter;
            }
        }
        return null;
    }

   private <S extends T> ConverterNode<S> getNode(Class<S> sclass) {
        for (ConverterNode<? extends T> node : nodes) {
            if (node.type.equals(sclass)) {
                return (ConverterNode<S>) node;
            }
        }
        return null;
    }

   public <S extends T> InformationSet encode(S obj, boolean verbose){
        ConverterNode<S> node = getNode((Class<S>)obj.getClass());
        if (node == null)
            return null;
        else{
            InformationSet info = (node.converter).encode(obj, verbose);
            info.setContent(node.description, null);
            return info;
        }
    }
}
