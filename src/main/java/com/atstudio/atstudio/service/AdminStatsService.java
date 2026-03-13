package com.atstudio.atstudio.service;

import com.atstudio.atstudio.dto.admin.DashboardStatsResponse;
import com.atstudio.atstudio.dto.user.UserListItemResponse;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.countByIsDeletedFalse();
        long totalTracks = trackRepository.countByIsActiveTrue();
        long totalSubscribers = userSubscriptionRepository.countActiveSubscribers();

        List<UserListItemResponse> recentUsers = userRepository.searchUsers(
                null, null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(UserListItemResponse::from).getContent();

        return new DashboardStatsResponse(totalUsers, totalTracks, totalSubscribers, recentUsers);
    }
}
