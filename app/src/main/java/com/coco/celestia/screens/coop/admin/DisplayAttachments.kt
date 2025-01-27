package com.coco.celestia.screens.coop.admin

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coco.celestia.R
import com.coco.celestia.service.AttachFileService
import com.coco.celestia.ui.theme.*

@Composable
fun DisplayAttachments(
    requestId: String,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    attachmentType: String = "general"
) {
    var attachments by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val suffixedRequestId = when (attachmentType) {
        "pickup" -> "${requestId}_pickup"
        "refund" -> "${requestId}_refund"
        else -> requestId
    }

    LaunchedEffect(suffixedRequestId) {
        AttachFileService.fetchAttachments(suffixedRequestId) { uris ->
            attachments = uris
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        if (showTitle) {
            Text(
                text = "Attachments:",
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp),
                color = Green4
            )
        } else if (attachments.isEmpty()) {
            Text(
                text = "N/A",
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .padding(2.dp)
            )
        } else {
            attachments.forEach { uri ->
                AttachmentItem(uri = uri)
            }
        }
    }
}

@Composable
fun AttachmentItem(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fileName = uri.lastPathSegment?.let { path ->
        path.split("/").lastOrNull() ?: "File"
    } ?: "File"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Green1.copy(alpha = 0.1f))
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = uri
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "No application found to open this file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.attachfile),
            contentDescription = "Attachment",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Green1)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = fileName,
            color = Green1,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}