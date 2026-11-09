
package com.example.autoclassify.feature.classification.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import com.example.autoclassify.feature.gallery.presentation.GalleryImage

@Composable
fun ClassificationScreen(
    images: List<GalleryImage>,
    processedCount: Int = 0,
    onCancel: () -> Unit
) {
    val totalCount = images.size
    val progress = if (totalCount > 0) {
        processedCount.toFloat() / totalCount
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE5E1))
            .padding(20.dp)
    ) {
        Text(
            text = "이미지 분류",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF292929)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "선택한 이미지를 분석하고 있어요.",
            fontSize = 14.sp,
            color = Color(0xFF777777)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "분류 진행률",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "$processedCount / $totalCount",
                        color = Color(0xFFE98B83),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color(0xFFE98B83),
                    trackColor = Color(0xFFFFE5E1)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (processedCount >= totalCount && totalCount > 0) {
                        "분류가 완료되었습니다."
                    } else {
                        "이미지를 분석하고 있습니다..."
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF777777)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "선택한 이미지",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(images, key = { it.id }) { image ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    AndroidView(
                        factory = { context ->
                            ImageView(context).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                setImageURI(image.uri)
                            }
                        },
                        update = {
                            it.setImageURI(image.uri)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("분류 취소")
        }
    }
}
