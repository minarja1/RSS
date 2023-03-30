package cz.minarik.nasapp.ui.about

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cz.minarik.nasapp.R
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.databinding.FragmentAboutBinding
import cz.minarik.nasapp.ui.base.BaseFragment
import cz.minarik.nasapp.utils.openPlayStore
import cz.minarik.nasapp.utils.sendFeedbackEmail

class AboutFragment : BaseFragment<FragmentAboutBinding>() {

    override fun getViewBinding(): FragmentAboutBinding =
        FragmentAboutBinding.inflate(layoutInflater)

    companion object {
        fun newInstance(): AboutFragment = AboutFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    private fun initViews(view: View) {
        binding.run {
            (requireActivity() as AppCompatActivity).run {
                setSupportActionBar(view.findViewById(R.id.toolbar))
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = requireContext().getString(R.string.about_app)
            }

            versionTextView.text = getString(R.string.version, RSSApp.sharedInstance.versionName)

            shareTheLoveButton.setOnClickListener { requireContext().openPlayStore() }

            contactDeveloperBorderedButton.setOnClickListener { requireContext().sendFeedbackEmail() }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}