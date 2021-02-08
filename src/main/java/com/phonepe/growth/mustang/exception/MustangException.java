/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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