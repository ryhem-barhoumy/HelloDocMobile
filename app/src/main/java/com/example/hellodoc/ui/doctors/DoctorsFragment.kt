package com.example.hellodoc.ui.doctors

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hellodoc.R
import com.example.hellodoc.databinding.FragmentDoctorsBinding
import com.example.hellodoc.viewmodel.DoctorsViewModel
import com.example.hellodoc.viewmodel.HelloDocViewModelFactory

class DoctorsFragment : Fragment() {

    private var _binding: FragmentDoctorsBinding? = null

    private val vm: DoctorsViewModel by viewModels {
        HelloDocViewModelFactory(requireActivity().application)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var delayedSearch: Runnable? = null
    private var currentQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentDoctorsBinding.inflate(inflater, parent, false)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)

        val b = requireNotNull(_binding)

        val adapter = DoctorAdapter { doctor ->
            findNavController().navigate(
                R.id.action_doctorsFragment_to_doctorDetailsFragment,
                bundleOf("doctorId" to doctor.id)
            )
        }

        b.rvDoctors.layoutManager = LinearLayoutManager(requireContext())
        b.rvDoctors.adapter = adapter

        val menuHost = requireActivity() as MenuHost

        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    val searchItem = menu.add(
                        Menu.NONE,
                        Menu.FIRST,
                        Menu.NONE,
                        "Rechercher"
                    )

                    searchItem.setIcon(android.R.drawable.ic_menu_search)

                    searchItem.setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM or
                                MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
                    )

                    val searchView = SearchView(requireContext())

                    searchView.queryHint = "Nom, ville ou spécialité"
                    searchView.maxWidth = Int.MAX_VALUE

                    searchItem.actionView = searchView

                    searchView.setOnQueryTextListener(
                        object : SearchView.OnQueryTextListener {

                            override fun onQueryTextSubmit(
                                query: String?
                            ): Boolean {
                                scheduleSearch(query.orEmpty())
                                searchView.clearFocus()
                                return true
                            }

                            override fun onQueryTextChange(
                                newText: String?
                            ): Boolean {
                                scheduleSearch(newText.orEmpty())
                                return true
                            }
                        }
                    )

                    searchItem.setOnActionExpandListener(
                        object : MenuItem.OnActionExpandListener {

                            override fun onMenuItemActionExpand(
                                item: MenuItem
                            ): Boolean = true

                            override fun onMenuItemActionCollapse(
                                item: MenuItem
                            ): Boolean {
                                scheduleSearch("")
                                return true
                            }
                        }
                    )
                }

                override fun onMenuItemSelected(
                    menuItem: MenuItem
                ): Boolean = false
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        vm.doctors.observe(viewLifecycleOwner) { stateValue ->

            b.progress.isVisible = stateValue.loading

            if (!stateValue.loading) {
                val doctors = stateValue.data.orEmpty()

                adapter.submit(doctors)

                b.tvCount.text =
                    if (stateValue.error != null) {
                        ""
                    } else {
                        "${doctors.size} résultat(s)"
                    }

                b.tvMessage.text =
                    stateValue.error
                        ?: if (doctors.isEmpty()) {
                            "Aucun médecin trouvé."
                        } else {
                            ""
                        }

                b.tvMessage.isVisible =
                    b.tvMessage.text.isNotEmpty()
            }
        }

        scheduleSearch("")
    }

    private fun scheduleSearch(query: String) {
        currentQuery = query

        delayedSearch?.let {
            handler.removeCallbacks(it)
        }

        delayedSearch = Runnable {
            vm.search(currentQuery, null, "", null)
        }.also {
            handler.postDelayed(it, 350)
        }
    }

    override fun onDestroyView() {
        delayedSearch?.let {
            handler.removeCallbacks(it)
        }

        delayedSearch = null
        _binding = null

        super.onDestroyView()
    }
}