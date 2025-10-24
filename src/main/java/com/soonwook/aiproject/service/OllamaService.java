package com.soonwook.aiproject.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class OllamaService implements ChatBotService{

  private final WebClient webClient;
  private final String modelName;

  public OllamaService(
      WebClient.Builder builder,
      @Value("${ollama.base-url}") String baseUrl,
      @Value("${ollama.model}") String modelName
  ) {
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
        .bodyToFlux(String.class)
        .map(this::extractResponse);
  }

  private String extractResponse(String json) {
    int start = json.indexOf("\"response\":\"");
    if (start == -1) return "";
    start += 12;
    int end = json.indexOf("\"", start);
    if (end == -1) end = json.length();
    return json.substring(start, end);
  }

}
