package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Like;
import com.atstudio.atstudio.entity.key.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
}
