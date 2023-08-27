package `fun`.lifeupapp.calmanager.ui.page.home

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import `fun`.lifeupapp.calmanager.BuildConfig
import `fun`.lifeupapp.calmanager.MainViewModel
import `fun`.lifeupapp.calmanager.R
import `fun`.lifeupapp.calmanager.R.string
import `fun`.lifeupapp.calmanager.common.Resource
import `fun`.lifeupapp.calmanager.common.Resource.Success
import `fun`.lifeupapp.calmanager.datasource.data.CalendarModel
import `fun`.lifeupapp.calmanager.ui.RouteDef
import `fun`.lifeupapp.calmanager.ui.theme.m3.CalendarManagerM3Theme
import `fun`.lifeupapp.calmanager.utils.launchStorePage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import splitties.init.appCtx

/**
 * home page in compose
 *
 * MIT License
 * Copyright (c) 2023 AyagiKei
 */

@ExperimentalUnitApi
@ExperimentalPermissionsApi
@Composable
fun Home(navController: NavController) {
    CalendarManagerM3Theme {
        //set status bar color
        rememberSystemUiController().setStatusBarColor(
            MaterialTheme.colorScheme.background
        )

        val snackbarHostState = remember {
            androidx.compose.material3.SnackbarHostState()
        }
        val scope = rememberCoroutineScope()
        val viewModel = viewModel<MainViewModel>()
        val enterSelectMode by viewModel.enterSelectMode.collectAsState()
        val deleteConfirmDialogState by viewModel.deleteConfirmDialogState.collectAsState()

        BackHandler(enabled = enterSelectMode) {
            viewModel.unselectedAll()
        }

        Scaffold(
            Modifier
                .fillMaxWidth()
                .systemBarsPadding(), floatingActionButton = {

                if (enterSelectMode.not()) {
                    FloatingActionButton(onClick = {
                        navController.navigate(RouteDef.ABOUT.path)
                    }) {
                        Icon(Filled.Info, contentDescription = "about")
                    }
                } else {
                    FloatingActionButton(onClick = {
                        viewModel.deleteSelected()
                    }) {
                        Icon(Filled.Delete, contentDescription = "delete")
                    }
                }
            }, snackbarHost = {
                androidx.compose.material3.SnackbarHost(hostState = snackbarHostState)
            }
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(it)
            ) {
                val baseState = remember {
                    BaseState(snackbarHostState, scope)
                }
                Column {
                    val context = LocalContext.current
                    HeaderTitle(context.getString(string.app_title))
                    // request permission and list calendar accounts
                    FeatureThatRequiresCalendarPermission(navigateToSettingsScreen = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    }, viewModel, baseState = baseState)

                    // delete confirm dialog
                    if (deleteConfirmDialogState.calendars.isNotEmpty()) {
                        DeleteConfirmDialog(
                            calendarModels = deleteConfirmDialogState.calendars,
                            countDown = deleteConfirmDialogState.countdown,
                            onDismissRequest = {
                                viewModel.dismissDeleteConfirmDialog()
                            },
                            onConfirmAction = {
                                viewModel.confirmDelete(deleteConfirmDialogState.calendars)

                                // show rate us snack bar only once in lifecycle
                                if (viewModel.shouldShownRateUs.value && baseState.snackbarHostState != null) {
                                    baseState.scope.launch(Dispatchers.Main) {
                                        val result = baseState.snackbarHostState.showSnackbar(
                                            message = appCtx.getString(R.string.delete_success_snackbar_message),
                                            actionLabel = appCtx.getString(R.string.btn_rate),
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            launchStorePage(appCtx, BuildConfig.APPLICATION_ID)
                                        }
                                    }
                                    viewModel.hasShownRateUs()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

data class BaseState(
    val snackbarHostState: androidx.compose.material3.SnackbarHostState?,
    val scope: CoroutineScope
)

@ExperimentalPermissionsApi
@Composable
fun FeatureThatRequiresCalendarPermission(
    navigateToSettingsScreen: () -> Unit,
    viewModel: MainViewModel,
    baseState: BaseState
) {
    // Track if the user doesn't want to see the rationale any more.
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    val cameraPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            permission.READ_CALENDAR,
            permission.WRITE_CALENDAR
        )
    )

    if (cameraPermissionState.allPermissionsGranted) {
        viewModel.fetchIfError()

        val multiSelectHintState by viewModel.multiSelectHint.collectAsState()
        if (multiSelectHintState) {
            LaunchedEffect(Unit) {
                baseState.scope.launch {
                    baseState.snackbarHostState?.showSnackbar(
                        message = appCtx.getString(R.string.hint_multi_select),
                        actionLabel = appCtx.getString(R.string.button_ok),
                        duration = SnackbarDuration.Indefinite
                    )
                }
            }
            viewModel.shownMultiSelectHint()
        }

        val calendarResource: Resource<List<CalendarModel>> by viewModel.calendarList.collectAsState()
        calendarResource.let {
            if (it is Success) {
                CalendarInfo(calendars = it.item, viewModel = viewModel, baseState)
            } else {
                Text(
                    text = stringResource(R.string.placeholder_loading),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    } else {
        if (cameraPermissionState.shouldShowRationale.not() && doNotShowRationale.not()) {
            Column {
                Text(
                    stringResource(R.string.text_permission_require_desc),
                    Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.padding(horizontal = 16.dp)) {
                    Button(onClick = { doNotShowRationale = true }) {
                        Text(stringResource(R.string.button_nope))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { cameraPermissionState.launchMultiplePermissionRequest() }) {
                        Text(stringResource(R.string.button_ok))
                    }
                }
            }
        } else {
            Text(
                stringResource(R.string.text_do_not_show_rationale),
                Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.padding(horizontal = 16.dp)) {
                Button(onClick = {
                    navigateToSettingsScreen()
                }) {
                    Text(stringResource(R.string.button_open_settings))
                }
            }
        }
    }
}

@Composable
fun HeaderTitle(title: String) {
    WindowInsets
    Spacer(
        modifier = Modifier
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .fillMaxWidth()
    )
    Text(
        title,
        Modifier.padding(start = 16.dp, bottom = 16.dp),
        style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun CalendarInfo(
    calendars: List<CalendarModel>,
    viewModel: MainViewModel,
    baseState: BaseState
) {
    if (calendars.isEmpty()) {
        Text(
            stringResource(R.string.it_seems_that_we_did_not_find_any_calendar_accounts),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
    } else {
        LazyColumn {
            itemsIndexed(calendars, key = { _, item ->
                item.hashCode()
            }) { index, cal ->
                CalendarCard(calendarModel = cal, viewModel = viewModel, baseState)
                if (index == calendars.size - 1) {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarCard(
    calendarModel: CalendarModel,
    viewModel: MainViewModel,
    baseState: BaseState
) {
    Card(
        modifier = Modifier
            .padding(top = 8.dp, start = 4.dp, end = 4.dp)
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {
                    viewModel.toggleSelect(calendarModel)
                }
            ) {
                if (viewModel.enterSelectMode.value) {
                    viewModel.toggleSelect(calendarModel)
                }
            },
        elevation = 0.dp,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    ) {


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .wrapContentWidth(Alignment.Start)
                    .weight(5f)
            ) {
                Text(calendarModel.displayName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${calendarModel.accountName} · ${calendarModel.ownerName} · id ${calendarModel.id}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                modifier = Modifier
                    .wrapContentWidth(Alignment.End)
                    .weight(1f)
            ) {
                Surface(
                    Modifier
                        .clickable {
                            if (viewModel.enterSelectMode.value.not()) {
                                viewModel.deleteItem(listOf(calendarModel))
                            } else {
                                viewModel.toggleSelect(calendarModel)
                            }
                        }
                        .width(46.dp)
                        .height(46.dp)
                        .background(color = MaterialTheme.colorScheme.secondaryContainer)) {
                    val selectedMode by viewModel.enterSelectMode.collectAsState()
                    if (calendarModel.isSelected.not()) {
                        if (selectedMode.not()) {
                            Icon(
                                Filled.Delete,
                                contentDescription = "delete button",
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                    .wrapContentWidth()
                                    .wrapContentHeight(),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Icon(
                                Filled.Add,
                                contentDescription = "unchecked item",
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                    .wrapContentWidth()
                                    .wrapContentHeight(),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    } else {
                        Icon(
                            Filled.Check,
                            contentDescription = "checked item",
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                .wrapContentWidth()
                                .wrapContentHeight(),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    calendarModels: List<CalendarModel>,
    countDown: Int,
    onDismissRequest: () -> Unit,
    onConfirmAction: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.dialog_title_delete))
        },
        text = {
            val calendarModelsText = remember {
                calendarModels.joinToString(separator = "\n") {
                    "${it.displayName}(${it.accountName})"
                }
            }
            Text(
                text = stringResource(
                    R.string.dialog_message_delete_cal_account,
                    calendarModelsText
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmAction()
                },
                enabled = countDown <= 0
            ) {
                if (countDown > 0) {
                    Text(stringResource(R.string.dialog_button_confirm) + "(${countDown})")
                } else {
                    Text(stringResource(R.string.dialog_button_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.dialog_button_dismiss))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CalendarCardPreview() {
    CalendarManagerM3Theme {
        CalendarCard(
            CalendarModel(
                0L,
                "Display Name",
                "accountName",
                "ownerName"
            ),
            MainViewModel(),
            BaseState(null, rememberCoroutineScope())
        )
    }
}
