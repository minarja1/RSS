package cz.minarik.nasapp.ui.settings

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import coil.Coil
import coil.request.ImageRequest
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import cz.minarik.base.common.extensions.hideKeyboard
import cz.minarik.base.ui.base.BaseListAdapter
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.databinding.FragmentNotificationSettingsBinding
import cz.minarik.nasapp.ui.MainActivity
import cz.minarik.nasapp.ui.base.BaseFragment
import cz.minarik.nasapp.utils.NotificationSettings
import cz.minarik.nasapp.utils.onImeOption
import kotlinx.coroutines.launch


class NotificationSettingsFragment : BaseFragment<FragmentNotificationSettingsBinding>() {

    companion object {
        fun newInstance() = NotificationSettingsFragment()
    }

    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // todo handle result
    }

    private lateinit var notificationSettings: NotificationSettings
    private var keywordsAdapter: ChipAdapter? = null
    override fun getViewBinding(): FragmentNotificationSettingsBinding =
        FragmentNotificationSettingsBinding.inflate(layoutInflater)

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

        binding.notifyAll.isChecked = it.notifyAll
        keywordsAdapter?.submitList(it.keyWords)
    }

    private fun initViews(view: View) {
        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).run {
            setSupportActionBar(view.findViewById(R.id.toolbar))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = requireContext().getString(R.string.notification_settings)
        }

        binding.notifyAll.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                notificationSettings.notifyAll = isChecked
                save()
            }
        }

        initKeywords()

        initSources()

        // todo handle properly
        pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun initSources() {
        binding.addSourceButton.setOnClickListener { goToSources() }
    }

    private fun goToSources() {
        (requireActivity() as MainActivity).navigateToAddSources()
    }

    private fun insertKeyword() {
        binding.keywordsEditText.text?.toString()?.let {
            if (it.isNotEmpty()) {
                notificationSettings.keyWords.add(0, NotificationKeyword(it))
                lifecycleScope.launch { save() }
                keywordsAdapter?.notifyItemInserted(0)

                binding.keywordsEditText.setText("")
                binding.keywordsEditText.clearFocus()
                hideKeyboard()
            }
        }
    }

    private suspend fun save() {
        DataStoreManager.setNotificationSettings(notificationSettings)
    }

    private fun initKeywords() {
        binding.keywordsEditText.onImeOption(EditorInfo.IME_ACTION_DONE) {
            insertKeyword()
        }

        keywordsAdapter =
            ChipAdapter { item, position ->
                notificationSettings.keyWords.remove(item)
                lifecycleScope.launch { save() }
                keywordsAdapter?.notifyItemRemoved(position)
            }

        binding.keywordsRecycler.adapter = keywordsAdapter

        val keywordsLayoutManager =
            ChipsLayoutManager.newBuilder(requireContext())
                .setScrollingEnabled(false)
                .setGravityResolver { Gravity.START }
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                .withLastRow(true)
                .build()

        binding.keywordsRecycler.layoutManager = keywordsLayoutManager
    }

    class ChipAdapter(
        private var onItemDeleted: ((item: NotificationKeyword, position: Int) -> Unit)? = null
    ) : BaseListAdapter<NotificationKeyword>(
        R.layout.view_notification_keyword,
        object : DiffUtil.ItemCallback<NotificationKeyword>() {
            override fun areItemsTheSame(
                oldItem: NotificationKeyword,
                newItem: NotificationKeyword
            ): Boolean {
                return oldItem.value == newItem.value
            }

            override fun areContentsTheSame(
                oldItem: NotificationKeyword,
                newItem: NotificationKeyword
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {
        override fun bind(
            itemView: View,
            item: NotificationKeyword,
            position: Int,
            viewHolder: BaseViewHolderImp
        ) {
            itemView.apply {
                val keywordChip =
                    findViewById<com.google.android.material.chip.Chip>(R.id.keywordChip)
                keywordChip.text = item.value

                val request = ImageRequest.Builder(context)
                    .target(
                        onSuccess = {
                            keywordChip.chipIcon = it
                        }
                    )
                    .data("https://ct24.ceskatelevize.cz/sites/default/files/styles/node-article_horizontal/public/images/2312363-nasa-logo-web-rgb.jpg")
                    .build()

                Coil.enqueue(request)

                keywordChip.setOnCloseIconClickListener {
                    onItemDeleted?.invoke(item, viewHolder.absoluteAdapterPosition)
                }
            }
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