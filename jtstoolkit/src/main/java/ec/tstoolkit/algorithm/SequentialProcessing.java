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
import java.util.HashMap;
import java.util.List;

/**
 * Describes a sequential processing, which can be split in successive steps.
 * Each step is processed by a processing node, specified by a separate
 * specification (possibly empty). It generates an output stored in a common
 * repository. The different steps can access/modify previous results and
 * generate their own output. Steps, specifications and results are stored in
 * maps indexed by commons identifiers (string).
 *
 * @author Jean Palate
 */
public class SequentialProcessing<I> implements IProcessing<I, CompositeResults>, Cloneable {

    public static final String INPUT = "input";

    private ArrayList<IProcessingNode<I>> nodes_ = new ArrayList<>();

    @Override
    public SequentialProcessing clone() {
        try {
            SequentialProcessing p = (SequentialProcessing) super.clone();
            return p;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public void add(IProcessingNode<I> node) {
        nodes_.add(node);
    }

    @Override
    public CompositeResults process(I input) {
        CompositeResults results = new CompositeResults();
        HashMap<String, IProcResults> map = new HashMap<>();
        boolean ok = true;
        for (IProcessingNode cur : nodes_) {
            String name = cur.getName();
            if (!ok) {
                results.put(name, null, null);
//                results.addInformation(ProcessingInformation.error(name, "unprocessed"));
            } else {
                try {
                    Status st = cur.process(input, map);
                    if (st == Status.Valid) {
                        results.put(name, map.get(name), cur.getPrefix());
                    } else if (st == Status.Invalid) {
                        ok = false;
                        results.put(name, null, null);
                        results.addInformation(ProcessingInformation.error(name, "failed"));
                    }
                    IProcResults r = map.get(name);
                    if (r != null) {
                        List<ProcessingInformation> infos = r.getProcessingInformation();
                        if (!infos.isEmpty()) {
                            results.addInformation(ProcessingInformation.addPrefix(infos, name));
                        }
                    }
                } catch (Exception err) {
                    ok = false;
                    results.put(name, null, null);
                    results.addInformation(ProcessingInformation.error(name, err));
                }
            }
        }
        return results;
    }

}
