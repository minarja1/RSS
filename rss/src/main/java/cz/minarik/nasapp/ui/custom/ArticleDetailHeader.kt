package cz.minarik.nasapp.ui.custom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.liaoinstan.springview.container.BaseSimpleHeader
import cz.minarik.nasapp.R
import cz.minarik.nasapp.RSSApp

class ArticleDetailHeader : BaseSimpleHeader() {
    private lateinit var view: View
    override fun getView(inflater: LayoutInflater?, viewGroup: ViewGroup?): View {
        val inflaterFinal =
            inflater ?: LayoutInflater.from(RSSApp.sharedInstance.applicationContext)
        view = inflaterFinal.inflate(R.layout.article_detail_footer_header, null)
        return view
    }

    override fun onDropAnim(rootView: View?, dy: Int) {
    }

    override fun onLimitDes(rootView: View?, upORdown: Boolean) {
        getLottieAnimationView()?.run {
            if (upORdown) {
                speed = -.65f
            } else {
                speed = 0.65f
                playAnimation()
            }
        }
    }

    override fun onStartAnim() {
    }

    override fun onFinishAnim() {
        getLottieAnimationView()?.run {
            pauseAnimation()
            progress = 0f
        }
    }

    private fun getLottieAnimationView(): LottieAnimationView? {
        return view.findViewById(R.id.footerAnimationView)
    }
}