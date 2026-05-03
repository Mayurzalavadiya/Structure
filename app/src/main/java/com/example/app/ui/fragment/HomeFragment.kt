package com.example.app.ui.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.databinding.FragmentHomeBinding
import com.example.app.ui.base.BaseFragment
import com.example.app.ui.adapter.UserAdapter
import com.example.app.ui.viewmodel.UserViewModel
import com.example.app.utils.PaginationScrollListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val userViewModel: UserViewModel by viewModels()

    private lateinit var userAdapter: UserAdapter

    private var limit = 20
    private var totalUser: Int = 0
    private var skip = 0
    private var scroll = false

    private var isSearching = false

    // Inside your fragment/activity
    private val connectivityManager by lazy {
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            super.onAvailable(network)
            // Network is available, fetch data if necessary
            loadDataIfRequired()
        }
    }


    /*override fun onStart() {
        super.onStart()
        connectivityManager.registerNetworkCallback(
            android.net.NetworkRequest.Builder().build(),
            networkCallback
        )
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onResume() {
        super.onResume()
        loadDataIfRequired() // Reload data if necessary when fragment is resumed
    }

    override fun onPause() {
        super.onPause()
        // Optionally, stop any ongoing network requests or clean up resources
        scroll = false // Stop pagination if needed
    }*/

    private fun loadDataIfRequired() {
        // Check if the app is not already loading
        if (!scroll && isNetworkAvailable(requireContext())) {
            skip = userAdapter.itemCount // Make sure skip is updated correctly
            userViewModel.getUser(limit = limit, skip = skip)
        }
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToRoot: Boolean,
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, attachToRoot)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeLiveData()
    }

    override fun bindData() {
        collectLoadingState()
        setUpAdapter()
        observeData()

        if (isNetworkAvailable(requireContext())) {
            userViewModel.getUser(limit = limit, skip = skip)
        } else {
            userViewModel.getCachedUsers()
        }

        setupSearchListener()
        scrollListener()
    }


    private fun setupSearchListener() = with(binding) {
        var searchJob: Job? = null

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(search: String?): Boolean {

                searchJob?.cancel() // Cancel previous debounce job
                searchJob = lifecycleScope.launch {
                    delay(300L)
                    isSearching = search.orEmpty().isNotBlank()
                    userAdapter.filter(search.orEmpty())
                }
                return true
            }
        })
    }

    private fun scrollListener() = with(binding) {
        recyclerViewUsers.addOnScrollListener(object :
            PaginationScrollListener(recyclerViewUsers.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                hideKeyBoard()
                if (isSearching) return
                skip += limit
                scroll = true
                userViewModel.getUser(limit = limit, skip = skip)
            }

            override fun isLastPage() = totalUser <= userAdapter.itemCount

            override fun isLoading() = scroll

        })
    }


    private fun collectLoadingState() {
        userViewModel.loadingState.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            isShowLoader(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onBackActionPerform(): Boolean {
        return true
    }


    /**
     * LiveData observers
     */
    private fun observeLiveData() {
        userViewModel.userLiveData.observe(requireActivity()) { responseBody ->
            scroll = false // ✅ Mark as done loading
            totalUser = responseBody.total!!

            if (skip == 0) {
                userAdapter.clear()
            }
            userAdapter.addItem(responseBody.users?.filterNotNull())
        }
    }

    private fun observeData() {
        userViewModel.cachedUsers.observe(viewLifecycleOwner) { list ->
            userAdapter.addItem(list)
        }
    }

    private fun setUpAdapter() = with(binding) {

        userAdapter = UserAdapter { item ->
            item.email?.let { showMessage(it) }
        }


        recyclerViewUsers.apply {
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = userAdapter
        }

    }


    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}