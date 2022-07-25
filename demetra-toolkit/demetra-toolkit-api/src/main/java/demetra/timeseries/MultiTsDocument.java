/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.timeseries;

import demetra.information.Explorable;
import demetra.processing.HasLog;
import demetra.processing.ProcDocument;
import demetra.processing.ProcSpecification;
import demetra.processing.ProcessingLog;
import demetra.processing.ProcessingStatus;
import java.util.List;

/**
 *
 * @author PALATEJ
 * @param <S>
 * @param <R>
 */
public interface MultiTsDocument<S extends ProcSpecification, R extends Explorable> extends ProcDocument<S, List<Ts>, R> {

    @Override
    List<Ts> getInput();

    @Override
    void set(S spec, List<Ts> input);

    @Override
    void set(List<Ts> input);

    void setAll(S spec, List<Ts> input, R result);

    @Override
    R getResult();

    default ProcessingLog getLog() {
        if (getStatus() != ProcessingStatus.Valid || !(getResult() instanceof HasLog)) {
            return ProcessingLog.dummy();
        } else {
            return ((HasLog) getResult()).getLog();
        }
    }
}
