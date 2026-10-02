package com.uplayer.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uplayer.music.data.tag.TagEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Orange = Color(0xFFFF6B00)
private val DarkBg = Color(0xFF0F0F0F)
private val Surface2 = Color(0xFF2C2C2C)
private val TextSecondary = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagEditorScreen(
    filePath: String,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath) {
        val result = withContext(Dispatchers.IO) { TagEditor.readTags(filePath) }
        result.onSuccess { tags ->
            title = tags.title ?: ""
            artist = tags.artist ?: ""
            album = tags.album ?: ""
            genre = tags.genre ?: ""
            year = tags.year ?: ""
        }
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        "Tutup",
                        tint = Color.White
                    )
                }
                Text(
                    "Edit Info Lagu",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Orange)
            } else {
                TagField("Judul", title) { title = it }
                TagField("Artis", artist) { artist = it }
                TagField("Album", album) { album = it }
                TagField("Genre", genre) { genre = it }
                TagField("Tahun", year) { year = it }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            val result = withContext(Dispatchers.IO) {
                                TagEditor.writeTags(
                                    filePath = filePath,
                                    title = title.ifBlank { null },
                                    artist = artist.ifBlank { null },
                                    album = album.ifBlank { null },
                                    genre = genre.ifBlank { null },
                                    year = year.ifBlank { null }
                                )
                            }
                            isSaving = false
                            result.onSuccess {
                                message = "✓ Berhasil disimpan"
                                onSaved()
                            }.onFailure {
                                message = "✗ ${it.message}"
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (isSaving) "Menyimpan..." else "Simpan Perubahan",
                        fontWeight = FontWeight.Bold
                    )
                }

                message?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, color = TextSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun TagField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange,
                unfocusedBorderColor = Surface2,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
    }
}
