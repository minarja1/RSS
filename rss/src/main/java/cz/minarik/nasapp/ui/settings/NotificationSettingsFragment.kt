package cz.minarik.nasapp.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.utils.NotificationSettings
import kotlinx.android.synthetic.main.fragment_notification_settings.*
import kotlinx.coroutines.launch

class NotificationSettingsFragment : BaseFragment(R.layout.fragment_notification_settings) {

    companion object {
        fun newInstance() = NotificationSettingsFragment()
    }

    lateinit var notificationSettings: NotificationSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
    }

    private fun initObserve() {
        DataStoreManager.getNotificationSettings().collectWhenStarted {
            updateViews(it)
        }
    }

    private fun updateViews(it: NotificationSettings) {
        this.notificationSettings = it

        notifyAll.isChecked = it.notifyAll
    }

    private fun initViews(view: View) {
        (requireActivity() as AppCompatActivity).run {
            setSupportActionBar(view.findViewById(R.id.toolbar))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = requireContext().getString(R.string.notification_settings)
        }

        notifyAll.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                notificationSettings.notifyAll = isChecked
                DataStoreManager.setNotificationSettings(notificationSettings)
            }
        }
    }
}