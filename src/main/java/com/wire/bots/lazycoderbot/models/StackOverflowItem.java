package com.wire.bots.lazycoderbot.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowItem {

    @JsonProperty
    public List<String> tags;

    @JsonProperty
    public String link;
}
