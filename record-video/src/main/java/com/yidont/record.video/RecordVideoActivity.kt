package com.yidont.record.video

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.yidont.compose.BaseComposeActivity
import com.yidont.compose.ui.PermissionBox
import java.io.File

class RecordVideoActivity : BaseComposeActivity() {

    private var permissionState by mutableIntStateOf(0)
    private val mainThreadExecutor by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getMainExecutor(this)
    }
    private val outputFile by lazy(LazyThreadSafetyMode.NONE) {
        File(externalCacheDir, "recordVideo.mp4")
    }
    private var currentRecording: Recording? = null

    @Composable
    override fun AppContent() {
        LaunchedEffect(Unit) {
            requestPermission()
        }
        when (permissionState) {
            0 -> PermissionBox("录像需要相机、录音权限") { requestPermission() }
            1 -> Body()
            else -> PermissionBox("相机、录音权限未授权，请授予使用相机、录音权限") {
                XXPermissions.startPermissionActivity(
                    this,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            }
        }
    }

    private fun requestPermission() {
        XXPermissions.with(this).permission(Permission.CAMERA, Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) permissionState = 1
                }

                override fun onDenied(
                    permissions: MutableList<String>,
                    doNotAskAgain: Boolean
                ) {
                    permissionState = if (doNotAskAgain) 2 else 0
                }
            })
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == XXPermissions.REQUEST_CODE) {
            requestPermission()
        }
    }

    @Composable
    private fun Body() {
        PreviewView()
        TitleBar()
    }

    @SuppressLint("MissingPermission")
    @Composable
    private fun PreviewView() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var camera by remember { mutableStateOf<Camera?>(null) }
        var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
        Box {
            AndroidView(
                factory = { context ->
                    val preview = PreviewView(context)
                    val future = ProcessCameraProvider.getInstance(context)
                    future.addListener({
                        val provider = future.get() ?: return@addListener
                        val recorder = Recorder.Builder()
                            .build()
                        videoCapture = VideoCapture.withOutput(recorder)
                        camera = bindPreview(provider, preview, lifecycleOwner, videoCapture!!)
                    }, mainThreadExecutor)
                    preview
                },
                Modifier.fillMaxSize()
            ) {

            }
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var torchState by remember { mutableStateOf(false) }
                IconButton(
                    onClick = {
                        torchState = !torchState
                        camera?.cameraControl?.enableTorch(torchState)
                    },
                ) {
                    Icon(
                        if (torchState) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        null,
                        tint = Color.White
                    )
                }
                var recordState by remember { mutableStateOf(false) }
                Box(
                    Modifier
                        .padding(top = 24.dp)
                        .clickable {
                            val recording = currentRecording
                            if (recording == null) {
                                currentRecording = videoCapture?.output
                                    ?.prepareRecording(
                                        context,
                                        FileOutputOptions
                                            .Builder(outputFile)
                                            .build()
                                    )
                                    ?.withAudioEnabled()
                                    ?.start(mainThreadExecutor) {
                                        when (it) {
                                            is VideoRecordEvent.Start -> recordState = true
                                            is VideoRecordEvent.Finalize -> {
                                                if (it.hasError()) {
                                                    outputFile.delete()
                                                    currentRecording = null
                                                } else {
                                                    val intent = Intent().apply {
                                                        putExtra("video_path", outputFile.path)
                                                    }
                                                    setResult(Activity.RESULT_OK, intent)
                                                    finish()
                                                }
                                                recordState = false
                                            }
                                        }
                                    }
                            } else {
                                recording.stop()
                                currentRecording = null
                            }
                        }
                ) {
                    Icon(
                        if (recordState) Icons.Outlined.StopCircle else Icons.Default.Videocam,
                        null,
                        Modifier.size(64.dp),
                        tint = if (recordState) Color.Red else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleBar() {
    val context = LocalContext.current as Activity
    Box(
        modifier = Modifier
            .systemBarsPadding()
            .padding(6.dp)
            .size(48.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .clickable { context.finish() },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

private fun bindPreview(
    provider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    videoCapture: VideoCapture<Recorder>
): Camera {
    val preview: Preview = Preview.Builder()
        .build()
        .apply { setSurfaceProvider(previewView.surfaceProvider) }
    val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()
    return provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
}
