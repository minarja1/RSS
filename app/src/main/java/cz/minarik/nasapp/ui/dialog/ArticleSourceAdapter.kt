package cz.minarik.nasapp.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.minarik.nasapp.R
import cz.minarik.nasapp.model.RSSSourceDTO
import cz.minarik.nasapp.ui.custom.SourceSelectionItemView

class ArticleSourceAdapter(
    var sources: List<RSSSourceDTO>,
    private var onItemClicked: ((item: RSSSourceDTO) -> Unit)?
) : RecyclerView.Adapter<ArticleSourceAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_source_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return sources.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = sources[position]
        holder.sourceView.set(item)
        holder.sourceView.setOnClickListener {
            onItemClicked?.invoke(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sourceView: SourceSelectionItemView = itemView.findViewById(R.id.sourceSelectionView)
    }
}