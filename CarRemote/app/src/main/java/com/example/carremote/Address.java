package com.example.carremote;

public enum Address {
    ESP32("C8:2E:18:25:E0:82"),
    ESP32_CAM("F8:B3:B7:32:6A:D6"),
    ESP32_S3("24:EC:4A:3A:2B:EE");

    private final String text;

    /**
     * @param text
     */
    Address(String text) {
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
