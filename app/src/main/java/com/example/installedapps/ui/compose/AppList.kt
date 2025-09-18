package com.example.installedapps.ui.compose

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.installedapps.ui.theme.InstalledAppsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.installedapps.R
import com.example.installedapps.ui.model.AppInfo

@Composable
fun InstalledAppsApp(
    innerPadding: PaddingValues,
    appInfoList: Flow<List<AppInfo>>,
    onClick: (AppInfo) -> Unit
) {
    AddListScreen(
        innerPadding,
        appInfoList = appInfoList,
        onClick = onClick
    )
}

@Composable
fun AddListScreen(
    innerPadding: PaddingValues,
    appInfoList: Flow<List<AppInfo>?>,
    onClick: (AppInfo) -> Unit
) {
    val items = appInfoList.collectAsStateWithLifecycle(null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        LazyColumn {
            items(items.value?.size ?: 0) { item ->
                items.value?.get(item)?.let { appInfo ->
                    IconTextCard(
                        appInfo = appInfo
                    ) {
                        onClick(appInfo)
                    }
                }
            }
        }
    }
}

fun Drawable.drawableToImageBitmap(): ImageBitmap {
    return this.toBitmap(this.intrinsicWidth, this.intrinsicHeight).asImageBitmap()
}

@Composable
fun IconTextCard(
    appInfo: AppInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .padding(horizontal = 16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                bitmap = appInfo.icon.drawableToImageBitmap(),
                contentDescription = appInfo.name,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = appInfo.name,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppListPreview() {
    InstalledAppsTheme {
        val context: Context = LocalContext.current
        val icon = AppCompatResources.getDrawable(context, R.mipmap.ic_launcher)
        AddListScreen(
            PaddingValues.Zero,
            flowOf(
                listOf(
                    AppInfo(
                        icon = icon!!,
                        name = "My App",
                        version = "1.0.0",
                        packageName = "package",
                        checkSum = "1234"
                    )
                )
            ),
        ) {}
    }
}
