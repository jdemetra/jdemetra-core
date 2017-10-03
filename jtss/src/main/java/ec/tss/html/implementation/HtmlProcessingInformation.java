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
package ec.tss.html.implementation;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.Bootstrap4;
import ec.tss.html.HtmlConverters;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class HtmlProcessingInformation extends AbstractHtmlElement {

    private final List<ProcessingInformation> infos_;
    private boolean err = true, wrn = true, info = false, verbose = true;

    public HtmlProcessingInformation(IProcResults rslts) {
        infos_ = rslts.getProcessingInformation();
    }

    public HtmlProcessingInformation(final List<ProcessingInformation> infos) {
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
        if (infos_.isEmpty()) {
            return;
        }
        if (err) {
            List<String> errs = ProcessingInformation.getErrorMessages(infos_);
            if (!errs.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Errors").newLine();
                for (String err : errs) {
                    stream.write(HtmlTag.IMPORTANT_TEXT, err, Bootstrap4.TEXT_DANGER).newLine();
                }
                stream.newLine();
            }
        }
        if (wrn) {
            List<String> msg = ProcessingInformation.getWarningMessages(infos_);
            if (!msg.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Warnings").newLine();
                for (String m : msg) {
                    stream.write(m, Bootstrap4.TEXT_INFO).newLine();
                }
                stream.newLine();
            }
        }
        if (info) {
            if (!verbose) {
                List<String> msg = ProcessingInformation.getMessages(infos_, ProcessingInformation.InformationType.Info);
                if (!msg.isEmpty()) {
                    stream.write(HtmlTag.HEADER2, "Log").newLine();
                    for (String m : msg) {
                        stream.write(m).newLine();
                    }
                    stream.newLine();
                }
            } else {
                String prevStep = null;
                for (ProcessingInformation cinfo : infos_) {
                    if (cinfo.type == ProcessingInformation.InformationType.Info) {
                        String curStep = cinfo.name;
                        if (prevStep == null || !prevStep.equals(curStep)) {
                            if (prevStep != null) {
                                stream.write(HtmlTag.LINEBREAK);
                            }
                            stream.write(HtmlTag.IMPORTANT_TEXT, curStep, Bootstrap4.TEXT_INFO).newLines(2);
                            prevStep = curStep;
                        }
                        stream.write(HtmlTag.EMPHASIZED_TEXT, cinfo.msg).newLine();
                        if (cinfo.details != null) {
                            stream.write(HtmlConverters.getDefault().convert(cinfo.details));
                        }
                        stream.newLine();
                    }
                }
            }
        }
    }
}
