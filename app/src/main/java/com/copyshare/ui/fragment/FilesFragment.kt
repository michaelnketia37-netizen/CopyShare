package com.copyshare.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.copyshare.databinding.FragmentFilesBinding
import com.copyshare.ui.adapter.MediaFileAdapter
import com.copyshare.ui.viewmodel.FilesViewModel
import com.copyshare.data.model.MediaFile
import com.google.android.material.chip.Chip

class FilesFragment : Fragment() {

    private lateinit var binding: FragmentFilesBinding
    private val viewModel: FilesViewModel by viewModels()
    private lateinit var mediaAdapter: MediaFileAdapter
    private var currentFilter = MediaFile.MediaType.IMAGE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChipFilters()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaFileAdapter(
            onItemClick = { mediaFile -> openFile(mediaFile) },
            onItemLongClick = { mediaFile -> showFileOptions(mediaFile) }
        )

        binding.filesRecyclerView.apply {
            adapter = mediaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupChipFilters() {
        binding.apply {
            chipAll.setOnClickListener { filterMediaType(null) }
            chipImages.setOnClickListener { filterMediaType(MediaFile.MediaType.IMAGE) }
            chipVideos.setOnClickListener { filterMediaType(MediaFile.MediaType.VIDEO) }
            chipAudio.setOnClickListener { filterMediaType(MediaFile.MediaType.AUDIO) }
            chipDocuments.setOnClickListener { filterMediaType(MediaFile.MediaType.DOCUMENT) }
            chipApks.setOnClickListener { filterMediaType(MediaFile.MediaType.APK) }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.emptyStateText.apply {
                    visibility = View.VISIBLE
                    text = "Error: $error"
                }
            }
        }

        viewModel.images.observe(viewLifecycleOwner) { images ->
            if (currentFilter == MediaFile.MediaType.IMAGE) {
                updateAdapter(images)
            }
        }

        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            if (currentFilter == MediaFile.MediaType.VIDEO) {
                updateAdapter(videos)
            }
        }

        viewModel.audio.observe(viewLifecycleOwner) { audio ->
            if (currentFilter == MediaFile.MediaType.AUDIO) {
                updateAdapter(audio)
            }
        }

        viewModel.documents.observe(viewLifecycleOwner) { documents ->
            if (currentFilter == MediaFile.MediaType.DOCUMENT) {
                updateAdapter(documents)
            }
        }

        viewModel.apks.observe(viewLifecycleOwner) { apks ->
            if (currentFilter == MediaFile.MediaType.APK) {
                updateAdapter(apks)
            }
        }

        viewModel.allMedia.observe(viewLifecycleOwner) { allMedia ->
            if (currentFilter == null) {
                updateAdapter(allMedia)
            }
        }
    }

    private fun filterMediaType(mediaType: MediaFile.MediaType?) {
        currentFilter = mediaType ?: MediaFile.MediaType.IMAGE
        val filtered = if (mediaType == null) {
            viewModel.allMedia.value ?: emptyList()
        } else {
            viewModel.filterByType(mediaType)
        }
        updateAdapter(filtered)
    }

    private fun updateAdapter(mediaList: List<MediaFile>) {
        binding.emptyStateText.visibility = if (mediaList.isEmpty()) View.VISIBLE else View.GONE
        mediaAdapter.submitList(mediaList)
    }

    private fun openFile(mediaFile: MediaFile) {
        // TODO: Implement file opening logic
        // This can launch appropriate app based on file type
    }

    private fun showFileOptions(mediaFile: MediaFile): Boolean {
        // TODO: Show bottom sheet or dialog with options:
        // - Share
        // - Open
        // - Delete
        // - Properties
        return true
    }
}