package com.interviai.backend.module.auth.mapper;

import com.interviai.backend.module.auth.dto.response.AuthenticationResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthMapperRefreshResponseTest {

    private final AuthMapper mapper = Mappers.getMapper(AuthMapper.class);

    @Test
    void refreshResponsePreservesRefreshToken() {
        AuthenticationResponse response = mapper.createRefreshResponse(
                "new-access-token",
                "existing-refresh-token",
                3600L
        );

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("existing-refresh-token", response.getRefreshToken());
        assertEquals(3600L, response.getExpiresIn());
    }
}
