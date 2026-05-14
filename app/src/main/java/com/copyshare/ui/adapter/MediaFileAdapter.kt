package com.copyshare.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.copyshare.databinding.ItemMediaFileBinding
import com.copyshare.data.model.MediaFile

class MediaFileAdapter(
    private val onItemClick: (MediaFile) -> Unit,
    private val onItemLongClick: (MediaFile) -> Boolean = { false }
) : ListAdapter<MediaFile, MediaFileAdapter.MediaFileViewHolder>(MediaFileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaFileViewHolder {
        val binding = ItemMediaFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MediaFileViewHolder(binding, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: MediaFileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MediaFileViewHolder(
        private val binding: ItemMediaFileBinding,
        private val onItemClick: (MediaFile) -> Unit,
        private val onItemLongClick: (MediaFile) -> Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mediaFile: MediaFile) {
            binding.apply {
                fileName.text = mediaFile.name
                fileSize.text = mediaFile.getSizeFormatted()
                fileDate.text = mediaFile.getDateFormatted()
                fileIcon.setImageResource(getIconForMediaType(mediaFile.mediaType))

                root.setOnClickListener { onItemClick(mediaFile) }
                root.setOnLongClickListener { onItemLongClick(mediaFile) }
            }
        }

        private fun getIconForMediaType(mediaType: MediaFile.MediaType): Int {
            return when (mediaType) {
                MediaFile.MediaType.IMAGE -> android.R.drawable.ic_menu_gallery
                MediaFile.MediaType.VIDEO -> android.R.drawable.ic_menu_view
                MediaFile.MediaType.AUDIO -> android.R.drawable.ic_menu_sort_by_size
                MediaFile.MediaType.DOCUMENT -> android.R.drawable.ic_menu_info_details
                MediaFile.MediaType.APK -> android.R.drawable.ic_menu_more
                MediaFile.MediaType.OTHER -> android.R.drawable.ic_menu_info_details
            }
        }
    }

    class MediaFileDiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean {
            return oldItem == newItem
        }
    }
}