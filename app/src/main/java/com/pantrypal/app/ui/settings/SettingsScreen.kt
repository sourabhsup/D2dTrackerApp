package com.pantrypal.app.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pantrypal.app.ui.common.rememberApp

@Composable
fun SettingsScreen(householdId: String) {
    val app = rememberApp()
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory { initializer { SettingsViewModel(app.session) } }
    )
    val householdName by viewModel.householdName.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showLeaveConfirm by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Household", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(householdName ?: "", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Share this code with your other phone:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = householdId,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            clipboardManager.setText(AnnotatedString(householdId))
                        }) { Text("Copy code") }
                        Button(onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Join our PantryPal household \"${householdName.orEmpty()}\" with code: $householdId"
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share household code"))
                        }) { Text("Share") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(onClick = { showLeaveConfirm = true }) {
                Text("Leave household")
            }
        }
    }

    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text("Leave household?") },
            text = { Text("This phone will stop syncing with \"${householdName.orEmpty()}\". You can rejoin later with the code.") },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveConfirm = false
                    viewModel.leaveHousehold()
                }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
