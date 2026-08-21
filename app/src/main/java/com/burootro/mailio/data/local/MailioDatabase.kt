package com.burootro.mailio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.burootro.mailio.data.local.dao.AddressDao
import com.burootro.mailio.data.local.dao.AttachmentDao
import com.burootro.mailio.data.local.dao.MessageDao
import com.burootro.mailio.data.local.entity.AddressEntity
import com.burootro.mailio.data.local.entity.AttachmentEntity
import com.burootro.mailio.data.local.entity.MessageEntity

@Database(
    entities = [
        AddressEntity::class,
        MessageEntity::class,
        AttachmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MailioDatabase : RoomDatabase() {

    abstract fun addressDao(): AddressDao
    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        const val DATABASE_NAME = "mailio.db"
    }
}
