package com.example.hellodoc.ui.favorites

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hellodoc.R
import com.example.hellodoc.data.local.entity.FavoriteDoctorEntity
import com.example.hellodoc.databinding.DialogEditFavoriteBinding
import com.example.hellodoc.databinding.FragmentFavoritesBinding
import com.example.hellodoc.viewmodel.FavoritesViewModel
import com.example.hellodoc.viewmodel.HelloDocViewModelFactory

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private var currentDialog: AlertDialog? = null

    private val vm: FavoritesViewModel by activityViewModels {
        HelloDocViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(
            inflater, container, false
        )
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val binding = requireNotNull(_binding)
        vm.setSearchQuery("")

        val adapter = FavoriteDoctorAdapter(
            onItemClick = { favorite ->
                findNavController().navigate(
                    R.id.doctorDetailsFragment,
                    bundleOf("doctorId" to favorite.doctorId)
                )
            },
            onItemUpdate = { favorite ->
                showEditDialog(favorite)
            },
            onItemDelete = { favorite ->
                showDeleteDialog(favorite)
            }
        )

        binding.rvFavorites.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter

        vm.favorites.observe(viewLifecycleOwner) { favorites ->
            adapter.updateList(favorites)

            binding.tvCount.text = "${favorites.size} favori(s)"

            binding.tvEmpty.visibility =
                if (favorites.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.busy.observe(viewLifecycleOwner) { busy ->
            binding.progress.visibility =
                if (busy) View.VISIBLE else View.GONE
        }

        vm.message.observe(viewLifecycleOwner) { event ->
            event.consume()?.let { message ->
                Toast.makeText(
                    requireContext(), message, Toast.LENGTH_SHORT
                ).show()
            }
        }

        vm.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(
                    requireContext(), error, Toast.LENGTH_SHORT
                ).show()
            }
        }

        val menuHost = requireActivity() as MenuHost

        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menu_favorites, menu)

                    val searchItem = menu.findItem(
                        R.id.action_search_favorites
                    )
                    val searchView = searchItem.actionView as SearchView

                    searchView.queryHint =
                        "Nom, spécialité, ville ou libellé"

                    searchView.setOnQueryTextListener(
                        object : SearchView.OnQueryTextListener {

                            override fun onQueryTextSubmit(
                                query: String?
                            ): Boolean {
                                searchView.clearFocus()
                                return true
                            }

                            override fun onQueryTextChange(
                                newText: String?
                            ): Boolean {
                                val query = newText.orEmpty()

                                binding.tvEmpty.text =
                                    if (query.isBlank()) {
                                        "Aucun médecin favori pour le moment."
                                    } else {
                                        "Aucun favori ne correspond à la recherche."
                                    }

                                vm.setSearchQuery(query)
                                return true
                            }
                        }
                    )
                }

                override fun onMenuItemSelected(
                    menuItem: MenuItem
                ): Boolean {
                    return false
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    private fun showEditDialog(favorite: FavoriteDoctorEntity) {
        if (vm.busy.value == true) return

        val dialogBinding = DialogEditFavoriteBinding.inflate(
            layoutInflater
        )
        dialogBinding.etLabel.setText(favorite.personalLabel)

        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(requireContext())
            .setTitle("Modifier le libellé")
            .setView(dialogBinding.root)
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Enregistrer") { _, _ ->
                vm.updateLabel(
                    favorite,
                    dialogBinding.etLabel.text.toString()
                )
            }
            .show()
    }

    private fun showDeleteDialog(favorite: FavoriteDoctorEntity) {
        if (vm.busy.value == true) return

        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(requireContext())
            .setTitle("Retirer ce favori ?")
            .setMessage("Retirer Dr ${favorite.doctorName} des favoris ?")
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Retirer") { _, _ ->
                vm.delete(favorite)
            }
            .show()
    }

    override fun onDestroyView() {
        currentDialog?.dismiss()
        currentDialog = null
        _binding?.rvFavorites?.adapter = null
        _binding = null
        super.onDestroyView()
    }
}