package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import coil.load
import cz.minarik.nasapp.R
import kotlinx.android.synthetic.main.button_bordered.view.*


class BorderedButton(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    init {
        inflate(context, R.layout.button_bordered, this)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BorderedButton,
            0, 0
        ).apply {
            try {
                val text = getString(R.styleable.BorderedButton_buttonText)
                val title = getString(R.styleable.BorderedButton_buttonTitle)
                val image = getDrawable(R.styleable.BorderedButton_buttonImage)
                setText(text)
                setTitle(title)
                image?.let {
                    setImage(image)
                }
            } finally {
                recycle()
            }
        }
    }

    fun set(title: String? = null, text: String, description: String? = null) {
        setText(text)
        setTitle(title)
        setDescription(description)
    }

    fun setText(text: String?) {
        nameTextView.text = text
    }

    fun setTitle(title: String? = null) {
        titleTextViewCollapsed.isVisible = !title.isNullOrEmpty()
        titleTextViewCollapsed.text = title
    }

    fun setDescription(description: String? = null) {
        descriptionTextView.isVisible = !description.isNullOrEmpty()
        descriptionTextView.text = description
    }

    override fun setOnClickListener(l: OnClickListener?) {
        cardView.setOnClickListener(l)
    }

    fun setImage(image: Drawable?) {
        imageView.load(image)
    }

    fun setImage(image: Int) {
        imageView.setImageResource(image)
    }

    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        cardView.isClickable = clickable
        imageView.isVisible = clickable
    }
}