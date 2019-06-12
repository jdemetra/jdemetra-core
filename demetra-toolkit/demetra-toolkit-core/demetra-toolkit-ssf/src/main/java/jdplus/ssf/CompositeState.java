/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf;

import demetra.design.BuilderPattern;
import java.util.ArrayList;
import java.util.List;

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
