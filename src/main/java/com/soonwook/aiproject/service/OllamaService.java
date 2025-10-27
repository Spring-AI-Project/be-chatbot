package com.soonwook.aiproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class OllamaService implements ChatBotService {

  private final WebClient webClient;
  private final String modelName;
  private final ObjectMapper mapper = new ObjectMapper();

  public OllamaService(WebClient.Builder builder,
      @Value("${ollama.base-url}") String baseUrl,
      @Value("${ollama.model}") String modelName) {
    this.webClient = builder.baseUrl(baseUrl).build();
    this.modelName = modelName;
  }

  @Override
  public Flux<String> streamResponse(String prompt) {
    return webClient.post()
        .uri("/api/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of("model", modelName, "prompt", prompt))
        .retrieve()
        .bodyToFlux(DataBuffer.class)
        .map(buf -> buf.toString(StandardCharsets.UTF_8))
        .concatMap(chunk -> Flux.fromArray(chunk.split("\n")))
        .map(this::safeExtractResponse)
        .filter(s -> !s.isBlank());
  }

  private String safeExtractResponse(String jsonLine) {
    try {
      JsonNode node = mapper.readTree(jsonLine);
      JsonNode resp = node.get("response");
      return (resp != null && !resp.isNull()) ? resp.asText() : "";
    } catch (Exception ignore) {
      return "";
    }
  }
}
