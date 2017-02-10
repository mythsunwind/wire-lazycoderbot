package com.wire.bots.lazycoderbot.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowQuestion {

    @JsonProperty
    public List<String> tags;

    @JsonProperty
    public String link;

    @JsonProperty
    public int question_id;
}
