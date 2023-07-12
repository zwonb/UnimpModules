package com.yidont.unimp.modules.demo.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.yidont.unimp.modules.demo.APP_ID
import com.yidont.unimp.modules.demo.MainActivity
import com.yidont.unimp.modules.demo.MainViewModel
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun MainPage(viewModel: MainViewModel) {
    Scaffold(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .padding(it)
                .fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            val tipText = viewModel.state.tipText
            if (tipText != null) {
                val context = LocalContext.current
                ErrorPage(tipText) {
                    val file = File(context.externalCacheDir, "$APP_ID.wgt")
                    viewModel.releaseStartMP(file.path)
                }
            } else {
                LogoImg { viewModel.openMP() }
            }
        }
    }

    val activity = LocalView.current.context as? MainActivity
    LaunchedEffect(Unit) {
        launch {
            activity?.lifecycle?.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.event.collect {
                    when (it) {
                        // 小程序启动后发送关闭主界面，此时已经启动小程序Activity，需要关闭小程序后回到主界面才会关闭
                        1 -> viewModel.openMP()
                        2 -> activity.finish()
                    }
                }
            }
        }

    }
}

@Composable
private fun LogoImg(onClick: () -> Unit) {
    Text(
        "unimp modules",
        Modifier.padding(top = 24.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun ErrorPage(tipText: String, retry: () -> Unit) {
    Image(
        Icons.Default.Refresh, null,
        Modifier.size(220.dp)
    )
    Text(tipText, Modifier.padding(vertical = 24.dp), fontSize = 16.sp)
    Button(onClick = retry, shape = CircleShape) {
        Text(text = "重试", fontSize = 16.sp)
    }
}
