uniffi_macros::include_scaffolding!("ruslin");

fn init_log() {
    use android_logger::Config;
    use log::Level;
    android_logger::init_once(
        Config::default()
            .with_min_level(Level::Trace)
            .with_tag("RuslinRust"),
    );
    log::debug!("ruslin-data loaded");
}

pub struct RuslinAndroidData {
    data_dir: String,
}

impl RuslinAndroidData {
    pub fn new(data_dir: String) -> Self {
        init_log();
        Self { data_dir }
    }

    pub fn simple_log(&self, message: String) {
        log::debug!("{} {}", message, self.data_dir);
    }
}
