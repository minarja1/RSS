package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import cz.minarik.nasapp.R
import kotlinx.android.synthetic.main.view_state_view.view.*


open class StateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    var onClick: (() -> Unit)? = null
    var onSecondClick: (() -> Unit)? = null
    var title: String? = null
    var subtitle: String? = null
    var buttonFirst: String? = null
    var buttonSecond: String? = null
    var imageDrawable: Drawable? = null
    var showProgressBar: Boolean = false
    var animateSuccess: Boolean = false

    var emptyImageRes: Drawable? = null

    protected var contentView: View? = null

    init {
        inflate(context, R.layout.view_state_view, this)
        if (!isInEditMode) {
            attrs?.let { attr ->
                context.obtainStyledAttributes(
                    attr, R.styleable.StateView, defStyleAttr, defStyleAttr
                ).apply {
                    try {
                        emptyImageRes = getDrawable(R.styleable.StateView_emptyImageRes)
                    } catch (e: Throwable) {

                    } finally {
                        recycle()
                    }
                    button.setOnClickListener {
                        onClick?.invoke()
                    }
                    secondButton.setOnClickListener {
                        onSecondClick?.invoke()
                    }
                }
            }
        }
    }

    private fun getEmptyDrawable(): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.ic_baseline_not_interested_24)
    }

    fun getErrorDrawable(): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.ic_baseline_error_outline_24)
    }

    private fun getNoInternetDrawable(): Drawable? {
        return ContextCompat.getDrawable(
            context,
            R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24
        )
    }

    fun attacheContentView(view: View) {
        this.contentView = view
    }

    protected fun viewPrepare() {
        titleTextView.apply {
            isVisible = !title.isNullOrEmpty()
            text = title
        }
        subtitleTextView.apply {
            isVisible = !subtitle.isNullOrEmpty()
            text = subtitle
        }

        button?.let {
            it.text = buttonFirst
            it.isInvisible = onClick == null
            setOnClickListener {
                onClick?.invoke()
            }
        }
        secondButton?.let {
            it.text = buttonSecond
            it.isInvisible = onSecondClick == null
            setOnClickListener {
                onSecondClick?.invoke()
            }
        }

        loadingProgressBar.isVisible = showProgressBar

        imageView.apply {
            isInvisible = imageDrawable == null
            setImageDrawable(imageDrawable)
        }

        //know-how just in case
//        animationView.isVisible = animateSuccess
//        if (animateSuccess) {
//            animationView.animateSuccess()
//        }
    }

    fun loading(show: Boolean, message: String? = null) {
        if (show) {
            setFields(showProgressBar = true, title = message)
        }
        show(show)
    }

    fun error(show: Boolean, exception: Throwable? = null) {
        error(show, exception?.message, onButtonClicked = null)
    }

    fun error(show: Boolean, message: String? = null, onButtonClicked: (() -> Unit)? = null) {
        if (show) {
            setFields(
                title = message ?: context.getString(R.string.common_base_error),
                onClick = onButtonClicked,
                buttonFirst = if (onButtonClicked != null) context.getString(R.string.try_again) else "",
                drawable = getErrorDrawable()
            )
        }
        show(show)
    }

    fun success(
        show: Boolean,
        message: String? = null,
        buttonText: String? = null,
        onButtonClicked: (() -> Unit)? = null
    ) {
        if (show) {
            setFields(
                title = message,
                animateSuccess = true,
                buttonFirst = buttonText,
                onClick = onButtonClicked
            )
        }
        show(show)
    }

    fun empty(show: Boolean, message: String? = null) {
        if (show) {
            setFields(
                title = message ?: context.getString(R.string.no_results),
                drawable = if (emptyImageRes == null) {
                    getEmptyDrawable()
                } else {
                    emptyImageRes
                }
            )
        }
        show(show)
    }

    fun noInternet(show: Boolean, onButtonClicked: (() -> Unit)? = null) {
        if (show) {
            setFields(
                onClick = onButtonClicked,
                title = context.getString(R.string.no_internet_connection),
                drawable = getNoInternetDrawable(),
                buttonFirst = context.getString(R.string.try_again)
            )
        }
        show(show)
    }

    fun show(show: Boolean) {
        contentView?.isVisible = !show
        isVisible = show
        viewPrepare()
    }

    fun setFields(
        onClick: (() -> Unit)? = null,
        onSecondClick: (() -> Unit)? = null,
        title: String? = null,
        subtitle: String? = null,
        buttonFirst: String? = null,
        buttonSecond: String? = null,
        drawable: Drawable? = null,
        showProgressBar: Boolean = false,
        animateSuccess: Boolean = false
    ) {
        this.onClick = onClick
        this.onSecondClick = onSecondClick
        this.title = title
        this.subtitle = subtitle
        this.buttonFirst = buttonFirst
        this.buttonSecond = buttonSecond
        this.imageDrawable = drawable
        this.showProgressBar = showProgressBar
        this.animateSuccess = animateSuccess
    }

}