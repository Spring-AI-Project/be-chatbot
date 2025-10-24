package com.soonwook.aiproject.repository;

import com.soonwook.aiproject.entity.ChatHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
  List<ChatHistory> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
}