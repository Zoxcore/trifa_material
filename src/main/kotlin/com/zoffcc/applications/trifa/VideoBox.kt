package com.zoffcc.applications.trifa

import VIDEO_BOX_BG_COLOR
import VideoStreamProcessor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun VideoBox(
    processor: VideoStreamProcessor,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VIDEO_BOX_BG_COLOR
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val bitmap = processor.composeBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Video Stream",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit // Handles aspect ratio automatically
            )
        }
    }
}
