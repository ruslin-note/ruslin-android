use camino::Utf8Path;
use std::{env, path::Path, process::Command};
use uniffi::KotlinBindingGenerator;

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
    let udl_file = "./src/ruslin.udl";
    uniffi::generate_scaffolding(udl_file).expect("generate_scaffolding error");
    generate_kotlin_bindings(udl_file);
}

pub fn generate_kotlin_bindings(udl_file: impl AsRef<Utf8Path>) {
    let udl_file = udl_file.as_ref();
    println!("cargo:rerun-if-changed={udl_file}");
    uniffi::generate_bindings(
        udl_file,
        None,
        KotlinBindingGenerator,
        Some("../uniffi/src/main/java".as_ref()),
        None,
        None,
        true,
    )
    .unwrap();
}
