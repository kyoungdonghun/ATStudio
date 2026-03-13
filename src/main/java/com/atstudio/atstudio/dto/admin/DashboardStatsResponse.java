package com.atstudio.atstudio.dto.admin;

import com.atstudio.atstudio.dto.user.UserListItemResponse;

import java.util.List;

public record DashboardStatsResponse(
        long totalUsers,
        long totalTracks,
        long totalSubscribers,
        List<UserListItemResponse> recentUsers
) {}
