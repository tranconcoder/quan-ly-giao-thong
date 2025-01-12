package com.example.carremote.ui.home;

import android.widget.Button;

public class TouchUpDownEvent {
    public Button element;
    public String startCommand;
    public String endCommand;

    public TouchUpDownEvent(Button element, String startCommand, String endCommand) {
        this.element = element;
        this.startCommand = startCommand;
        this.endCommand = endCommand;
    }

    public Button getElement() {
        return element;
    }

    public void setElement(Button element) {
        this.element = element;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public void setStartCommand(String startCommand) {
        this.startCommand = startCommand;
    }

    public String getEndCommand() {
        return endCommand;
    }

    public void setEndCommand(String endCommand) {
        this.endCommand = endCommand;
    }
}
