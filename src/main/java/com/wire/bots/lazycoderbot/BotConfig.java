package com.wire.bots.lazycoderbot;

public class BotConfig extends com.wire.bots.sdk.Configuration {

    public String name;
    public int accent;
    public String api_key;

    public String getName() {
        return name;
    }

    public int getAccent() {
        return accent;
    }

    public String getApiKey() {
        return api_key;
    }
}
