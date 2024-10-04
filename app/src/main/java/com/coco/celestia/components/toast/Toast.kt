package com.coco.celestia.components.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ToastStatus {
    SUCCESSFUL,
    FAILED,
    WARNING,
    INFO
}

@Preview
@Composable
fun Toast_Preview() {
    val offlineMessage = "You're offline. Please check your internet connection."
    val onlineMessage = "Online!"
    Column {
        Toast(message = offlineMessage, status = ToastStatus.FAILED, visibility = true)
        Toast(message = onlineMessage, status = ToastStatus.SUCCESSFUL, visibility = true)
    }
}

@Composable
fun Toast(message: String, status: ToastStatus, visibility: Boolean) {
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            ToastStatus.SUCCESSFUL -> Color(0xFF00bb77)
            ToastStatus.FAILED -> Color(0xFFe84b3d)
            ToastStatus.WARNING -> Color(0xFFff9900)
            ToastStatus.INFO -> Color.White
        },
        animationSpec = tween(durationMillis = 300, easing = { it }),
        label = "ToastColorAnimation"
    )
    AnimatedVisibility(
        visible = visibility,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(350.dp)
                        .height(40.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor),
                    ) {
                        Text(
                            text = message,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .padding(10.dp, 0.dp, 10.dp, 0.dp)
                        )
                    }
                }
            }
        }
    }

}