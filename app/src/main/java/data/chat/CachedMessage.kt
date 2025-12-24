package data.chat

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class CachedMessage(
    @PrimaryKey
    val messageId: String,
    val matchId: String,
    val senderId: String,
    val text: String, // Contains text, image URL, audio URL, or video URL
    val timestamp: Long,
    val isSynced: Boolean = false,
    val ownerId: String,
    val isNotified: Boolean = false,
    val messageType: String = "text", // "text", "image", "audio", or "video"
    val duration: Long? = null // Added in the previous step
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
        messageType = parcel.readString() ?: "text",
        duration = parcel.readNullableLong()
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
        parcel.writeNullableLong(duration)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CachedMessage> {
        override fun createFromParcel(parcel: Parcel): CachedMessage = CachedMessage(parcel)
        override fun newArray(size: Int): Array<CachedMessage?> = arrayOfNulls(size)
    }
}

// Extension functions to handle nullable Long in Parcel
fun Parcel.writeNullableLong(value: Long?) {
    if (value == null) {
        writeByte(0)
    } else {
        writeByte(1)
        writeLong(value)
    }
}

fun Parcel.readNullableLong(): Long? {
    return if (readByte() == 0.toByte()) {
        null
    } else {
        readLong()
    }
}