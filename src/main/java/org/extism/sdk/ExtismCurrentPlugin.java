package org.extism.sdk;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ExtismCurrentPlugin {
    public long pointer;

    public ExtismCurrentPlugin(long pointer) {
        this.pointer = pointer;
    }

    public ByteBuffer memory(long off, long n) {
        return LibExtism.INSTANCE.extism_current_plugin_memory(this.pointer, off, n);
    }

    public long alloc(int n) {
        return LibExtism.INSTANCE.extism_current_plugin_memory_alloc(this.pointer, n);
    }

    public void free(long offset) {
        LibExtism.INSTANCE.extism_current_plugin_memory_free(this.pointer, offset);
    }

    public long memoryLength(long offset) {
        return LibExtism.INSTANCE.extism_current_plugin_memory_length(this.pointer, offset);
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
        long offset = this.alloc(b.length);
        ByteBuffer bb = this.memory(offset, b.length);
        bb.put(b);
        output.v.i64 = offset;
    }

    /**
     * Get bytes from host function parameter
     * @param input - The input to read
     */
    public byte[] inputBytes(LibExtism.ExtismVal input) {
        switch (input.t) {
            case 0: {
                var len = LibExtism.INSTANCE.extism_current_plugin_memory_length(this.pointer, input.v.i32);
                byte[] bb = new byte[len];
                this.memory(input.v.i32, len).get(bb);
                return bb;
            }
            case 1: {
                var len = LibExtism.INSTANCE.extism_current_plugin_memory_length(this.pointer, input.v.i64);
                byte[] bb = new byte[len];
                this.memory(input.v.i64, len).get(bb);
                return bb;
            }
            default:
                throw new ExtismException("inputBytes error: ExtismValType " + LibExtism.ExtismValType.values()[input.t] + " not implemtented");
        }
    }


    /**
     * Get string from host function parameter
     * @param input - The input to read
     */
    public String inputString(LibExtism.ExtismVal input) {
        return new String(this.inputBytes(input));
    }
}