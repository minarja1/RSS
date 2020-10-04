package cz.minarik.nasapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import cz.minarik.base.common.extensions.dividerMedium
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.model.RSSSourceDTO
import cz.minarik.nasapp.utils.UniversePrefManager
import kotlinx.android.synthetic.main.dialog_articles_source_selection.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ArticlesSourceSelectionDialogFragment : DialogFragment(), KoinComponent {
    private lateinit var sources: List<RSSSourceDTO>
    private val prefManager by inject<UniversePrefManager>()

    companion object {
        const val argSources = "argSources"

        var onSouurceSelected: ((item: RSSSourceDTO) -> Unit)? = null

        fun newInstance(sources: List<RSSSourceDTO>): ArticlesSourceSelectionDialogFragment {
            return ArticlesSourceSelectionDialogFragment().apply {
                arguments = bundleOf(
                    argSources to sources,
                )
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_articles_source_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initViews()
        }
    }

    private fun initViews() {
        sources = arguments?.getSerializable(argSources) as List<RSSSourceDTO>
        val adapter = ArticleSourceAdapter(sources) {
            onSouurceSelected?.invoke(it)
            dismiss()
        }
        articleSourcesRecyclerView.dividerMedium()
        articleSourcesRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.attributes?.apply {
            width = resources.getDimension(R.dimen.dialog_width).toInt()
            height = resources.getDimension(R.dimen.dialog_min_height).toInt()
            dialog?.window?.attributes = this
        }
        dialog?.window?.setBackgroundDrawable(null)
    }
}