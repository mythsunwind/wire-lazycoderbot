package com.wire.bots.lazycoderbot.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowSearch {

    @JsonProperty
    public List<StackOverflowItem> items;

    @JsonProperty
    public boolean has_more;

    @JsonProperty
    public int quota_max;

    @JsonProperty
    public int quota_remaining;

}
