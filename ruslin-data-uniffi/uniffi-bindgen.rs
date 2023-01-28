fn main() {
    #[cfg(not(target_os = "android"))]
    uniffi::uniffi_bindgen_main()
}
