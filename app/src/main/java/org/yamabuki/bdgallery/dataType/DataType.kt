package org.yamabuki.bdgallery.dataType

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

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
    val area: CardArea = CardArea.JP,
    val band: Band = member.band,
)

fun getCGurl (target: Card, trained: Boolean) : String{
    val imgURL2 = "/assets/" + target.area.name.lowercase() + "/characters/resourceset/" +
            target.resSet + "_rip/" + if (trained) "card_after_training" else "card_normal" + ".png"
    return imgURL2
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

enum class CardArea(val index: Int, val lower: String){
    JP(0, "jp"),
    EN(1, "en"),
    TW(2, "tw"),
    CN(3, "cn"),
    KR(3, "kr");
    companion object {
        fun fromIndex(ordinal: Int) : CardArea {
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

enum class Member(val index: Int, val fullName: String, val band: Band){
    KASUMI(1, "戸山 香澄", Band.POPPIN_PARTY),
    TAE(2, "花園 たえ", Band.POPPIN_PARTY),
    RIMI(3, "牛込 りみ", Band.POPPIN_PARTY),
    SAYA(4, "山吹 沙綾", Band.POPPIN_PARTY),
    ARISA(5, "市ヶ谷 有咲", Band.POPPIN_PARTY),
    RAN(6, "美竹 蘭", Band.AFTERGLOW),
    MOCA(7, "青葉 モカ", Band.AFTERGLOW),
    HIMARI(8, "上原 ひまり", Band.AFTERGLOW),
    TOMOE(9, "宇田川 巴", Band.AFTERGLOW),
    TSUGUMI(10, "羽沢 つぐみ", Band.AFTERGLOW),
    KOKORO(11, "弦巻 こころ", Band.HHW),
    KAORU(12, "瀬田 薫", Band.HHW),
    HAGUMI(13, "北沢 はぐみ", Band.HHW),
    KANON(14, "松原 花音", Band.HHW),
    MISAKI(15, "奥沢 美咲", Band.HHW),
    AYA(16, "丸山 彩", Band.PASTEL_PALETTES),
    HINA(17, "氷川 日菜", Band.PASTEL_PALETTES),
    CHISATO(18, "白鷺 千聖", Band.PASTEL_PALETTES),
    MAYA(19, "大和 麻弥", Band.PASTEL_PALETTES),
    EVE(20, "若宮 イヴ", Band.PASTEL_PALETTES),
    YUKINA(21, "湊 友希那", Band.ROSELIA),
    SAYO(22, "氷川 紗夜", Band.ROSELIA),
    LISA(23, "今井 リサ", Band.ROSELIA),
    AKO(24, "宇田川 あこ", Band.ROSELIA),
    RINKO(25, "白金 燐子", Band.ROSELIA),
    MASHIRO(26, "倉田 ましろ", Band.MORFONICA),
    TOKO(27, "桐ヶ谷 透子", Band.MORFONICA),
    NANAMI(28, "広町 七深", Band.MORFONICA),
    TSUKUSHI(29, "二葉 つくし", Band.MORFONICA),
    RUI(30, "八潮 瑠唯", Band.MORFONICA),
    REI(31, "和奏 レイ", Band.RAS),
    ROKKA(32, "朝日 六花", Band.RAS),
    MASUKI(33, "佐藤 ますき", Band.RAS),
    REONA(34, "鳰原 令王那", Band.RAS),
    CHIYU(35, "珠手 ちゆ", Band.RAS);
    companion object {
        fun fromIndex(ordinal: Int) : Member {
            return values()[ordinal]
        }
    }
}