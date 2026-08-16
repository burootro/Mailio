package com.burootro.mailio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.burootro.mailio.data.local.entity.AddressEntity
import com.burootro.mailio.data.local.entity.AttachmentEntity
import com.burootro.mailio.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {

    @Query("SELECT * FROM addresses ORDER BY isPinned DESC, lastActivityAt DESC")
    fun observeAll(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE isActive = 1 ORDER BY isPinned DESC, lastActivityAt DESC")
    fun observeActive(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getById(id: String): AddressEntity?

    @Query("SELECT * FROM addresses WHERE email = :email")
    suspend fun getByEmail(email: String): AddressEntity?

    @Query("SELECT * FROM addresses")
    suspend fun getAllOnce(): List<AddressEntity>

    @Query("SELECT COUNT(*) FROM addresses")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(address: AddressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(addresses: List<AddressEntity>)

    @Update
    suspend fun update(address: AddressEntity)

    @Query("UPDATE addresses SET label = :label WHERE id = :id")
    suspend fun rename(id: String, label: String?)

    @Query("UPDATE addresses SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE addresses SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: String, active: Boolean)

    @Query("UPDATE addresses SET unreadCount = :count WHERE id = :id")
    suspend fun setUnreadCount(id: String, count: Int)

    @Query("UPDATE addresses SET lastActivityAt = :time WHERE id = :id")
    suspend fun touch(id: String, time: Long)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM addresses")
    suspend fun deleteAll()
}

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE addressId = :addressId ORDER BY receivedAt DESC")
    fun observeByAddress(addressId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY receivedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isStarred = 1 ORDER BY receivedAt DESC")
    fun observeStarred(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :id")
    fun observeById(id: String): Flow<MessageEntity?>

    @Query("SELECT COUNT(*) FROM messages WHERE addressId = :addressId AND isRead = 0")
    suspend fun countUnread(addressId: String): Int

    @Query("SELECT COUNT(*) FROM messages WHERE isRead = 0")
    fun observeTotalUnread(): Flow<Int>

    @Query(
        """
        SELECT * FROM messages
        WHERE subject LIKE '%' || :query || '%'
           OR fromEmail LIKE '%' || :query || '%'
           OR preview LIKE '%' || :query || '%'
        ORDER BY receivedAt DESC
        """
    )
    fun search(query: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("UPDATE messages SET isRead = :read WHERE id = :id")
    suspend fun setRead(id: String, read: Boolean)

    @Query("UPDATE messages SET isRead = 1 WHERE addressId = :addressId")
    suspend fun markAllReadForAddress(addressId: String)

    @Query("UPDATE messages SET isStarred = :starred WHERE id = :id")
    suspend fun setStarred(id: String, starred: Boolean)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM messages WHERE addressId = :addressId")
    suspend fun deleteByAddress(addressId: String)

    @Query("DELETE FROM messages WHERE receivedAt < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE messageId = :messageId")
    fun observeByMessage(messageId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE messageId = :messageId")
    suspend fun getByMessage(messageId: String): List<AttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attachments: List<AttachmentEntity>)

    @Query("UPDATE attachments SET localPath = :path WHERE id = :id")
    suspend fun setLocalPath(id: String, path: String)

    @Query("DELETE FROM attachments WHERE messageId = :messageId")
    suspend fun deleteByMessage(messageId: String)
}
