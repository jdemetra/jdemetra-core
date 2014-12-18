/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.algorithm;

/**
 *
 * @author Jean
 */
public interface IProcessingHook<S, I> {

    public static final String EMPTY = "";

    public static class HookInformation<S, I> {

        public HookInformation(S source, I info) {
            this.source = source;
            this.information = info;
            cancel = false;
        }

        public final S source;
        public final I information;
        public boolean cancel;
        public String message = EMPTY;
    }

    void process(HookInformation<S, I> info, boolean cancancel);
}
