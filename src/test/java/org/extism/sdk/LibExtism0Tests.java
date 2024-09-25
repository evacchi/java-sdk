package org.extism.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibExtismTests_ {
    @Test
    public void test_free() {
        LibExtism.extism_function_free(100);
    }

    @Test
    public void test_extism_current_plugin_memory_length() {
        assertEquals(0, LibExtism.extism_current_plugin_memory_length(100, 100));
    }

    @Test
    public void test_extism_current_plugin_memory() {
        assertEquals(0, LibExtism.extism_current_plugin_memory(100, 0, 10));
    }

    @Test
    public void test_extism_current_plugin_alloc() {
        assertEquals(0, LibExtism.extism_current_plugin_memory_alloc(100, 100));
    }

    @Test
    public void test_extism_current_plugin_memory_free() {
        LibExtism.extism_current_plugin_memory_free(100, 100);
    }

    @Test
    public void test_extism_log_file() {
        assertTrue(LibExtism.extism_log_file("a", "B"));
    }

    @Test
    public void test_extism_plugin_error() {
        assertNull(LibExtism.extism_plugin_error(100));
    }


    @Test
    public void test_extism_plugin_new() {
        assertEquals(0, LibExtism.extism_plugin_new(null, 1, null, 1, true, null));
    }

    @Test
    public void test_extism_plugin_new_with_fuel_limit() {
        assertEquals(0, LibExtism.extism_plugin_new_with_fuel_limit(null, 1, null, 1, true, 10, null));
    }

    @Test
    public void test_extism_plugin_new_error_free() {
        LibExtism.extism_plugin_new_error_free(10);
    }

    @Test
    public void test_extism_version() {
        assertNull(LibExtism.extism_version());
    }

    @Test
    public void test_extism_plugin_call() {
        assertEquals(0, LibExtism.extism_plugin_call(0, "test", null, 1));
    }

    @Test
    public void test_extism_plugin_output_length() {
        assertEquals(0, LibExtism.extism_plugin_output_length(0));
    }

    @Test
    public void test_extism_plugin_output_data() {
        assertNull(LibExtism.extism_plugin_output_data(0));
    }

    @Test
    public void test_extism_plugin_free() {
        LibExtism.extism_plugin_free(0);
    }

    @Test
    public void test_extism_plugin_config() {
        assertTrue(LibExtism.extism_plugin_config(100, null, 0));
    }

    @Test
    public void test_extism_plugin_cancel_handle() {
        assertEquals(0, LibExtism.extism_plugin_cancel_handle(0));
    }

    @Test
    public void test_extism_plugin_cancel() {
        assertTrue(LibExtism.extism_plugin_cancel(0));
    }

    @Test
    public void test_extism_function_set_namespace() {
        LibExtism.extism_function_set_namespace(0, "foo");
    }


}
