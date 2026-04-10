package com.phantomshard.keystream.ui.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phantomshard.keystream.R
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.model.Service
import com.phantomshard.keystream.ui.common.AppToast
import com.phantomshard.keystream.ui.common.ToastMessage
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun ServicesScreen(
    paddingValues: PaddingValues = PaddingValues(),
    viewModel: ServicesViewModel = koinViewModel()
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

    ServicesContent(
        uiState = uiState,
        paddingValues = paddingValues,
        toast = toast,
        onSearchChange = viewModel::onSearchChange,
        onRefresh = viewModel::onRefresh,
        onCategoryFilterChange = viewModel::onCategoryFilterChange,
        onAddClick = viewModel::onShowAddDialog,
        onEditClick = viewModel::onShowEditDialog,
        onDeleteClick = viewModel::onShowDeleteDialog,
        onDismissDialog = viewModel::onDismissDialog,
        onDialogImageUrlChange = viewModel::onDialogImageUrlChange,
        onDialogNameChange = viewModel::onDialogNameChange,
        onDialogDescriptionChange = viewModel::onDialogDescriptionChange,
        onDialogPriceChange = viewModel::onDialogPriceChange,
        onDialogStockChange = viewModel::onDialogStockChange,
        onDialogMaxProfilesChange = viewModel::onDialogMaxProfilesChange,
        onDialogCategoryChange = viewModel::onDialogCategoryChange,
        onConfirmAdd = viewModel::onAddService,
        onConfirmEdit = viewModel::onEditService,
        onConfirmDelete = viewModel::onDeleteService
    )
}

@Composable
fun ServicesContent(
    uiState: ServicesUiState,
    paddingValues: PaddingValues,
    toast: ToastMessage? = null,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onCategoryFilterChange: (Category?) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Service) -> Unit,
    onDeleteClick: (Service) -> Unit,
    onDismissDialog: () -> Unit,
    onDialogImageUrlChange: (String) -> Unit,
    onDialogNameChange: (String) -> Unit,
    onDialogDescriptionChange: (String) -> Unit,
    onDialogPriceChange: (String) -> Unit,
    onDialogStockChange: (String) -> Unit,
    onDialogMaxProfilesChange: (String) -> Unit,
    onDialogCategoryChange: (String?) -> Unit,
    onConfirmAdd: () -> Unit,
    onConfirmEdit: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val refreshRotation = if (uiState.isRefreshing) {
        val infiniteTransition = rememberInfiniteTransition(label = "servicesRefreshSpin")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "servicesRefreshRotation"
        ).value
    } else {
        0f
    }
    val tableScrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .padding(paddingValues)
                .padding(top = 27.dp)
        ) {
            Text(
                text = "GESTIÓN DE SERVICIOS",
                color = Color(0xFF006FEE),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 9.dp, start = 15.dp)
            )
            Text(
                text = "Servicios",
                color = Color(0xFFFFFFFF),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 15.dp, end = 15.dp)
            )
            Text(
                text = "Gestiona tus servicios de streaming y su stock de cuentas.",
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
                            .padding(end = 13.dp)
                            .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x05FFFEFE))
                            .padding(vertical = 10.dp, horizontal = 13.dp)
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchChange,
                            singleLine = true,
                            textStyle = TextStyle(color = Color(0xFFFFFFFF), fontSize = 14.sp),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            "Buscar servicios...",
                                            color = Color(0xFFA1A1AA),
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    CategoryFilterDropdown(
                        categories = uiState.categories,
                        selected = uiState.selectedCategoryFilter,
                        onSelect = onCategoryFilterChange,
                        modifier = Modifier.padding(end = 12.dp)
                    )

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
                    Text("Añadir Servicio", color = Color(0xFFFFFFFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                Column(modifier = Modifier.horizontalScroll(tableScrollState)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(500.dp)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "SERVICIO",
                            color = Color(0xFF51515C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(120.dp).padding(vertical = 12.dp)
                        )
                        Text(
                            text = "CATEGORÍA",
                            color = Color(0xFF51515C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(100.dp).padding(vertical = 12.dp)
                        )
                        Text(
                            text = "PRECIO",
                            color = Color(0xFF51515C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp).padding(vertical = 12.dp)
                        )
                        Text(
                            text = "PERFILES",
                            color = Color(0xFF51515C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp).padding(vertical = 12.dp)
                        )
                        Text(
                            text = "ACCIONES",
                            color = Color(0xFF51515C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(56.dp).padding(vertical = 12.dp)
                        )
                    }
                    HorizontalDivider(
                        color = Color(0x1AFFFFFF),
                        thickness = 1.dp,
                        modifier = Modifier.width(500.dp)
                    )

                    when {
                        uiState.isLoading -> Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.width(500.dp).padding(36.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF006FEE))
                        }
                        uiState.error != null -> Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.width(500.dp).padding(36.dp)
                        ) {
                            Text(uiState.error, color = Color(0xFFFF3B30), fontSize = 14.sp)
                        }
                        uiState.services.isEmpty() -> Box(modifier = Modifier.width(500.dp)) {
                            EmptyServicesState(onAddClick = onAddClick)
                        }
                        else -> LazyColumn(modifier = Modifier.width(500.dp)) {
                            items(uiState.services) { service ->
                                ServiceRow(
                                    service = service,
                                    onEditClick = { onEditClick(service) },
                                    onDeleteClick = { onDeleteClick(service) }
                                )
                                HorizontalDivider(color = Color(0x0DFFFFFF), thickness = 1.dp)
                            }
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
        ServiceModal(
            title = "Nuevo Servicio",
            confirmLabel = "Crear Servicio",
            uiState = uiState,
            onImageUrlChange = onDialogImageUrlChange,
            onNameChange = onDialogNameChange,
            onDescriptionChange = onDialogDescriptionChange,
            onPriceChange = onDialogPriceChange,
            onStockChange = onDialogStockChange,
            onMaxProfilesChange = onDialogMaxProfilesChange,
            onCategoryChange = onDialogCategoryChange,
            onConfirm = onConfirmAdd,
            onDismiss = onDismissDialog
        )
    }

    if (uiState.showEditDialog) {
        ServiceModal(
            title = "Editar Servicio",
            confirmLabel = "Guardar Cambios",
            uiState = uiState,
            onImageUrlChange = onDialogImageUrlChange,
            onNameChange = onDialogNameChange,
            onDescriptionChange = onDialogDescriptionChange,
            onPriceChange = onDialogPriceChange,
            onStockChange = onDialogStockChange,
            onMaxProfilesChange = onDialogMaxProfilesChange,
            onCategoryChange = onDialogCategoryChange,
            onConfirm = onConfirmEdit,
            onDismiss = onDismissDialog
        )
    }

    if (uiState.showDeleteDialog) {
        DeleteModal(
            title = "Eliminar Servicio",
            itemName = uiState.selectedService?.name ?: "",
            isLoading = uiState.isDialogLoading,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDialog
        )
    }
}

@Composable
private fun CategoryFilterDropdown(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var filterQuery by remember { mutableStateOf("") }
    val label = if (selected == null) "TODAS" else selected.name.uppercase()
    val filteredCategories = categories.filter { it.name.contains(filterQuery.trim(), ignoreCase = true) }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x05FFFEFE))
                .clickable { expanded = !expanded }
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFF9E9EA9),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 4.dp)
            )
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null,
                tint = Color(0xFF9E9EA9),
                modifier = Modifier.size(14.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                filterQuery = ""
            },
            containerColor = Color(0xEE111113),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 12.dp,
            tonalElevation = 0.dp,
            modifier = Modifier
                .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(16.dp))
                .background(Color(0xEE111113), RoundedCornerShape(16.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x08FFFEFE))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Search, null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color(0xFFFFFFFF), fontSize = 13.sp),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (filterQuery.isEmpty()) {
                                Text("Buscar categoria...", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                            }
                            innerTextField()
                        }
                    }
                )
            }

            HorizontalDivider(color = Color(0x14FFFFFF), thickness = 1.dp, modifier = Modifier.padding(horizontal = 12.dp))

            Column(
                modifier = Modifier
                    .heightIn(max = 260.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val allSelected = selected == null
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Todas",
                                color = if (allSelected) Color(0xFFFFFFFF) else Color(0xFF9E9EA9),
                                fontSize = 13.sp,
                                fontWeight = if (allSelected) FontWeight.Medium else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (allSelected) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF006FEE), modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    modifier = Modifier.background(if (allSelected) Color(0xFF1D3A6E) else Color.Transparent),
                    onClick = {
                        onSelect(null)
                        expanded = false
                        filterQuery = ""
                    }
                )
                HorizontalDivider(color = Color(0x14FFFFFF), thickness = 1.dp, modifier = Modifier.padding(horizontal = 12.dp))
                filteredCategories.forEach { category ->
                    val isSelected = selected?.id == category.id
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    category.name,
                                    color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF9E9EA9),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF006FEE), modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        modifier = Modifier.background(if (isSelected) Color(0xFF1D3A6E) else Color.Transparent),
                        onClick = {
                            onSelect(category)
                            expanded = false
                            filterQuery = ""
                        }
                    )
                }
                if (filteredCategories.isEmpty()) {
                    Text(
                        text = "No se encontraron categorias.",
                        color = Color(0xFF70707B),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteModal(
    title: String,
    itemName: String,
    isLoading: Boolean,
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
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF6B81),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
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
                    text = "Esta accion eliminara el elemento de forma permanente.",
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
                            text = "Elemento a eliminar",
                            color = Color(0xFFFFA7B5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = itemName,
                            color = Color(0xFFFFFFFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Esta accion no se puede deshacer.",
                    color = Color(0xFF7C7C86),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

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
                        .clickable(enabled = !isLoading) { onDismiss() }
                        .padding(vertical = 9.dp, horizontal = 17.dp)
                ) {
                    Text("Cancelar", color = Color(0xFF9E9EA9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF04F78))
                        .clickable(enabled = !isLoading) { onConfirm() }
                        .padding(vertical = 9.dp, horizontal = 16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Eliminar", color = Color(0xFFFFFFFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceModal(
    title: String,
    confirmLabel: String,
    uiState: ServicesUiState,
    onImageUrlChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onMaxProfilesChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
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
                Text(
                    title,
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color(0xFF9E9EA9),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp)
                        .clickable { onDismiss() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                ModalLabel("URL DE LA IMAGEN")
                ModalTextField(
                    value = uiState.dialogImageUrl,
                    onValueChange = onImageUrlChange,
                    placeholder = "https://ejemplo.com/logo.png",
                    enabled = !uiState.isDialogLoading,
                    leadingIcon = {
                        Icon(Icons.Default.Link, null, tint = Color(0xFFA1A1AA), modifier = Modifier.padding(end = 6.dp).size(16.dp))
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 5.dp)
                ) {
                    Text("NOMBRE DEL SERVICIO", color = Color(0xFFECEDEE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(" *", color = Color(0xFFFF3B30), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                ModalTextField(
                    value = uiState.dialogName,
                    onValueChange = onNameChange,
                    placeholder = "ej. Netflix Premium, Spotify...",
                    enabled = !uiState.isDialogLoading,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ModalLabel("CATEGORÍA", color = Color(0xFF70707B))
                var categoryExpanded by remember { mutableStateOf(false) }
                var categoryFilterQuery by remember { mutableStateOf("") }
                val selectedCategoryName = uiState.categories.firstOrNull { it.id == uiState.dialogCategoryId }?.name
                    ?: "Selecciona una categoria"
                val filteredModalCategories = uiState.categories.filter {
                    it.name.contains(categoryFilterQuery.trim(), ignoreCase = true)
                }
                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                            .background(Color(0x05FFFEFE))
                            .clickable { categoryExpanded = true }
                            .padding(vertical = 12.dp, horizontal = 13.dp)
                    ) {
                        Text(
                            selectedCategoryName,
                            color = Color(0xFF70707B),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF70707B), modifier = Modifier.padding(start = 10.dp).size(14.dp))
                    }
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = {
                            categoryExpanded = false
                            categoryFilterQuery = ""
                        },
                        containerColor = Color(0xEE111113),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 12.dp,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(16.dp))
                            .background(Color(0xEE111113), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x08FFFEFE))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = categoryFilterQuery,
                                onValueChange = { categoryFilterQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(color = Color(0xFFFFFFFF), fontSize = 13.sp),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (categoryFilterQuery.isEmpty()) {
                                            Text("Buscar categoria...", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        HorizontalDivider(color = Color(0x14FFFFFF), thickness = 1.dp, modifier = Modifier.padding(horizontal = 12.dp))
                        Column(
                            modifier = Modifier
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            filteredModalCategories.forEach { cat ->
                                val isSelected = uiState.dialogCategoryId == cat.id
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                cat.name,
                                                color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF9E9EA9),
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, null, tint = Color(0xFF006FEE), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    },
                                    modifier = Modifier.background(if (isSelected) Color(0xFF1D3A6E) else Color.Transparent),
                                    onClick = {
                                        onCategoryChange(cat.id)
                                        categoryExpanded = false
                                        categoryFilterQuery = ""
                                    }
                                )
                            }
                            if (uiState.categories.isEmpty()) {
                                Text(
                                    text = "No hay categorias disponibles.",
                                    color = Color(0xFF70707B),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }

                ModalLabel("DESCRIPCIÓN")
                ModalTextField(
                    value = uiState.dialogDescription,
                    onValueChange = onDescriptionChange,
                    placeholder = "Descripción opcional",
                    enabled = !uiState.isDialogLoading,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        ModalLabel("PRECIO (USD)")
                        ModalTextField(
                            value = uiState.dialogPrice,
                            onValueChange = onPriceChange,
                            placeholder = "0.00",
                            enabled = !uiState.isDialogLoading,
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ModalLabel("PERFILES MÁXIMOS")
                        ModalTextField(
                            value = uiState.dialogMaxProfiles,
                            onValueChange = onMaxProfilesChange,
                            placeholder = "1",
                            enabled = !uiState.isDialogLoading,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                if (uiState.dialogError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(uiState.dialogError, color = Color(0xFFFF3B30), fontSize = 13.sp)
                }
            }

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
                        .clickable(enabled = !uiState.isDialogLoading) { onDismiss() }
                        .padding(vertical = 9.dp, horizontal = 17.dp)
                ) {
                    Text("Cancelar", color = Color(0xFF9E9EA9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF006FEE))
                        .clickable(enabled = !uiState.isDialogLoading) { onConfirm() }
                        .padding(vertical = 9.dp, horizontal = 16.dp)
                ) {
                    if (uiState.isDialogLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(confirmLabel, color = Color(0xFFFFFFFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
private fun ModalLabel(text: String, color: Color = Color(0xFFECEDEE)) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}

@Composable
private fun ModalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .background(Color(0x05FFFEFE))
            .padding(12.dp)
    ) {
        leadingIcon?.invoke()
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            enabled = enabled,
            textStyle = TextStyle(color = Color(0xFFFFFFFF), fontSize = 14.sp),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) Text(placeholder, color = Color(0xFFA1A1AA), fontSize = 14.sp)
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun ServiceRow(
    service: Service,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(service.name, color = Color(0xFFFFFFFF), fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(120.dp))
        Text(service.category?.name ?: "—", color = Color(0xFF70707B), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(100.dp))
        Text("$%.2f".format(service.price), color = Color(0xFFFFFFFF), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(70.dp))
        Text("${service.maxProfiles}", color = Color(0xFFFFFFFF), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(70.dp))

        Box(
            modifier = Modifier
                .width(64.dp)
                .padding(end = 6.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
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
fun ActionsDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = Color(0xEE111113),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        modifier = Modifier
            .border(1.dp, Color(0x1AFFFEFE), RoundedCornerShape(16.dp))
            .background(Color(0xEE111113), RoundedCornerShape(16.dp))
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x140A84FF))
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF8CC8FF), modifier = Modifier.size(15.dp))
                    }
                    Text("Editar", color = Color(0xFFFFFFFF), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            },
            colors = androidx.compose.material3.MenuDefaults.itemColors(
                textColor = Color(0xFFFFFFFF)
            ),
            onClick = onEdit
        )
        HorizontalDivider(color = Color(0x14FFFFFF), thickness = 1.dp, modifier = Modifier.padding(horizontal = 12.dp))
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x14FF3B5C))
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B81), modifier = Modifier.size(15.dp))
                    }
                    Text("Eliminar", color = Color(0xFFFFC8D1), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            },
            colors = androidx.compose.material3.MenuDefaults.itemColors(
                textColor = Color(0xFFFFC8D1)
            ),
            onClick = onDelete
        )
    }
}

@Composable
private fun EmptyServicesState(onAddClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_empty_services),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color(0xFF3A3A3E)),
            modifier = Modifier.size(64.dp).padding(bottom = 20.dp)
        )
        Text(
            "Aún no hay servicios",
            color = Color(0xFFFFFFFF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            "Añade tu primer servicio para empezar a gestionar el acceso.",
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
            Text("Añadir Servicio", color = Color(0xFFD3D3D8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ServicesContentPreview() {
    MaterialTheme {
        ServicesContent(
            uiState = ServicesUiState(),
            paddingValues = PaddingValues(),
            toast = null,
            onSearchChange = {}, onRefresh = {}, onCategoryFilterChange = {},
            onAddClick = {}, onEditClick = {}, onDeleteClick = {}, onDismissDialog = {},
            onDialogImageUrlChange = {}, onDialogNameChange = {}, onDialogDescriptionChange = {},
            onDialogPriceChange = {}, onDialogStockChange = {}, onDialogMaxProfilesChange = {},
            onDialogCategoryChange = {}, onConfirmAdd = {}, onConfirmEdit = {}, onConfirmDelete = {}
        )
    }
}
