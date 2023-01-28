use std::{env, path::Path, process::Command};

fn main() {
    let target = env::var("TARGET").unwrap();
    if target == "x86_64-linux-android" || target == "i686-linux-android" {
        let cc = env::var_os("CC").unwrap();
        let cc = Path::new(&cc);
        let output = Command::new(cc)
            .arg("-print-libgcc-file-name")
            .output()
            .unwrap();
        let rtlib_path = String::from_utf8(output.stdout).unwrap();
        println!("cargo:rustc-link-arg={}", rtlib_path.trim()); // https://github.com/termux/termux-packages/issues/8029#issuecomment-1369150244
    }
    uniffi::generate_scaffolding("./src/ruslin.udl").expect("generate_scaffolding error");
}
