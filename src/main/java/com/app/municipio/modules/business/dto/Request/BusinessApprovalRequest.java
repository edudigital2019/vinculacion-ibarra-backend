package com.app.municipio.modules.business.dto.Request;

public record BusinessApprovalRequest(
        Long businessId,
        boolean approve,
        String rejectionReason
) {}