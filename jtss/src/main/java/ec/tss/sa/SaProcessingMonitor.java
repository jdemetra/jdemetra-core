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


package ec.tss.sa;

import javax.swing.event.EventListenerList;

/**
 *
 * @author Kristof Bayens
 */
public class SaProcessingMonitor {
    private SaProcessing processing_;
    private SaItem[] items_;
    private boolean closing_, stop_;
    private Thread hRun_;

    protected EventListenerList list = new EventListenerList();

    public void addSaEventListener(SaEventListener listener) {
        list.add(SaEventListener.class, listener);
    }

    public void removeSaEventListener(SaEventListener listener) {
        list.remove(SaEventListener.class, listener);
    }

    public SaProcessingMonitor() {
        
    }

    public SaProcessing getProcessing() {
        return processing_;
    }
    public void setProcessing(SaProcessing value) {
        if (processing_ != null)
            stop();
        processing_ = value;
    }

    public boolean isRunning() {
        return hRun_ != null;
    }

    public void start() {

    }

    public void stop() {

    }

    public void close() {
        closing_ = true;
        if (hRun_ != null && hRun_.isAlive()) {
            hRun_.interrupt();
        }
    }
}
