package org.yamabuki.bdgallery.dataLayer.card

import org.yamabuki.bdgallery.dataLayer.database.CardDao
import org.yamabuki.bdgallery.dataType.Card

interface CardRepo {

    suspend fun loadAllCards(cardDao: CardDao) : Result<List<Card>>

    suspend fun delAllCards(cardDao: CardDao)

}