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

/**
 *
 * @author PALATEJ
 * @param <S>
 * @param <R>
 */
public interface TsDocument<S extends ProcSpecification, R extends Explorable> extends ProcDocument<S, Ts, R> {

    @Override
    Ts getInput();

    @Override
    void set(S spec, Ts input);

    @Override
    void set(Ts input);

    void setAll(S spec, Ts input, R result);

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
