package id.yukngoding.player.myPlayer

import android.content.Context
import android.text.TextUtils

class MyPlayerPresenter(var context: Context, var view: MyPlayerContract.View) :
    MyPlayerContract.Presenter {

    override fun getContentById(contentId: Int, type: Int) {
        //Content for page
    }

    /** Last watch */
    override fun saveLastTime(contentId: Int, type: Int, lastTime: Float) {
    }

    /** START Fav in local or API */
    override fun checkIsFav(contentId: Int, type: Int) {
    }

    override fun setFav(contentId: Int, type: Int) {
    }

    private fun deleteFav(courseId: Int, courseLessonId: Int, favoriteId: Int) {
    }

    private fun deleteFavLocal(contentId: Int, type: Int) {}

    private fun createFav(courseId: Int, courseTypeId: Int, courseLessonId: Int) {
    }

    private fun saveFavLocal(contentId: Int, type: Int) {
    }
    /** END Fav in local or API */

    override fun setHistory(contentId: Int, type: Int) {
    }

    override fun getWistiaMediaById(embed: String?) {
        ///Example result
        view.resultWistiaMedia(null)
    }

}
