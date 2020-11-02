package cz.minarik.nasapp.ui.articles.simple

import android.os.Bundle
import android.view.View
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.articles.GenericArticlesFragment


class SimpleArticlesFragment : GenericArticlesFragment(R.layout.fragment_simple_articles) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

}
