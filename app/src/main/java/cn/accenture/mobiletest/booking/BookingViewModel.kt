package cn.accenture.mobiletest.booking

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookingViewModel : ViewModel() {
    private val bookingService = BookingService()
    private val _bookingData = MutableStateFlow("")
    val bookingData: StateFlow<String> = _bookingData

    fun loadData() {
        bookingService.request {
            if (it == null) {
                _bookingData.value = "no data"
                false
            } else {
                _bookingData.value = it
                true
            }
        }
    }
}
