package org.extism.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class HostFunctionTests {
    
    long NULL_PTR = 0;
    
    @Test
    public void callbackShouldAcceptNullParameters() {
        var callback = new HostFunction.Callback<>(
                (plugin, params, returns, userData) -> {/* NOOP */}, new LibExtism.ExtismValType[0], new LibExtism.ExtismValType[0], null);
        callback.invoke(NULL_PTR, null, 0, null, 0, NULL_PTR);
    }

    @Test
    public void callbackShouldThrowOnNullParametersAndNonzeroCounts() {
        var callback = new HostFunction.Callback<>(
                (plugin, params, returns, userData) -> {/* NOOP */}, new LibExtism.ExtismValType[0], new LibExtism.ExtismValType[0], null);
        assertThrows(ExtismException.class, () ->
                callback.invoke(NULL_PTR, null, 1, null, 0, NULL_PTR));
        assertThrows(ExtismException.class, () ->
                callback.invoke(NULL_PTR, null, 0, null, 1, NULL_PTR));
    }
}
