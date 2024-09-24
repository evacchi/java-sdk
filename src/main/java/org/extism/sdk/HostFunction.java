package org.extism.sdk;

import java.util.Arrays;
import java.util.Optional;

public class HostFunction<T extends HostUserData> {

    private final LibExtism0.InternalExtismFunction callback;

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

        this.pointer = LibExtism0.INSTANCE.extism_function_new(
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

    static void convertOutput(LibExtism0.ExtismVal original, LibExtism0.ExtismVal fromHostFunction) {
        if (fromHostFunction.t != original.t)
            throw new ExtismException(String.format("Output type mismatch, got %d but expected %d", fromHostFunction.t, original.t));

        // FIXME

//        if (fromHostFunction.t == LibExtism.ExtismValType.I32.v) {
//            original.v.setType(Integer.TYPE);
//            original.v.i32 = fromHostFunction.v.i32;
//        } else if (fromHostFunction.t == LibExtism.ExtismValType.I64.v) {
//            original.v.setType(Long.TYPE);
//            // PTR is an alias for I64
//            if (fromHostFunction.v.i64 == 0 && fromHostFunction.v.ptr > 0) {
//                original.v.i64 = fromHostFunction.v.ptr;
//            } else {
//                original.v.i64 = fromHostFunction.v.i64;
//            }
//        } else if (fromHostFunction.t == LibExtism.ExtismValType.F32.v) {
//            original.v.setType(Float.TYPE);
//            original.v.f32 = fromHostFunction.v.f32;
//        } else if (fromHostFunction.t == LibExtism.ExtismValType.F64.v) {
//            original.v.setType(Double.TYPE);
//            original.v.f64 = fromHostFunction.v.f64;
//        } else
//            throw new ExtismException(String.format("Unsupported return type: %s", original.t));
    }

    public void setNamespace(String name) {
        if (this.pointer != 0) {
            LibExtism0.INSTANCE.extism_function_set_namespace(this.pointer, name);
        }
    }

    public HostFunction withNamespace(String name) {
        this.setNamespace(name);
        return this;
    }

    public void free() {
        if (!this.freed) {
            LibExtism0.INSTANCE.extism_function_free(this.pointer);
            this.freed = true;
        }
    }

    static class Callback<T> implements LibExtism0.InternalExtismFunction {
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

            LibExtism.ExtismVal[] inVals = new LibExtism.ExtismVal[ins.length];
            LibExtism.ExtismVal[] outVals = new LibExtism.ExtismVal[outs.length];

            for (int i = 0; i < nInputs; i++) {
                inVals[i] = convert(ins[i], params[i]);
            }

            for (int i = 0; i < nOutputs; i++) {
                outVals[i] = convert(outs[i], params[i]);
            }


            // FIXME
            f.invoke(new ExtismCurrentPlugin(currentPlugin), inVals, outVals, userData);

//            for (long output : outs) {
//                convertOutput(output, output);
//            }
        }

        static LibExtism.ExtismVal convert(long in, LibExtism.ExtismValType param) {
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
