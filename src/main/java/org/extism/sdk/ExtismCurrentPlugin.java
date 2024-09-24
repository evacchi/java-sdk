package org.extism.sdk;

import java.nio.charset.StandardCharsets;

public class ExtismCurrentPlugin {
    public long pointer;

    public ExtismCurrentPlugin(long pointer) {
        this.pointer = pointer;
    }

    public long memory() {
        return LibExtism0.INSTANCE.extism_current_plugin_memory(this.pointer);
    }

    public int alloc(int n) {
        return LibExtism0.INSTANCE.extism_current_plugin_memory_alloc(this.pointer, n);
    }

    public void free(long offset) {
        LibExtism0.INSTANCE.extism_current_plugin_memory_free(this.pointer, offset);
    }

    public long memoryLength(long offset) {
        return LibExtism0.INSTANCE.extism_current_plugin_memory_length(this.pointer, offset);
    }

    /**
     * Return a string from a host function
     * @param output - The output to set
     * @param s - The string to return
     */
    public void returnString(LibExtism.ExtismVal output, String s) {
        returnBytes(output, s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Return bytes from a host function
     * @param output - The output to set
     * @param b - The buffer to return
     */
    public void returnBytes(LibExtism.ExtismVal output, byte[] b) {
        int offs = this.alloc(b.length);
        long ptr = this.memory();
//        ptr.write(offs, b, 0, b.length); /// FIXME
        output.v.i64 = offs;
    }

    /**
     * Get bytes from host function parameter
     * @param input - The input to read
     */
    public byte[] inputBytes(LibExtism.ExtismVal input) {
        // FIXME
        return null;
//        switch (input.t) {
//            case 0:
//                return this.memory()
//                        .getByteArray(input.v.i32,
//                                LibExtism0.INSTANCE.extism_current_plugin_memory_length(this.pointer, input.v.i32));
//            case 1:
//                return this.memory()
//                        .getByteArray(input.v.i64,
//                                LibExtism0.INSTANCE.extism_current_plugin_memory_length(this.pointer, input.v.i64));
//            default:
//                throw new ExtismException("inputBytes error: ExtismValType " + LibExtism.ExtismValType.values()[input.t] + " not implemtented");
//        }
    }


    /**
     * Get string from host function parameter
     * @param input - The input to read
     */
    public String inputString(LibExtism.ExtismVal input) {
        return new String(this.inputBytes(input));
    }
}