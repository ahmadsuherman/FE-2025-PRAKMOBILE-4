package com.pasundane_budget.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.pasundane_budget.data.local.db.entities.CategoryEntity
import com.pasundane_budget.data.local.db.entities.TransactionEntity
import com.pasundane_budget.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    userId: Int,
    apiToken: String,
    categories: List<CategoryEntity>,
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by transactionViewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Form fields
    var categoryId by remember { mutableStateOf(0) }
    var type by remember { mutableStateOf("in") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var expanded by remember { mutableStateOf(false) }
    var selectedCategoryName by remember { mutableStateOf("") }

    LaunchedEffect(categories) {
        Log.d("TransactionScreen", "Jumlah kategori: ${categories.size}")
        categories.forEach { cat ->
            Log.d("TransactionScreen", "Kategori: ${cat.id} - ${cat.name}")
        }
    }

    LaunchedEffect(editingTransaction, categories) {
        editingTransaction?.let { tx ->
            val selectedCat = categories.find { it.id == tx.categoryId }
            selectedCategoryName = selectedCat?.name ?: ""
            categoryId = tx.categoryId
            type = tx.type
            amount = tx.amount.toString()
            date = tx.date
        } ?: run {
            // Reset form jika tambah transaksi baru
            selectedCategoryName = ""
            categoryId = 0
            type = "in"
            amount = ""
            date = System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) {
        transactionViewModel.syncAndLoadTransactions()
    }

    if (showDialog) {
        Dialog(onDismissRequest = {
            showDialog = false
            editingTransaction = null
            errorMsg = null
        }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingTransaction == null) "Tambah Transaksi" else "Edit Transaksi",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CategoryRadioGroup(
                        categories = categories,
                        selectedCategoryId = categoryId,
                        onCategorySelected = { id ->
                            categoryId = id
                            selectedCategoryName = categories.find { it.id == id }?.name ?: ""
                        }
                    )


                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tipe",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row {
                        listOf("in", "out").forEach { t ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                RadioButton(selected = (type == t), onClick = { type = t })
                                Text(text = t.uppercase())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    OutlinedTextField(
                        value = sdf.format(Date(date)),
                        onValueChange = {
                            try {
                                date = sdf.parse(it)?.time ?: date
                            } catch (_: Exception) {}
                        },
                        label = { Text("Date (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMsg != null) {
                        Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = {
                            showDialog = false
                            editingTransaction = null
                            errorMsg = null
                        }) {
                            Text("Batal")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (categoryId <= 0) {
                                errorMsg = "Pilih kategori terlebih dahulu"
                                return@Button
                            }
                            if (amt == null || amt <= 0) {
                                errorMsg = "Amount harus angka positif"
                                return@Button
                            }
                            errorMsg = null
                            if (editingTransaction == null) {
                                transactionViewModel.addTransaction(apiToken, userId, categoryId, type, amt, date)
                            } else {
                                transactionViewModel.updateTransaction(apiToken, userId, editingTransaction!!.id, categoryId, type, amt, date)
                            }
                            showDialog = false
                            editingTransaction = null
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
            Text(text = "Transaksi", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    Log.d("TransactionScreen", "Klik Tambah Transaksi - Kategori size: ${categories.size}")
                    if (categories.isNotEmpty()) {
                        categoryId = 0
                        selectedCategoryName = ""
                        type = "in"
                        amount = ""
                        date = System.currentTimeMillis()

                        editingTransaction = null
                        showDialog = true
                    } else {
                        errorMsg = "Kategori tidak ditemukan, silahkan buat kategori dulu"
                        Log.d("TransactionScreen", "Kategori belum siap, tunggu sebentar...")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tambah Transaksi")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.error != null) {
                Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn {
                    items(uiState.transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onEdit = {
                                editingTransaction = it
                                showDialog = true
                            },
                            onDelete = {
                                transactionViewModel.deleteTransaction(apiToken, userId, it.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Kategory: ${transaction.categoryName}")
                Text("Type: ${transaction.type.uppercase()}")
                Text("Amount: Rp ${"%,.2f".format(transaction.amount)}")
                Text("Date: ${sdf.format(Date(transaction.date))}")
            }
            IconButton(onClick = { onEdit(transaction) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDelete(transaction) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaticCategoryDropdown() {
    print("JELAS KESINI")
    val categories = listOf("Makanan", "Transportasi", "Listrik", "Internet")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategori") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            println("KESINI GAK")
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        selectedCategory = category
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TestStaticDropdownScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Dropdown Statis Contoh", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        StaticCategoryDropdown()
    }
}

@Composable
fun CategoryRadioGroup(
    categories: List<CategoryEntity>,
    selectedCategoryId: Int,
    onCategorySelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "Kategori",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)  // dikurangi juga biar gak terlalu jauh dari opsi
        )
        categories.forEach { category ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategorySelected(category.id) }
                    .padding(vertical = 2.dp) // dikurangi supaya gak terlalu jauh
            ) {
                RadioButton(
                    selected = (category.id == selectedCategoryId),
                    onClick = { onCategorySelected(category.id) }
                )
                Spacer(modifier = Modifier.width(2.dp)) // jarak radio dan teks dibuat lebih kecil
                Text(text = category.name)
            }
        }
    }
}

