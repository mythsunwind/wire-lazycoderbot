package com.wire.bots.lazycoderbot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowOwner {

    @JsonProperty
    public String display_name;

}
