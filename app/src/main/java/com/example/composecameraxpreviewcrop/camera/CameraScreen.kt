package com.example.composecameraxpreviewcrop.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.composecameraxpreviewcrop.extensions.cropImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CameraScreen(
    viewmodel: CameraViewModel = viewModel()
) {

    CameraContent()
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraContent() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember {
        LifecycleCameraController(context)
    }

    val screenWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val screenHeightPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }

    // number
    var numberOfTicket by remember {
        mutableStateOf<Int>(1)
    }
    val halfOriginalCropBoxHeightPx = with(LocalDensity.current) {
        25.dp.toPx()
    }

    // cropbox
    var cropBoxStartYPx by remember {
        mutableStateOf<Float>(screenHeightPx / 2 - numberOfTicket * halfOriginalCropBoxHeightPx)
    }
    var cropBoxEndYPx by remember {
        mutableStateOf<Float>(screenHeightPx / 2 + numberOfTicket * halfOriginalCropBoxHeightPx)
    }
    val cropBoxHorizontalPadding = screenWidthPx / 5

    val cropOffsetX = cropBoxHorizontalPadding
    val cropOffsetY = cropBoxStartYPx
    val cropWidth = screenWidthPx / 5 * 4
    val cropHeight = cropBoxEndYPx - cropBoxStartYPx

    // capturedImage
    var capturedImage by remember {
        mutableStateOf<Bitmap?>(null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val mainExecutor = ContextCompat.getMainExecutor(context)
                    cameraController.takePicture(
                        mainExecutor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun  onCaptureSuccess(image: ImageProxy) {
                                super.onCaptureSuccess(image)

                                lifecycleOwner.lifecycleScope.launch {
                                    capturedImage = image.image?.cropImage(
                                        image.imageInfo.rotationDegrees,
                                        cropOffsetX.roundToInt(),
                                        cropOffsetY.roundToInt(),
                                        cropWidth.roundToInt(),
                                        cropHeight.roundToInt(),
                                        screenWidthPx.roundToInt()
                                    )


                                    image.image?.close()
                                    image.close()
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                super.onError(exception)
                            }
                        }
                    )
                }
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Camera"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {

                IconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = "Exit",
                        modifier = Modifier.size(100.dp)
                    )
                }

                NumberDropDownMenu(
                    modifier = Modifier.weight(2f)
                ) { num ->
                    cropBoxStartYPx = screenHeightPx / 2 - num * halfOriginalCropBoxHeightPx
                    cropBoxEndYPx = screenHeightPx / 2 + num * halfOriginalCropBoxHeightPx
                }

                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(100.dp)
                    )
                }
                //Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues: PaddingValues ->

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.BLACK)
                    scaleType = PreviewView.ScaleType.FILL_START
                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            }
        )

        MaskedView(
            startPoint = Offset(cropBoxHorizontalPadding / 2, cropBoxStartYPx),
            endPoint = Offset(screenWidthPx / 5 * 4 + cropBoxHorizontalPadding / 2, cropBoxEndYPx),
            maskColor = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f)
        )

        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            capturedImage?.let {
                AsyncImage(
                    model = capturedImage,
                    contentDescription = "",
                    modifier = Modifier.width(300.dp)
                )
            }
        }
    }
}