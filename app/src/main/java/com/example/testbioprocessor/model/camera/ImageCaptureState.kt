package com.example.testbioprocessor.model.camera

// Состояние для управления процессом съемки
data class ImageCaptureState(
    val capturedImages: List<CapturedImage> = emptyList(),
    val currentStep: Int = 1, // Текущий шаг (1-5)
    val isCaptureInProgress: Boolean = false,
    val isUploading: Boolean = false,
    val uploadMessage: String? = null
)