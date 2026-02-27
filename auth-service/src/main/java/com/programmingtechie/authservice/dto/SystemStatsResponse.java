package com.programmingtechie.authservice.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemStatsResponse {

    private Long totalUsers;
    private Long activeUsers;
    private Long lockedUsers;
    private Long adminUsers;
    private Long moderatorUsers;
    private Long regularUsers;
    private Long activeSessions;
    private Long totalSessions;
}