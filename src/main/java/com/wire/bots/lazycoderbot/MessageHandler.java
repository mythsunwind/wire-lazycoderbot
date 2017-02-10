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
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.message.GZipEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class MessageHandler extends MessageHandlerBase {

    private static final String INFOTEXT = "Hi, I'm Lazy Coder Bot!"
            + " I search for complete solutions for you on stackoverflow when you're too lazy to code stuff yourself."
            + " I support many languages. Just send a text with a language as first word.\nExample: **java bubblesort**";
    private static final String API_URL = "https://api.stackexchange.com";

    private BotConfig config;
    HashMap<Integer, String> questions = new HashMap<>();
    private HashMap<String, List<Integer>> usedAnswerIds = new HashMap<>();
    private HashMap<String, StackOverflowAnswers> lastAnswers = new HashMap<>();

    public MessageHandler(BotConfig config) {
        this.config = config;
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            String message = msg.getText();

            if (message != null) {
                if (message.toLowerCase().equals("more")
                        && usedAnswerIds.containsKey(client.getConversationId())
                        && lastAnswers.containsKey(client.getConversationId())) {
                    for (StackOverflowAnswer answer : lastAnswers.get(client.getConversationId()).items) {
                        List<Integer> used = usedAnswerIds.get(client.getConversationId());
                        if (used != null && !used.contains(answer.answer_id)) {
                            used.add(answer.answer_id);
                            usedAnswerIds.put(client.getConversationId(), used);
                            postAnswer(client, questions, answer);
                            return;
                        }
                    }
                    client.sendText("No further answers for this query. You have to deal we the ones you got or write the " +
                            "code yourself!");
                    return;
                }
                if (isValidQuery(message)) {
                    String language = getLanguage(message);
                    Logger.info(String.format("Bot received query for language %s", language));
                    ClientConfig cfg = new ClientConfig(JacksonJsonProvider.class);
                    cfg.register(GZipEncoder.class);
                    JerseyClient http = JerseyClientBuilder.createClient(cfg);
                    Response response = http.target(API_URL)
                            .path("2.2")
                            .path("search")
                            .queryParam("order", "desc")
                            .queryParam("sort", "votes")
                            .queryParam("tagged", language)
                            .queryParam("site", "stackoverflow")
                            .queryParam("key", config.getApiKey())
                            .queryParam("intitle", message.substring(language.length()))
                            .request(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.CONTENT_ENCODING, "identity")
                            .get();
                    if (!response.hasEntity()) {
                        client.sendText("No matching question found! You have to sit down and write the code yourself!");
                    } else {
                        StackOverflowSearch search = response.readEntity(StackOverflowSearch.class);
                        Logger.info(String.format("Bot has remaining quota for api: %s", search.quota_remaining));
                        if (search.items.size() == 0) {
                            client.sendText("No matching question found! You have to sit down and write the code yourself!");
                        } else if (search.quota_remaining < 1) {
                            client.sendText("Quota of stackoverflow api exceeded");
                        } else {
                            String questionIds = "";
                            questions.clear();
                            for (StackOverflowQuestion item : search.items) {
                                questions.put(item.question_id, item.title);
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
                                    .queryParam("key", config.getApiKey())
                                    .request(MediaType.APPLICATION_JSON)
                                    .get();
                            StackOverflowAnswers answers = response.readEntity(StackOverflowAnswers.class);
                            if (answers.quota_remaining < 1) {
                                client.sendText("Quota of stackoverflow api exceeded");
                            } else {
                                if (answers.items.size() > 0) {
                                    lastAnswers.put(client.getConversationId(), answers);
                                    List<Integer> used = new ArrayList<>();
                                    used.add(answers.items.get(0).answer_id);
                                    usedAnswerIds.put(client.getConversationId(), used);
                                    postAnswer(client, questions, answers.items.get(0));
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

    private void postAnswer(WireClient client, HashMap<Integer, String> questions, StackOverflowAnswer answer) throws
            Exception {
        String body = sanatizeBody(replaceWithMarkDown(answer.body));
        String question = questions.get(answer.question_id);
        client.sendText("**" + question + "**\n"
                + body + "\n"
                + "*Answered by " + answer.owner.display_name + " on* " + answer.link);
    }

    private String sanatizeBody(String body) {
        return body.replaceAll("\\<.*?>", "");
    }

    private String replaceWithMarkDown(String body) {
        body = body.replaceAll("<\\/?em>", "**");
        body = body.replaceAll("<\\/?code>", "\n```\n");
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
}
