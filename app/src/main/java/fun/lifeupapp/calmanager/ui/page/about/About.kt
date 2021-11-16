package `fun`.lifeupapp.calmanager.ui.page.about

import `fun`.lifeupapp.calmanager.R
import `fun`.lifeupapp.calmanager.common.Val
import `fun`.lifeupapp.calmanager.ui.page.home.HeaderTitle
import `fun`.lifeupapp.calmanager.ui.page.home.Home
import `fun`.lifeupapp.calmanager.ui.theme.CalendarManagerTheme
import `fun`.lifeupapp.calmanager.ui.theme.MYPinkAccent
import `fun`.lifeupapp.calmanager.ui.theme.MYPinkBackground
import `fun`.lifeupapp.calmanager.ui.theme.MyDarkPinkBackground
import `fun`.lifeupapp.calmanager.utils.VersionUtil
import `fun`.lifeupapp.calmanager.utils.logE
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
    CalendarManagerTheme {
        // 加入ProvideWindowInsets
        ProvideWindowInsets {
            Surface(
                color = MaterialTheme.colors.surface,
                modifier = Modifier
                    .fillMaxHeight()
                    .systemBarsPadding()
            ) {
                Column {
                    // 3. 获取状态栏高度并设置占位
                    HeaderTitle(stringResource(R.string.about_title))
                    Column(
                        Modifier
                            .padding(start = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        AppIcon()
                        Spacer(modifier = Modifier.padding(top = 16.dp))
                        Text(
                            stringResource(R.string.about_appname), style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.15.sp
                            )
                        )
                        Spacer(modifier = Modifier.padding(top = 2.dp))
                        Canvas(
                            modifier = Modifier
                                .height(4.dp)
                                .width(32.dp)
                        ) {
                            drawRect(color = MYPinkAccent, size = size)
                        }
                        Text(
                            stringResource(R.string.about_app_desc),
                            Modifier.padding(top = 16.dp, end = 16.dp),
                            style = TextStyle.Default.copy(
                                fontSize = TextUnit(
                                    14f,
                                    TextUnitType.Sp
                                ),
                                letterSpacing = 0.15.sp,
                                lineHeight = TextUnit(
                                    20f,
                                    TextUnitType.Sp
                                )
                            )
                        )
                        AboutSubTitleWithSpacers(stringResource(R.string.about_versions))
                        AboutBodyText(
                            "v${VersionUtil.getLocalVersionName(appCtx)} (${
                                VersionUtil.getLocalVersion(
                                    appCtx
                                )
                            })"
                        )
                        AboutSubTitleWithSpacers(stringResource(R.string.about_permission))
                        AboutBodyText(stringResource(R.string.about_permission_desc))
                        AboutSubTitleWithSpacers(stringResource(R.string.about_link))

                        val context = LocalContext.current
                        val intentViewGithub =
                            remember { Intent(Intent.ACTION_VIEW, Uri.parse(Val.GITHUB_LINK)) }
                        ClickableBodyText(
                            stringResource(R.string.about_link_github),
                            Icons.Default.Star
                        ) {
                            kotlin.runCatching {
                                context.startActivity(intentViewGithub)
                            }.onFailure {
                                toast(R.string.about_toast_failed_to_open)
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
                            append(stringResource(R.string.about_lifeup_desc))
                        }, Icons.Default.Favorite) {
                            try {
                                val uri = Uri.parse("market://details?id=net.sarasarasa.lifeup")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                toast(R.string.about_not_found_android_store)
                                logE(e)
                            }
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
        modifier = Modifier.size(48.dp)
    )
}

@Composable
fun AboutSubTitleWithSpacers(text: String) {
    Spacer(modifier = Modifier.padding(top = 24.dp))
    Text(
        text, style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 0.15.sp
        )
    )
    Spacer(modifier = Modifier.padding(top = 8.dp))
}

@ExperimentalUnitApi
@Composable
fun AboutBodyText(text: String) {
    Text(
        text, style = TextStyle(
            fontSize = 14.sp,
            letterSpacing = 0.15.sp
        ), lineHeight = TextUnit(
            20f,
            TextUnitType.Sp
        )
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
        Icon(icon, contentDescription = null, tint = MaterialTheme.colors.secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text, style = TextStyle(
                fontSize = 14.sp,
                letterSpacing = 0.15.sp
            ), lineHeight = TextUnit(
                20f,
                TextUnitType.Sp
            )
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
        Icon(icon, contentDescription = null, tint = MaterialTheme.colors.secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text, style = TextStyle(
                fontSize = 14.sp,
                letterSpacing = 0.15.sp
            )
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