/*
 * Copyright 2020 National Bank of Belgium
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

import nbbrd.design.Development;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Exploratory)
public class ProcessingLog {

    public static enum InformationType {

        /**
         * Error in an algorithm
         */
        Error, 
        /**
         * Warning for the user (usually, options automatically changed by the software
         */
        Warning, 
        /**
         * Step in a complex processing. a given step should not have any "child information"
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
            if (details != null){
                builder.append('=').append(details.toString());
            }
            return builder.toString();
        }
        
    }
    
    private final List<Information> logs=new ArrayList<>();
    private final Stack<String> context=new Stack<>();
    private boolean verbose=true;
    
    private static final String SEP=" > ";
    
    private String context(){
        StringBuilder builder=new StringBuilder();
        if (!context.isEmpty()){
            builder.append(context.get(0));
            for (int i=1; i<context.size(); ++i){
                builder.append(SEP).append(context.get(i));
            }
        }
        return builder.toString();
    }
    
    public void push(String routine){
        context.push(routine);
    }
    
    public void pop(){
        context.pop();
    }
    
    public List<Information> all(){
        return Collections.unmodifiableList(logs);
    }
    
    public List<String> getMessages(InformationType type) {
        if (logs.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> msg = new ArrayList();
        for (Information inf : logs) {
            if (inf.type == type) {
                msg.add(inf.toString());
            }
        }
        return msg;
    }

    public List<String> getErrorMessages() {
        return getMessages(InformationType.Error);
    }

    public List<String> getWarningMessages() {
        return getMessages(InformationType.Warning);
    }

    public boolean hasInformation(InformationType type) {
        return logs.stream().anyMatch(log->log.getType()==type);
    }

    public boolean hasErrors() {
        return hasInformation(InformationType.Error);
    }

    public boolean hasWarnings() {
        return hasInformation(InformationType.Warning);
    }


    public void error(Exception err) {
        logs.add(new Information(context(), null, err != null ? err.getMessage() : "Unexpected error", InformationType.Error, err));
    }

    public void error(String msg, Object err) {
        logs.add(new Information(context(), null, msg, InformationType.Error, err));
    }

    public void warning(String msg, Object info) {
        logs.add(new Information(context(), null, msg, InformationType.Warning, info));
    }

    public void error(String msg) {
        logs.add(new Information(context(), null, msg, InformationType.Error, null));
    }

    public void error(String origin, Exception err) {
        logs.add(new Information(context(), origin, err.getMessage(), InformationType.Error, err));
    }

    public void error(String origin, String msg, Object err) {
        logs.add(new Information(context(), origin, msg, InformationType.Error, err));
    }

    public void warning(String msg) {
        logs.add(new Information(context(), null, msg, InformationType.Warning, null));
    }

    public void warning(String origin, String msg, Object info) {
        logs.add(new Information(context(), origin, msg, InformationType.Warning, info));
    }

    public void warning(String origin, String msg) {
        logs.add(new Information(context(), origin, msg, InformationType.Warning, null));
    }

   public void info(String msg) {
        logs.add(new Information(context(), null, msg, InformationType.Info, null));
    }

    public void info(String msg, Object details) {
        logs.add(new Information(context(), null, msg, InformationType.Info, details));
    }

    public void info(String origin, String msg, Object info) {
        logs.add(new Information(context(), origin, msg, InformationType.Info, info));
    }

    public void info(String origin, String msg) {
        logs.add(new Information(context(), origin, msg, InformationType.Info, null));
    }
    
    public void step(String msg) {
        logs.add(new Information(context(), null, msg, InformationType.Log, null));
    }

    public void step(String msg, Object info) {
        logs.add(new Information(context(), null, msg, InformationType.Log, info));
    }

    /**
     * @return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
