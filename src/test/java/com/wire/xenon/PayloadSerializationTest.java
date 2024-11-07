package com.wire.xenon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.xenon.backend.models.Member;
import com.wire.xenon.backend.models.Payload;
import com.wire.xenon.backend.models.QualifiedId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PayloadSerializationTest {

    @Test
    public void qualifiedIdSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        QualifiedId id = new QualifiedId(UUID.randomUUID(), "domain.com");

        // Serialize the QualifiedId instance to JSON
        String json = objectMapper.writeValueAsString(id);

        assertTrue(json.contains("domain.com"));

        // Deserialize the JSON back to a QualifiedId instance
        QualifiedId deserializedId = objectMapper.readValue(json, QualifiedId.class);

        // Assert that the original and deserialized QualifiedId instances are equal
        assertEquals(id.id, deserializedId.id);
        assertEquals(id.domain, deserializedId.domain);
    }

    @Test
    public void qualifiedIdPartialSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UUID id = UUID.randomUUID();

        // Serialize the QualifiedId instance to JSON
        String json = objectMapper.writeValueAsString(id);

        // Deserialize the JSON back to a QualifiedId instance
        QualifiedId deserializedId = objectMapper.readValue(json, QualifiedId.class);

        // Assert that the original and deserialized QualifiedId instances are equal
        assertEquals(id, deserializedId.id);
        assertNull(deserializedId.domain);
    }

    @Test
    public void membersSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UUID memberId = UUID.randomUUID();

        Member member = new Member();
        member.id = new QualifiedId(memberId, "domain.com");
        Payload.Members members = new Payload.Members();
        members.others = List.of(member);

        // Serialize the Members instance to JSON
        String json = objectMapper.writeValueAsString(members);

        assertTrue(json.contains("domain.com"));

        // Deserialize the JSON back to a Members instance
        Payload.Members deserializedPayload = objectMapper.readValue(json, Payload.Members.class);

        // Assert that the original and deserialized Members instances are equal
        assertEquals(members.others.get(0).id, deserializedPayload.others.get(0).id);

        // Serialize the Members instance to JSON, just as a List<Member>
        String jsonArray = objectMapper.writeValueAsString(members.others);

        assertTrue(jsonArray.contains("domain.com"));

        // Deserialize the JSON back to a Members instance
        Payload.Members deserializedArrayPayload = objectMapper.readValue(jsonArray, Payload.Members.class);

        // Assert that the original and deserialized Members instances are equal
        assertEquals(members.others.get(0).id, deserializedArrayPayload.others.get(0).id);
    }

    @Test
    public void payloadSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UUID memberId = UUID.randomUUID();

        Member member = new Member();
        member.id = new QualifiedId(memberId, "domain.com");
        Payload.Members members = new Payload.Members();
        members.others = List.of(member);
        Payload.Data data = new Payload.Data();
        data.members = members;

        Payload payload = new Payload();
        payload.type = "conversation.create";
        payload.conversation = new QualifiedId(UUID.randomUUID(), "domain.com");
        payload.from = new QualifiedId(UUID.randomUUID(), "domain.com");
        payload.time = "2023-10-01T12:00:00Z";
        payload.team = UUID.randomUUID();
        payload.data = data;

        // Serialize the Payload instance to JSON
        String json = objectMapper.writeValueAsString(payload);

        assertTrue(json.contains("conversation.create"));
        assertTrue(json.contains(memberId.toString()));

        // Deserialize the JSON back to a Payload instance
        Payload deserializedPayload = objectMapper.readValue(json, Payload.class);

        // Assert that the original and deserialized Payload instances are equal
        assertEquals(payload.type, deserializedPayload.type);
        assertEquals(payload.conversation, deserializedPayload.conversation);
        assertEquals(payload.from, deserializedPayload.from);
        assertEquals(payload.time, deserializedPayload.time);
        assertEquals(payload.team, deserializedPayload.team);
        assertEquals(payload.data.members.others.get(0).id, deserializedPayload.data.members.others.get(0).id);
    }
}
