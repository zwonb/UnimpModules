package com.yidont.barcode.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.yidont.compose.BaseComposeActivity
import com.yidont.compose.ui.PermissionBox
import java.util.concurrent.Executors

class BarcodeScanActivity : BaseComposeActivity() {

    private var permissionState by mutableIntStateOf(0)

    @Composable
    override fun AppContent() {
        LaunchedEffect(Unit) {
            requestPermission()
        }
        when (permissionState) {
            0 -> PermissionBox("扫码需要相机权限") { requestPermission() }
            1 -> Body()
            else -> PermissionBox("相机权限未授权，请授予使用相机权限") {
                XXPermissions.startPermissionActivity(this, Manifest.permission.CAMERA)
            }
        }
    }

    private fun requestPermission() {
        XXPermissions.with(this).permission(Permission.CAMERA)
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

}

@Composable
private fun Body() {
    PreviewView()
    TitleBar()
}

@Composable
private fun PreviewView() {
    val activity = LocalContext.current as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    Box {
        AndroidView(
            factory = { context ->
                val preview = PreviewView(context)
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    val provider = future.get() ?: return@addListener
                    camera = bindPreview(provider, preview, lifecycleOwner) { listBarcode ->
                        val intent = Intent().apply {
                            putExtra("scan_result", listBarcode.first().rawValue)
                        }
                        activity.setResult(Activity.RESULT_OK, intent)
                        activity.finish()
                    }
                }, ContextCompat.getMainExecutor(context))
                preview
            },
            Modifier.fillMaxSize()
        ) {
//            loge(it.bitmap?.toString())
        }
        ScanBar()
        var torchState by remember { mutableStateOf(false) }
        IconButton(
            onClick = {
                torchState = !torchState
                camera?.cameraControl?.enableTorch(torchState)
            },
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .navigationBarsPadding()
                .size(52.dp)
        ) {
            Icon(
                if (torchState) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun BoxScope.ScanBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "bar")
    val height by infiniteTransition.animateValue(
        initialValue = (-160).dp,
        targetValue = 160.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "bar"
    )
    Image(
        painterResource(id = R.drawable.pic_scan_bar), null,
        Modifier
            .align(Alignment.Center)
            .offset { IntOffset(0, height.roundToPx()) }
            .fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
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
    block: (List<Barcode>) -> Unit
): Camera {
    val preview: Preview = Preview.Builder()
        .build()
        .apply { setSurfaceProvider(previewView.surfaceProvider) }
    val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()
    val scanImg = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build().apply {
            setAnalyzer(
                Executors.newSingleThreadExecutor(),
                ScanImgAnalyzer { list ->
                    provider.unbindAll()
                    block(list)
                })
        }

    return provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, scanImg)
}

private class ScanImgAnalyzer(val block: (List<Barcode>) -> Unit) :
    ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//        BarcodeScannerOptions.Builder()
//            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
//            .build()
        val scanner = BarcodeScanning.getClient()
        scanner.process(image).addOnSuccessListener { list ->
//            logE(list.firstOrNull()?.rawValue)
            if (list.any { !it.rawValue.isNullOrEmpty() }) {
                block(list)
            }
        }.addOnFailureListener {
            Log.e("zwonb", "ScanImgAnalyzer:${it.message}")
        }.addOnCompleteListener { imageProxy.close() }
    }
}
