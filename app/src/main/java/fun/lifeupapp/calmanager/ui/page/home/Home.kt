package `fun`.lifeupapp.calmanager.ui.page.home

import `fun`.lifeupapp.calmanager.MainViewModel
import `fun`.lifeupapp.calmanager.R
import `fun`.lifeupapp.calmanager.common.Resource
import `fun`.lifeupapp.calmanager.common.Resource.Success
import `fun`.lifeupapp.calmanager.datasource.data.CalendarModel
import `fun`.lifeupapp.calmanager.ui.theme.m3.CalendarManagerM3Theme
import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * home page in compose
 *
 * MIT License
 * Copyright (c) 2021 AyagiKei
 */

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalUnitApi
@ExperimentalPermissionsApi
@Composable
fun Home(navController: NavController) {
    CalendarManagerM3Theme {
        // add ProvideWindowInsets
        ProvideWindowInsets {
            //set status bar color
            rememberSystemUiController().setStatusBarColor(
                MaterialTheme.colorScheme.background
            )

            Scaffold(
                Modifier
                    .fillMaxWidth()
                    .systemBarsPadding(), floatingActionButton = {
                    FloatingActionButton(onClick = {
                        navController.navigate("about")
                    }) {
                        Icon(Filled.Info, contentDescription = "about")
                    }
                }
            ) {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxHeight()) {
                    Column {
                        val context = LocalContext.current
                        HeaderTitle(context.getString(R.string.app_title))
                        // request permission and list calendar accounts
                        FeatureThatRequiresCameraPermission(navigateToSettingsScreen = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                            )
                        }, MainViewModel())
                    }
                }
            }
        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun FeatureThatRequiresCameraPermission(
    navigateToSettingsScreen: () -> Unit,
    viewModel: MainViewModel
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
    }

    PermissionsRequired(
        multiplePermissionsState = cameraPermissionState,
        permissionsNotGrantedContent = {
            if (doNotShowRationale) {
                Text(stringResource(R.string.text_do_not_show_rationale))
            } else {
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
            }
        },
        permissionsNotAvailableContent = {
            Column {
                Text(
                    stringResource(R.string.text_permissions_not_available)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = navigateToSettingsScreen) {
                    Text(stringResource(R.string.button_open_settings))
                }
            }
        }
    ) {
        val calendarResource: Resource<List<CalendarModel>> by viewModel.calendarList.collectAsState()
        calendarResource.let {
            if (it is Success) {
                CalendarInfo(calendars = it.item, viewModel = viewModel)
            } else {
                Text(text = stringResource(R.string.placeholder_loading))
            }
        }
    }
}

@Composable
fun HeaderTitle(title: String) {
    Spacer(
        modifier = Modifier
            .statusBarsHeight()
            .fillMaxWidth()
    )
    Text(
        title,
        Modifier.padding(start = 16.dp, bottom = 16.dp),
        style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun CalendarInfo(calendars: List<CalendarModel>, viewModel: MainViewModel) {
    if (calendars.isEmpty()) {
        Text(
            "It seems that we did not find any calendar accounts",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
    } else {
        LazyColumn {
            itemsIndexed(calendars) { index, cal ->
                CalendarCard(calendarModel = cal, viewModel = viewModel)
                if (index == calendars.size - 1) {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun CalendarCard(calendarModel: CalendarModel, viewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .padding(top = 8.dp, start = 4.dp, end = 4.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        var openDialog by remember { mutableStateOf(false) }
        var countDown by remember {
            mutableStateOf(3)
        }
        val scope = rememberCoroutineScope()

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
                            openDialog = true
                            countDown = 3
                        }
                        .width(46.dp)
                        .height(46.dp)
                        .background(color = MaterialTheme.colorScheme.secondaryContainer)) {
                    Icon(
                        Filled.Delete,
                        contentDescription = "delete button",
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.secondaryContainer)
                            .wrapContentWidth()
                            .wrapContentHeight(),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

            }
        }
        if (openDialog) {
            DeleteConfirmDialog(calendarModel = calendarModel, countDown = countDown,
                onDismissRequest = {
                    openDialog = false
                }, onConfirmAction = {
                    openDialog = false
                    viewModel.delete(calendarModel.id)
                })
            // FIXME: launch effect
            scope.launch {
                withContext(Dispatchers.IO) {
                    if (countDown > 0) {
                        delay(1000L)
                        countDown -= 1
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    calendarModel: CalendarModel,
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
            Text(
                text = stringResource(
                    R.string.dialog_message_delete_cal_account,
                    "${calendarModel.displayName}(${calendarModel.accountName})"
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
            MainViewModel()
        )
    }
}
