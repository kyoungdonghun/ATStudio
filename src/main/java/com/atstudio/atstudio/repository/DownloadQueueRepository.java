package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.DownloadQueue;
import com.atstudio.atstudio.entity.key.DownloadQueueId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadQueueRepository extends JpaRepository<DownloadQueue, DownloadQueueId> {
}
