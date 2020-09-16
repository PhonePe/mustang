package com.phonepe.growth.mustang.index.core;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "name", "value", "order" })
@NoArgsConstructor
@AllArgsConstructor
public class Key {
    @NotBlank
    private String name;
    @NotNull
    private Object value;
    private int order;
    @Builder.Default
    private long upperBoundScore = 10;
}