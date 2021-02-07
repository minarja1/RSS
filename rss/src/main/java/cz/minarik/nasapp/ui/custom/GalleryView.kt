package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import com.chimbori.crux.articles.Article
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.interfaces.Imagable
import cz.minarik.nasapp.utils.loadImageWithDefaultSettings
import kotlinx.android.synthetic.main.gallery_view.view.*


class GalleryView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var images: List<Imagable>? = null

    private var first: ShapeableImageView
    private var second: ShapeableImageView
    private var secondSecondary: ShapeableImageView // if only 2 images
    private var third: ShapeableImageView
    private var remainingTextView: TextView

    private var listener: GalleryViewClickListener? = null

    init {
        inflate(context, R.layout.gallery_view, this)
        val radius = resources.getDimension(R.dimen.image_view_corner_radius)

        first = findViewById(R.id.first)
        first.shapeAppearanceModel =
            first.shapeAppearanceModel.toBuilder().setBottomLeftCorner(CornerFamily.ROUNDED, radius)
                .setTopLeftCorner(CornerFamily.ROUNDED, radius).build()

        secondSecondary = findViewById(R.id.secondSecondary)
        secondSecondary.shapeAppearanceModel =
            secondSecondary.shapeAppearanceModel.toBuilder()
                .setBottomRightCorner(CornerFamily.ROUNDED, radius)
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, radius)
                .setTopLeftCorner(CornerFamily.ROUNDED, radius).build()

        second = findViewById(R.id.second)
        second.shapeAppearanceModel =
            second.shapeAppearanceModel.toBuilder()
                .setTopRightCorner(CornerFamily.ROUNDED, radius).build()

        third = findViewById(R.id.third)
        third.shapeAppearanceModel =
            third.shapeAppearanceModel.toBuilder()
                .setBottomRightCorner(CornerFamily.ROUNDED, radius).build()
        remainingTextView = findViewById(R.id.remainingTextView)

    }

    private fun viewPrepare() {
        images?.let { images ->
            val radius = resources.getDimension(R.dimen.image_view_corner_radius)
            if (images.size >= 3) {
                secondSecondary.isVisible = false
                fulLayout.isVisible = true
                first.loadImageWithDefaultSettings(images[0].getImageUrl())
                second.loadImageWithDefaultSettings(images[1].getImageUrl())
                third.loadImageWithDefaultSettings(images[2].getImageUrl())
            }

            if (images.size >= 4) {
                third.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(context, R.color.sixtyFivePercentTransparentBlack),
                    PorterDuff.Mode.SRC_ATOP
                )
                remainingTextView.text = "+${images.size - 3}"
            }

            if (images.size == 2) {
                secondSecondary.isVisible = true
                fulLayout.isVisible = false
                first.loadImageWithDefaultSettings(images[0].getImageUrl())
                first.shapeAppearanceModel =
                    first.shapeAppearanceModel.toBuilder()
                        .setBottomLeftCorner(CornerFamily.ROUNDED, radius)
                        .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                        .setBottomRightCorner(CornerFamily.ROUNDED, radius)
                        .setTopRightCorner(CornerFamily.ROUNDED, radius).build()

                secondSecondary.load(images[1].getImageUrl())
            }
            if (images.size == 1) {
                secondSecondary.isVisible = false
                fulLayout.isVisible = false
                first.loadImageWithDefaultSettings(images[0].getImageUrl())
                first.shapeAppearanceModel =
                    first.shapeAppearanceModel.toBuilder()
                        .setBottomLeftCorner(CornerFamily.ROUNDED, radius)
                        .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                        .setBottomRightCorner(CornerFamily.ROUNDED, radius)
                        .setTopRightCorner(CornerFamily.ROUNDED, radius).build()
            }

            first.setOnClickListener {
                listener?.onImageClicked(0, first)
            }
            second.setOnClickListener {
                listener?.onImageClicked(1, second)
            }
            third.setOnClickListener {
                listener?.onImageClicked(2, third)
            }
            secondSecondary.setOnClickListener {
                listener?.onImageClicked(1, secondSecondary)
            }
        }
        requestLayout()
    }

    fun setImages(images: List<Imagable>) {
        this.images = images
        viewPrepare()
        invalidate()
    }

    fun setListener(listener: GalleryViewClickListener) {
        this.listener = listener
    }

    fun getImageForTransition(position: Int): ImageView? {
        if (position >= 2) return third
        if (position == 1) {
            return if (images?.size == 2) secondSecondary else second
        }
        if (position == 0) return first
        return first
    }
}

interface GalleryViewClickListener {
    fun onImageClicked(position: Int, clickedView: ImageView)
}

data class GalleryViewImageDTO(
    val image: String,
) : Imagable {
    override fun getImageUrl() = image

    companion object {
        fun fromApi(response: Article.Image): GalleryViewImageDTO {
            return GalleryViewImageDTO(
                response.srcUrl.toString()
            )
        }
    }
}