package com.example.backupflow.domain.model

enum class BackupType(val defaultPort: Int) {
    FTP(21),
    SFTP(22),
    WEBDAV(443)
}

enum class BackupStatus {
    IDLE,
    PREPARING,
    SCANNING,
    UPLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class BackupDeletionPolicy {
    APPEND_ONLY, // Default for new tasks
    MIRROR,
    ARCHIVE
}

enum class BackupChecksumMode {
    OFF,           // Default
    VERIFY_UPLOADS // protocol-specific verification + manifest upload
}

enum class BackupFailureCategory {
    NETWORK,
    AUTH,
    PERMISSION,
    SERVER,
    QUOTA,
    CLOUD_ONLY,
    CERTIFICATE,
    CANCELLED,
    UNKNOWN
}

enum class MediaType {
    PHOTOS_ONLY,
    VIDEOS_ONLY,
    PHOTOS_AND_VIDEOS
}

enum class FrequencyType {
    MANUAL,
    DAILY,
    WEEKLY,
    MONTHLY,
    INTERVAL_HOURS
}
