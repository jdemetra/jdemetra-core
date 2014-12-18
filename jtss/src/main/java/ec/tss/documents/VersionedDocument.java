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
package ec.tss.documents;

import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.IActiveProcDocument;
import ec.tstoolkit.algorithm.IProcDocument;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.utilities.IModifiable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Jean Palate
 * @param <S>
 * @param <I>
 * @param <R>
 * @param <D> The document type
 */
public abstract class VersionedDocument<S extends IProcSpecification, I, R extends IProcResults, D extends IActiveProcDocument<S, I, R>>
        implements IModifiable, IActiveProcDocument<S, I, R> {

    public static final String CDOC = "cdoc", VDOC = "vdoc", VDOCS = "vdoc*";

    protected abstract D newDocument(D doc);

    protected abstract D restore(D document);

    protected abstract D archive(D document);

    private List<D> versions = new ArrayList<>();
    private D current;
    private boolean dirty;

    protected VersionedDocument(D doc) {
        current = doc;
    }

    protected void addAll(Collection<D> docs) {
        versions.addAll(docs);
        dirty = true;
    }

    protected void add(D doc) {
        versions.add(doc);
        dirty = true;
    }
    
    public void removeVersion(int ver){
        versions.remove(ver);
        dirty=true;
    }

    @Override
    protected VersionedDocument<S, I, R, D> clone() throws CloneNotSupportedException {
        VersionedDocument<S, I, R, D> vd = (VersionedDocument<S, I, R, D>) super.clone();
        vd.versions = new ArrayList<>();
        return vd;
    }

    public int getVersionCount() {
        return versions.size();
    }

    public D getVersion(int idx) {
        return versions.get(idx);
    }

    public D getLastVersion() {
        int n = versions.size();
        return n == 0 ? null : versions.get(n - 1);
    }

    /**
     * Clears the versions till a given number (included)
     *
     * @param ver
     */
    public void clearVersions(int ver) {
        if (ver >= versions.size()) {
            return;
        }
        if (ver == 0) {
            versions.clear();
        } else {
            for (int i = versions.size() - 1; i >= ver; --i) {
                versions.remove(i);
            }
        }
        dirty = true;

    }

    public D getCurrent() {
        return current;
    }

    @Override
    public boolean isDirty() {
        if (dirty) {
            return true;
        } else if (current instanceof IModifiable) {
            return ((IModifiable) current).isDirty();
        } else {
            return false;
        }
    }

    public void setCurrent(D current) {
        this.current = current;
        dirty = true;
    }

    public void archive() {
        D ndoc=newDocument(current);
        versions.add(archive(current));
        current = ndoc;
        dirty = true;
    }

    public boolean restore(int version) {
        if (version >= versions.size()) {
            return false;
        }
        D rdoc = restore(versions.get(version));
        if (rdoc == null) {
            return false;
        }
        current = rdoc;
        for (int i = versions.size() - 1; i >= version; --i) {
            versions.remove(i);
        }
        dirty = true;
        return true;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(CDOC, current.write(verbose));
        for (int i = 0; i < versions.size(); ++i) {
            info.add(VDOC + i, versions.get(i).write(verbose));
        }

        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        versions.clear();
        List<Information<InformationSet>> v = info.select(VDOCS, InformationSet.class);
        for (Information<InformationSet> vinfo : v) {
            D d = newDocument(null);
            if (d.read(vinfo.value)) {
                versions.add(archive(d));
            }
        }
        current = newDocument(null);
        InformationSet cur = info.getSubSet(CDOC);
        if (cur != null) {
            if (!current.read(cur)) {
                return false;
            }
        } else {    // unversioned format
            if (!current.read(info)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void resetDirty() {
        dirty = false;
        if (current instanceof IModifiable) {
            ((IModifiable) current).resetDirty();
        }
    }
    
    protected void setDirty(){
        dirty=true;
    }

    @Override
    public I getInput() {
        return current.getInput(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public S getSpecification() {
        return current.getSpecification(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public R getResults() {
        return current.getResults(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getKey() {
        return current.getKey(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDescription() {
        return current.getDescription(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MetaData getMetaData() {
        return current.getMetaData(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInput(I input) {
        current.setInput(input); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSpecification(S spec) {
        current.setSpecification(spec); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDescription(String desc) {
        current.setDescription(desc); //To change body of generated methods, choose Tools | Templates.
    }
}
