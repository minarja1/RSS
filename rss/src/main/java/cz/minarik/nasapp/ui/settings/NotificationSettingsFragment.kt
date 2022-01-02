package cz.minarik.nasapp.ui.settings

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import cz.minarik.base.common.extensions.hideKeyboard
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.base.ui.base.BaseListAdapter
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.utils.NotificationSettings
import cz.minarik.nasapp.utils.onImeOption
import kotlinx.android.synthetic.main.bottom_sheet_article.*
import kotlinx.android.synthetic.main.fragment_notification_settings.*
import kotlinx.android.synthetic.main.view_notification_keyword.view.*
import kotlinx.coroutines.launch


class NotificationSettingsFragment : BaseFragment(R.layout.fragment_notification_settings) {

    companion object {
        fun newInstance() = NotificationSettingsFragment()
    }

    private lateinit var notificationSettings: NotificationSettings
    private var keywordsAdapter: NotificationKeywordAdapter? = null

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
        keywordsAdapter?.submitList(it.keyWords)
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
                save()
            }
        }

        initKeywords()

        initSources()
    }

    private fun initSources() {
        sourceButton.setOnClickListener { showSourcesDialog() }
    }

    private fun showSourcesDialog() {

    }

    private fun insertKeyword() {
        keywordsEditText.text?.toString()?.let {
            if (it.isNotEmpty()) {
                notificationSettings.keyWords.add(0, NotificationKeyword(it))
                lifecycleScope.launch { save() }
                keywordsAdapter?.notifyItemInserted(0)

                keywordsEditText.setText("")
                keywordsEditText.clearFocus()
                hideKeyboard()
            }
        }
    }

    private suspend fun save() {
        DataStoreManager.setNotificationSettings(notificationSettings)
    }

    private fun initKeywords() {
        keywordsEditText.onImeOption(EditorInfo.IME_ACTION_DONE) {
            insertKeyword()
        }

        keywordsAdapter =
            NotificationKeywordAdapter { item, position ->
                notificationSettings.keyWords.remove(item)
                lifecycleScope.launch { save() }
                keywordsAdapter?.notifyItemRemoved(position)
            }

        keywordsRecycler.adapter = keywordsAdapter

        val keywordsLayoutManager =
            ChipsLayoutManager.newBuilder(requireContext())
                .setScrollingEnabled(false)
                .setGravityResolver { Gravity.START }
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                .withLastRow(true)
                .build()

        keywordsRecycler.layoutManager = keywordsLayoutManager
    }

    class NotificationKeywordAdapter(
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
                keywordChip.text = item.value
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