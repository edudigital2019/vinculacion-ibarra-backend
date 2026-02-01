package com.app.municipio.modules.users.dto.Request;

public record UserApprovalRequest(
    Long userId,
    boolean approve,
    String rejectionReason
) {}
