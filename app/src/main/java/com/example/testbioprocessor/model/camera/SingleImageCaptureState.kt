package com.example.testbioprocessor.model.camera

// Состояние для управления процессом съемки
data class SingleImageCaptureState(
    val capturedImage: CapturedImage? = null,
    val isLoaded: Boolean = false,
    val uploadMessage: String? = null
)