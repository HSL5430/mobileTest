package cn.accenture.mobiletest.booking

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookingViewModel : ViewModel() {

    private val _bookingData = MutableStateFlow("")
    val bookingData: StateFlow<String> = _bookingData

    private val mainHandler = Handler(Looper.getMainLooper())
    private val bookingService = BookingService(mainHandler) {
        if (it == null) {
            _bookingData.value = "no data"
            false
        } else {
            _bookingData.value = it
            true
        }
    }

    fun loadData() = bookingService.request()

    override fun onCleared() {
        mainHandler.removeCallbacksAndMessages(null)
    }
}
