package com.fuchsbau.shorin.Engine.System.Character;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = AbilityScoreEntry.Deserializer.class)
public class AbilityScoreEntry {
    public final AbilityScore abilityScore;
    public int value;

    public AbilityScoreEntry(AbilityScore abilityScore, int value) {
        this.abilityScore = abilityScore;
        this.value = value;
    }

    public AbilityScoreEntry(AbilityScore abilityScore) {
        this.abilityScore = abilityScore;
        this.value = 1;
    }

    public static class Deserializer extends JsonDeserializer<AbilityScoreEntry> {
        @Override
        public AbilityScoreEntry deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                return new AbilityScoreEntry(AbilityScore.valueOf(p.getText()));
            }

            JsonNode node = p.getCodec().readTree(p);
            AbilityScore score = AbilityScore.valueOf(node.get("abilityScore").asText());
            int value = node.has("value") ? node.get("value").asInt() : 1;
            return new AbilityScoreEntry(score, value);
        }
    }
}