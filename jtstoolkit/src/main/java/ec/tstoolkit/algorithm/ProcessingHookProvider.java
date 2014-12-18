/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.algorithm;

import ec.tstoolkit.algorithm.IProcessingHook.HookInformation;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;

/**
 *
 * @author Jean
 * @param <S> Source of the information
 * @param <I> Information dispatched in hooking
 */
public class ProcessingHookProvider<S, I> implements IProcessingHookProvider<S, I> {

    private final ArrayList<IProcessingHook<S, I>> hooks = new ArrayList<>();
    private volatile String message = IProcessingHook.EMPTY;

    @Override
    public void setHookMessage(String msg) {
        message = msg;
    }

    @Override
    public String getHookMessage() {
        return message;
    }

    @Override
    public synchronized boolean hasHooks() {
        return !hooks.isEmpty();
    }

    @Override
    public synchronized void register(IProcessingHook<S, I> hook) {
        hooks.add(hook);
    }

    @Override
    public synchronized void unregister(IProcessingHook<S, I> hook) {
        for (IProcessingHook<S, I> cur : hooks) {
            if (hook == cur) {
                hooks.remove(cur);
                return;
            }
        }
    }

    @Override
    public synchronized void processHooks(HookInformation<S, I> info, boolean cancancel) {
        info.message = InformationSet.item(message, info.message);
        for (IProcessingHook<S, I> cur : hooks) {
            cur.process(info, cancancel);
            if (cancancel && info.cancel) {
                return;
           }
        }
    }
 
    public void copyHooks(final ProcessingHookProvider<S, I> ph) {
        synchronized (ph.hooks) {
            hooks.addAll(ph.hooks);
            message = ph.message;
        }
    }

}
