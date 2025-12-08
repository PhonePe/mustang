package com.phonepe.central.mustang.response;

import com.phonepe.growth.mustang.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MustangResponse<T> {
    private boolean success;
    private T data;
    private ErrorCode errorCode;
}
