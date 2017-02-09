package com.wire.bots.lazycoderbot;

import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.setup.Environment;

public class BotService extends Server<BotConfig> {

    public static void main(String[] args) throws Exception {
        new BotService().run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(BotConfig config) {
        return new MessageHandler(config);
    }

    @Override
    protected void onRun(BotConfig botConfig, Environment environment) {

    }
}
