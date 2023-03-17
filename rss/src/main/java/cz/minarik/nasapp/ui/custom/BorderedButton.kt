package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import coil.load
import cz.minarik.nasapp.R
import cz.minarik.nasapp.databinding.ButtonBorderedBinding


class BorderedButton(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var binding: ButtonBorderedBinding

    init {
        binding = ButtonBorderedBinding.inflate(LayoutInflater.from(context), this, true)
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
        binding.nameTextView.text = text
    }

    fun setTitle(title: String? = null) {
        binding.titleTextViewCollapsed.isVisible = !title.isNullOrEmpty()
        binding.titleTextViewCollapsed.text = title
    }

    fun setDescription(description: String? = null) {
        binding.descriptionTextView.isVisible = !description.isNullOrEmpty()
        binding.descriptionTextView.text = description
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.cardView.setOnClickListener(l)
    }

    fun setImage(image: Drawable?) {
        binding.imageView.load(image)
    }

    fun setImage(image: Int) {
        binding.imageView.setImageResource(image)
    }

    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        binding.cardView.isClickable = clickable
        binding.imageView.isVisible = clickable
    }
}