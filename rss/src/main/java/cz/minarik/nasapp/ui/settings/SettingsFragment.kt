package cz.minarik.nasapp.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.launch

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
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
        (requireActivity() as AppCompatActivity).run {
            setSupportActionBar(view.findViewById(R.id.toolbar))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = requireContext().getString(R.string.settings)
        }

        DataStoreManager.getExpandAllCards().collectWhenStarted {
            expandAllCardsSwitch.isChecked = it
        }
        expandAllCardsSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                DataStoreManager.setExpandAllCards(isChecked)
            }
        }

        DataStoreManager.getOpenArticlesInBrowser().collectWhenStarted {
            openBrowserSwitch.isChecked = it
        }
        openBrowserSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                DataStoreManager.setOpenArticlesInBrowser(isChecked)
            }
        }

        DataStoreManager.getUseExternalBrowser().collectWhenStarted {
            useExternalBrowserSwitch.isChecked = it
        }
        useExternalBrowserSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                DataStoreManager.setUseExternalBrowser(isChecked)
            }
        }

        notificationsTextView.setOnClickListener {
            Snackbar.make(notificationsTextView, R.string.notifications_coming_soon, Snackbar.LENGTH_LONG).show()
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