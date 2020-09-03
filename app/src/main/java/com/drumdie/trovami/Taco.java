package com.drumdie.trovami;

public class Taco {
    private double latitude;
    private double longitude;
    private String flavor;

    public Taco(double latitude, double longitude, String flavor) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.flavor = flavor;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getFlavor() {
        return flavor;
    }


}
