package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
