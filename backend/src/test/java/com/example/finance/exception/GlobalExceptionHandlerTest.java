package com.example.finance.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import jakarta.persistence.EntityNotFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void duplicateKeyMapsToConflict() {
        var response = handler.handleDuplicate(new DuplicateKeyException("dup"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("dup");
    }

    @Test
    void entityNotFoundMapsToNotFound() {
        var response = handler.handleNotFound(new EntityNotFoundException("missing"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("missing");
    }

    @Test
    void illegalArgumentMapsToBadRequest() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad input"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("bad input");
    }

    @Test
    void authenticationExceptionMapsToUnauthorized() {
        var response = handler.handleAuthentication(new BadCredentialsException("nope"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
