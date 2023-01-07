use android_logger::AndroidLogger;
use log::{Level, LevelFilter, Log};
use log4rs::{
    append::{file::FileAppender, Append},
    config::{Appender, Root},
    encode::pattern::PatternEncoder,
    Config,
};
use ruslin_data::{
    sync::{SyncConfig, SyncError},
    DatabaseError, Folder, Note, RuslinData, SearchBodyOption, UpdateSource,
};
use std::path::Path;
use tokio::runtime::Runtime;

mod ffi;
use ffi::{FFIAbbrNote, FFIFolder, FFINote, FFISearchNote, FFIStatus, FFISyncInfo};

uniffi_macros::include_scaffolding!("ruslin");

struct AndroidAppender(AndroidLogger);

impl std::fmt::Debug for AndroidAppender {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_tuple("AndroidAppender")
            .field(&"android_logger")
            .finish()
    }
}

impl Append for AndroidAppender {
    fn append(&self, record: &log::Record) -> uniffi::deps::anyhow::Result<()> {
        self.0.log(record);
        Ok(())
    }

    fn flush(&self) {
        self.0.flush()
    }
}

fn init_log(log_text_file: &str) -> log4rs::Handle {
    #[cfg(debug_assertions)]
    let level_filter = LevelFilter::Debug;
    #[cfg(debug_assertions)]
    let level = Level::Debug;
    #[cfg(not(debug_assertions))]
    let level_filter = LevelFilter::Info;
    #[cfg(not(debug_assertions))]
    let level = Level::Info;

    let android_appender = AndroidAppender(AndroidLogger::new(
        android_logger::Config::default()
            .with_min_level(level)
            .with_tag("RuslinRust"),
    ));

    let log_file = FileAppender::builder()
        .encoder(Box::new(PatternEncoder::new(
            "{d(%m-%d %H:%M:%S %Z)(utc)} [{l}]: {m}\n",
        )))
        .build(Path::new(log_text_file))
        .unwrap();
    let config = Config::builder()
        .appender(Appender::builder().build("log_file", Box::new(log_file)))
        .appender(Appender::builder().build("log_android", Box::new(android_appender)))
        .build(
            Root::builder()
                .appender("log_file")
                .appender("log_android")
                .build(level_filter),
        )
        .unwrap();
    log4rs::init_config(config).unwrap()
}

pub struct RuslinAndroidData {
    data: RuslinData,
    rt: Runtime,
    _log_handle: log4rs::Handle,
}

impl RuslinAndroidData {
    pub fn new(data_dir: String, log_text_file: String) -> Result<Self, SyncError> {
        let log_handle = init_log(&log_text_file);
        let rt = tokio::runtime::Builder::new_multi_thread()
            .enable_all()
            .build()
            .expect(&format!("unwrap error in {}:{}", file!(), line!()));
        let data = RuslinData::new(Path::new(&data_dir))?;
        let db = data.db.clone();
        rt.spawn(async move {
            // prepare jieba
            db.search_notes("", None).ok();
        });
        Ok(Self {
            data,
            rt,
            _log_handle: log_handle,
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
        let result = self.rt.block_on(self.data.save_sync_config(config));
        if let Err(e) = &result {
            log::error!("save sync config error: {e}");
        }
        result
    }

    pub fn get_sync_config(&self) -> Result<Option<SyncConfig>, SyncError> {
        self.data.get_sync_config()
    }

    pub fn sync(&self) -> Result<FFISyncInfo, SyncError> {
        let result = self.rt.block_on(self.data.sync());
        if let Err(e) = &result {
            log::error!("sync error: {e}");
        }
        result
    }

    pub fn new_folder(&self, parent_id: Option<String>, title: String) -> FFIFolder {
        Folder::new(title, parent_id).into()
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

    pub fn load_abbr_notes(
        &self,
        parent_id: Option<String>,
    ) -> Result<Vec<FFIAbbrNote>, DatabaseError> {
        let notes = self.data.db.load_abbr_notes(parent_id.as_deref())?;
        let notes = notes
            .into_iter()
            .map(|x| x.into())
            .collect::<Vec<FFIAbbrNote>>();
        Ok(notes)
    }

    pub fn new_note(&self, parent_id: Option<String>, title: String, body: String) -> FFINote {
        Note::new(parent_id, title, body).into()
    }

    pub fn load_note(&self, id: String) -> Result<FFINote, DatabaseError> {
        Ok(self.data.db.load_note(&id)?.into())
    }

    pub fn replace_note(&self, note: FFINote) -> Result<(), DatabaseError> {
        self.data
            .db
            .replace_note(&note.into(), UpdateSource::LocalEdit)
    }

    pub fn delete_note(&self, id: String) -> Result<(), DatabaseError> {
        self.data.db.delete_note(&id, UpdateSource::LocalEdit)
    }

    pub fn conflict_note_exists(&self) -> Result<bool, DatabaseError> {
        self.data.db.conflict_note_exists()
    }

    pub fn load_abbr_conflict_notes(&self) -> Result<Vec<FFIAbbrNote>, DatabaseError> {
        let notes = self.data.db.load_abbr_conflict_notes()?;
        let notes = notes
            .into_iter()
            .map(|x| x.into())
            .collect::<Vec<FFIAbbrNote>>();
        Ok(notes)
    }

    pub fn database_status(&self) -> Result<FFIStatus, DatabaseError> {
        Ok(self.data.db.status()?)
    }

    pub fn search(&self, search_term: String) -> Result<Vec<FFISearchNote>, DatabaseError> {
        Ok(self.data.db.search_notes(
            &search_term,
            Some(SearchBodyOption::Snippet { max_tokens: 16 }),
        )?)
    }
}
