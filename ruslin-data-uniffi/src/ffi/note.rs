use ruslin_data::{AbbrNote, DateTimeTimestamp, Note};

pub struct FFISearchNote {
    pub id: String,
    pub title: String,
    pub body: String,
    pub title_highlight_ranges: Vec<i32>,
    pub body_highlight_ranges: Vec<i32>,
}

pub struct FFIAbbrNote {
    pub id: String,
    pub parent_id: Option<String>,
    pub title: String,
    pub user_created_time: i64,
    pub user_updated_time: i64,
}

impl From<FFIAbbrNote> for AbbrNote {
    fn from(note: FFIAbbrNote) -> Self {
        Self {
            id: note.id,
            parent_id: note.parent_id,
            title: note.title,
            user_created_time: DateTimeTimestamp::from_timestamp_millis(note.user_created_time),
            user_updated_time: DateTimeTimestamp::from_timestamp_millis(note.user_updated_time),
        }
    }
}

impl From<AbbrNote> for FFIAbbrNote {
    fn from(note: AbbrNote) -> Self {
        Self {
            id: note.id,
            parent_id: note.parent_id,
            title: note.title,
            user_created_time: note.user_created_time.timestamp_millis(),
            user_updated_time: note.user_updated_time.timestamp_millis(),
        }
    }
}

pub struct FFINote {
    pub id: String,
    pub parent_id: Option<String>,
    pub title: String,
    pub body: String,
    pub created_time: i64,
    pub updated_time: i64,
    pub is_conflict: bool,
    pub latitude: f64,
    pub longitude: f64,
    pub altitude: f64,
    pub author: String,
    pub source_url: String,
    pub is_todo: bool,
    pub todo_due: bool,
    pub todo_completed: bool,
    pub source: String,
    pub source_application: String,
    pub application_data: String,
    pub order: i64,
    pub user_created_time: i64,
    pub user_updated_time: i64,
    pub encryption_cipher_text: String,
    pub encryption_applied: bool,
    pub markup_language: bool,
    pub is_shared: bool,
    pub share_id: String,
    pub conflict_original_id: Option<String>,
    pub master_key_id: String,
}

impl From<Note> for FFINote {
    fn from(note: Note) -> Self {
        Self {
            id: note.id,
            parent_id: note.parent_id,
            title: note.title,
            body: note.body,
            created_time: note.created_time.timestamp_millis(),
            updated_time: note.updated_time.timestamp_millis(),
            is_conflict: note.is_conflict,
            latitude: note.latitude,
            longitude: note.longitude,
            altitude: note.altitude,
            author: note.author,
            source_url: note.source_url,
            is_todo: note.is_todo,
            todo_due: note.todo_due,
            todo_completed: note.todo_completed,
            source: note.source,
            source_application: note.source_application,
            application_data: note.application_data,
            order: note.order,
            user_created_time: note.user_created_time.timestamp_millis(),
            user_updated_time: note.user_updated_time.timestamp_millis(),
            encryption_cipher_text: note.encryption_cipher_text,
            encryption_applied: note.encryption_applied,
            markup_language: note.markup_language,
            is_shared: note.is_shared,
            share_id: note.share_id,
            conflict_original_id: note.conflict_original_id,
            master_key_id: note.master_key_id,
        }
    }
}

impl From<FFINote> for Note {
    fn from(note: FFINote) -> Self {
        Self {
            id: note.id,
            parent_id: note.parent_id,
            title: note.title,
            body: note.body,
            created_time: DateTimeTimestamp::from_timestamp_millis(note.created_time),
            updated_time: DateTimeTimestamp::from_timestamp_millis(note.updated_time),
            is_conflict: note.is_conflict,
            latitude: note.latitude,
            longitude: note.longitude,
            altitude: note.altitude,
            author: note.author,
            source_url: note.source_url,
            is_todo: note.is_todo,
            todo_due: note.todo_due,
            todo_completed: note.todo_completed,
            source: note.source,
            source_application: note.source_application,
            application_data: note.application_data,
            order: note.order,
            user_created_time: DateTimeTimestamp::from_timestamp_millis(note.user_created_time),
            user_updated_time: DateTimeTimestamp::from_timestamp_millis(note.user_updated_time),
            encryption_cipher_text: note.encryption_cipher_text,
            encryption_applied: note.encryption_applied,
            markup_language: note.markup_language,
            is_shared: note.is_shared,
            share_id: note.share_id,
            conflict_original_id: note.conflict_original_id,
            master_key_id: note.master_key_id,
        }
    }
}
