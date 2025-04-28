package com.example.pineappleexpense.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel

@Composable
fun AccountMapping(navController: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf("") }
    
    val categories = listOf("Meals", "Travel", "Supplies", "Safety", "Other")
    val mappings = viewModel.accountMappings

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = { BottomBar(navController, viewModel) },
        topBar = { TopBar(navController, viewModel) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (categories.filter { it !in mappings.keys }.isNotEmpty()) {
                        showAddDialog = true 
                    }
                },
                containerColor = if (categories.filter { it !in mappings.keys }.isEmpty()) Color.Gray else Color(0xFF4E0AA6),
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add mapping",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Category Mappings",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF4E0AA6),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (mappings.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3E5F5)
                    )
                ) {
                    Text(
                        text = "No account mappings set up yet. Tap the + button to add your first mapping.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mappings.entries.toList()) { (category, account) ->
                        CategoryMappingCard(
                            category = category,
                            account = account,
                            onEdit = {
                                selectedCategory = category
                                selectedAccount = account
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.removeAccountMapping(category)
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            MappingDialog(
                title = "Add Mapping",
                onDismiss = { showAddDialog = false },
                onConfirm = { category, account ->
                    viewModel.addAccountMapping(category, account)
                    showAddDialog = false
                },
                categories = categories.filter { it !in mappings.keys }
            )
        }

        if (showEditDialog) {
            MappingDialog(
                title = "Edit Mapping",
                initialCategory = selectedCategory,
                initialAccount = selectedAccount,
                onDismiss = { showEditDialog = false },
                onConfirm = { category, account ->
                    viewModel.updateAccountMapping(selectedCategory, category, account)
                    showEditDialog = false
                },
                categories = categories
            )
        }
    }
}

@Composable
fun CategoryMappingCard(
    category: String,
    account: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4E0AA6)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Account: $account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete mapping",
                    tint = Color(0xFF666666)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingDialog(
    title: String,
    initialCategory: String = "",
    initialAccount: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    categories: List<String>
) {
    var category by remember { mutableStateOf(initialCategory) }
    var account by remember { mutableStateOf(initialAccount) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4E0AA6),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .padding(bottom = 16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF4E0AA6),
                            focusedLabelColor = Color(0xFF4E0AA6)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("Account Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF4E0AA6),
                        focusedLabelColor = Color(0xFF4E0AA6)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF666666)
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(category, account) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4E0AA6)
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAccountMapping() {
    val navController = rememberNavController()
    val viewModel: AccessViewModel = viewModel()
    AccountMapping(navController, viewModel)
}