package cz.minarik.nasapp.ui.articles.bottomSheet

interface ArticleBottomSheetListener {
    fun onStarred()
    fun onRead()
    fun onShare()
    fun onSource(sourceUrl:String)
}