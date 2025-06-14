package com.pasundane_budget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pasundane_budget.data.local.db.entities.CategoryEntity
import com.pasundane_budget.viewmodel.CategoryViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog

@Composable
fun CategoryScreen(
    userId: Int,
    apiToken: String,
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by categoryViewModel.uiState.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var editName by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        categoryViewModel.syncAndLoadCategories()
    }

    if (showEditDialog && editingCategory != null) {
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Surface(shape = MaterialTheme.shapes.medium, shadowElevation = 4.dp) { // disini
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Edit Kategori", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Kategori") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (editName.isNotBlank() && editingCategory != null) {
                                categoryViewModel.updateCategory(apiToken, userId, editingCategory!!.id, editName.trim())
                                showEditDialog = false
                                editingCategory = null
                            }
                        }) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Kategori", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                label = { Text("Tambah Kategori Baru") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (newCategoryName.isNotBlank()) {
                        categoryViewModel.addCategory(apiToken, userId, newCategoryName.trim())
                        newCategoryName = ""
                        errorMsg = null
                    } else {
                        errorMsg = "Nama kategori tidak boleh kosong"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tambah")
            }

            errorMsg?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.error != null) {
                Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn {
                    items(uiState.categories) { category ->
                        CategoryItem(
                            category,
                            onEdit = {
                                editingCategory = category
                                editName = category.name
                                showEditDialog = true
                            },
                            onDelete = {
                                categoryViewModel.deleteCategory(apiToken, userId, category.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}