package com.phonepe.growth.mustang.preoperation.impl;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.preoperation.PreOperation;
import com.phonepe.growth.mustang.preoperation.PreOperationType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ToDateTimePreOperation extends PreOperation {
    @NotBlank
    private String dateTimeFormat;

    @Builder
    @JsonCreator
    public ToDateTimePreOperation(@JsonProperty("dateTimeFormat") String dateTimeFormat) {
        super(PreOperationType.TO_DATETIME);
        this.dateTimeFormat = dateTimeFormat;
    }

    @Override
    public Object operate(Object lhs) {
        if (canApply(lhs)) {
            try {
                final long time = ZonedDateTime.parse(lhs.toString(), getFormatter())
                        .toEpochSecond() * 1000L;
                return time; // epoch in millis
            } catch (Exception e) {
                log.error(String.format("Error while parsing the date string - {} for the format - {}",
                        lhs.toString(),
                        getFormatter()), e);
                return lhs;
            }
        }
        return lhs;
    }

    private DateTimeFormatter getFormatter() {
        try {
            return DateTimeFormatter.ofPattern(dateTimeFormat);
        } catch (Exception e) {
            log.error(
                    String.format("Error with datetime pattern - {}. Using ISO_OFFSET_DATE_TIME instead ",
                            dateTimeFormat),
                    e);
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        }
    }

    @Override
    public boolean canApply(Object lhs) {
        return Objects.nonNull(lhs)
                && (String.class.isAssignableFrom(lhs.getClass()) || Number.class.isAssignableFrom(lhs.getClass()));
    }

}
