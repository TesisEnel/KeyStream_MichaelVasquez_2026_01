package com.phantomshard.keystream.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

data class ToastMessage(
    val title: String,
    val message: String,
    val isError: Boolean
)

@Composable
fun AppToast(
    toast: ToastMessage?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (toast != null) {
            val accentColor = if (toast.isError) Color(0xFFFF6B81) else Color(0xFF34C759)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                    .background(Color(0xFF101012))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = if (toast.isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(18.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = toast.title,
                        color = Color(0xFFFFFFFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = toast.message,
                        color = if (toast.isError) Color(0xFFFFA7B5) else Color(0xFFA8F1B8),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppToastSuccessPreview() {
    AppToast(
        toast = ToastMessage(
            title = "Creado",
            message = "La categoría ha sido creada.",
            isError = false
        )
    )
}

@Preview
@Composable
private fun AppToastErrorPreview() {
    AppToast(
        toast = ToastMessage(
            title = "Error",
            message = "No se pudo crear la categoría.",
            isError = true
        )
    )
}
