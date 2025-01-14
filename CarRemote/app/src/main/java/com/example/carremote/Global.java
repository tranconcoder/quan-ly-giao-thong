package com.example.carremote;

public enum Global {
    TAG("CarRemoteLog");

    private final String text;

    /**
     * @param text
     */
    Global(String text) {
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
