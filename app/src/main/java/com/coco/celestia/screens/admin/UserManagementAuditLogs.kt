package com.coco.celestia.screens.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.BlueGradientBrush
import com.coco.celestia.ui.theme.mintsansFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserManagementAuditLogs(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(BlueGradientBrush)
            .semantics { testTag = "android:id/AuditLogsScreen" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/HeaderRow" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = { navController.navigate(Screen.AdminUserManagement.route) },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(top = 5.dp)
                    .semantics { testTag = "android:id/BackButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Audit Logs",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(top = 5.dp)
                    .semantics { testTag = "android:id/AuditLogsTitle" }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .semantics { testTag = "android:id/AuditLogsList" }
        ) {
            // Sticky Header
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(13.dp)
                        .semantics { testTag = "android:id/AuditLogsHeader" }
                ) {
                    Text(
                        text = "DATE",
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/DateHeader" },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "USER",
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/UserHeader" },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ACTION",
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/ActionHeader" },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Rows for audit logs
        }
    }
}
