package fr.polytech.unice;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

class SignalEmitter {
    private final List<ActionListener> listeners = new ArrayList<>();

    @SuppressWarnings("unused")
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeActionListener(ActionListener listener) {
        listeners.remove(listener);
    }

    @SuppressWarnings("unused")
    public void removeAllActionListeners() {
        listeners.clear();
    }

    @SuppressWarnings("unused")
    public void sendSignal(String command) {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);
        for (ActionListener listener : listeners) listener.actionPerformed(event);
    }
}