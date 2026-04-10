package com.phantomshard.keystream.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phantomshard.keystream.R
import com.phantomshard.keystream.ui.categories.CategoriesScreen
import com.phantomshard.keystream.ui.services.ServicesScreen
import org.koin.androidx.compose.koinViewModel

enum class DashboardTab { CATEGORIES, SERVICES }

@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    val viewModel: DashboardViewModel = koinViewModel()
    var selectedTab by remember { mutableStateOf(DashboardTab.CATEGORIES) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect { onLogout() }
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Scaffold(
        containerColor = Color(0xFF000000),
        bottomBar = {
            KeyStreamBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onLogoutClick = { showLogoutDialog = true }
            )
        }
    ) { paddingValues ->
        when (selectedTab) {
            DashboardTab.CATEGORIES -> CategoriesScreen(paddingValues)
            DashboardTab.SERVICES -> ServicesScreen(paddingValues)
        }
    }
}

@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                        .background(Color(0x1AFF6B81))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFFF6B81),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Cerrar sesión",
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color(0xFF9E9EA9),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onDismiss() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Esta acción cerrará tu sesión en la aplicación.",
                    color = Color(0xFFB3B3BD),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x12FF6B81))
                        .border(1.dp, Color(0x26FF6B81), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "Acción a realizar",
                            color = Color(0xFFFFA7B5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Cerrar sesión y limpiar datos locales",
                            color = Color(0xFFFFFFFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Necesitarás tu API key para volver a iniciar sesión.",
                    color = Color(0xFF7C7C86),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 17.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .border(2.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 9.dp, horizontal = 17.dp)
                ) {
                    Text("Cancelar", color = Color(0xFF9E9EA9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF04F78))
                        .clickable { onConfirm() }
                        .padding(vertical = 9.dp, horizontal = 16.dp)
                ) {
                    Text("Cerrar sesión", color = Color(0xFFFFFFFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun KeyStreamBottomNav(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0A))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                iconRes = R.drawable.ic_empty_categories,
                label = "Categorías",
                selected = selectedTab == DashboardTab.CATEGORIES,
                onClick = { onTabSelected(DashboardTab.CATEGORIES) }
            )
            BottomNavItem(
                iconRes = R.drawable.ic_empty_services,
                label = "Servicios",
                selected = selectedTab == DashboardTab.SERVICES,
                onClick = { onTabSelected(DashboardTab.SERVICES) }
            )
            BottomNavLogoutItem(onClick = onLogoutClick)
        }
    }
}

@Composable
private fun BottomNavItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF0A84FF)
    val inactiveColor = Color(0xFF8E9AAF)
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(if (selected) activeColor else inactiveColor),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) activeColor else inactiveColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun BottomNavLogoutItem(onClick: () -> Unit) {
    val inactiveColor = Color(0xFF8E9AAF)
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logout),
            contentDescription = "Cerrar sesión",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(inactiveColor),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Salir",
            color = inactiveColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyStreamBottomNavPreview() {
    MaterialTheme {
        KeyStreamBottomNav(
            selectedTab = DashboardTab.CATEGORIES,
            onTabSelected = {},
            onLogoutClick = {}
        )
    }
}
