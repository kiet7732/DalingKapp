package data.chat

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class CachedMessage(
    @PrimaryKey
    val messageId: String,
    val matchId: String,
    val senderId: String,
    val text: String, // Chứa văn bản, URL ảnh, hoặc URL âm thanh
    val timestamp: Long,
    val isSynced: Boolean = false,
    val ownerId: String,
    val isNotified: Boolean = false,
    val messageType: String = "text" // "text", "image", hoặc "audio"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        messageId = parcel.readString() ?: "",
        matchId = parcel.readString() ?: "",
        senderId = parcel.readString() ?: "",
        text = parcel.readString() ?: "",
        timestamp = parcel.readLong(),
        isSynced = parcel.readByte() != 0.toByte(),
        ownerId = parcel.readString() ?: "",
        isNotified = parcel.readByte() != 0.toByte(),
        messageType = parcel.readString() ?: "text"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(messageId)
        parcel.writeString(matchId)
        parcel.writeString(senderId)
        parcel.writeString(text)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (isSynced) 1 else 0)
        parcel.writeString(ownerId)
        parcel.writeByte(if (isNotified) 1 else 0)
        parcel.writeString(messageType)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CachedMessage> {
        override fun createFromParcel(parcel: Parcel): CachedMessage = CachedMessage(parcel)
        override fun newArray(size: Int): Array<CachedMessage?> = arrayOfNulls(size)
    }
}