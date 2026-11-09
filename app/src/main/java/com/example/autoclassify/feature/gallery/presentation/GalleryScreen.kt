
package com.example.autoclassify.feature.gallery.presentation

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

data class GalleryImage(
    val id: Long,
    val uri: Uri,
    val name: String,
    val isScreenshot: Boolean
)

@Composable
fun GalleryScreen(
    images: List<GalleryImage>,
    onBack: () -> Unit,
    onImportSelected: (List<GalleryImage>) -> Unit
) {
    val selectedIds = remember { mutableStateOf(setOf<Long>()) }
    val showAllImages = remember { mutableStateOf(false) }

    val visibleImages = if (showAllImages.value) {
        images
    } else {
        images.filter { it.isScreenshot }
    }

    val selectedImages = images.filter {
        it.id in selectedIds.value
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "← 뒤로",
            modifier = Modifier.clickable { onBack() },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "이미지 불러오기",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "분류할 사진을 선택해 주세요.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showAllImages.value,
                onClick = { showAllImages.value = false },
                label = { Text("스크린샷") }
            )

            FilterChip(
                selected = showAllImages.value,
                onClick = { showAllImages.value = true },
                label = { Text("전체 이미지") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "총 ${visibleImages.size}장 · ${selectedIds.value.size}장 선택",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (visibleImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showAllImages.value) {
                        "불러올 이미지가 없습니다."
                    } else {
                        "스크린샷이 없습니다.\n전체 이미지에서 확인해 보세요."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = visibleImages,
                    key = { it.id }
                ) { image ->

                    val isSelected = image.id in selectedIds.value

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedIds.value =
                                    if (isSelected) {
                                        selectedIds.value - image.id
                                    } else {
                                        selectedIds.value + image.id
                                    }
                            }
                    ) {
                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    scaleType =
                                        ImageView.ScaleType.CENTER_CROP
                                    setImageURI(image.uri)
                                }
                            },
                            update = {
                                it.setImageURI(image.uri)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onImportSelected(selectedImages) },
            enabled = selectedImages.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "선택한 사진 가져오기 (${selectedImages.size})"
            )
        }
    }
}
