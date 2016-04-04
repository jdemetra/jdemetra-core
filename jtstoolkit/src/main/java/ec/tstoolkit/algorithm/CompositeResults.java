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

import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Jean Palate
 */
public class CompositeResults implements IProcResults {

    public static class Node {

        public Node(IProcResults results, String prefix) {
            this.results = results;
            this.prefix = prefix;
        }
        public final IProcResults results;
        public final String prefix;
    }
    private final LinkedHashMap<String, Node> nodes = new LinkedHashMap<>();
    private final List<ProcessingInformation> infos = new ArrayList<>();

    public CompositeResults() {
    }

    public boolean isSuccessful() {
        return !ProcessingInformation.hasErrors(infos);
    }

    public void put(String name, IProcResults rslts, String prefix) {
        nodes.put(name, new Node(rslts, prefix));
    }

    public void remove(String name) {
        nodes.remove(name);
    }

    public void removeAll() {
        this.nodes.clear();
    }

    public int getNodesCount() {
        return nodes.size();
    }

    public Node getNode(String name) {
        return nodes.get(name);
    }

    public <R extends IProcResults> R get(String name, Class<R> rclass) {
        Node node = nodes.get(name);
        if (node == null || node.results == null || !(rclass.isAssignableFrom(node.results.getClass()))) {
            return null;
        } else {
            return (R) node.results;
        }
    }

    public IProcResults get(String name) {
        Node node = nodes.get(name);
        if (node == null) {
            return null;
        } else {
            return node.results;
        }
    }

    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.unmodifiableList(infos);
    }

    public void addInformation(ProcessingInformation info) {
        infos.add(info);
    }

    public void addInformation(List<ProcessingInformation> info) {
        infos.addAll(info);
    }

    @Override
    public boolean contains(String id) {
        for (Node node : nodes.values()) {
            if (node.results != null) {
                String cid = null;
                if (node.prefix != null && InformationSet.isPrefix(id, node.prefix)) {
                    cid = InformationSet.removePrefix(id);
                } else {
                    cid = id;
                }
                if (cid != null && node.results.contains(cid)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> dic = new LinkedHashMap<>();
        for (Node node : nodes.values()) {
            if (node.results != null) {
                Map<String, Class> cur = node.results.getDictionary();
                ProcUtilities.fillDictionary(dic, node.prefix, cur);
            }
        }
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        for (Entry<String, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (node.results != null) {
                String cid = null;
                if (node.prefix != null) {
                    if (InformationSet.isPrefix(id, node.prefix)) {
                        cid = InformationSet.removePrefix(id);
                    }
                } else {
                    cid = id;
                }
                if (cid != null && node.results.contains(cid)) {
                    return node.results.getData(cid, tclass);
                }
            }
        }
        // if it doesn't succeed, try another strategy, less strict:
        // we search in all the sub-results for the first item corresponding to 
        // the identification id
        for (Entry<String, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (node.results != null) {
                String cid;
                if (node.prefix != null && InformationSet.isPrefix(id, node.prefix)) {
                    cid = InformationSet.removePrefix(id);
                } else {
                    cid = id;
                }
                if (cid != null && node.results.contains(cid)) {
                    return node.results.getData(cid, tclass);
                }
            }
        }
        return null;
    }

    public static <T> T searchData(Map<String, IProcResults> results, String id, Class<T> tclass) {
        for (Entry<String, IProcResults> entry : results.entrySet()) {
            if (entry.getValue() != null && entry.getValue().contains(id)) {
                return entry.getValue().getData(id, tclass);
            }
        }
        return null;
    }
}
