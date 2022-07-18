/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.composite;

import nbbrd.design.BuilderPattern;
import java.util.ArrayList;
import java.util.List;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public class CompositeState {

    @BuilderPattern(StateComponent.class)
    public static class Builder {

        private final List<StateComponent> components = new ArrayList<>();

        private int[] dim, pos;

        public Builder add(StateComponent cmp) {
            components.add(cmp);
            return this;
        }

        public StateComponent build() {
            if (components.isEmpty()) {
                return null;
            }
            // build dim / pos
            int n = components.size();
            dim = new int[n];
            pos = new int[n];
            ISsfInitialization[] i = new ISsfInitialization[n];
            ISsfDynamics[] d = new ISsfDynamics[n];
            int cpos = 0;
            for (int j = 0; j < n; ++j) {
                StateComponent cur = components.get(j);
                i[j] = cur.initialization();
                d[j] = cur.dynamics();
                pos[j] = cpos;
                dim[j] = i[j].getStateDim();
                cpos += dim[j];
            }
            return new StateComponent(new CompositeInitialization(dim, i),
                    new CompositeDynamics(dim, d));
        }

        public int[] getComponentsDimension() {
            return dim;
        }

        public int[] getComponentsPosition() {
            return pos;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
