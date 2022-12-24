package org.dianqk.ruslin.ui.page.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.FilledButtonWithIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun LoginPage(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
) {
    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarState)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(bottom = 20.dp),
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.url,
                    label = {
                        Text(text = stringResource(id = R.string.url))
                    },
                    onValueChange = viewModel::setUrl,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                        keyboardType = KeyboardType.Uri,
                    )
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.email,
                    label = {
                        Text(text = stringResource(id = R.string.email))
                    },
                    singleLine = true,
                    onValueChange = viewModel::setEmail,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                        keyboardType = KeyboardType.Email,
                    )
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.password,
                    label = {
                        Text(text = stringResource(id = R.string.password))
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    onValueChange = viewModel::setPassword,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                        keyboardType = KeyboardType.Password,
                    )
                )
                FilledButtonWithIcon(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            viewModel.login()
                                .onSuccess {
                                    onLoginSuccess()
                                }
                                .onFailure { e ->
                                    e.localizedMessage?.let { snackbarState.showSnackbar(it) }
                                }
                        }
                    },
                    icon = Icons.Outlined.Login,
                    text = stringResource(id = R.string.sign_in)
                )
            }
        }
    }
}
