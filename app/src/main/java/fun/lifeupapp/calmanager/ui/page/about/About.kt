package `fun`.lifeupapp.calmanager.ui.page.about

import `fun`.lifeupapp.calmanager.R
import `fun`.lifeupapp.calmanager.R.string
import `fun`.lifeupapp.calmanager.common.Val
import `fun`.lifeupapp.calmanager.ui.page.home.HeaderTitle
import `fun`.lifeupapp.calmanager.ui.theme.m3.CalendarManagerM3Theme
import `fun`.lifeupapp.calmanager.utils.VersionUtil
import `fun`.lifeupapp.calmanager.utils.launchStorePage
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import splitties.init.appCtx
import splitties.toast.toast

/**
 * about page in compose
 *
 * MIT License
 * Copyright (c) 2021 AyagiKei
 */

@ExperimentalUnitApi
@ExperimentalPermissionsApi
@Composable
fun About() {
    CalendarManagerM3Theme {
        ProvideWindowInsets {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxHeight()
                    .systemBarsPadding()
            ) {
                Column {
                    HeaderTitle(stringResource(R.string.about_title))
                    Column(
                        Modifier
                            .padding(start = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        AppIcon()
                        Spacer(modifier = Modifier.padding(top = 16.dp))
                        Text(
                            stringResource(string.about_appname),
                            style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(modifier = Modifier.padding(top = 2.dp))
                        val color = MaterialTheme.colorScheme.secondary
                        Canvas(
                            modifier = Modifier
                                .height(4.dp)
                                .width(32.dp)
                        ) {
                            drawRect(color = color, size = size)
                        }
                        Text(
                            stringResource(string.about_app_desc),
                            Modifier.padding(top = 16.dp, end = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        AboutSubTitleWithSpacers(stringResource(string.about_versions))
                        AboutBodyText(
                            "v${VersionUtil.getLocalVersionName(appCtx)} (${
                                VersionUtil.getLocalVersion(
                                    appCtx
                                )
                            })"
                        )
                        AboutSubTitleWithSpacers(stringResource(string.about_permission))
                        AboutBodyText(stringResource(string.about_permission_desc))
                        AboutSubTitleWithSpacers(stringResource(string.about_link))

                        val context = LocalContext.current
                        val intentViewGithub =
                            remember { Intent(Intent.ACTION_VIEW, Uri.parse(Val.GITHUB_LINK)) }
                        ClickableBodyText(
                            stringResource(string.about_link_github),
                            Icons.Default.Star
                        ) {
                            kotlin.runCatching {
                                context.startActivity(intentViewGithub)
                            }.onFailure {
                                toast(string.about_toast_failed_to_open)
                            }
                        }

                        ClickableBodyText(buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append("LifeUp")
                            }
                            append("\n")
                            append(stringResource(string.about_lifeup_desc))
                        }, Icons.Default.Favorite) {
                            launchStorePage(context, "net.sarasarasa.lifeup")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIcon() {
    Image(
        painter = painterResource(id = R.drawable.ic_calendar),
        contentDescription = "icon",
        modifier = Modifier.size(36.dp)
    )
}

@Composable
fun AboutSubTitleWithSpacers(text: String) {
    Spacer(modifier = Modifier.padding(top = 24.dp))
    Text(
        text, style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.padding(top = 16.dp))
}

@ExperimentalUnitApi
@Composable
fun AboutBodyText(text: String) {
    Text(
        text, style = MaterialTheme.typography.bodySmall
    )
}

@ExperimentalUnitApi
@Composable
fun ClickableBodyText(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text, style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ClickableBodyText(text: AnnotatedString, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text, style = MaterialTheme.typography.bodySmall
        )
    }
}

@ExperimentalUnitApi
@ExperimentalPermissionsApi
@Preview
@Composable
fun AboutPreView() {
    About()
}