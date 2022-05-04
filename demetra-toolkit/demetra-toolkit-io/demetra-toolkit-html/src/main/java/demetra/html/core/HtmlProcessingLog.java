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
package demetra.html.core;

import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlStream;
import demetra.html.HtmlTag;
import demetra.processing.ProcessingLog;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
public class HtmlProcessingLog extends AbstractHtmlElement {

    private final ProcessingLog infos_;
    private boolean err = true, wrn = true, info = false, verbose = true;

    public HtmlProcessingLog(final ProcessingLog infos) {
        infos_ = infos;
    }

    public void displayErrors(boolean e) {
        err = e;
    }

    public void displayWarnings(boolean e) {
        wrn = e;
    }

    public void displayInfos(boolean e) {
        info = e;
    }

    public void setVerbose(boolean v) {
        verbose = v;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        List<ProcessingLog.Information> all = infos_.all();
        if (all.isEmpty()) {
            return;
        }
        if (err) {
            List<String> errs = all.stream()
                    .filter(info -> info.getType() == ProcessingLog.InformationType.Error)
                    .map(info -> info.getMsg())
                    .collect(Collectors.toList());
            if (!errs.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Errors").newLine();
                for (String err : errs) {
                    stream.write(HtmlTag.IMPORTANT_TEXT, err, Bootstrap4.TEXT_DANGER).newLine();
                }
                stream.newLine();
            }
        }
        if (wrn) {
            List<String> msg = all.stream()
                    .filter(info -> info.getType() == ProcessingLog.InformationType.Warning)
                    .map(info -> info.getMsg())
                    .collect(Collectors.toList());
            if (!msg.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Warnings").newLine();
                for (String m : msg) {
                    stream.write(m, Bootstrap4.TEXT_INFO).newLine();
                }
                stream.newLine();
            }
        }
        if (info) {
            List<String> msg = all.stream()
                    .filter(info -> info.getType() == ProcessingLog.InformationType.Info)
                    .map(info -> info.getMsg())
                    .collect(Collectors.toList());
            if (!msg.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Log").newLine();
                for (String m : msg) {
                    stream.write(m).newLine();
                }
                stream.newLine();
            }
        }
    }

}
