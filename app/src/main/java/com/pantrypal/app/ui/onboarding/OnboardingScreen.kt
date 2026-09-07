package com.pantrypal.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pantrypal.app.ui.common.rememberApp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onHouseholdReady: () -> Unit) {
    val app = rememberApp()
    val viewModel: OnboardingViewModel = viewModel(
        factory = viewModelFactory {
            initializer { OnboardingViewModel(app.householdRepository, app.session) }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.dismissError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Kitchen,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "PantryPal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Shared grocery inventory, meal planning and shopping lists for your household.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            var selectedTab by rememberSaveable { mutableIntStateOf(0) }
            TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Create household") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Join household") }
                )
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                if (selectedTab == 0) {
                    var householdName by rememberSaveable { mutableStateOf("") }
                    OutlinedTextField(
                        value = householdName,
                        onValueChange = { householdName = it },
                        label = { Text("Household name, e.g. \"The Smiths\"") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.createHousehold(householdName, onHouseholdReady) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Create and get a shareable code")
                    }
                    Text(
                        text = "You'll get a 6-character code to enter on your other phone.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    var code by rememberSaveable { mutableStateOf("") }
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("Household code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { viewModel.joinHousehold(code, onHouseholdReady) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Join household")
                    }
                    Text(
                        text = "Ask whoever set up the household for its code.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
                }
            }
        }
    }
}
