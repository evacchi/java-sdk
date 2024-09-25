package org.extism.sdk;

/**
 * CancelHandle is used to cancel a running Plugin
 */
public class CancelHandle {
    private long handle;

    public CancelHandle(long handle) {
        this.handle = handle;
    }

    /**
     * Cancel execution of the Plugin associated with the CancelHandle
     */
    boolean cancel() {
        return LibExtism.INSTANCE.extism_plugin_cancel(this.handle);
    }
}
