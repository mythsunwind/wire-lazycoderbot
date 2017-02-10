package com.wire.bots.lazycoderbot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowAnswer {

    @JsonProperty
    public StackOverflowOwner owner;

    @JsonProperty
    public String body;

    @JsonProperty
    public String link;

    @JsonProperty
    public int answer_id;

    @JsonProperty
    public int question_id;

}
