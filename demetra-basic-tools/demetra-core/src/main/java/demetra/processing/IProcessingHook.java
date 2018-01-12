/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.processing;

import java.util.concurrent.atomic.AtomicBoolean;

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
        }
 
        private final S source;
        private final I information;
        private AtomicBoolean cancel=new AtomicBoolean(false);
        private volatile String message = EMPTY;

        /**
         * @return the source
         */
        public S getSource() {
            return source;
        }

        /**
         * @return the information
         */
        public I getInformation() {
            return information;
        }

        /**
         * @return the cancel
         */
        public boolean isCancelled() {
            return cancel.get();
        }

        /**
         * @param cancel the cancel to set
         */
        public void setCancel(boolean cancel) {
            this.cancel.set(cancel);
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @param message the message to set
         */
        public void setMessage(String message) {
            this.message = message;
        }
    }

    void process(HookInformation<S, I> info, boolean canCancel);
}
