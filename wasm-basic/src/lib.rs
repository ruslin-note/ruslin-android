use wapc_guest as wapc;

#[no_mangle]
pub fn wapc_init() {
  wapc::register_function("ping", ping);
}

fn ping(msg: &[u8]) -> wapc::CallResult {
  wapc::console_log(&format!(
    "IN_WASM: Received request for `ping` operation with payload : {}",
    std::str::from_utf8(msg).unwrap()
  ));
  Ok(msg.to_vec())
}
