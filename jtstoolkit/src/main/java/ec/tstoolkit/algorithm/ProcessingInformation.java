/*
 * Copyright 2013-2014 National Bank of Belgium
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class ProcessingInformation {

    public static final String SUCCEEDED = "Succeeded",
            FAILED = "Failed", STARTED = "Started";

    public static enum InformationType {

        Error, Warning, Info, Log
    }

    public final String name;
    public final String origin;
    public final String msg;
    public final InformationType type;
    public final Object details;

    protected ProcessingInformation(String name, String origin, String msg, InformationType type, Object details) {
        this.name = name;
        this.origin = origin;
        this.msg = msg == null ? "Undefined" : msg;
        this.type = type;
        this.details = details;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": ").append(msg);
        return builder.toString();
    }

    public static List<String> getMessages(List<ProcessingInformation> infos, InformationType type) {
        if (infos == null || infos.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<String> msg = new ArrayList();
        for (ProcessingInformation inf : infos) {
            if (inf.type == type) {
                msg.add(inf.toString());
            }
        }
        return msg;
    }

    public static List<ProcessingInformation> addPrefix(List<ProcessingInformation> infos, String prefix) {
        if (infos == null || infos.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<ProcessingInformation> msg = new ArrayList();
        for (ProcessingInformation inf : infos) {
            msg.add(inf.addPrefix(prefix));
        }
        return msg;
    }

    public static List<String> getErrorMessages(List<ProcessingInformation> infos) {
        return getMessages(infos, InformationType.Error);
    }

    public static List<String> getWarningMessages(List<ProcessingInformation> infos) {
        return getMessages(infos, InformationType.Warning);
    }

    public static boolean hasInformation(List<ProcessingInformation> infos, InformationType type) {
        if (infos == null || infos.isEmpty()) {
            return false;
        }
        for (ProcessingInformation inf : infos) {
            if (inf.type == type) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasErrors(List<ProcessingInformation> infos) {
        return hasInformation(infos, InformationType.Error);
    }

    public static boolean hasWarnings(List<ProcessingInformation> infos) {
        return hasInformation(infos, InformationType.Warning);
    }

    public ProcessingInformation addPrefix(String prefix) {
        return new ProcessingInformation(prefix + '.' + name, origin, msg, type, details);
    }

    public static ProcessingInformation error(String name, Exception err) {
        return new ProcessingInformation(name, null, err != null ? err.getMessage() : "Unexpected error", InformationType.Error, err);
    }

    public static ProcessingInformation error(String name, String msg, Object err) {
        return new ProcessingInformation(name, null, msg, InformationType.Error, err);
    }

    public static ProcessingInformation warning(String name, String msg, Object info) {
        return new ProcessingInformation(name, null, msg, InformationType.Warning, info);
    }

    public static ProcessingInformation error(String name, String msg) {
        return new ProcessingInformation(name, null, msg, InformationType.Error, null);
    }

    public static ProcessingInformation warning(String name, String msg) {
        return new ProcessingInformation(name, null, msg, InformationType.Warning, null);
    }

    public static ProcessingInformation info(String name, String msg, Object details) {
        return new ProcessingInformation(name, null, msg, InformationType.Info, details);
    }

    public static ProcessingInformation info(String name, String msg) {
        return new ProcessingInformation(name, null, msg, InformationType.Info, null);
    }

    public static ProcessingInformation error(String name, String origin, Exception err) {
        return new ProcessingInformation(name, origin, err != null ? err.getMessage() : "Unexpected error", InformationType.Error, err);
    }

    public static ProcessingInformation error(String name, String origin, String msg, Object err) {
        return new ProcessingInformation(name, origin, msg, InformationType.Error, err);
    }

    public static ProcessingInformation warning(String name, String origin, String msg, Object info) {
        return new ProcessingInformation(name, origin, msg, InformationType.Warning, info);
    }

    public static ProcessingInformation error(String name, String origin, String msg) {
        return new ProcessingInformation(name, origin, msg, InformationType.Error, null);
    }

    public static ProcessingInformation warning(String name, String origin, String msg) {
        return new ProcessingInformation(name, origin, msg, InformationType.Warning, null);
    }

    public static ProcessingInformation info(String name, String origin, String msg, Object details) {
        return new ProcessingInformation(name, origin, msg, InformationType.Info, details);
    }

    public static ProcessingInformation info(String name, String origin, String msg) {
        return new ProcessingInformation(name, origin, msg, InformationType.Info, null);
    }

    public static ProcessingInformation start(String name, String origin) {
        return new ProcessingInformation(name, origin, STARTED, InformationType.Log, null);
    }

    public static ProcessingInformation success(String name, String origin) {
        return new ProcessingInformation(name, origin, SUCCEEDED, InformationType.Log, null);
    }

    public static ProcessingInformation failure(String name, String origin) {
        return new ProcessingInformation(name, origin, FAILED, InformationType.Log, null);
    }

    public static ProcessingInformation log(String name, String origin, String message) {
        return new ProcessingInformation(name, origin, message, InformationType.Log, null);
    }
}
