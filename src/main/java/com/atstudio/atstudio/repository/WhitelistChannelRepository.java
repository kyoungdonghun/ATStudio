package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.WhitelistChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhitelistChannelRepository extends JpaRepository<WhitelistChannel, Long> {

    List<WhitelistChannel> findByUserOrderByCreatedAtDesc(User user);

    long countByUser(User user);
}
