use ruslin_data::{DateTimeTimestamp, Folder};

pub struct FFIFolder {
    pub id: String,
    pub title: String,
    pub created_time: i64,
    pub updated_time: i64,
    pub user_created_time: i64,
    pub user_updated_time: i64,
    pub encryption_cipher_text: String,
    pub encryption_applied: bool,
    pub parent_id: Option<String>,
    pub is_shared: bool,
    pub share_id: String,
    pub master_key_id: String,
    pub icon: String,
}

impl From<Folder> for FFIFolder {
    fn from(folder: Folder) -> Self {
        Self {
            id: folder.id,
            title: folder.title,
            created_time: folder.created_time.timestamp_millis(),
            updated_time: folder.updated_time.timestamp_millis(),
            user_created_time: folder.user_created_time.timestamp_millis(),
            user_updated_time: folder.user_updated_time.timestamp_millis(),
            encryption_cipher_text: folder.encryption_cipher_text,
            encryption_applied: folder.encryption_applied,
            parent_id: folder.parent_id,
            is_shared: folder.is_shared,
            share_id: folder.share_id,
            master_key_id: folder.master_key_id,
            icon: folder.icon,
        }
    }
}

impl From<FFIFolder> for Folder {
    fn from(folder: FFIFolder) -> Self {
        Self {
            id: folder.id,
            title: folder.title,
            created_time: DateTimeTimestamp::from_timestamp_millis(folder.created_time),
            updated_time: DateTimeTimestamp::from_timestamp_millis(folder.updated_time),
            user_created_time: DateTimeTimestamp::from_timestamp_millis(folder.user_created_time),
            user_updated_time: DateTimeTimestamp::from_timestamp_millis(folder.user_updated_time),
            encryption_cipher_text: folder.encryption_cipher_text,
            encryption_applied: folder.encryption_applied,
            parent_id: folder.parent_id,
            is_shared: folder.is_shared,
            share_id: folder.share_id,
            master_key_id: folder.master_key_id,
            icon: folder.icon,
        }
    }
}
