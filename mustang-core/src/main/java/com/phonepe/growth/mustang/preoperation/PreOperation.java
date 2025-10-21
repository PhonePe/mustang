package com.phonepe.growth.mustang.preoperation;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.growth.mustang.preoperation.impl.AdditionPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.BinaryConversionPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.DivisionPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.FromDateTimePreOperation;
import com.phonepe.growth.mustang.preoperation.impl.IdentityOperation;
import com.phonepe.growth.mustang.preoperation.impl.LengthPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.ModuloPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.MultiplicationPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SizePreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SubStringPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SubtractionPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.ToDateTimePreOperation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(name = PreOperationType.IDENTITY_TEXT, value = IdentityOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.ADDITION_TEXT, value = AdditionPreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.SUBTRACTION_TEXT, value = SubtractionPreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.MULTIPLCATION_TEXT, value = MultiplicationPreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.DIVISION_TEXT, value = DivisionPreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.MODULO_TEXT, value = ModuloPreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.BINARY_CONVERSION_TEXT, value = BinaryConversionPreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.SIZE_TEXT, value = SizePreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.LENGTH_TEXT, value = LengthPreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.SUBSTRING_TEXT, value = SubStringPreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.FROM_DATETIME_TEXT, value = FromDateTimePreOperation.class),
        @JsonSubTypes.Type(name = PreOperationType.TO_DATETIME_TEXT, value = ToDateTimePreOperation.class), })
@JsonPropertyOrder({ "type" })
@EqualsAndHashCode(of = { "type" })
public abstract class PreOperation {
    @NotNull
    private final PreOperationType type;

    public abstract Object operate(Object lhs);

    public abstract boolean canApply(Object lhs);
}
