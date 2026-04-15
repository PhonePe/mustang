package com.phonepe.central.mustang.common;

@FunctionalInterface
public interface SecureExecution<T> {
    T execute();
}
