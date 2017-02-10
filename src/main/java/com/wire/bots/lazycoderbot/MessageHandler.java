package com.wire.bots.lazycoderbot;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.lazycoderbot.models.StackOverflowAnswer;
import com.wire.bots.lazycoderbot.models.StackOverflowAnswers;
import com.wire.bots.lazycoderbot.models.StackOverflowQuestion;
import com.wire.bots.lazycoderbot.models.StackOverflowSearch;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class MessageHandler extends MessageHandlerBase {

    private static final String INFOTEXT = "Hi, I'm lazycoderbot!";
    private static final String API_URL = "https://api.stackexchange.com";

    private BotConfig config;
    private HashMap<String, String> lastAnswers = new HashMap<>();

    public MessageHandler(BotConfig config) {
        this.config = config;
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            Logger.info(String.format("Bot %s read a message", client.getId()));

            String message = msg.getText();

            if (message != null) {
                if (message.toLowerCase().equals("more") && lastAnswers.containsKey(client.getConversationId())) {
                    client.sendText("Feature not implemented yet!");
                    return;
                }
                if (isValidQuery(message)) {
                    String language = getLanguage(message);
                    ClientConfig cfg = new ClientConfig(JacksonJsonProvider.class);
                    JerseyClient http = JerseyClientBuilder.createClient(cfg);
                    Response response = http.target(API_URL)
                            .path("2.2")
                            .path("search")
                            .queryParam("order", "desc")
                            .queryParam("sort", "votes")
                            .queryParam("tagged", language)
                            .queryParam("site", "stackoverflow")
                            .queryParam("intitle", message.substring(language.length()))
                            .request(MediaType.APPLICATION_JSON)
                            .get();
                    if (!response.hasEntity()) {
                        client.sendText("No matching question found! You have to sit down and write the code yourself!");
                    } else {
                        StackOverflowSearch search = response.readEntity(StackOverflowSearch.class);
                        if (search.quota_remaining < 1) {
                            client.sendText("Quota of stackoverflow api exceeded");
                        } else {

                            String questionIds = "";
                            for (StackOverflowQuestion item : search.items) {
                                if (questionIds.isEmpty()) {
                                    questionIds = String.valueOf(item.question_id);
                                } else {
                                    questionIds = questionIds + ";" + item.question_id;
                                }
                            }
                            response = http.target(API_URL)
                                    .path("2.2")
                                    .path("questions")
                                    .path(String.valueOf(questionIds))
                                    .path("answers")
                                    .queryParam("order", "desc")
                                    .queryParam("sort", "votes")
                                    .queryParam("site", "stackoverflow")
                                    .queryParam("filter", "!-*f(6s6U7ofL")
                                    .request(MediaType.APPLICATION_JSON)
                                    .get();
                            StackOverflowAnswers answers = response.readEntity(StackOverflowAnswers.class);
                            if (answers.quota_remaining < 1) {
                                client.sendText("Quota of stackoverflow api exceeded");
                            } else {
                                if (answers.items.size() > 0) {
                                    StackOverflowAnswer answer = answers.items.get(0);
                                    String body = sanatizeBody(replaceWithMarkDown(answer.body));
                                    client.sendText(body + "\n\nAnswered by " + answer.owner.display_name + " on " +
                                            "stackoverflow");
                                } else {
                                    client.sendText("No answers");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
    }

    private String sanatizeBody(String body) {
        return body.replaceAll("\\<.*?>", "");
    }

    private String replaceWithMarkDown(String body) {
        body = body.replaceAll("<\\/?em>", "**");
        body = body.replaceAll("<\\/?code>", "```");
        return body;
    }

    private boolean isValidQuery(String message) {
        // no two long queries
        if (message.length() > 400) return false;

        return getLanguage(message) != null;
    }

    private String getLanguage(String message) {
        String supportedLanguages = "(java|python|javascript|swift|perl|html|css|c++|c#|bash)";
        Pattern pattern = Pattern.compile(String.format("^%s .*$", supportedLanguages));
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private static void sendRandomText(WireClient client) throws Exception {
        client.sendText(UUID.randomUUID().toString());
    }

    /**
     * @param newBot NewBot object containing info about the conversation this bot is being added to
     *               This method is called when the User adds this bot into existing conversation.
     * @return True if this user is entitled to create new conversation with this bot
     */
    @Override
    public boolean onNewBot(NewBot newBot) {
        Logger.info(String.format("onNewBot: user: %s/%s, locale: %s",
                newBot.origin.id,
                newBot.origin.name,
                newBot.locale));

        // return false in case you don't want to allow this user to open new conv with your bot
        return true;
    }

    /**
     * This method is called when bot is added into new conversation and it's ready to posts into it.
     *
     * @param client BotClient object that can be used to post new content into this conversation
     */
    @Override
    public void onNewConversation(WireClient client) {
        try {
            Conversation conversation = client.getConversation();

            Logger.info(String.format("Bot %s was added to conversation %s with name: %s",
                    client.getId(),
                    conversation.id,
                    conversation.name));

            client.sendText(INFOTEXT);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
    }


    /**
     * This method is called when new participant joins the conversation
     *
     * @param client  BotClient object that can be used to post new content into this conversation
     * @param userIds List of New participants that were just added into the conv.
     */
    @Override
    public void onMemberJoin(WireClient client, ArrayList<String> userIds) {
        try {
            Collection<User> users = client.getUsers(userIds);
            for (User user : users) {
                Logger.info(String.format("onMemberJoin: user: %s/%s, bot: %s",
                        user.id,
                        user.name,
                        client.getId()));

                // say Hi to new participant
                client.sendText("Hi there " + user.name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
    }

    /**
     * This method is called when somebody leaves the conversation
     *
     * @param client  BotClient object that can be used to post new content into this conversation
     * @param userIds List of participants that just left the conv (or being kicked out of it :-p).
     */
    @Override
    public void onMemberLeave(WireClient client, ArrayList<String> userIds) {

    }

    /**
     * Overrides default bot name.
     *
     * @return Bot name
     */
    @Override
    public String getName() {
        return config.getName();
    }

    /**
     * Overrides default bot's accent colour.
     *
     * @return accent colour id [0-7]
     */
    @Override
    public int getAccentColour() {
        return config.getAccent();
    }

    @Override
    public String getSmallProfilePicture() {
        return config.getSmallProfile();
    }

    @Override
    public String getBigProfilePicture() {
        return config.getBigProfile();
    }
}
