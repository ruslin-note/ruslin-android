use ruslin_data::{DateTimeTimestamp, Resource};

pub struct FFIResource {
    pub id: String,
    pub title: String,
    pub mime: String,
    pub filename: String,
    pub created_time: i64,
    pub updated_time: i64,
    pub user_created_time: i64,
    pub user_updated_time: i64,
    pub file_extension: String,
    pub encryption_cipher_text: String,
    pub encryption_applied: bool,
    pub encryption_blob_encrypted: bool,
    pub size: i32,
    pub is_shared: bool,
    pub share_id: String,
    pub master_key_id: String,
}

impl From<FFIResource> for Resource {
    fn from(value: FFIResource) -> Self {
        Self {
            id: value.id,
            title: value.title,
            mime: value.mime,
            filename: value.filename,
            created_time: DateTimeTimestamp::from_timestamp_millis(value.created_time),
            updated_time: DateTimeTimestamp::from_timestamp_millis(value.updated_time),
            user_created_time: DateTimeTimestamp::from_timestamp_millis(value.user_created_time),
            user_updated_time: DateTimeTimestamp::from_timestamp_millis(value.user_updated_time),
            file_extension: value.file_extension,
            encryption_cipher_text: value.encryption_cipher_text,
            encryption_applied: value.encryption_applied,
            encryption_blob_encrypted: value.encryption_blob_encrypted,
            size: value.size,
            is_shared: value.is_shared,
            share_id: value.share_id,
            master_key_id: value.master_key_id,
        }
    }
}

impl From<Resource> for FFIResource {
    fn from(value: Resource) -> Self {
        Self {
            id: value.id,
            title: value.title,
            mime: value.mime,
            filename: value.filename,
            created_time: value.created_time.timestamp_millis(),
            updated_time: value.updated_time.timestamp_millis(),
            user_created_time: value.user_created_time.timestamp_millis(),
            user_updated_time: value.user_updated_time.timestamp_millis(),
            file_extension: value.file_extension,
            encryption_cipher_text: value.encryption_cipher_text,
            encryption_applied: value.encryption_applied,
            encryption_blob_encrypted: value.encryption_blob_encrypted,
            size: value.size,
            is_shared: value.is_shared,
            share_id: value.share_id,
            master_key_id: value.master_key_id,
        }
    }
}
