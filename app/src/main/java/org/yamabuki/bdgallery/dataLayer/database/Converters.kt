package org.yamabuki.bdgallery.dataLayer.database

import androidx.room.TypeConverter
import org.yamabuki.bdgallery.dataType.Band
import org.yamabuki.bdgallery.dataType.ServerArea
import org.yamabuki.bdgallery.dataType.CardAttr
import org.yamabuki.bdgallery.dataType.Member

class Converters {
//    @TypeConverter
//    fun fromMemberToInt(value: Member): Int {
//        return value.id
//    }
//
//    @TypeConverter
//    fun fromIntToMember(index: Int): Member {
//        return Member.fromId(index)
//    }

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
    fun fromAreaToInt(value: ServerArea): Int {
        return value.index
    }

    @TypeConverter
    fun fromIntToArea(index: Int): ServerArea {
        return ServerArea.fromIndex(index)
    }

    @TypeConverter
    fun charsToFlag(chars: List<Member>): Long{
        var result: Long = 0
        for (char in chars){
            result = result or char.flag
        }
        return result
    }

    @TypeConverter
    fun flagToChars(flag: Long): List<Member>{
        val result = mutableListOf<Member>()
        for (member in Member.values()){
            if (flag and member.flag != 0L) result.add(member)
        }
        return result
    }

    @TypeConverter
    fun areaToFlag(areaList: List<ServerArea>): Int{
        var result: Int = 0
        for (area in areaList){
            result = result or area.flag
        }
        return result
    }

    @TypeConverter
    fun flagToArea(flag: Int): List<ServerArea>{
        val result = mutableListOf<ServerArea>()
        for (area in ServerArea.values()){
            if (flag and area.flag != 0) result.add(area)
        }
        return result
    }

    @TypeConverter
    fun fromNullableMemberToInt(value: Member?): Int {
        if (value != null)return value.id
        return 0
    }

    @TypeConverter
    fun fromIntToNullableMember(index: Int): Member? {
        if (index != 0) return Member.fromId(index)
        return null
    }

}