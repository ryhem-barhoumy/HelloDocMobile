package com.example.hellodoc.ui.favorites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hellodoc.data.local.entity.FavoriteDoctorEntity
import com.example.hellodoc.databinding.ItemFavoriteDoctorBinding

class FavoriteDoctorAdapter(
    private val onItemClick: (FavoriteDoctorEntity) -> Unit,
    private val onItemUpdate: (FavoriteDoctorEntity) -> Unit,
    private val onItemDelete: (FavoriteDoctorEntity) -> Unit
) : RecyclerView.Adapter<FavoriteDoctorAdapter.FavoriteViewHolder>() {

    private var favorites = emptyList<FavoriteDoctorEntity>()

    fun updateList(newList: List<FavoriteDoctorEntity>) {
        favorites = newList
        notifyDataSetChanged()
    }

    inner class FavoriteViewHolder(
        private val binding: ItemFavoriteDoctorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteDoctorEntity) {
            binding.tvName.text = "Dr ${favorite.doctorName}"
            binding.tvSpecialty.text = favorite.specialty.orEmpty()
            binding.tvCity.text = favorite.city.orEmpty()
            binding.tvLabel.text = favorite.personalLabel

            binding.tvLabel.visibility =
                if (favorite.personalLabel.isBlank()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            binding.root.setOnClickListener {
                onItemClick(favorite)
            }

            binding.btnOpen.setOnClickListener {
                onItemClick(favorite)
            }

            binding.btnEdit.setOnClickListener {
                onItemUpdate(favorite)
            }

            binding.btnDelete.setOnClickListener {
                onItemDelete(favorite)
            }

            binding.root.setOnLongClickListener {
                onItemDelete(favorite)
                true
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavoriteViewHolder {
        val binding = ItemFavoriteDoctorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FavoriteViewHolder,
        position: Int
    ) {
        holder.bind(favorites[position])
    }

    override fun getItemCount(): Int {
        return favorites.size
    }
}