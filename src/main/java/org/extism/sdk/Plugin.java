package org.extism.sdk;

import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.support.JsonSerde;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents a Extism plugin.
 */
public class Plugin implements AutoCloseable {

    /**
     * Holds the Extism plugin pointer
     */
    private final long pluginPointer;

    private final HostFunction[] functions;

    /**
     * @param manifestBytes The manifest for the plugin
     * @param functions     The Host functions for th eplugin
     * @param withWASI      Set to true to enable WASI
     */
    public Plugin(byte[] manifestBytes, boolean withWASI, HostFunction[] functions) {

        Objects.requireNonNull(manifestBytes, "manifestBytes");

        long[] ptrArr = new long[functions == null ? 0 : functions.length];

        if (functions != null)
            for (int i = 0; i < functions.length; i++) {
               ptrArr[i] = functions[i].pointer;
            }

        String[] errormsg = new String[1];
        long p = LibExtism.INSTANCE.extism_plugin_new(manifestBytes, manifestBytes.length,
                ptrArr,
                functions == null ? 0 : functions.length,
                withWASI,
                errormsg);
        if (p == 0) {
            if (functions != null) {
                for (int i = 0; i < functions.length; i++) {
                    LibExtism.INSTANCE.extism_function_free(functions[i].pointer);
                }
            }
//            LibExtism0.INSTANCE.extism_plugin_new_error_free();
            throw new ExtismException(errormsg[0]);
        }

        this.functions = functions;
        this.pluginPointer = p;
    }

    
    public Plugin(byte[] manifestBytes, boolean withWASI, HostFunction[] functions, long fuelLimit) {

        Objects.requireNonNull(manifestBytes, "manifestBytes");

        long[] ptrArr = new long[functions == null ? 0 : functions.length];

        if (functions != null)
            for (int i = 0; i < functions.length; i++) {
               ptrArr[i] = functions[i].pointer;
            }

        long[] errormsg = new long[1];
        long p = LibExtism.INSTANCE.extism_plugin_new_with_fuel_limit(manifestBytes, manifestBytes.length,
                ptrArr,
                functions == null ? 0 : functions.length,
                withWASI,
                fuelLimit,
                errormsg);
        if (p == 0) {
            if (functions != null) {
                for (int i = 0; i < functions.length; i++) {
                    LibExtism.INSTANCE.extism_function_free(functions[i].pointer);
                }
            }
//            String msg = errormsg[0].getString(0);
            String msg = "FIXME err init"; //FIXME
            LibExtism.INSTANCE.extism_plugin_new_error_free(errormsg[0]);
            throw new ExtismException(msg);
        }

        this.functions = functions;
        this.pluginPointer = p;
    }

    public Plugin(Manifest manifest, boolean withWASI, HostFunction[] functions) {
        this(serialize(manifest), withWASI, functions);
    }

    
    public Plugin(Manifest manifest, boolean withWASI, HostFunction[] functions, long fuelLimit) {
        this(serialize(manifest), withWASI, functions, fuelLimit);
    }

    private static byte[] serialize(Manifest manifest) {
        Objects.requireNonNull(manifest, "manifest");
        return JsonSerde.toJson(manifest).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Invoke a function with the given name and input.
     *
     * @param functionName The name of the exported function to invoke
     * @param inputData    The raw bytes representing any input data
     * @return A byte array representing the raw output data
     * @throws ExtismException if the call fails
     */
    public byte[] call(String functionName, byte[] inputData) {

        Objects.requireNonNull(functionName, "functionName");

        int inputDataLength = inputData == null ? 0 : inputData.length;
        int exitCode = LibExtism.INSTANCE.extism_plugin_call(this.pluginPointer, functionName, inputData, inputDataLength);
        if (exitCode != 0) {
            String error = this.error();
            throw new ExtismException(error);
        }

//        int length = LibExtism0.INSTANCE.extism_plugin_output_length(this.pluginPointer);
        return LibExtism.INSTANCE.extism_plugin_output_data(this.pluginPointer);
    }


    /**
     * Invoke a function with the given name and input.
     *
     * @param functionName The name of the exported function to invoke
     * @param input        The string representing the input data
     * @return A string representing the output data
     */
    public String call(String functionName, String input) {

        Objects.requireNonNull(functionName, "functionName");

        var inputBytes = input == null ? null : input.getBytes(StandardCharsets.UTF_8);
        var outputBytes = call(functionName, inputBytes);
        return new String(outputBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Get the error associated with a plugin
     *
     * @return the error message
     */
    protected String error() {
        String error = LibExtism.INSTANCE.extism_plugin_error(this.pluginPointer);
        if (error == null){
            return new String("Unknown error encountered when running Extism plugin function");
        }
        return error;
    }

    /**
     * Frees a plugin from memory
     */
    public void free() {
        if (this.functions != null){
            for (int i = 0; i < this.functions.length; i++) {
                this.functions[i].free();
            }
        }
        LibExtism.INSTANCE.extism_plugin_free(this.pluginPointer);
    }

    /**
     * Update plugin config values, this will merge with the existing values.
     *
     * @param json
     * @return
     */
    public boolean updateConfig(String json) {
        Objects.requireNonNull(json, "json");
        return updateConfig(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Update plugin config values, this will merge with the existing values.
     *
     * @param jsonBytes
     * @return {@literal true} if update was successful
     */
    public boolean updateConfig(byte[] jsonBytes) {
        Objects.requireNonNull(jsonBytes, "jsonBytes");
        return LibExtism.INSTANCE.extism_plugin_config(this.pluginPointer, jsonBytes, jsonBytes.length);
    }

    /**
     * Calls {@link #free()} if used in the context of a TWR block.
     */
    @Override
    public void close() {
        free();
    }

    /**
     * Return a new `CancelHandle`, which can be used to cancel a running Plugin
     */
    public CancelHandle cancelHandle() {
        long handle = LibExtism.INSTANCE.extism_plugin_cancel_handle(this.pluginPointer);
        return new CancelHandle(handle);
    }
}
