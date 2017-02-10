package com.wire.bots.lazycoderbot.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowAnswers {

    @JsonProperty
    public List<StackOverflowAnswer> items;

    @JsonProperty
    public int quota_remaining;
}
