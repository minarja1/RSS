package cz.minarik.nasapp.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.MainActivity

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        const val fragmentTag = "fragmentTag"
    }

    fun replaceFragment(
        fragment: Fragment,
    ) {
        if (supportFragmentManager.executePendingTransactions()) return

        val currentFragment = supportFragmentManager.primaryNavigationFragment
        if (fragment.javaClass == currentFragment?.javaClass) return

        val transaction = supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            setReorderingAllowed(true)
        }

        currentFragment?.let {
            transaction.hide(it)
        }

        transaction.add(R.id.container, fragment, fragmentTag)

        transaction.apply {
            setPrimaryNavigationFragment(fragment)
            addToBackStack(null)
            commitAllowingStateLoss()
        }

        onFragmentReplaced()
    }

    open fun onFragmentReplaced() {}
}