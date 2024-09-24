use core::slice;
use std::{ffi::{c_void, CStr, CString}, io::stdout, ptr::{null, null_mut}, thread::{self, panicking}, time::Duration};

use extism::{sdk::{self, extism_current_plugin_memory, extism_current_plugin_memory_alloc, extism_current_plugin_memory_free, extism_current_plugin_memory_length, extism_function_free, extism_function_new, extism_plugin_call, extism_plugin_error, extism_plugin_new, extism_plugin_new_with_fuel_limit, extism_plugin_output_data, extism_plugin_output_length, ExtismFunction, ExtismFunctionType, ExtismMemoryHandle, ExtismVal, Size}, CurrentPlugin, Function, Plugin, UserData, ValType};
use jni_simple::{ *};



// (JNIEnv *, jobject, jstring, jintArray, jint, jintArray, jint, jobject, jobject, jlong);

#[no_mangle]
pub unsafe extern "system" fn JNI_OnLoad(vm: JavaVM, _reserved: *mut c_void) -> jint {

    use std::io::Write; // <--- bring the trait into scope

    //Optional: Only needed if you need to spawn "rust" threads that need to interact with the JVM.
    jni_simple::init_dynamic_link(JNI_CreateJavaVM as *mut c_void, JNI_GetCreatedJavaVMs as *mut c_void);

    //All error codes are jint, never JNI_OK. See JNI documentation for their meaning when you handle them.
    //This is a Result<JNIEnv, jint>.
    let env : JNIEnv = vm.GetEnv(JNI_VERSION_1_8).unwrap();


    //This code does not check for failure or exceptions checks or "checks" for success in general.
    let sys = env.FindClass_str("java/lang/System");
    let nano_time = env.GetStaticMethodID_str(sys, "nanoTime", "()J");
    let nanos = env.CallStaticLongMethodA(sys, nano_time, null());
    println!("RUST: JNI_OnLoad {}", nanos);
    stdout().flush().unwrap();

    return JNI_VERSION_1_8;
}




#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1function_1new(
  env: JNIEnv, _this: jobject, name: jstring, inputs: jintArray, n_inputs: jint, 
  outputs: jintArray, n_outputs: jint, callback: jobject, user_data_ptr: jobject, _free_user_data: jlong) -> jlong {


    let inputs_arr = env.GetIntArrayElements(inputs, null_mut());
    let outputs_arr = env.GetIntArrayElements(outputs, null_mut());

    let input_slice = slice::from_raw_parts(inputs_arr, n_inputs as usize);
    let output_slice = slice::from_raw_parts(outputs_arr, n_outputs as usize);

    let mut ins = vec![ValType::I32; n_inputs as usize];
    let mut outs = vec![ValType::I32; n_outputs as usize];
    let mut i = 0;
    for ele in input_slice {
        ins[i] = conv(*ele);
        i+=1;
    }

    i=0;
    for ele in output_slice {
      outs[i] = conv(*ele); 
      i+=1;
    }

    let clazz = env.FindClass_str("org/extism/sdk/LibExtism0$InternalExtismFunction");
    let method_id = env.GetMethodID_str(clazz, "invoke", "(J[JI[JIJ)V");
    // env.CallVoidMethodA(callback, method_id, [].as_ptr());

    // let f = Function::new(
    //     CStr::from_ptr(env.GetStringUTFChars(name, null_mut())).to_str().unwrap(), 
    //     ins, outs, UserData::new(user_data_ptr), |p, ins, outs, data| { Ok(()) });


        
    // Box::into_raw(Box::new(ExtismFunction(std::cell::Cell::new(Some(f)))))



    extism_function_new(
      env.GetStringUTFChars(name, null_mut()), 
      ins.as_mut_ptr(), n_inputs as u64, 
      outs.as_mut_ptr(), n_outputs as u64, 
      nop, user_data_ptr as *mut std::ffi::c_void, Option::None) as jlong
}


fn conv(i:i32) -> ValType {
  match i  {
      0 => ValType::I32,
      1 => ValType::I64,
      2 => ValType::F32,
      3 => ValType::F64,
      4 => ValType::V128,
      5 => ValType::FuncRef,
      6 => ValType::ExternRef,
      _ => panic!("Unknown value")
  }
}

extern "C"  fn nop(
  plugin: *mut CurrentPlugin,
  inputs: *const ExtismVal,
  n_inputs: Size,
  outputs: *mut ExtismVal,
  n_outputs: Size,
  data: *mut std::ffi::c_void,
)  {
    unsafe {
    use std::io::Write; // <--- bring the trait into scope


    // let mut buf : [*mut c_void; 64] = [null_mut(); 64];
    // let mut count : jint = 0;
    // let res = JNI_GetCreatedJavaVMs(buf.as_mut_ptr() as *mut c_void, 64, &mut count);
    // if res != JNI_OK {
    //     panic!("NOOO")
    // }

    if is_jvm_loaded() {
        panic!("OK")
    }
    


    return;
    }
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_function_free
 * Signature: (J)V
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1function_1free(
    _env: JNIEnv,
    _this: jobject,
    func_ptr: jlong,
) {
  extism_function_free(func_ptr as *mut ExtismFunction);
}

#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1current_1plugin_1memory_1length(
    _env: JNIEnv,
    _this: jobject,
    plugin_ptr: jlong,
    n: jlong,
) -> jint {
    return extism_current_plugin_memory_length(plugin_ptr as *mut CurrentPlugin, n as ExtismMemoryHandle) as i32;
}

#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1current_1plugin_1memory(
    _env: JNIEnv,
    _this: jobject,
    plugin_ptr: jlong,
) -> jlong {
    return extism_current_plugin_memory(plugin_ptr as *mut CurrentPlugin) as i64
}

#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1current_1plugin_1memory_1alloc(
    _env: JNIEnv,
    _this: jobject,
    plugin_ptr: jlong,
    n: jlong,
) -> jlong {
    return extism_current_plugin_memory_alloc(plugin_ptr as *mut CurrentPlugin, n as sdk::Size) as i64;
}
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1current_1plugin_1memory_1free(
    _env: JNIEnv,
    _this: jobject,
    plugin_ptr: jlong,
    ptr: jlong) {
  extism_current_plugin_memory_free(plugin_ptr as *mut CurrentPlugin, ptr as ExtismMemoryHandle);
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_log_file
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1log_1file(
    _env: JNIEnv,
    _this: jobject,
    _path: jstring,
    _log_level: jstring,
) -> jboolean {
    return true;
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_error
 * Signature: (J)Ljava/lang/String;
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1error(
    env: JNIEnv,
    _this: jobject,
    plugin_ptr: jlong,
) -> jstring {
    let chars = extism_plugin_error(plugin_ptr as *mut Plugin);
    env.NewStringUTF(chars)
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_new
 * Signature: ([BJ[JIZ[J)J
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1new(
    env: JNIEnv,
    _this: jobject,
    wasm: jbyteArray,
    wasm_size: jlong,
    function_ptrs: jlongArray,
    n_funcs: jint,
    wasi: jboolean,
    errmsg: jlongArray,
) -> jlong {
    return extism_plugin_new(
      env.GetByteArrayElements(wasm, null_mut()) as *const u8, wasm_size as u64, 
      env.GetLongArrayElements(function_ptrs, null_mut()) as *mut *const ExtismFunction, n_funcs as u64, 
      wasi, errmsg as *mut*mut i8) as jlong
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_new_with_fuel_limit
 * Signature: ([BJ[JIZJ[J)J
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1new_1with_1fuel_1limit(
    env: JNIEnv,
    _this: jobject,
    wasm: jbyteArray,
    wasm_size: jlong,
    function_ptrs: jlongArray,
    n_funcs: jint,
    wasi: jboolean,
    fuel: jlong,
    errmsg: jlongArray,
) -> jlong {
  return extism_plugin_new_with_fuel_limit(
    env.GetByteArrayElements(wasm, null_mut()) as *const u8, wasm_size as u64, 
    function_ptrs as *mut *const ExtismFunction, n_funcs as u64, 
    wasi,
    fuel as u64,
    errmsg as *mut*mut i8) as jlong
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_new_error_free
 * Signature: (J)V
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1new_1error_1free(
    _env: JNIEnv,
    _this: jobject,
    _errmsg: jlong,
) {
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_version
 * Signature: ()Ljava/lang/String;
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1version(
    _env: JNIEnv,
    _this: jobject,
) -> jstring {
    return null_mut();
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_call
 * Signature: (Lcom/sun/jna/Pointer;Ljava/lang/String;[BI)I
 */

#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1call(
    env: JNIEnv,
    _this: jobject,
    plugin_ptr: jlong,
    function_name: jstring,
    data: jbyteArray,
    data_len: jint,
) -> jint {

  let chars = env.GetStringUTFChars(function_name, null_mut());
  let fname = CStr::from_ptr(chars);
  println!("{:?}", fname.to_str());

    return extism_plugin_call(
      plugin_ptr as *mut Plugin, 
      chars, 
      env.GetByteArrayElements(data, null_mut()) as *const u8, 
      data_len as u64);
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_output_length
 * Signature: (J)I
 */

#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1output_1length(
    _env: JNIEnv,
    _this: jobject,
    plugin_ptr: jlong,
) -> jint {
    return extism_plugin_output_length(plugin_ptr as *mut Plugin) as i32;
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_output_data
 * Signature: (J)J
 */

#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1output_1data(
    env: JNIEnv,
    _this: jobject,
    plugin_ptr: jlong,
) -> jbyteArray {
    let p = plugin_ptr as *mut Plugin;
    let res = extism_plugin_output_data(p);
    let len = extism_plugin_output_length(p);
    if len == 0 {
      return null_mut();
    };
    let arr = env.NewByteArray(len as jsize);
    env.SetByteArrayRegion(arr, 0, len as jsize, res as *const i8);

    return arr;
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_free
 * Signature: (J)V
 */

#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1free(
    _env: JNIEnv,
    _this: jobject,
    _plugin_ptr: jlong,
) -> jlong {
    return 0;
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_config
 * Signature: (J[BI)Z
 */

#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1config(
    _env: JNIEnv,
    _this: jobject,
    _plugin_ptr: jlong,
    _json: jbyteArray,
    _json_len: jint,
) -> jboolean {
    return true;
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_cancel_handle
 * Signature: (J)J
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1cancel_1handle(
    _env: JNIEnv,
    _this: jobject,
    _plugin_ptr: jlong,
) -> jlong {
    return 0;
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_plugin_cancel
 * Signature: (J)Z
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1plugin_1cancel(
    _env: JNIEnv,
    _this: jobject,
    _cancel_handle: jlong,
) -> jboolean {
    return true;
}

/*
 * Class:     org_extism_sdk_LibExtism0
 * Method:    extism_function_set_namespace
 * Signature: (JLjava/lang/String;)V
 */
#[no_mangle]
pub unsafe extern "system" fn Java_org_extism_sdk_LibExtism0_extism_1function_1set_1namespace(
    _env: JNIEnv,
    _this: jobject,
    _p: jlong,
    _name: jstring,
) {
}
