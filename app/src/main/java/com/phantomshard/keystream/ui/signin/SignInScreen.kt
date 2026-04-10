package com.phantomshard.keystream.ui.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phantomshard.keystream.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: SignInViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                SignInNavigationEvent.NavigateToDashboard -> onNavigateToDashboard()
            }
        }
    }

    SignInContent(
        uiState = uiState,
        onApiKeyChange = viewModel::onApiKeyChange,
        onSignIn = viewModel::onSignIn
    )
}

@Composable
fun SignInContent(
    uiState: SignInUiState,
    onApiKeyChange: (String) -> Unit,
    onSignIn: () -> Unit
) {
    var showApiKeyHelpDialog by remember { mutableStateOf(false) }
    var showPasteFeedback by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val pasteScale by animateFloatAsState(
        targetValue = if (showPasteFeedback) 1.16f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 420f),
        label = "pasteScale"
    )
    val pasteTint by animateColorAsState(
        targetValue = if (showPasteFeedback) Color(0xFF5AC8FA) else Color(0xFF636366),
        label = "pasteTint"
    )

    LaunchedEffect(showPasteFeedback) {
        if (showPasteFeedback) {
            kotlinx.coroutines.delay(850)
            showPasteFeedback = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Iniciar Sesión 👋",
                color = Color(0xFFFFFFFF),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 11.dp)
            )
            Text(
                text = "Por favor, introduce tu api key para iniciar sesión.",
                color = Color(0xFF8E8E93),
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Api Key ",
                    color = Color(0xFFFFFFFF),
                    fontSize = 14.sp
                )
                Text(
                    text = "*",
                    color = Color(0xFFFF3B30),
                    fontSize = 14.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(vertical = 18.dp, horizontal = 17.dp)
            )
            {
                BasicTextField(
                    value = uiState.apiKey,
                    onValueChange = onApiKeyChange,
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    textStyle = TextStyle(
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.apiKey.isEmpty()) {
                                Text(
                                    text = "Introduce tu api key",
                                    color = Color(0xFF636366),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = if (showPasteFeedback) Icons.Default.Check else Icons.Default.ContentPaste,
                    contentDescription = "Pegar api key",
                    tint = pasteTint,
                    modifier = Modifier
                        .size(20.dp)
                        .scale(pasteScale)
                        .clickable(enabled = !uiState.isLoading) {
                            val text = clipboard.getText()?.text?.trim().orEmpty()
                            if (text.isNotEmpty()) {
                                onApiKeyChange(text)
                                showPasteFeedback = true
                            }
                        }
                )
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.error,
                    color = Color(0xFFFF3B30),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onSignIn,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF),
                    disabledContainerColor = Color(0xFF007AFF).copy(alpha = 0.5f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFFFFF),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        color = Color(0xFFFFFFFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿No tienes Api Key? ",
                    color = Color(0xFF8E8E93),
                    fontSize = 14.sp
                )
                Text(
                    text = "Crear una api key",
                    color = Color(0xFF007AFF),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { showApiKeyHelpDialog = true }
                )
            }
        }
    }

    if (showApiKeyHelpDialog) {
        Dialog(
            onDismissRequest = { showApiKeyHelpDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF101012))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, top = 18.dp, bottom = 12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x140A84FF))
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_keystream_logo),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Crear una api key",
                        color = Color(0xFFFFFFFF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF9E9EA9),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showApiKeyHelpDialog = false }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Para generar tu API key:",
                        color = Color(0xFFB3B3BD),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x08FFFEFE))
                            .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(14.dp))
                            .clickable {
                                uriHandler.openUri("https://keystream.phantomshard.com")
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Ir al portal de KeyStream",
                            color = Color(0xFF5AC8FA),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Luego dirigete a Ajuste > Api Keys y sigue las instrucciones indicadas alli.",
                        color = Color(0xFFFFFFFF),
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                    Text(
                        text = "Cuando la tengas, pegala aqui para iniciar sesion.",
                        color = Color(0xFF8E8E93),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                androidx.compose.material3.HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 17.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .border(2.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showApiKeyHelpDialog = false }
                            .padding(vertical = 9.dp, horizontal = 17.dp)
                    ) {
                        Text(
                            text = "Cerrar",
                            color = Color(0xFF9E9EA9),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInContentPreview() {
    MaterialTheme {
        SignInContent(
            uiState = SignInUiState(apiKey = "", isApiKeyVisible = false),
            onApiKeyChange = {},
            onSignIn = {}
        )
    }
}
