package com.starter.app.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.starter.app.core.Session
import com.starter.app.data.pojo.dataclass.Event
import com.starter.app.databinding.FragmentEventBinding
import com.starter.app.ui.activity.IsolatedActivity
import com.starter.app.ui.adapter.EventAdapter
import com.starter.app.ui.adapter.UserAdapter
import com.starter.app.ui.base.BaseFragment
import com.starter.app.ui.viewmodel.EventViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EventFragment : BaseFragment<FragmentEventBinding>() {


    private lateinit var eventAdapter: EventAdapter

    private val eventViewModel: EventViewModel by viewModels()


    @Inject
    lateinit var session: Session


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToRoot: Boolean,
    ): FragmentEventBinding {
        return FragmentEventBinding.inflate(inflater, container, attachToRoot)
    }


    override fun bindData() {
        setUpAdapter()
        eventViewModel.getAllEvents()
        observeData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("TAG", "onViewCreated: ")
    }

    override fun onPause() {
        super.onPause()
        Log.e("TAG", "onPause: ")
    }

    override fun onResume() {
        super.onResume()
        eventViewModel.getAllEvents()
    }

    private fun setUpAdapter() = with(binding) {
        eventAdapter = EventAdapter({ event ->
            //Edit navigation
            val bundle = Bundle().apply {
                putInt("id", event.id)
            }
            navigator.loadActivity(IsolatedActivity::class.java, AddEventFragment::class.java)
                .addBundle(bundle).start()
        }, { event -> showDeleteConfirmationDialog(event) })

        recyclerViewUsers.apply {
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = eventAdapter
        }
    }

    private fun observeData() {
        eventViewModel.events.observe(viewLifecycleOwner) { list ->
            eventAdapter.addItem(list)
        }
    }

    override fun onBackActionPerform(): Boolean {
        return true
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showDeleteConfirmationDialog(event: Event) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Event")
        builder.setMessage("Are you sure you want to delete this event?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            // Perform delete action
            eventViewModel.deleteEvent(event)
            eventAdapter.removeItem(event)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }


}