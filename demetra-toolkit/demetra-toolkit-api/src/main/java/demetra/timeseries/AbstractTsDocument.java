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
package demetra.timeseries;

import demetra.processing.ProcSpecification;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import demetra.information.Explorable;
import demetra.processing.ProcessingStatus;
import java.util.UUID;

/**
 *
 * @author PALATEJ
 * @param <S>
 * @param <R>
 */
public abstract class AbstractTsDocument<S extends ProcSpecification, R extends Explorable> implements TsDocument<S, R> {

    private final UUID uuid;

    private Map<String, String> metadata = Collections.emptyMap();

    @lombok.NonNull
    private S specification;

    private Ts input;

    private R result;

    private volatile ProcessingStatus status = ProcessingStatus.Unprocessed;

    protected AbstractTsDocument(S spec) {
        this.specification = spec;
        uuid = UUID.randomUUID();
    }
    
    private void clear(){
        result=null;
        status=ProcessingStatus.Unprocessed;
    }

    @Override
    public Ts getInput() {
        return input;
    }

    @Override
    public S getSpecification() {
        return specification;
    }

    @Override
    public void set(S newSpec, Ts newInput) {
        synchronized (this) {
            specification = newSpec;
            input = newInput;
            clear();
         }
    }

    @Override
    public void set(S newSpec) {
        synchronized (this) {
            specification = newSpec;
            clear();
        }
    }

    @Override
    public void set(Ts newInput) {
        synchronized (this) {
            input = newInput;
            clear();
        }
    }

    @Override
    public void setMetadata(Map<String, String> newMetadata) {
        metadata = Collections.unmodifiableMap(newMetadata);
    }

    @Override
    public void updateMetadata(Map<String, String> update) {
        Map<String, String> md = new HashMap<>(metadata);
        md.putAll(update);
        metadata = Collections.unmodifiableMap(md);
    }

    @Override
    public ProcessingStatus process() {
        ProcessingStatus cur = status;
        if (cur == ProcessingStatus.Unprocessed) {
            synchronized (this) {
                if (status == ProcessingStatus.Unprocessed) {
                    rawProcess();
                }
                cur = status;
            }
        }
        return cur;
    }

    private boolean check() {
        if (input == null) {
            return false;
        }
        if (input.getData().isEmpty()) {
            Ts s = TsFactory.getDefault().makeTs(input.getMoniker(), TsInformationType.Data);
            if (s.getData().isEmpty()) {
                throw new IllegalArgumentException("No data");
            } else {
                input = s;
            }
        }
        return true;
    }

    private void rawProcess() {
        if (check()) {
            try {
                result = internalProcess(specification, input.getData());
                status = result == null ? ProcessingStatus.Invalid : ProcessingStatus.Valid;
            } catch (Exception err) {
                Map<String, String> md = new HashMap<>(metadata);
                md.put(ERROR, err.getLocalizedMessage());
                result = null;
                status = ProcessingStatus.Invalid;
            }
        } else {
            result = null;
            status = ProcessingStatus.Unprocessed;
        }
    }

    protected abstract R internalProcess(S spec, TsData data);

    @Override
    public UUID getKey() {
        return uuid;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public R getResult() {
        ProcessingStatus cur = status;
        if (cur == ProcessingStatus.Unprocessed) {
            synchronized (this) {
                if (status == ProcessingStatus.Unprocessed) {
                    rawProcess();
                }
            }
        }
        return result;
    }

    @Override
    public ProcessingStatus getStatus() {
        return status;
    }
    
    @Override
    public void setAll(S spec, Ts input, R result){
        this.input=input;
        this.specification=spec;
        this.result=result;
        this.status = result != null ? ProcessingStatus.Valid : ProcessingStatus.Invalid;
    }

}
