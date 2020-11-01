package id.yukngoding.player.myPlayer

interface MyPlayerContract {
    interface View {
        fun setContent(dao: String)
        fun setContentByUrl(contentUrl: String?)
        fun resultWistiaMedia(url: String?)

        fun resultCheckIsFav(isFav: Boolean)
        fun resultSetFav(type: Int, boolean: Boolean)
    }

    interface Presenter {
        fun getContentById(contentId: Int, type: Int)
        fun saveLastTime(contentId: Int, type: Int, lastTime: Float)
        fun setHistory(contentId: Int, type: Int)
        fun getWistiaMediaById(embed: String?)

        fun checkIsFav(contentId: Int, type: Int)
        fun setFav(contentId: Int, type: Int)
    }

}