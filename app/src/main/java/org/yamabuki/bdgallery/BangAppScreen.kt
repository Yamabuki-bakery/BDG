package org.yamabuki.bdgallery

enum class BangAppScreen(
    val title: Int,
    val icon: Int,
) {
    Home(
        title = R.string.screen_title_home,
        icon = R.drawable.icon_filled_home
    ),
    Gallery(
        title = R.string.screen_title_gallery,
        icon = R.drawable.icon_filled_gallery
    ),
    Favorite(
        title = R.string.screen_title_favorite,
        icon = R.drawable.icon_filled_star
    ),
    Stickers(
        title = R.string.screen_title_stickers,
        icon = R.drawable.icon_filled_stickers
    );

    companion object {
        fun fromRoute(route: String?): BangAppScreen =
            when (route?.substringBefore("/")) {
                Home.name -> Home
                Gallery.name -> Gallery
                Favorite.name -> Favorite
                Stickers.name -> Stickers
                null -> Home
                else -> throw IllegalArgumentException("Route $route is not recognized.")
            }
    }
}