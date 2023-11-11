package com.example.composecameraxpreviewcrop.camera

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Rational
import android.view.OrientationEventListener
import android.view.Surface
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.composecameraxpreviewcrop.MainActivity
import com.example.composecameraxpreviewcrop.extensions.getCameraProvider
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.delay


/*
  * ViewPort has some bug
  *
  * cropBoxMinHeight = screenHeight / 10
  *
  * for viewPort y = screenHeight / 10
  * the cropbox must be
  * cropBoxHeight = cropBoxMinHeight
  * CropBoxStartY = screenHeight / 2 - cropBoxMinHeight
  * cropBoxEndY = screenHeight / 2
  *
  *
  * for veiwport y = screenHeight / 10 * 3
  * the cropbox has to be
  * cropBoxStartY = screenHeight / 2 - cropBoxMinHeight * 2
  * cropBoxEndY = screenHeight / 2 + cropBoxHeig
 */
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
    val screenWidthPx =
        with(LocalDensity.current) {
            LocalConfiguration.current.screenWidthDp.dp.toPx()
        }
    val screenHeightPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val halfSingleNumberCropBoxHeightPx = screenHeightPx / 20
    val cropBoxMinHeight = screenHeightPx / 10
    var doubleOfMinCropBox by remember {
        mutableStateOf(1)
    }

    // cropbox
    // number
    var numberOfTicket by remember {
        mutableStateOf<Int>(1)
    }
    var cropBoxStartYPx by remember {
        mutableStateOf<Float>(screenHeightPx / 2 - cropBoxMinHeight * 2)
    }
    var cropBoxEndYPx by remember {
        mutableStateOf<Float>(screenHeightPx / 2 + cropBoxMinHeight)
    }
    val cropBoxHorizontalPadding = screenWidthPx / 5




    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lensFacing = CameraSelector.LENS_FACING_BACK
    val preview = Preview.Builder().build()
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }
    var rotation = remember {
        Surface.ROTATION_0
    }
    val orientationEventListener = remember {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                //val rotation: Int = when (orientation) {
                rotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture.targetRotation = rotation
                //Log.d("Main", "ratation: $rotation")
            }
        }
    }
    orientationEventListener.enable()
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()




    val recognizer = remember {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    var recognizedText by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(doubleOfMinCropBox) {
        val cameraProvider = context.getCameraProvider()
        val viewPort = ViewPort.Builder(
                Rational(screenWidthPx.toInt(),(screenHeightPx / 10 * doubleOfMinCropBox).toInt()),
                rotation
            ).build()
        cropBoxStartYPx = screenHeightPx / 2 - (doubleOfMinCropBox / 2f + 0.5f) * cropBoxMinHeight
        cropBoxEndYPx = screenHeightPx / 2 + (doubleOfMinCropBox / 2f - 0.5f) * cropBoxMinHeight

        cameraProvider.unbindAll()
        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(imageCapture)
            .setViewPort(viewPort)
            .build()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            useCaseGroup
        )
    }


    // capturedImage
    var capturedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val mainExecutor = ContextCompat.getMainExecutor(context)

                    imageCapture.takePicture(
                        context,
                        onError = {}
                    ) {
                        capturedImageUri = it
                    }
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
                    doubleOfMinCropBox = 3
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
                    setBackgroundColor(android.graphics.Color.BLACK)
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                }.also { previewView ->
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                }
            }
        )

        MaskedView(
            startPoint = Offset(0f, cropBoxStartYPx),
            endPoint = Offset(screenWidthPx, cropBoxEndYPx),
            maskColor = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f)
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            capturedImageUri?.let {
                AsyncImage(
                    model = capturedImageUri,
                    contentDescription = "",
                    modifier = Modifier.width(300.dp)
                )

                LaunchedEffect(it) {
                    delay(3000)

                    capturedImageUri = null
                }
            }

            recognizedText?.let {
                Text(
                    text = it,
                    color = Color.Red
                )
            }
        }
    }
}