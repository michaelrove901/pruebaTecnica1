package com.example.funds.infrastructure.adapters.persistence.dynamodb.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Money;
import com.example.funds.domain.model.Role;
import com.example.funds.infrastructure.adapters.persistence.dynamodb.document.ClientItem;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public final class ClientDynamoDbMapper {

    private ClientDynamoDbMapper() {
    }

    public static ClientItem toItem(Client client) {
        return new ClientItem(
                client.clientId(),
                client.fullName(),
                client.email(),
                client.phone(),
                client.passwordHash(),
                client.roles().stream().map(Role::name).collect(Collectors.toSet()),
                client.balance().toLong(),
                client.createdAt().toString()
        );
    }

    public static Map<String, AttributeValue> toAttributes(ClientItem item) {
        return Map.of(
                "clientId", AttributeValue.fromS(item.clientId()),
                "fullName", AttributeValue.fromS(item.fullName()),
                "email", AttributeValue.fromS(item.email()),
                "phone", AttributeValue.fromS(item.phone()),
                "passwordHash", AttributeValue.fromS(item.passwordHash()),
                "roles", AttributeValue.fromSs(toStringList(item.roles())),
                "balance", AttributeValue.fromN(String.valueOf(item.balance())),
                "createdAt", AttributeValue.fromS(item.createdAt())
        );
    }

    public static Client toDomain(Map<String, AttributeValue> attributes) {
        return new Client(
                attributes.get("clientId").s(),
                attributes.get("fullName").s(),
                attributes.get("email").s(),
                attributes.get("phone").s(),
                attributes.get("passwordHash").s(),
                mapRoles(getStringSet(attributes, "roles")),
                Money.of(Long.parseLong(attributes.get("balance").n())),
                OffsetDateTime.parse(attributes.get("createdAt").s())
        );
    }

    private static Set<Role> mapRoles(Set<String> roles) {
        return roles.stream().map(Role::valueOf).collect(Collectors.toSet());
    }

    private static List<String> toStringList(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(values);
    }

    private static Set<String> getStringSet(Map<String, AttributeValue> attributes, String attributeName) {
        AttributeValue attributeValue = attributes.get(attributeName);
        if (attributeValue == null || attributeValue.ss() == null || attributeValue.ss().isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(attributeValue.ss());
    }
}
