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
package ec.tstoolkit.algorithm;

import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.utilities.IModifiable;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractDocument<S extends IProcSpecification, I, R extends IProcResults> implements IProcDocument<S, I, R>, IModifiable {

    public static final String LOCKED = "__locked";

    private static final AtomicLong g_id = new AtomicLong(0);
    private long id_;
    private boolean locked_;
    private MetaData metadata_;

    @Override
    public long getKey() {
        return id_;
    }

    public String getDocumentId() {
        StringBuilder builder = new StringBuilder();
        builder.append("Doc-").append(id_);
        return builder.toString();
    }

    @Override
    public MetaData getMetaData() {
        if (metadata_ == null) {
            metadata_ = new MetaData();
        }
        return metadata_;
    }

    protected AbstractDocument() {
        id_ = g_id.incrementAndGet();
    }

    @Override
    protected AbstractDocument clone() throws CloneNotSupportedException {
        AbstractDocument doc = (AbstractDocument) super.clone();
        if (metadata_ != null) {
            doc.metadata_ = metadata_.clone();
        }
        doc.id_ = g_id.incrementAndGet();
        return doc;
    }

    @Override
    public boolean isDirty() {
        if (metadata_ != null) {
            return metadata_.isDirty();
        } else {
            return false;
        }
    }

    public boolean isLocked() {
        return locked_;
    }

    public void setLocked(boolean value) {
        locked_ = value;
    }

    @Override
    public void resetDirty() {
        if (metadata_ != null) {
            metadata_.resetDirty();
        }
    }

    public boolean hasMetaData() {
        return MetaData.isNullOrEmpty(metadata_);
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (verbose || locked_) {
            info.set(LOCKED, locked_);
        }
        if (!MetaData.isNullOrEmpty(metadata_)) {
            info.set(METADATA, InformationSetHelper.fromMetaData(metadata_));
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        if (info == null) {
            return false;
        }
        InformationSet subSet = info.getSubSet(METADATA);
        if (subSet != null) {
            metadata_ = new MetaData();
            InformationSetHelper.fillMetaData(subSet, metadata_);
        }
        Boolean locked = info.get(LOCKED, Boolean.class);
        if (locked != null){
            locked_=locked;
        }
       
        return true;
    }

}
