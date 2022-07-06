package com.example.drinkernote;

import java.io.Serializable;

public class WhiskyData implements Serializable {
    String whisky_name;
    String whisky_label;
    String whisky_cask;
    String whisky_proof;
    String whisky_price;
    String contents;

    Float Body,Sweet,Spice,Malty,Fruit,Tannic,Floral;

    public WhiskyData(String whisky_name, String whisky_label, String whisky_cask, String whisky_proof, String whisky_price, String contents, Float body, Float sweet, Float spice, Float malty, Float fruit, Float tannic, Float floral) {
        this.whisky_name = whisky_name;
        this.whisky_label = whisky_label;
        this.whisky_cask = whisky_cask;
        this.whisky_proof = whisky_proof;
        this.whisky_price = whisky_price;
        this.contents = contents;
        Body = body;
        Sweet = sweet;
        Spice = spice;
        Malty = malty;
        Fruit = fruit;
        Tannic = tannic;
        Floral = floral;
    }

    public String getWhisky_name() {
        return whisky_name;
    }

    public void setWhisky_name(String whisky_name) {
        this.whisky_name = whisky_name;
    }

    public String getWhisky_label() {
        return whisky_label;
    }

    public void setWhisky_label(String whisky_label) {
        this.whisky_label = whisky_label;
    }

    public String getWhisky_cask() {
        return whisky_cask;
    }

    public void setWhisky_cask(String whisky_cask) {
        this.whisky_cask = whisky_cask;
    }

    public String getWhisky_proof() {
        return whisky_proof;
    }

    public void setWhisky_proof(String whisky_proof) {
        this.whisky_proof = whisky_proof;
    }

    public String getWhisky_price() {
        return whisky_price;
    }

    public void setWhisky_price(String whisky_price) {
        this.whisky_price = whisky_price;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Float getBody() {
        return Body;
    }

    public void setBody(Float body) {
        Body = body;
    }

    public Float getSweet() {
        return Sweet;
    }

    public void setSweet(Float sweet) {
        Sweet = sweet;
    }

    public Float getSpice() {
        return Spice;
    }

    public void setSpice(Float spice) {
        Spice = spice;
    }

    public Float getMalty() {
        return Malty;
    }

    public void setMalty(Float malty) {
        Malty = malty;
    }

    public Float getFruit() {
        return Fruit;
    }

    public void setFruit(Float fruit) {
        Fruit = fruit;
    }

    public Float getTannic() {
        return Tannic;
    }

    public void setTannic(Float tannic) {
        Tannic = tannic;
    }

    public Float getFloral() {
        return Floral;
    }

    public void setFloral(Float floral) {
        Floral = floral;
    }
}
