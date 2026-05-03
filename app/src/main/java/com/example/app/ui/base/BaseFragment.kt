package com.example.app.ui.base

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.app.R
import com.example.app.exception.ApplicationException
import com.example.app.ui.manager.Navigator
import com.example.app.utils.AppUtil.applyEdgeToEdgeInsets
import javax.inject.Inject

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    lateinit var toolbar: HasToolbar

    @Inject
    lateinit var navigator: Navigator

    private var _binding: T? = null

    protected val binding: T
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createViewBinding(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        handleEdgeToEdge()
        applyEdgeToEdgeInsets(
            false,
            true,
            ContextCompat.getDrawable(requireActivity(), R.color.colorPrimary)
        )

        bindData()
        setupBackPressedDispatcher()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is HasToolbar)
            toolbar = activity as HasToolbar
    }

    fun isShowLoader(show:Boolean){
    if (show) showLoader() else hideLoader()
    }

    fun showLoader() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).toggleLoader(true)
        }
    }

    fun hideLoader() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).toggleLoader(false)
        }
    }

    fun hideKeyBoard() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).hideKeyboard()
        }
    }

    fun showKeyBoard() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showKeyboard()
        }
    }

    fun <T : BaseFragment<*>> getParentFragment(targetFragment: Class<T>): T? {
        if (parentFragment == null) return null
        try {
            return targetFragment.cast(parentFragment)
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return null
    }

    open fun onShow() {

    }

    fun showMessage(message: String) {
        (activity as BaseActivity).showMessage(message)
    }

    fun showMessage(@StringRes stringId: Int) {
        (activity as BaseActivity).showMessage(stringId)
    }

    fun showMessage(applicationException: ApplicationException) {
        (activity as BaseActivity).showMessage(applicationException)
    }

    private fun shouldGoBack(): Boolean {
        return true
    }

    open fun onBackActionPerform(): Boolean {
        return true
    }

    open fun onViewClick(view: View) {

    }

    private fun setupBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (onBackActionPerform()) {
                isEnabled = false
                remove()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun handleEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= 35) {
            view?.let {
                ViewCompat.setOnApplyWindowInsetsListener(it) { view, windowInsets ->
                    val insetsSystemGestures =
                        windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
                    val insetsNavigationBars =
                        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

                    if (insetsSystemGestures.bottom == insetsNavigationBars.bottom) {
                        view.updatePadding(
                            insetsSystemGestures.left,
                            insetsSystemGestures.top,
                            insetsSystemGestures.right,
                            insetsSystemGestures.bottom
                        )
                    } else {
                        view.updatePadding(
                            0,
                            insetsSystemGestures.top,
                            0,
                            insetsSystemGestures.bottom
                        )
                    }
                    WindowInsetsCompat.CONSUMED
                }
            }
        }
    }

    fun onError(throwable: Throwable) {
        (activity as BaseActivity).onError(throwable)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * This method is used for binding view with your binding
     */
    protected abstract fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToRoot: Boolean
    ): T

    protected abstract fun bindData()
}
