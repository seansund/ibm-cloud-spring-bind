package com.ibm.cloud.spring.env.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ibm.cloud.spring.env.exceptions.ConfigurationNameNotFound;
import com.ibm.cloud.spring.env.exceptions.ConfigurationValueNotFound;
import com.ibm.cloud.spring.env.values.ValueResolver;
import com.ibm.cloud.spring.env.values.ValueResolverFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.ibm.cloud.spring.env.ConfigUtil.elementsAsStream;
import static com.ibm.cloud.spring.env.ConfigUtil.fieldsAsStream;

public class MappingConfig {
    private final ApplicationContext applicationContext;
    private final Map<String, Collection<ValueResolver>> searchPatternResolvers;

    public MappingConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.searchPatternResolvers = new WeakHashMap<>();
    }

    public String getValue(String name) {
        Collection<ValueResolver> retrievers = this.searchPatternResolvers.get(name);

        if (retrievers == null) {
            throw new ConfigurationNameNotFound(name);
        }

        Optional<String> value = retrievers.stream()
                .map(valueResolver -> valueResolver.get())      // apply the resolver logic to get the value
                .filter(resolvedValue -> resolvedValue != null) // filter out the empty results
                .findFirst();                                   // get the first non-empty result

        return value.orElseThrow(() -> new ConfigurationValueNotFound(name)); // throw an error if none of the resolvers returned a value
    }

    public MappingConfig addConfig(JsonNode node) {
        final Optional<JsonNode> optionalNode = Optional.ofNullable(node);
        if (!optionalNode.isPresent()) {
            return this;
        }

        final Predicate<Map.Entry<String, JsonNode>> notVersionKey = e -> !"version".equals(e.getKey());

        final int version = optionalNode.map(n -> n.get("version")).map(n -> n.asInt()).orElse(1);
        final Stream<Map.Entry<String, JsonNode>> nodeStream = fieldsAsStream(node).filter(notVersionKey);

        processNode(version, nodeStream, "");

        return this;
    }

    void processNode(int version, Stream<Map.Entry<String, JsonNode>> entryStream, String parentPath) {
        entryStream.forEach(e -> {
            if ("searchPatterns".equals(e.getKey())) {
                Stream<JsonNode> searchPatterns = Optional.of(e.getValue())
                        .filter(v -> v instanceof ArrayNode)              // test for ArrayNode
                        .map(v -> elementsAsStream((ArrayNode)v))                 // convert to a stream
                        .orElse(Stream.of());                             // return an empty stream if one is not present

                processSearchPatternsNode(version, searchPatterns, parentPath);
            } else if ("credentials".equals(e.getKey())) {
                processNode(version, fieldsAsStream(e.getValue()), parentPath);
            } else {
                processNode(version, fieldsAsStream(e.getValue()), appendKey(parentPath, e.getKey()));
            }
        });
    }

    static String appendKey(String parentKey, String newKey) {
        if (StringUtils.isEmpty(parentKey)) {
            return newKey;
        }

        return parentKey + "." + newKey;
    }

    void processSearchPatternsNode(int version, Stream<JsonNode> arrayStream, String parentPath) {
        arrayStream
                .map(n -> ValueResolverFactory.getValueResolver(applicationContext, version, n.asText()))
                .forEach(valueRetriever -> this.add(parentPath, valueRetriever));
    }

    void add(String name, ValueResolver retriever) {
        if (retriever == null) {
            return;
        }

        searchPatternResolvers.compute(name, (String key, Collection<ValueResolver> value) -> {
            if (value == null) {
                return Arrays.asList(retriever);
            }

            final List<ValueResolver> newValue = new ArrayList<>(value);
            newValue.add(retriever);

            return newValue;
        });
    }
}
