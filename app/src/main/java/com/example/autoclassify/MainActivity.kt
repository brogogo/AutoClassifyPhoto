
package com.example.autoclassify

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.autoclassify.feature.gallery.presentation.GalleryImage
import com.example.autoclassify.feature.gallery.presentation.GalleryScreen
import com.example.autoclassify.feature.home.presentation.HomeScreen
import com.example.autoclassify.ui.theme.AutoClassifyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.autoclassify.feature.classification.presentation.ClassificationScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val images = remember {
                mutableStateOf<List<GalleryImage>>(emptyList())
            }

            val hasLoadedImages = rememberSaveable {
                mutableStateOf(false)
            }

            val showGallery = rememberSaveable {
                mutableStateOf(false)
            }

            // 분류할 이미지 목록
            val selectedImages = remember {
                mutableStateOf<List<GalleryImage>>(emptyList())
            }

            // 분류 화면 표시 여부
            val showClassification = rememberSaveable {
                mutableStateOf(false)
            }

            // 갤러리 이미지 조회
            val loadImages: () -> Unit = {
                lifecycleScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        queryImages()
                    }

                    images.value = result
                    hasLoadedImages.value = true
                    showGallery.value = true
                }
            }

            // 권한 요청 결과 처리
            val permissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    if (hasImagePermission()) {
                        loadImages()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "사진 접근 권한이 필요합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            AutoClassifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showClassification.value) {

                        ClassificationScreen(
                            images = selectedImages.value,
                            processedCount = 0,
                            onCancel = {
                                selectedImages.value = emptyList()
                                showClassification.value = false
                            }
                        )

                    } else if (showGallery.value) {

                        GalleryScreen(
                            images = images.value,
                            onBack = {
                                showGallery.value = false
                            },
                            onImportSelected = { selected ->

                                // 선택한 이미지 저장
                                selectedImages.value = selected

                                // 갤러리 닫기
                                showGallery.value = false

                                // 분류 화면 열기
                                showClassification.value = true
                            }
                        )

                    } else {

                        HomeScreen(
                            imageCount = if (hasLoadedImages.value) {
                                images.value.size
                            } else {
                                null
                            },
                            unclassifiedCount = if (hasLoadedImages.value) {
                                images.value.size
                            } else {
                                null
                            },
                            onImportClick = {
                                if (hasImagePermission()) {
                                    loadImages()
                                } else {
                                    permissionLauncher.launch(
                                        imagePermissionsToRequest()
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // 현재 사진 접근 권한 확인
    private fun hasImagePermission(): Boolean {
        return imagePermissionsToRequest().any { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Android 버전별 권한 목록
    private fun imagePermissionsToRequest(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )

            Build.VERSION.SDK_INT >= 33 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )

            else -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    // MediaStore에서 이미지 목록 조회
    private fun queryImages(): List<GalleryImage> {

        val projection = mutableListOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        if (Build.VERSION.SDK_INT >= 29) {
            projection.add(MediaStore.Images.Media.RELATIVE_PATH)
        } else {
            projection.add(MediaStore.Images.Media.DATA)
        }

        val pathColumn = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Images.Media.RELATIVE_PATH
        } else {
            MediaStore.Images.Media.DATA
        }

        val result = mutableListOf<GalleryImage>()

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection.toTypedArray(),
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {

            val idIndex = it.getColumnIndexOrThrow(
                MediaStore.Images.Media._ID
            )

            val nameIndex = it.getColumnIndexOrThrow(
                MediaStore.Images.Media.DISPLAY_NAME
            )

            val bucketIndex = it.getColumnIndexOrThrow(
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )

            val pathIndex = it.getColumnIndexOrThrow(pathColumn)

            while (it.moveToNext()) {

                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex) ?: ""
                val bucket = it.getString(bucketIndex) ?: ""
                val path = it.getString(pathIndex) ?: ""

                val isScreenshot =
                    bucket.contains("screenshot", ignoreCase = true) ||
                            bucket.contains("스크린샷") ||
                            bucket.contains("화면 캡처") ||
                            path.contains("screenshot", ignoreCase = true) ||
                            path.contains("스크린샷") ||
                            path.contains("화면 캡처") ||
                            name.startsWith("Screenshot", ignoreCase = true) ||
                            name.startsWith("스크린샷")

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                result.add(
                    GalleryImage(
                        id = id,
                        uri = uri,
                        name = name,
                        isScreenshot = isScreenshot
                    )
                )
            }
        }

        return result
    }
}
