fn main() {
    uniffi_build::generate_scaffolding("./src/ruslin.udl").expect("generate_scaffolding error");
}
