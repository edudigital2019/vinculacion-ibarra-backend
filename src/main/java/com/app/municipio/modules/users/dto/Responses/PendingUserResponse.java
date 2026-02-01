package com.app.municipio.modules.users.dto.Responses;

public record PendingUserResponse(
    Long id,
    String name,
    String email,
    String identification
) {}
