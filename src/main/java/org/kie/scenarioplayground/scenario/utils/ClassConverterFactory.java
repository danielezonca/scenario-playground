package org.kie.scenarioplayground.scenario.utils;

import java.util.Optional;
import java.util.function.Function;

public class ClassConverterFactory {

    @SuppressWarnings("unchecked")
    public static <T> Function<Object, T> getConverter(Class<T> clazz) {
        if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
            return (rawValue) -> rawValue instanceof Integer ? (T) rawValue : (T) Integer.valueOf(String.valueOf(rawValue));
        } else if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
            return (rawValue) -> rawValue instanceof Double ? (T) rawValue : (T) Double.valueOf(String.valueOf(rawValue));
        }
        // TODO add other primitive types
        else {
            return (rawValue) -> clazz.isInstance(rawValue) ? (T) rawValue : applyConstructor(clazz, rawValue);
        }
    }

    private static <T> T applyConstructor(Class<T> clazz, Object rawValue) {
        final Optional<T> valueWithOwnConstructor = Try(() -> clazz.getConstructor(rawValue.getClass()).newInstance(rawValue));
        if (valueWithOwnConstructor.isPresent()) {
            return valueWithOwnConstructor.get();
        }
        final Optional<T> valueWithStringConstructor = Try(() -> clazz.getConstructor(String.class).newInstance(String.valueOf(rawValue)));
        return valueWithStringConstructor.orElseThrow(
                () -> new IllegalArgumentException("Impossible to instantiate a '" + clazz.getCanonicalName() +
                                                           "' with a '" + rawValue.getClass().getCanonicalName() + "'"));
    }

    private static <T> Optional<T> Try(SupplierException<T> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private interface SupplierException<T> {
        T get() throws Exception;
    }
}
