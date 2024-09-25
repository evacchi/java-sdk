package org.extism.sdk;

import java.util.Arrays;
import java.util.Optional;

public class HostFunction<T extends HostUserData> {

    private final LibExtism.InternalExtismFunction callback;

    private boolean freed;

    public final long pointer;

    public final String name;

    public final LibExtism.ExtismValType[] params;

    public final LibExtism.ExtismValType[] returns;

    public HostFunction(String name, LibExtism.ExtismValType[] params, LibExtism.ExtismValType[] returns, ExtismFunction f, Optional<T> userData) {
        this.freed = false;
        this.name = name;
        this.params = params;
        this.returns = returns;
        this.callback = new Callback(f, params, returns, userData);

        this.pointer = LibExtism.extism_function_new(
                this.name,
                Arrays.stream(this.params).mapToInt(r -> r.v).toArray(),
                this.params.length,
                Arrays.stream(this.returns).mapToInt(r -> r.v).toArray(),
                this.returns.length,
                this.callback,
                userData.orElse(null),
                0
        );
    }

    static long convertOutput(LibExtism.ExtismVal original, LibExtism.ExtismValType t) {
        if (t == LibExtism.ExtismValType.I32) {
            return original.v.i32;
        } else if (t == LibExtism.ExtismValType.I64 || t == LibExtism.ExtismValType.PTR) {
            // PTR is an alias for I64
            if (original.v.i64 == 0 && original.v.ptr > 0) {
                return original.v.ptr;
            } else {
                return original.v.i64;
            }
        } else if (t == LibExtism.ExtismValType.F32) {
            return Float.floatToIntBits(original.v.f32);
        } else if (t == LibExtism.ExtismValType.F64) {
            return Double.doubleToLongBits(original.v.f64);
        } else
            throw new ExtismException(String.format("Unsupported return type: %s", original.t));
    }

    public void setNamespace(String name) {
        if (this.pointer != 0) {
            LibExtism.extism_function_set_namespace(this.pointer, name);
        }
    }

    public HostFunction withNamespace(String name) {
        this.setNamespace(name);
        return this;
    }

    public void free() {
        if (!this.freed) {
            LibExtism.extism_function_free(this.pointer);
            this.freed = true;
        }
    }

    static class Callback<T> implements LibExtism.InternalExtismFunction {
        private final ExtismFunction f;
        private final LibExtism.ExtismValType[] params;
        private final LibExtism.ExtismValType[] returns;
        private final Optional<T> userData;

        public Callback(ExtismFunction f, LibExtism.ExtismValType[] params, LibExtism.ExtismValType[] returns, Optional<T> userData) {
            this.f = f;
            this.params = params;
            this.returns = returns;
            this.userData = userData;
        }

        @Override
        public void invoke(long currentPlugin, long[] ins, int nInputs, long[] outs, int nOutputs, long data) {
            LibExtism.ExtismVal[] inVals = checkVals(ins, nInputs, params);
            LibExtism.ExtismVal[] outVals = checkVals(outs, nOutputs, returns);

            f.invoke(new ExtismCurrentPlugin(currentPlugin), inVals, outVals, userData);

            for (int i = 0; i < nOutputs; i++) {
                outs[i] = convertOutput(outVals[i], returns[i]);
            }
        }

        private static LibExtism.ExtismVal[] checkVals(long[] vs, int len, LibExtism.ExtismValType[] types) {
            LibExtism.ExtismVal[] vals = null;

            if (vs == null) {
                if (len > 0) {
                    throw new ExtismException("Output array is null but nOutputs is greater than 0");
                }
            } else {
                if (len != types.length) {
                    throw new ExtismException("Output array size does not match the expected number of result types");
                }
                vals = new LibExtism.ExtismVal[len];
            }

            for (int i = 0; i < len; i++) {
                vals[i] = convert(vs[i], types[i]);
            }


            return vals;
        }

        private static LibExtism.ExtismVal convert(long in, LibExtism.ExtismValType param) {
            LibExtism.ExtismVal val = new LibExtism.ExtismVal();
            val.t = param.v;
            val.v = new LibExtism.ExtismValUnion();

            switch (param) {
                case I32:
                    val.v.i32= (int) in;
                    break;
                case PTR:
                    // Fallthrough
                case I64:
                    val.v.i64= in;
                    val.v.ptr= in;
                    break;
                case F32:
                    val.v.f32 = Float.intBitsToFloat((int) in);
                    break;
                case F64:
                    val.v.f64 = Double.longBitsToDouble(in);
                    break;
                default:
                    throw new UnsupportedOperationException(param.name());
//                case V128:
//                    break;
//                case FuncRef:
//                    break;
//                case ExternRef:
//                    break;
            }
            return val;
        }


    }
}
