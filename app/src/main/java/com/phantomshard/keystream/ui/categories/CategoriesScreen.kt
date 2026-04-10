package com.phantomshard.keystream.ui.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phantomshard.keystream.R
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.ui.common.AppToast
import com.phantomshard.keystream.ui.common.ToastMessage
import com.phantomshard.keystream.ui.services.ActionsDropdown
import com.phantomshard.keystream.ui.services.DeleteModal
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun CategoriesScreen(
    paddingValues: PaddingValues = PaddingValues(),
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var toast by remember { mutableStateOf<ToastMessage?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { event ->
            toast = event
            delay(3000)
            toast = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    CategoriesContent(
        uiState = uiState,
        paddingValues = paddingValues,
        toast = toast,
        onSearchChange = viewModel::onSearchChange,
        onRefresh = viewModel::onRefresh,
        onAddClick = viewModel::onShowAddDialog,
        onEditClick = viewModel::onShowEditDialog,
        onDeleteClick = viewModel::onShowDeleteDialog,
        onDismissDialog = viewModel::onDismissDialog,
        onDialogNameChange = viewModel::onDialogNameChange,
        onDialogDescriptionChange = viewModel::onDialogDescriptionChange,
        onConfirmAdd = viewModel::onAddCategory,
        onConfirmEdit = viewModel::onEditCategory,
        onConfirmDelete = viewModel::onDeleteCategory
    )
}

@Composable
fun CategoriesContent(
    uiState: CategoriesUiState,
    paddingValues: PaddingValues,
    toast: ToastMessage? = null,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    onDismissDialog: () -> Unit,
    onDialogNameChange: (String) -> Unit,
    onDialogDescriptionChange: (String) -> Unit,
    onConfirmAdd: () -> Unit,
    onConfirmEdit: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val refreshRotation = if (uiState.isRefreshing) {
        val infiniteTransition = rememberInfiniteTransition(label = "refreshSpin")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "refreshRotation"
        ).value
    } else {
        0f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .padding(paddingValues)
                .padding(top = 15.dp)
        ) {
            Text(
                text = "GESTION DE CATEGORIAS",
                color = Color(0xFF006FEE),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 9.dp, start = 15.dp)
            )
            Text(
                text = "Categorias",
                color = Color(0xFFFFFFFF),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 15.dp, end = 15.dp)
            )
            Text(
                text = "Crea y gestiona tus categorias personales de productos.",
                color = Color(0xFF70707B),
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 21.dp, start = 15.dp, end = 15.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 12.dp, start = 15.dp, end = 15.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                            .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x05FFFEFE))
                            .padding(vertical = 10.dp, horizontal = 13.dp)
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchChange,
                            singleLine = true,
                            textStyle = TextStyle(color = Color(0xFFFFFFFF), fontSize = 14.sp),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text("Buscar categorias...", color = Color(0xFFA1A1AA), fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                            .background(Color(0x08FFFEFE))
                            .clickable { onRefresh() }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            "Actualizar",
                            tint = Color(0xFFD3D3D8),
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(refreshRotation)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .background(Color(0xFF006FEE))
                        .clickable { onAddClick() }
                        .padding(vertical = 9.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFFFFFFFF), modifier = Modifier.padding(end = 8.dp).size(18.dp))
                    Text("Anadir Categoria", color = Color(0xFFFFFFFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 15.dp, end = 15.dp)
                    .border(1.dp, Color(0x0DFFFEFE), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x03FFFEFE))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text("NOMBRE", color = Color(0xFF51515C), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(vertical = 12.dp))
                    Text("DESCRIPCION", color = Color(0xFF51515C), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(vertical = 12.dp))
                    Text("ACCIONES", color = Color(0xFF51515C), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
                }
                HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)

                when {
                    uiState.isLoading -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(48.dp)) {
                        CircularProgressIndicator(color = Color(0xFF006FEE))
                    }
                    uiState.error != null -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(48.dp)) {
                        Text(uiState.error, color = Color(0xFFFF3B30), fontSize = 14.sp)
                    }
                    uiState.categories.isEmpty() -> EmptyCategoriesState(onAddClick = onAddClick)
                    else -> LazyColumn {
                        items(uiState.categories) { category ->
                            CategoryRow(
                                category = category,
                                onEditClick = { onEditClick(category) },
                                onDeleteClick = { onDeleteClick(category) }
                            )
                            HorizontalDivider(color = Color(0x0DFFFFFF), thickness = 1.dp)
                        }
                    }
                }
            }
        }

        AppToast(
            toast = toast,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        )
    }

    if (uiState.showAddDialog) {
        CategoryModal(
            title = "Nueva Categoria",
            confirmLabel = "Crear Categoria",
            name = uiState.dialogName,
            description = uiState.dialogDescription,
            isLoading = uiState.isDialogLoading,
            error = uiState.dialogError,
            onNameChange = onDialogNameChange,
            onDescriptionChange = onDialogDescriptionChange,
            onConfirm = onConfirmAdd,
            onDismiss = onDismissDialog
        )
    }

    if (uiState.showEditDialog) {
        CategoryModal(
            title = "Editar Categoria",
            confirmLabel = "Guardar Cambios",
            name = uiState.dialogName,
            description = uiState.dialogDescription,
            isLoading = uiState.isDialogLoading,
            error = uiState.dialogError,
            onNameChange = onDialogNameChange,
            onDescriptionChange = onDialogDescriptionChange,
            onConfirm = onConfirmEdit,
            onDismiss = onDismissDialog
        )
    }

    if (uiState.showDeleteDialog) {
        DeleteModal(
            title = "Eliminar Categoria",
            itemName = uiState.selectedCategory?.name ?: "",
            isLoading = uiState.isDialogLoading,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDialog
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(category.name, color = Color(0xFFFFFFFF), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(category.description ?: "-", color = Color(0xFF70707B), fontSize = 13.sp, modifier = Modifier.weight(1f))

        Box {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(10.dp))
                    .background(Color(0x08FFFEFE))
                    .clickable { menuExpanded = true }
            ) {
                Icon(Icons.Default.MoreVert, null, tint = Color(0xFFD3D3D8), modifier = Modifier.size(17.dp))
            }
            ActionsDropdown(
                expanded = menuExpanded,
                onDismiss = { menuExpanded = false },
                onEdit = { menuExpanded = false; onEditClick() },
                onDelete = { menuExpanded = false; onDeleteClick() }
            )
        }
    }
}

@Composable
private fun EmptyCategoriesState(onAddClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_empty_categories),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(64.dp).padding(bottom = 20.dp)
        )
        Text(
            "Aun no hay categorias",
            color = Color(0xFFFFFFFF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            "Anade categorias para organizar tus servicios.",
            color = Color(0xFF51515C),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x08FFFEFE))
                .clickable { onAddClick() }
                .padding(vertical = 7.dp, horizontal = 13.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color(0xFFD3D3D8), modifier = Modifier.padding(end = 8.dp).size(15.dp))
            Text("Anadir Categoria", color = Color(0xFFD3D3D8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryModal(
    title: String,
    confirmLabel: String,
    name: String,
    description: String,
    isLoading: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
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
                .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0A0A))
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_keystream_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp, start = 24.dp, end = 12.dp)
                        .size(32.dp)
                )
                Text(title, color = Color(0xFFFFFFFF), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color(0xFF9E9EA9),
                    modifier = Modifier.padding(end = 16.dp).size(24.dp).clickable { onDismiss() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 5.dp)
                ) {
                    Text("NOMBRE", color = Color(0xFFECEDEE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(" *", color = Color(0xFFFF3B30), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .background(Color(0x05FFFEFE))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = onNameChange,
                        singleLine = true,
                        enabled = !isLoading,
                        textStyle = TextStyle(color = Color(0xFFFFFFFF), fontSize = 14.sp),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box {
                                if (name.isEmpty()) Text("ej. Gaming, Peliculas, Musica", color = Color(0xFFA1A1AA), fontSize = 14.sp)
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("DESCRIPCION", color = Color(0xFFECEDEE), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .background(Color(0x05FFFEFE))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        enabled = !isLoading,
                        textStyle = TextStyle(color = Color(0xFFFFFFFF), fontSize = 14.sp),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box {
                                if (description.isEmpty()) Text("Descripcion opcional", color = Color(0xFFA1A1AA), fontSize = 14.sp)
                                innerTextField()
                            }
                        }
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(error, color = Color(0xFFFF3B30), fontSize = 13.sp)
                }
            }

            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 17.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .border(2.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isLoading) { onDismiss() }
                        .padding(vertical = 9.dp, horizontal = 17.dp)
                ) {
                    Text("Cancelar", color = Color(0xFF9E9EA9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF006FEE))
                        .clickable(enabled = !isLoading) { onConfirm() }
                        .padding(vertical = 9.dp, horizontal = 16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(confirmLabel, color = Color(0xFFFFFFFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoriesContentPreview() {
    MaterialTheme {
        CategoriesContent(
            uiState = CategoriesUiState(
                categories = listOf(
                    Category(
                        id = "1",
                        name = "Univeridad",
                        description = "Michael Jose Vasquez"
                    )
                )
            ),
            paddingValues = PaddingValues(),
            toast = null,
            onSearchChange = {},
            onRefresh = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onDismissDialog = {},
            onDialogNameChange = {},
            onDialogDescriptionChange = {},
            onConfirmAdd = {},
            onConfirmEdit = {},
            onConfirmDelete = {}
        )
    }
}
