package com.soonwook.aiproject.service;

import reactor.core.publisher.Flux;

public interface ChatBotService {

  Flux<String> streamResponse(String prompt);

}
