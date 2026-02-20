package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
}
