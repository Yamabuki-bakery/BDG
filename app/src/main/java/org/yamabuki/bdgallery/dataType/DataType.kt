package org.yamabuki.bdgallery.dataType

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Card(
    @PrimaryKey val id: Int,
    val title: String,
    val attribute: CardAttr,
    val member: Member,
    val star: Int,
    val releasedAt: Long,
    val resSet: String,
    val imgNormal: Boolean = false,
    val imgTrained: Boolean = false,
    val area: ServerArea = ServerArea.JP,
    val band: Band = member.band,
){
    fun getCGurl (trained: Boolean) : String{
        if (!(trained and imgTrained) or !(!trained and imgNormal))
            throw IllegalArgumentException("[MangaGetCGurl] 特訓，過於惡俗！")
        val imgURL2 = "/assets/${this.area.lower}/characters/resourceset/${this.resSet}_rip/${if (trained) "card_after_training" else "card_normal"}.png"
        return imgURL2
    }
    fun getThumburl (trained: Boolean) : String{
        if (!(trained and imgTrained) or !(!trained and imgNormal))
            throw IllegalArgumentException("[MangaGetThumbUrl] 特訓，過於惡俗！")
        val imgURL2 = "/assets/${this.area.lower}/thumb/chara/card${(this.id / 50).toString().padStart(5, '0')}_rip/${this.resSet}_${if (trained) "after_training" else "normal"}.png"
        return imgURL2
    }
}

@Entity
data class Manga(
    @PrimaryKey val id: Int,
    val assetName: String,
    val titleJP: String?,
    val titleEN: String?,
    val titleTW: String?,
    val titleCN: String?,
    val titleKR: String?,
    val publicStartAt: Long = 1,
    val areaAvailability: List<ServerArea>,
    val fourFrame: Boolean,
    val characters: List<Member> = mutableListOf()
){
    fun getImgUrl(serverArea: ServerArea): String{
        if (serverArea !in this.areaAvailability)
            throw IllegalArgumentException("[MangaGetImgUrl] 地區代碼過於惡俗！")
        val imgURL2 =  "/assets/${serverArea.lower}/comic/comic_${if(this.fourFrame) "fourframe/" else "singleframe/"}${assetName}_rip/${assetName}.png"
        return imgURL2
    }
    fun getThumbUrl(serverArea: ServerArea): String{
        if (serverArea !in this.areaAvailability)
            throw IllegalArgumentException("[MangaGetThumbUrl] 地區代碼過於惡俗！")
        val imgURL2 =  "/assets/${serverArea.lower}/comic/comic_${if(this.fourFrame) "fourframe" else "singleframe"}_thumbnail/${assetName}_rip/${assetName}.png"
        return imgURL2
    }
}

enum class CardAttr(val index: Int){
    POWERFUL(0),
    COOL(1),
    HAPPY(2),
    PURE(3);
    companion object {
        fun fromIndex(ordinal: Int) : CardAttr {
            return CardAttr.values()[ordinal]
        }
    }
}

enum class ServerArea(val index: Int, val lower: String, val flag: Int){
    JP(0, "jp", 1),
    EN(1, "en", 2),
    TW(2, "tw", 4),
    CN(3, "cn", 8),
    KR(3, "kr", 16);
    companion object {
        fun fromIndex(ordinal: Int) : ServerArea {
            return values()[ordinal]
        }
    }
}

enum class Band(val id: Int, val bandName: String){
    POPPIN_PARTY(1, "Poppin'Party"),
    AFTERGLOW(2, "Afterglow"),
    HHW(3, "ハロー、ハッピーワールド！"),
    PASTEL_PALETTES(4, "Pastel＊Palettes"),
    ROSELIA(5, "Roselia"),
    MORFONICA(6, "Morfonica"),
    RAS(7, "RAISE A SUILEN");
    companion object {
        fun fromId(ordinal: Int) : Band {
            return values()[ordinal - 1]
        }
    }
}

enum class Member(val id: Int, val flag: Long, val fullName: String, val band: Band){
    KASUMI(1,              1, "戸山 香澄", Band.POPPIN_PARTY),
    TAE(2,                 2, "花園 たえ", Band.POPPIN_PARTY),
    RIMI(3,                4, "牛込 りみ", Band.POPPIN_PARTY),
    SAYA(4,                8, "山吹 沙綾", Band.POPPIN_PARTY),
    ARISA(5,            0x10, "市ヶ谷 有咲", Band.POPPIN_PARTY),
    RAN(6,              0x20, "美竹 蘭", Band.AFTERGLOW),
    MOCA(7,             0x40, "青葉 モカ", Band.AFTERGLOW),
    HIMARI(8,           0x80, "上原 ひまり", Band.AFTERGLOW),
    TOMOE(9,           0x100, "宇田川 巴", Band.AFTERGLOW),
    TSUGUMI(10,        0x200, "羽沢 つぐみ", Band.AFTERGLOW),
    KOKORO(11,         0x400, "弦巻 こころ", Band.HHW),
    KAORU(12,          0x800, "瀬田 薫",   Band.HHW),
    HAGUMI(13,        0x1000, "北沢 はぐみ", Band.HHW),
    KANON(14,         0x2000, "松原 花音", Band.HHW),
    MISAKI(15,        0x4000, "奥沢 美咲", Band.HHW),
    AYA(16,           0x8000, "丸山 彩", Band.PASTEL_PALETTES),
    HINA(17,         0x10000, "氷川 日菜", Band.PASTEL_PALETTES),
    CHISATO(18,      0x20000, "白鷺 千聖", Band.PASTEL_PALETTES),
    MAYA(19,         0x40000, "大和 麻弥", Band.PASTEL_PALETTES),
    EVE(20,          0x80000, "若宮 イヴ", Band.PASTEL_PALETTES),
    YUKINA(21,      0x100000, "湊 友希那", Band.ROSELIA),
    SAYO(22,        0x200000, "氷川 紗夜", Band.ROSELIA),
    LISA(23,        0x400000, "今井 リサ", Band.ROSELIA),
    AKO(24,         0x800000, "宇田川 あこ", Band.ROSELIA),
    RINKO(25,      0x1000000, "白金 燐子", Band.ROSELIA),
    MASHIRO(26,    0x2000000, "倉田 ましろ", Band.MORFONICA),
    TOKO(27,       0x4000000, "桐ヶ谷 透子", Band.MORFONICA),
    NANAMI(28,     0x8000000, "広町 七深", Band.MORFONICA),
    TSUKUSHI(29,  0x10000000, "二葉 つくし", Band.MORFONICA),
    RUI(30,       0x20000000, "八潮 瑠唯", Band.MORFONICA),
    REI(31,       0x40000000, "和奏 レイ", Band.RAS),
    ROKKA(32,     0x80000000, "朝日 六花", Band.RAS),
    MASUKI(33,   0x100000000, "佐藤 ますき", Band.RAS),
    REONA(34,    0x200000000, "鳰原 令王那", Band.RAS),
    CHIYU(35,    0x400000000, "珠手 ちゆ", Band.RAS);
    companion object {
        fun fromId(ordinal: Int) : Member {
            return values()[ordinal - 1]
        }
    }
}