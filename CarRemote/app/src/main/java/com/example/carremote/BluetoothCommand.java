package com.example.carremote;

public enum BluetoothCommand {
    // Touch
    UP_START("UP_START"),
    UP_STOP("UP_STOP"),
    DOWN_START("DOWN_START"),
    DOWN_STOP("DOWN_STOP"),
    LEFT_START("LEFT_START"),
    LEFT_STOP("LEFT_STOP"),
    RIGHT_START("RIGHT_START"),
    RIGHT_STOP("RIGHT_STOP"),

    // Switch
    ADAS_ON("ADAS_ON"),
    ADAS_OFF("ADAS_OFF"),
    SLEEP_DETECT_ON("SLEEP_DETECT_ON"),
    SLEEP_DETECT_OFF("SLEEP_DETECT_OFF"),
    BLIND_SPOT_ON("BLIND_SPOT_ON"),
    BLIND_SPOT_OFF("BLIND_SPOT_OFF"),
    LANE_KEEPING_ON("LANE_KEEPING_ON"),
    LANE_KEEPING_OFF("LANE_KEEPING_OFF");


    private final String text;

    /**
     * @param text
     */
    BluetoothCommand(String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
