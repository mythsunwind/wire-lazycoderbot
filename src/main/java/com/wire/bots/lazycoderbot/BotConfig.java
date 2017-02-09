package com.wire.bots.lazycoderbot;

public class BotConfig extends com.wire.bots.sdk.Configuration {

    public String name;
    public int accent;
    private String smallProfile;
    private String bigProfile;

    public String getName() {
        return name;
    }

    public int getAccent() {
        return accent;
    }

    public String getSmallProfile() {
        return smallProfile;
    }

    public String getBigProfile() {
        return bigProfile;
    }
}
