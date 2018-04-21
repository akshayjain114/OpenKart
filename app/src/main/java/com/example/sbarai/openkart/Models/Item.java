package com.example.sbarai.openkart.Models;

/**
 * Created by zaheenkhan on 4/16/18.
 */

public class Item {
    private String link;
    private String name;
    private long rate;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRate() {
        return rate;
    }

    public void setRate(long rate) {
        this.rate = rate;
    }
}
