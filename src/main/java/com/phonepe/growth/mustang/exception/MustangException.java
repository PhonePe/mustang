package com.phonepe.growth.mustang.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MustangException extends RuntimeException {

    private static final long serialVersionUID = -4278856680596761879L;
    private final ErrorCode errorCode;

    @Builder
    public MustangException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public static MustangException propagate(final Throwable throwable) {
        return propagate("Error occurred", throwable);
    }

    public static MustangException propagate(final String message, final Throwable throwable) {
        if (throwable instanceof MustangException) {
            return (MustangException) throwable;
        } else if (throwable.getCause() instanceof MustangException) {
            return (MustangException) throwable.getCause();
        }
        return new MustangException(ErrorCode.INTERNAL_ERROR, message, throwable);
    }
}