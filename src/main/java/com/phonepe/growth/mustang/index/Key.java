package com.phonepe.growth.mustang.index;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "name", "value" })
@NoArgsConstructor
@AllArgsConstructor
public class Key {
    @NotBlank
    private String name;
    @NotNull
    private Object value;
    @Builder.Default
    private long upperBoundScore = 10;
}