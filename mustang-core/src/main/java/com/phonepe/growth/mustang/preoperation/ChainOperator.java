package com.phonepe.growth.mustang.preoperation;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ChainOperator {

    public static Object operate(final List<PreOperation> operations, final Object value) {
        final AtomicReference<Object> outcome = new AtomicReference<Object>(value);
        operations.forEach(operation -> outcome.getAndSet(operation.operate(outcome.get())));
        return outcome.get();
    }

}
