use android_logger::AndroidLogger;
use log::{LevelFilter, Log};
use log4rs::{
    append::{file::FileAppender, Append},
    config::{Appender, Root},
    encode::pattern::PatternEncoder,
    Config,
};
use ruslin_data::{
    sync::{SyncConfig, SyncError},
    DatabaseError, Folder, Note, Resource, RuslinData, SearchBodyOption, UpdateSource,
};
use std::path::Path;
use tokio::runtime::Runtime;
mod ffi;
mod html;
use ffi::{FFIAbbrNote, FFIFolder, FFINote, FFIResource, FFISearchNote, FFIStatus, FFISyncInfo};

uniffi_macros::include_scaffolding!("ruslin");

pub use html::parse_markdown_to_html;

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
    #[cfg(not(debug_assertions))]
    let level_filter = LevelFilter::Info;

    let android_appender = AndroidAppender(AndroidLogger::new(
        android_logger::Config::default()
            .with_max_level(level_filter)
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
    pub fn new(
        data_dir: String,
        resource_dir: String,
        log_text_file: String,
    ) -> Result<Self, SyncError> {
        let log_handle = init_log(&log_text_file);
        let rt = tokio::runtime::Builder::new_multi_thread()
            .enable_all()
            .build()
            .unwrap_or_else(|_| panic!("unwrap error in {}:{}", file!(), line!()));
        let data = RuslinData::new(Path::new(&data_dir), Path::new(&resource_dir))?;
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
        self.data.db.status()
    }

    pub fn search(&self, search_term: String) -> Result<Vec<FFISearchNote>, DatabaseError> {
        self.data.db.search_notes(
            &search_term,
            Some(SearchBodyOption::Snippet { max_tokens: 16 }),
        )
    }

    pub fn create_resource(
        &self,
        title: String,
        mime: String,
        file_extension: String,
        size: i32,
    ) -> FFIResource {
        Resource::new(title, mime, file_extension, size).into()
    }

    pub fn save_resource(&self, resource: FFIResource) -> Result<(), DatabaseError> {
        self.data
            .db
            .replace_resource(&resource.into(), UpdateSource::LocalEdit)
    }

    pub fn load_resource(&self, id: String) -> Result<FFIResource, DatabaseError> {
        let resource = self.data.db.load_resource(&id)?;
        Ok(resource.into())
    }
}

#[derive(Debug)]
pub enum MarkdownTagRange {
    Heading {
        level: i32,
        start: i32,
        end: i32,
    },
    Emphasis {
        start: i32,
        end: i32,
    },
    Strong {
        start: i32,
        end: i32,
    },
    Strikethrough {
        start: i32,
        end: i32,
    },
    InlineCode {
        start: i32,
        end: i32,
    },
    MList {
        start: i32,
        end: i32,
        order: i32,
        nested_level: i32,
    },
    ListItem {
        start: i32,
        end: i32,
        nested_level: i32,
        ordered: bool,
    },
    Paragraph {
        start: i32,
        end: i32,
    },
    Link {
        start: i32,
        end: i32,
        url_offset: i32,
    },
    Image {
        start: i32,
        end: i32,
        url_offset: i32,
    },
    Rule {
        start: i32,
        end: i32,
    },
    BlockQuote {
        start: i32,
        end: i32,
    },
    TaskListMarker {
        start: i32,
        end: i32,
        is_checked: bool,
    },
    CodeBlock {
        start: i32,
        end: i32,
    },
}

pub fn parse_markdown(s: String) -> Vec<MarkdownTagRange> {
    use pulldown_cmark::{CodeBlockKind, Event, LinkType, Options, Parser, Tag};
    let mut tag_ranges: Vec<MarkdownTagRange> = Vec::new();
    let mut utf8_to_uft16_offsets: Vec<usize> = Vec::with_capacity(s.len());
    let mut offset: usize = 0;
    for c in s.chars() {
        for _ in 0..c.len_utf8() {
            utf8_to_uft16_offsets.push(offset);
        }
        offset += c.len_utf16();
    }
    utf8_to_uft16_offsets.push(offset);
    let parser = Parser::new_ext(&s, Options::all());
    let mut list_nested_level: i32 = 0;
    let mut is_ordered_list: bool = false;
    for (event, range) in parser.into_offset_iter() {
        let start = utf8_to_uft16_offsets[range.start] as i32;
        let end = utf8_to_uft16_offsets[range.end] as i32;
        match event {
            Event::Start(tag) => {
                let tag_range = match tag {
                    Tag::Heading(level, _, _) => MarkdownTagRange::Heading {
                        level: level as i32,
                        start,
                        end,
                    },
                    Tag::Emphasis => MarkdownTagRange::Emphasis { start, end },
                    Tag::Strong => MarkdownTagRange::Strong { start, end },
                    Tag::Strikethrough => MarkdownTagRange::Strikethrough { start, end },
                    Tag::List(order) => {
                        list_nested_level += 1;
                        is_ordered_list = order.is_some();
                        MarkdownTagRange::MList {
                            start,
                            end,
                            order: order.unwrap_or(0) as i32,
                            nested_level: list_nested_level,
                        }
                    }
                    Tag::Item => MarkdownTagRange::ListItem {
                        start,
                        end,
                        nested_level: list_nested_level,
                        ordered: is_ordered_list,
                    },
                    Tag::Paragraph => MarkdownTagRange::Paragraph { start, end },
                    Tag::Link(link_type, url, title) => {
                        if link_type == LinkType::Inline {
                            let url_offset = if title.is_empty() {
                                1 + url.len()
                            } else {
                                2 + url.len() + title.len()
                            };
                            MarkdownTagRange::Link {
                                start,
                                end,
                                url_offset: utf8_to_uft16_offsets[range.end - url_offset] as i32,
                            }
                        } else {
                            continue;
                        }
                    }
                    Tag::Image(link_type, url, title) => {
                        if link_type == LinkType::Inline {
                            let url_offset = if title.is_empty() {
                                1 + url.len()
                            } else {
                                2 + url.len() + title.len()
                            };
                            MarkdownTagRange::Image {
                                start,
                                end,
                                url_offset: utf8_to_uft16_offsets[range.end - url_offset] as i32,
                            }
                        } else {
                            continue;
                        }
                    }
                    Tag::BlockQuote => MarkdownTagRange::BlockQuote { start, end },
                    Tag::CodeBlock(CodeBlockKind::Fenced(_)) => {
                        MarkdownTagRange::CodeBlock { start, end }
                    }
                    _ => continue,
                };
                tag_ranges.push(tag_range);
            }
            Event::Code(_) => {
                tag_ranges.push(MarkdownTagRange::InlineCode { start, end });
            }
            Event::End(tag) => match tag {
                Tag::List(_) => {
                    list_nested_level -= 1;
                }
                _ => continue,
            },
            Event::Rule => {
                tag_ranges.push(MarkdownTagRange::Rule { start, end });
            }
            Event::TaskListMarker(is_checked) => {
                tag_ranges.push(MarkdownTagRange::TaskListMarker {
                    start,
                    end,
                    is_checked,
                });
            }
            Event::Text(_)
            | Event::Html(_)
            | Event::FootnoteReference(_)
            | Event::SoftBreak
            | Event::HardBreak => {}
        }
    }
    tag_ranges
}
