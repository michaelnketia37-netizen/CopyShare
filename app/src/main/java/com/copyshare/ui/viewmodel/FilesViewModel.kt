package com.copyshare.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.copyshare.data.model.MediaFile
import com.copyshare.data.repository.MediaRepository
import kotlinx.coroutines.launch

class FilesViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepository = MediaRepository(application)

    private val _allMedia = MutableLiveData<List<MediaFile>>()
    val allMedia: LiveData<List<MediaFile>> = _allMedia

    private val _images = MutableLiveData<List<MediaFile>>()
    val images: LiveData<List<MediaFile>> = _images

    private val _videos = MutableLiveData<List<MediaFile>>()
    val videos: LiveData<List<MediaFile>> = _videos

    private val _audio = MutableLiveData<List<MediaFile>>()
    val audio: LiveData<List<MediaFile>> = _audio

    private val _documents = MutableLiveData<List<MediaFile>>()
    val documents: LiveData<List<MediaFile>> = _documents

    private val _apks = MutableLiveData<List<MediaFile>>()
    val apks: LiveData<List<MediaFile>> = _apks

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAllMedia()
    }

    fun loadAllMedia() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val allMediaList = mediaRepository.getAllMedia()
                _allMedia.value = allMediaList

                _images.value = allMediaList.filter { it.mediaType == MediaFile.MediaType.IMAGE }
                _videos.value = allMediaList.filter { it.mediaType == MediaFile.MediaType.VIDEO }
                _audio.value = allMediaList.filter { it.mediaType == MediaFile.MediaType.AUDIO }
                _documents.value = allMediaList.filter { it.mediaType == MediaFile.MediaType.DOCUMENT }
                _apks.value = allMediaList.filter { it.mediaType == MediaFile.MediaType.APK }

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun filterByType(mediaType: MediaFile.MediaType): List<MediaFile> {
        return _allMedia.value?.filter { it.mediaType == mediaType } ?: emptyList()
    }

    fun searchMedia(query: String): List<MediaFile> {
        return _allMedia.value?.filter {
            it.name.contains(query, ignoreCase = true)
        } ?: emptyList()
    }
}