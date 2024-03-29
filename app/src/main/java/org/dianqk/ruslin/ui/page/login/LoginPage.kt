package org.dianqk.ruslin.ui.page.login

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.FilledButtonWithIcon

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun LoginPage(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
    onPopBack: () -> Unit
) {
    val snackbarState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val autofill = LocalAutofill.current
    val syncAngle by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarState.showSnackbar(errorMessage)
            viewModel.dismissSnackBar()
        }
    }

    if (uiState.loginSuccess) {
        LaunchedEffect(Unit) {
            onLoginSuccess()
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onClick = onPopBack) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarState)
        }
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
            Text(
                modifier = Modifier.padding(bottom = 30.dp),
                text = stringResource(id = R.string.login_tip),
                style = MaterialTheme.typography.labelLarge
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
                        keyboardType = KeyboardType.Uri
                    )
                )
                Autofill(
                    autofillTypes = listOf(AutofillType.EmailAddress),
                    onFill = viewModel::setEmail
                ) { autofillNode ->
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                autofill?.run {
                                    if (focusState.isFocused) {
                                        requestAutofillForNode(autofillNode)
                                    } else {
                                        cancelAutofillForNode(autofillNode)
                                    }
                                }
                            },
                        value = uiState.email,
                        label = {
                            Text(text = stringResource(id = R.string.email))
                        },
                        singleLine = true,
                        onValueChange = viewModel::setEmail,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrect = false,
                            keyboardType = KeyboardType.Email
                        )
                    )
                }
                Autofill(
                    autofillTypes = listOf(AutofillType.Password),
                    onFill = viewModel::setPassword
                ) { autofillNode ->
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                autofill?.run {
                                    if (focusState.isFocused) {
                                        requestAutofillForNode(autofillNode)
                                    } else {
                                        cancelAutofillForNode(autofillNode)
                                    }
                                }
                            },
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
                            keyboardType = KeyboardType.Password
                        )
                    )
                }
                FilledButtonWithIcon(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    onClick = viewModel::login,
                    enabled = !uiState.isLoggingIn,
                    icon = {
                        Icon(
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (uiState.isLoggingIn) syncAngle else 0f),
                            imageVector = if (uiState.isLoggingIn) Icons.Outlined.Refresh else Icons.Outlined.Login,
                            contentDescription = null
                        )
                    },
                    text = stringResource(id = R.string.sign_in)
                )
            }
        }
    }
}

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/ui/ui/integration-tests/ui-demos/src/main/java/androidx/compose/ui/demos/autofill/ExplicitAutofillTypesDemo.kt
@ExperimentalComposeUiApi
@Composable
private fun Autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
    content: @Composable (AutofillNode) -> Unit
) {
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)

    val autofillTree = LocalAutofillTree.current
    autofillTree += autofillNode

    Box(
        Modifier.onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
    ) {
        content(autofillNode)
    }
}
