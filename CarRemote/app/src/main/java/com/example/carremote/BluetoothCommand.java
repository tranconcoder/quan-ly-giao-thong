package com.example.carremote;

public enum BluetoothCommand {
    UP_START("UP_START"),
    UP_STOP("UP_STOP"),
    DOWN_START("DOWN_START"),
    DOWN_STOP("DOWN_STOP"),
    LEFT_START("LEFT_START"),
    LEFT_STOP("LEFT_STOP"),
    RIGHT_START("RIGHT_START"),
    RIGHT_STOP("RIGHT_STOP");


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
