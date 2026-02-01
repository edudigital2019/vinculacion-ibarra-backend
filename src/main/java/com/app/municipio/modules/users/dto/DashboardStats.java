package com.app.municipio.modules.users.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStats {
    private long totalUsers;
    private long pendingUsers;
    private long approvedUsers;

}
