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

package ec.tstoolkit.algorithm;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.utilities.Jdk6;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Jean Palate
 */
@Development (status = Development.Status.Exploratory)
public class CompositeSpecification implements IProcSpecification, Cloneable {

    public static class Node {

        public Node(IProcSpecification spec, String prefix) {
            this.spec = spec;
            this.prefix = prefix;
        }
        public final IProcSpecification spec;
        public final String prefix;
    }
    private LinkedHashMap<String, Node> nodes_ = new LinkedHashMap<>();

    public CompositeSpecification() {
    }

    public void add(String name, Node node) {
        nodes_.put(name, node);
    }

    void sePartialSpecification(String part, IProcSpecification xspec) {
        Node cur=nodes_.get(part);
        if (cur != null){
            nodes_.put(part, new Node(xspec, cur.prefix));
        }
    }

    public Set<Entry<String, Node>> nodesSet(){
        return nodes_.entrySet();
    }
    
    public Set<String> keySet(){
        return nodes_.keySet();
    }
    

    public IProcSpecification search(String name) {
        Node node = nodes_.get(name);
        if (node == null) {
            return null;
        } else {
            return node.spec;
        }
    }

    @Override
    public CompositeSpecification clone() {
        try {
            CompositeSpecification spec = (CompositeSpecification) super.clone();
            spec.nodes_ = new LinkedHashMap<>();
            spec.nodes_.putAll(nodes_);
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        for (Node cur: nodes_.values()) {
            if (Jdk6.isNullOrEmpty(cur.prefix)) {
                InformationSet sinfo = cur.spec.write(verbose);
                if (sinfo != null) {
                    info.merge(sinfo);
                }
            } else {
                InformationSet sinfo = cur.spec.write(verbose);
                if (sinfo != null) {
                    info.add(cur.prefix, sinfo);
                }
            }
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        boolean ok = true;
        for (Node cur: nodes_.values()) {
            String cprefix = cur.prefix;
            if (Jdk6.isNullOrEmpty(cprefix)) {
                if (!cur.spec.read(info)) {
                    ok = false;
                }
            } else {
                InformationSet sinfo = info.getSubSet(cprefix);
                if (sinfo != null) {
                    if (!cur.spec.read(sinfo)) {
                        ok = false;
                    }
                }
            }
        }
        return ok;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof CompositeSpecification && equals((CompositeSpecification) obj));
    }

    private boolean equals(CompositeSpecification spec) {
        return Objects.equals(spec.nodes_, nodes_);
    }
//    @Override
//    public void fillDictionary(String prefix, List<String> dic) {
//        for (Node cur : nodes_.values()) {
//            String cprefix = cur.prefix;
//            cprefix = InformationSet.item(prefix, cprefix);
//            cur.spec.fillDictionary(cprefix, dic);
//        }
//    }
}
