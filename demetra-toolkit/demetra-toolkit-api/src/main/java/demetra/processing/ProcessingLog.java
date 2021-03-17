/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.processing;

import java.util.Collections;
import java.util.List;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Exploratory)
public interface ProcessingLog {

    public static enum InformationType {

        /**
         * Error in an algorithm
         */
        Error,
        /**
         * Warning for the user (usually, options automatically changed by the
         * software
         */
        Warning,
        /**
         * Step in a complex processing. a given step should not have any "child
         * information"
         */
        Log,
        /**
         * More detailed info on a specific step
         */
        Info
    }

    @lombok.Value
    public static class Information {

        private String name;
        private String origin;
        private String msg;
        private InformationType type;
        private Object details;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name).append(": ").append(msg);
            if (details != null) {
                builder.append('=').append(details.toString());
            }
            return builder.toString();
        }

    }
    
    default List<Information> all(){
        return Collections.emptyList();
    }

    default void push(String routine) {
    }

    default void pop() {
    }

    default void error(Exception err) {
        error(null, err.getMessage(), null);
    }

    default void error(String msg) {
        error(null, msg, null);
    }

    default void error(String msg, Object detail) {
        error(null, msg, detail);
    }

    default void error(String origin, Exception err) {
        error(origin, err.getMessage(), null);
    }

    default void error(String origin, String msg, Object details) {
    }

    default void warning(String msg) {
        warning(null, msg, null);
    }

    default void warning(String msg, Object details) {
        warning(null, msg, details);
    }

    default void warning(String origin, String msg) {
        warning(origin, msg, null);
    }

    default void warning(String origin, String msg, Object details) {
    }

    default void info(String msg) {
        info(null, msg, null);
    }

    default void info(String msg, Object details) {
        info(null, msg, details);
    }

    default void info(String origin, String msg) {
        info(origin, msg, null);
    }

    default void info(String origin, String msg, Object details) {
    }

    default void step() {
        step(null, null);
    }

    default void step(String msg) {
        step(msg, null);
    }

    default void step(String msg, Object details) {
    }

    
    public static ProcessingLog dummy(){return DummyLog.DUMMY;}

}
 
class DummyLog implements ProcessingLog {
        static final DummyLog DUMMY=new DummyLog();
}
