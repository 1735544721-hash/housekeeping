package org.example.springboot.enumClass;

import io.swagger.v3.oas.annotations.media.Schema;

public enum AccountStatus {
    @Schema(description = "") ENABLE(1),
    @Schema(description = "") DISABLE(0);

    private final Integer value;

    AccountStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}