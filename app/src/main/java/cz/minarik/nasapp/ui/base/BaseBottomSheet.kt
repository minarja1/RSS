package cz.minarik.nasapp.ui.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheet : BottomSheetDialogFragment() {
    @get:LayoutRes
    abstract val layoutId: Int

    lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(layoutId, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (rootView.parent as View).setBackgroundColor(Color.TRANSPARENT)

        //know-how for adding margin to bottomshit
//        val parent = rootView.parent as View
//        val layoutParams = parent.layoutParams as CoordinatorLayout.LayoutParams
//        layoutParams.setMargins(
//            8.dpToPx,
//            0,
//            8.dpToPx,
//            0
//        )
//        parent.layoutParams = layoutParams
    }
}