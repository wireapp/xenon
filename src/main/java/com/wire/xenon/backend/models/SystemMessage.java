package com.wire.xenon.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

/**
 * DTO mapped from Payload, used for message handling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemMessage {
    public UUID id;
    public String type;
    public String time;
    public QualifiedId from;
    public Conversation conversation;
    public List<QualifiedId> users;
}
