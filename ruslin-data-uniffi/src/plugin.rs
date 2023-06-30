use thiserror::Error;

#[derive(Error, Debug)]
pub enum PluginError {
    #[error("The plugin directory does not exist.")]
    PluginDirNotExists,
    #[error("Download failed.")]
    DownloadFailed,
    #[error("Delete Failed")]
    DeleteFailed,
    #[error("Start Failed")]
    StartFailed,
}
