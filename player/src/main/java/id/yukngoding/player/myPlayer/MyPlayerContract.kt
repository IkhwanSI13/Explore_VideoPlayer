package id.yukngoding.player.myPlayer

interface MyPlayerContract {
    interface View {
        fun setContent(dao: String?)
    }

    interface Presenter {
        fun getContentById(contentId: Int, type: Int)

        fun saveDaoHistory(contentId: Int, type: Int)
        fun saveDaoLastWatch(contentId: Int, type: Int, lastTime: Float)

        fun checkIsFav(contentId: Int, type: Int)
        fun setFav(contentId: Int, type: Int)
    }

}