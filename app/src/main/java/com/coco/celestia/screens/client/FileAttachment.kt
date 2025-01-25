package com.coco.celestia.screens.client

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.coco.celestia.R
import com.coco.celestia.service.AttachFileService
import com.coco.celestia.ui.theme.*

@Composable
fun FileAttachment(
    requestId: String,
    selectedFiles: List<Uri>,
    onFilesSelected: (List<Uri>) -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    onUploadComplete: (Boolean) -> Unit,
    onRemoveFile: (Uri) -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        uris?.let { newUris ->
            if (newUris.size + selectedFiles.size > 5) {
                Toast.makeText(
                    context,
                    "Maximum 5 files allowed",
                    Toast.LENGTH_SHORT
                ).show()
                return@let
            }

            val maxFileSize = 5 * 1024 * 1024 // 5MB
            val oversizedFiles = newUris.filter { uri ->
                context.contentResolver.openInputStream(uri)?.use {
                    it.available() > maxFileSize
                } ?: false
            }

            if (oversizedFiles.isNotEmpty()) {
                Toast.makeText(
                    context,
                    "Some files exceed 5MB limit",
                    Toast.LENGTH_SHORT
                ).show()
                return@let
            }

            val updatedFiles = (selectedFiles + newUris).distinct()
            onFilesSelected(updatedFiles)

            newUris.forEach { uri ->
                val fileName = AttachFileService.getFileName(uri)
                AttachFileService.uploadAttachment(
                    requestId = requestId,
                    fileUri = uri,
                    fileName = fileName
                ) { success ->
                    if (!success) {
                        Toast.makeText(
                            context,
                            "Failed to upload $fileName",
                            Toast.LENGTH_SHORT
                        ).show()
                        onRemoveFile(uri)
                    }
                }
            }
        }
    }

    LaunchedEffect(requestId) {
        AttachFileService.fetchAttachments(requestId) { uris ->
            if (uris.isNotEmpty()) {
                onFilesSelected(uris)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White1),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            selectedFiles.forEach { uri ->
                Row(
                    modifier = Modifier
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val iconResource = when {
                            isImageFile(uri.toString()) -> R.drawable.image
                            isPdfFile(uri.toString()) -> R.drawable.pdf
                            else -> R.drawable.attachfile
                        }

                        Icon(
                            painter = painterResource(id = iconResource),
                            contentDescription = "File Type Icon",
                            tint = Green1,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AttachFileService.getFileName(uri),
                            color = Green1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = {
                            val fileName = AttachFileService.getFileName(uri)
                            AttachFileService.deleteAttachment(
                                requestId = requestId,
                                fileName = fileName
                            ) { success ->
                                if (success) {
                                    onRemoveFile(uri)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to delete $fileName",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove file",
                            tint = Green1
                        )
                    }
                }
            }

            if (isUploading) {
                LinearProgressIndicator(
                    progress = uploadProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            if (selectedFiles.size < 5) {
                Button(
                    onClick = { launcher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green4,
                        contentColor = Green1
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "Attach file",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach Files")
                    }
                }
            }

            Text(
                text = "Max 5 files, 5MB each (Images, PDFs, Documents)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun isImageFile(path: String): Boolean {
    val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp")
    return imageExtensions.any { path.lowercase().endsWith(it) }
}

private fun isPdfFile(path: String): Boolean {
    return path.lowercase().endsWith("pdf")
}