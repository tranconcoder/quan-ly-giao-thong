package com.example.carremote.ui.home;

import androidx.appcompat.widget.SwitchCompat;

public class SwitchEvent {
    public SwitchCompat switchCompat;
    public String enableCommand;
    public String disableCommand;

    public SwitchEvent(SwitchCompat switchCompat, String enableCommand, String disableCommand) {
        this.switchCompat = switchCompat;
        this.enableCommand = enableCommand;
        this.disableCommand = disableCommand;
    }

    public SwitchCompat getSwitchCompat() {
        return switchCompat;
    }

    public void setSwitchCompat(SwitchCompat switchCompat) {
        this.switchCompat = switchCompat;
    }

    public String getEnableCommand() {
        return enableCommand;
    }

    public void setEnableCommand(String enableCommand) {
        this.enableCommand = enableCommand;
    }

    public String getDisableCommand() {
        return disableCommand;
    }

    public void setDisableCommand(String disableCommand) {
        this.disableCommand = disableCommand;
    }

    public void setChecked(boolean checked) {
        switchCompat.setChecked(checked);
    }
}
