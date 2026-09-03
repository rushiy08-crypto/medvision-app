package com.medvision.app.data.api

import retrofit2.http.POST
import retrofit2.http.Header
import okhttp3.MultipartBody
import retrofit2.Response

interface VisionApiService {
    @POST("v1/vision:analyze")
    suspend fun analyzeImage(
        @Header("Authorization") authHeader: String,
        @Header("Content-Type") contentType: String = "multipart/form-data",
        body: MultipartBody
    ): Response<VisionAnalysisResponse>
}

data class VisionAnalysisResponse(
    val description: String,
    val objects: List<String>,
    val colors: List<String>,
    val text: String?,
    val confidence: Float
)
