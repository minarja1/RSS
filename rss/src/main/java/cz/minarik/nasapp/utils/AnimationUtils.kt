package cz.minarik.nasapp.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.DecelerateInterpolator
import cz.minarik.nasapp.utils.Constants.Companion.circularRevealDuration
import kotlin.math.hypot

/**
 * Starts circular reveal animation
 * [fromLeft] if `true` then start animation from the bottom left of the [View] else start from the bottom right
 */
fun View.startCircularReveal(fromLeft: Boolean, onFinished: (() -> Unit)? = null) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int,
            oldRight: Int, oldBottom: Int
        ) {
            v.removeOnLayoutChangeListener(this)
            // TODO: Inject this from arguments
            val cx = if (fromLeft) v.left else v.right
            val cy = v.bottom
            val radius = hypot(right.toDouble(), bottom.toDouble()).toInt()
            ViewAnimationUtils.createCircularReveal(v, cx, cy, 0f, radius.toFloat()).apply {
                interpolator = DecelerateInterpolator(1f)
                duration = circularRevealDuration
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onFinished?.invoke()
                        super.onAnimationEnd(animation)
                    }
                })
                start()
            }
        }
    })
}

/**
 * Animate fragment exit using given parameters as animation end point. Runs the given block of code
 * after animation completion.
 *
 * @param exitX: Animation end point X coordinate.
 * @param exitY: Animation end point Y coordinate.
 * @param onFinished: Block of code to be executed on animation completion.
 */
fun View.exitCircularReveal(exitX: Int, exitY: Int, onFinished: () -> Unit) {
    try {
        val startRadius = Math.hypot(this.width.toDouble(), this.height.toDouble())
        ViewAnimationUtils.createCircularReveal(this, exitX, exitY, startRadius.toFloat(), 0f)
            .apply {
                duration = circularRevealDuration
                interpolator = DecelerateInterpolator(1f)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onFinished()
                        super.onAnimationEnd(animation)
                    }
                })

                start()
            }
    } catch (ignored: IllegalStateException) {
    }
}

/**
 * @return the position of the current [View]'s center in the screen
 */
fun View.findLocationOfCenterOnTheScreen(): IntArray {
    val positions = intArrayOf(0, 0)
    getLocationInWindow(positions)
    // Get the center of the view
    positions[0] = positions[0] + width / 2
    positions[1] = positions[1] + height / 2
    return positions
}

/**
 * Needs to be implemented by fragments that should exit with circular reveal
 * animation along with [isToBeExitedWithAnimation] returning true
 * @property referencedViewPosX the X-axis position of the center of circular reveal
 * @property referencedViewPosY the Y-axis position of the center of circular reveal
 */
interface ExitWithAnimation {
    var referencedViewPosX: Int
    var referencedViewPosY: Int

    /**
     * Must return true if required to exit with circular reveal animation
     */
    fun isToBeExitedWithAnimation(): Boolean
}


inline fun getValueAnimator(
    forward: Boolean = true,
    duration: Long,
    interpolator: TimeInterpolator,
    crossinline updateListener: (progress: Float) -> Unit
): ValueAnimator {
    val a =
        if (forward) ValueAnimator.ofFloat(0f, 1f)
        else ValueAnimator.ofFloat(1f, 0f)
    a.addUpdateListener { updateListener(it.animatedValue as Float) }
    a.duration = duration
    a.interpolator = interpolator
    return a
}
