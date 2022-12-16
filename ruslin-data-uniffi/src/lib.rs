use ruslin_data::{
    sync::{SyncConfig, SyncError},
    DatabaseError, RuslinData, UpdateSource,
};
use std::path::Path;
use tokio::runtime::Runtime;

mod ffi;
use ffi::{FFIFolder, FFINote};

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
    data: RuslinData,
    rt: Runtime,
}

impl RuslinAndroidData {
    pub fn new(data_dir: String) -> Result<Self, SyncError> {
        init_log();
        let rt = tokio::runtime::Builder::new_multi_thread()
            .worker_threads(2)
            .enable_all()
            .build()
            .unwrap();
        Ok(Self {
            data: RuslinData::new(Path::new(&data_dir))?,
            rt,
        })
    }

    // pub fn simple_log(&self, message: String) {
    //     self.rt.spawn(async {
    //         std::thread::sleep(std::time::Duration::from_millis(10000));
    //         log::debug!("In Async {:?}", std::thread::current().id());
    //     });
    //     self.rt.spawn(async {
    //         log::debug!("In Async {:?}", std::thread::current().id());
    //     });
    //     log::debug!("Spawn done");
    // }

    pub fn sync_config_exists(&self) -> bool {
        self.data.sync_exists()
    }

    pub fn save_sync_config(&self, config: SyncConfig) -> Result<(), SyncError> {
        // self.rt.spawn_blocking(func)
        self.rt.block_on(self.data.save_sync_config(config))
    }

    pub fn sync(&self) -> Result<(), SyncError> {
        self.rt.block_on(self.data.sync())
    }

    pub fn replace_folder(&self, folder: FFIFolder) -> Result<(), DatabaseError> {
        self.data
            .db
            .replace_folder(&folder.into(), ruslin_data::UpdateSource::LocalEdit)
    }

    pub fn load_folders(&self) -> Result<Vec<FFIFolder>, DatabaseError> {
        let folders = self.data.db.load_folders()?;
        let folders = folders
            .into_iter()
            .map(|x| x.into())
            .collect::<Vec<FFIFolder>>();
        Ok(folders)
    }

    pub fn delete_folder(&self, id: String) -> Result<(), DatabaseError> {
        self.data.db.delete_folder(&id, UpdateSource::LocalEdit)
    }
}
