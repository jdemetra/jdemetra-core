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
package ec.tss.documents;

import ec.tstoolkit.algorithm.AbstractDocument;
import ec.tstoolkit.algorithm.IActiveProcDocument;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.design.Development;

/**
 * An active document is a document that generates new results when its
 * specifications or its input are changed.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class ActiveDocument<S extends IProcSpecification, I, R extends IProcResults>
        extends AbstractDocument<S, I, R> implements IActiveProcDocument<S, I, R> {

    private I input_;
    private S spec_;
    private R results_;
    private boolean dirty_;
    private IProcessing.Status status_ = IProcessing.Status.Unprocessed;
    private final ProcessingContext context_;
    private volatile String desc_;

    /**
     * Description of the document
     *
     * @param desc
     */
    public ActiveDocument(String desc) {
        desc_ = desc;
        context_ = ProcessingContext.getActiveContext();
    }

    public ActiveDocument(String desc, ProcessingContext context) {
        desc_ = desc;
        context_ = context;
    }

    public void dispose() {
        DocumentManager.instance.remove(this.getKey());
    }

    public IProcessing.Status getStatus() {
        return status_;
    }

    public ProcessingContext getContext() {
        return context_;
    }

    protected void notify(DocumentEvent.Change event) {
        DocumentManager.instance.notify(new DocumentEvent(this, event));
    }

    @Override
    public ActiveDocument clone() {
        try {
            ActiveDocument desc = (ActiveDocument) super.clone();
            return desc;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public S getSpecification() {
        return spec_;
    }

    @Override
    public void setSpecification(S spec) {
        setSpecification(spec, false);
    }

    @Override
    public void setDescription(String desc) {
        desc_ = desc;
    }

    protected void setSpecification(S spec, boolean quiet) {
        if (quiet) {
            spec_ = spec;
        } else if (isLocked() || spec.equals(spec_)) {
            return;
        } else {
            spec_ = spec;
            clear();
            DocumentManager.instance.notify(new DocumentEvent(this, DocumentEvent.Change.Specification));
        }
    }

    @Override
    public I getInput() {
        return input_;
    }

    @Override
    public void setInput(I input
    ) {
        setInput(input, false);
    }

    protected void setInput(I input, boolean quiet) {
        if (quiet) {
            input_ = input;
        } else if (!isLocked()) {
            input_ = input;
            clear();
            DocumentManager.instance.notify(new DocumentEvent(this, DocumentEvent.Change.Input));
        }
    }

    protected abstract R recalc(S spec, I input);

    protected void results(R r) {
        results_ = r;
        status_ = results_ == null ? IProcessing.Status.Invalid : IProcessing.Status.Valid;
    }

    public boolean update() {
        boolean rslt = internalUpdate();
        DocumentManager.instance.update(this);
        return rslt;
    }

    private boolean internalUpdate() {
        if (status_ != IProcessing.Status.Unprocessed) {
            return false;
        }
        if (spec_ == null || input_ == null) {
            status_ = IProcessing.Status.Invalid;
            return false;
        }
        results_ = recalc(spec_, input_);
        if (results_ == null) {
            status_ = IProcessing.Status.Invalid;
            return false;
        } else {
            status_ = IProcessing.Status.Valid;
            return true;
        }
    }

    @Override
    public String getDescription() {
        return desc_;
    }

    @Override
    public R getResults() {
        if (status_ == IProcessing.Status.Unprocessed) {
            update();
        }
        return results_;
    }

    /**
     * Clear the results. The dirty flag is also set to true. You should call
     * that method when some parts of the specification and/or of the input have
     * been modified by external code
     */
    public void clear() {
        dirty_ = true;
        status_ = IProcessing.Status.Unprocessed;
        results_ = null;
    }

    @Override
    public boolean isDirty() {
        return dirty_ || super.isDirty();
    }

    @Override
    public void resetDirty() {
        dirty_ = false;
        super.resetDirty();
    }

    protected void setDirty() {
        dirty_ = true;
    }
}
