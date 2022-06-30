package org.yamabuki.bdgallery.dataLayer.database

import androidx.room.TypeConverter
import org.yamabuki.bdgallery.dataType.Band
import org.yamabuki.bdgallery.dataType.CardArea
import org.yamabuki.bdgallery.dataType.CardAttr
import org.yamabuki.bdgallery.dataType.Member

class Converters {
    @TypeConverter
    fun fromMemberToInt(value: Member): Int {
        return value.index
    }

    @TypeConverter
    fun fromIntToMember(index: Int): Member {
        return Member.fromIndex(index)
    }

    @TypeConverter
    fun fromAttrToInt(value: CardAttr): Int {
        return value.index
    }

    @TypeConverter
    fun fromIntToAttr(index: Int): CardAttr {
        return CardAttr.fromIndex(index)
    }

    @TypeConverter
    fun fromBandToId(value: Band): Int {
        return value.id
    }

    @TypeConverter
    fun fromIdToBand(id: Int): Band {
        return Band.fromId(id)
    }

    @TypeConverter
    fun fromAreaToInt(value: CardArea): Int {
        return value.index
    }

    @TypeConverter
    fun fromIntToArea(index: Int): CardArea {
        return CardArea.fromIndex(index)
    }
}