package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClientHelpOverlay(isVisible: MutableState<Boolean>) {
    if (isVisible.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
                .clickable { isVisible.value = false } // Click to dismiss
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "How to Navigate the Client Dashboard",
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(//placeholder instructions
                        text = "1. Use the menu to access various features.\n" +
                                "2. The toolbar allows quick access to notifications and settings.\n" +
                                "3. Check your orders and notifications from the dashboard.\n" +
                                "4. Reach out to support if you have any issues.",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = { isVisible.value = false }
                    ) {
                        Text("Got it")
                    }
                }
            }
        }
    }
}