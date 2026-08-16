package com.burootro.mailio.di

import android.content.Context
import androidx.room.Room
import com.burootro.mailio.data.local.MailioDatabase
import com.burootro.mailio.data.local.dao.AddressDao
import com.burootro.mailio.data.local.dao.AttachmentDao
import com.burootro.mailio.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MailioDatabase {
        return Room.databaseBuilder(
            context,
            MailioDatabase::class.java,
            MailioDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAddressDao(db: MailioDatabase): AddressDao = db.addressDao()

    @Provides
    @Singleton
    fun provideMessageDao(db: MailioDatabase): MessageDao = db.messageDao()

    @Provides
    @Singleton
    fun provideAttachmentDao(db: MailioDatabase): AttachmentDao = db.attachmentDao()
}
