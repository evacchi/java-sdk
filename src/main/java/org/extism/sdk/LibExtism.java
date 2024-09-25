package org.extism.sdk;


import java.nio.ByteBuffer;

/**
 * Wrapper around the Extism library.
 */
public class LibExtism {

    static {
        System.load("/Users/evacchi/Devel/extism/extism/target/debug/libextism.dylib");
    }

    /**
     * Holds the extism library instance.
     * Resolves the extism library based on the resolution algorithm defined in ...
     */
    public static LibExtism INSTANCE = new LibExtism();

    public static interface InternalExtismFunction  {
        void invoke(
                long currentPlugin,
                long[] inputs,
                int nInputs,
                long[] outputs,
                int nOutputs,
                long data
        );
    }

    public static class ExtismVal  {
        public int t;
        public ExtismValUnion v;
    }
    public static class ExtismValUnion  {
        public int i32;
        public long i64;
        public long ptr;
        public float f32;
        public double f64;
    }

    enum ExtismValType {
        I32(0),
        I64(1),
        // PTR is an alias for I64
        PTR(1),
        F32(2),
        F64(3),
        V128(4),
        FuncRef(5),
        ExternRef(6);

        public final int v;

        ExtismValType(int value) {
            this.v = value;
        }
    }


    // TODO
    public native long extism_function_new(String name,
                                int[] inputs,
                                int nInputs,
                                int[] outputs,
                                int nOutputs,
                                InternalExtismFunction func,
                                Object userData,
                                long freeUserData);

    public native void extism_function_free(long function);


    /**
     * Get the length of an allocated block
     * NOTE: this should only be called from host functions.
     */
    public native int extism_current_plugin_memory_length(long plugin, long n);

    /**
     * Returns a pointer to the memory of the currently running plugin
     * NOTE: this should only be called from host functions.
     */
    public native ByteBuffer extism_current_plugin_memory(long plugin, long off, long size);

    /**
     * Allocate a memory block in the currently running plugin
     * NOTE: this should only be called from host functions.
     */
    public native int extism_current_plugin_memory_alloc(long plugin, long n);

    /**
     * Free an allocated memory block
     * NOTE: this should only be called from host functions.
     */
    public native void extism_current_plugin_memory_free(long plugin, long ptr);

    /**
     * Sets the logger to the given path with the given level of verbosity
     *
     * @param path     The file path of the logger
     * @param logLevel The level of the logger
     * @return true if successful
     */
    public native boolean extism_log_file(String path, String logLevel);

    /**
     * Returns the error associated with a @{@link Plugin}
     *
     * @param pluginPointer
     * @return
     */
    public native String extism_plugin_error(long pluginPointer);

    /**
     * Create a new plugin.
     *
     * @param wasm           is a WASM module (wat or wasm) or a JSON encoded manifest
     * @param wasmSize       the length of the `wasm` parameter
     * @param functions      host functions
     * @param nFunctions     the number of host functions
     * @param withWASI       enables/disables WASI
     * @param errmsg         get the error message if the return value is null
     * @return pointer to the plugin, or null in case of error
     */
    public native long extism_plugin_new(byte[] wasm, long wasmSize, long[] functions, int nFunctions, boolean withWASI, String[] errmsg);
    public native long extism_plugin_new_with_fuel_limit(byte[] wasm, long wasmSize, long[] functions, int nFunctions, boolean withWASI, long fuelLimit, String[] errmsg);


    public native String extism_plugin_new_error_get(long errmsg);


    /**
     * Free error message from `extism_plugin_new`
     */
    public native void extism_plugin_new_error_free(long errmsg);

    /**
     * Returns the Extism version string
     */
    public native String extism_version();


    /**
     * Calls a function from the @{@link Plugin} at the given {@code pluginIndex}.
     *
     * @param pluginPointer
     * @param function_name  is the function to call
     * @param data           is the data input data
     * @param dataLength     is the data input data length
     * @return the result code of the plugin call. non-zero in case of error, {@literal 0} otherwise.
     */
    public native int extism_plugin_call(long pluginPointer, String function_name, byte[] data, int dataLength);

    /**
     * Returns
     * @return the length of the output data in bytes.
     */
    public native int extism_plugin_output_length(long pluginPointer);

    /**

     * @return
     */
    public native byte[] extism_plugin_output_data(long pluginPointer);

    /**
     * Remove a plugin from the
     */
    public native void extism_plugin_free(long pluginPointer);

    /**
     * Update plugin config values, this
     * @param json
     * @param jsonLength
     * @return {@literal true} if update was successful
     */
    public native boolean extism_plugin_config(long pluginPointer, byte[] json, int jsonLength);
    public native long extism_plugin_cancel_handle(long pluginPointer);
    public native boolean extism_plugin_cancel(long cancelHandle);
    public native void extism_function_set_namespace(long p, String name);
}
