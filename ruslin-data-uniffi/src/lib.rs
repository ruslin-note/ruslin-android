use tokio::runtime::Runtime;

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
    rt: Runtime,
}

impl RuslinAndroidData {
    pub fn new(data_dir: String) -> Self {
        init_log();
        let rt = tokio::runtime::Builder::new_multi_thread()
            .worker_threads(2)
            .enable_all()
            .build()
            .unwrap();
        Self { data_dir, rt }
    }

    pub fn simple_log(&self, message: String) {
        log::debug!("{} {}", message, self.data_dir);

        self.rt.spawn(async {
            std::thread::sleep(std::time::Duration::from_millis(10000));
            log::debug!("In Async {:?}", std::thread::current().id());
        });

        self.rt.spawn(async {
            log::debug!("In Async {:?}", std::thread::current().id());
        });

        self.rt.spawn(async {
            log::debug!("In Async {:?}", std::thread::current().id());
        });

        log::debug!("Spawn done");
    }
}
