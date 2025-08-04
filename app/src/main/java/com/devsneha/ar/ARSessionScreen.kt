package com.devsneha.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.ar.core.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARSessionScreen(drillName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(drillName) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        if (hasPermission) {
            ARSurfaceView()
            Text(
                "\uD83C\uDFAF Tap on detected ground planes to place drill markers",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Waiting for camera permission…")
            }
        }
    }
}

@Composable
fun ARSurfaceView() {
    val backgroundRenderer = BackgroundRenderer()
    val cubeRenderer = CubeRenderer()

    AndroidView(factory = { context ->
        val glView = object : GLSurfaceView(context) {
            private var session: Session? = null
            private var textureId = -1
            private var surfaceTexture: SurfaceTexture? = null
            private val anchors = mutableListOf<Anchor>()
            private var queuedSingleTap: MotionEvent? = null
            private val tapLock = Any()

            init {
                setEGLContextClientVersion(2)
                setRenderer(object : Renderer {
                    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
                        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                        GLES20.glEnable(GLES20.GL_BLEND)
                        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

                        textureId = backgroundRenderer.init()
                        cubeRenderer.init()

                        surfaceTexture = SurfaceTexture(textureId)
                        surfaceTexture?.setOnFrameAvailableListener {
                            requestRender()
                        }

                        try {
                            if (session == null) {
                                session = Session(context).apply {
                                    // Configure ARCore session
                                    val config = Config(this)
                                    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                                    configure(config)
                                }
                            }
                            session?.setCameraTextureName(textureId)
                        } catch (e: Exception) {
                            Log.e("ARCore", "Session creation failed", e)
                        }
                    }

                    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                        GLES20.glViewport(0, 0, width, height)
                        session?.setDisplayGeometry(Surface.ROTATION_0, width, height)
                    }

                    override fun onDrawFrame(gl: GL10?) {
                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

                        if (surfaceTexture == null || session == null) return

                        surfaceTexture?.updateTexImage()

                        val frame = try {
                            session!!.update()
                        } catch (e: Exception) {
                            Log.e("ARCore", "Frame update failed", e)
                            return
                        }

                        val camera = frame.camera

                        if (camera.trackingState != TrackingState.TRACKING) return

                        synchronized(tapLock) {
                            queuedSingleTap?.let { tap ->
                                handleTap(frame, tap)
                                queuedSingleTap = null
                            }
                        }

                        val projMatrix = FloatArray(16)
                        val viewMatrix = FloatArray(16)
                        camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f)
                        camera.getViewMatrix(viewMatrix, 0)

                        backgroundRenderer.draw(textureId)

                        anchors.removeAll { anchor ->
                            if (anchor.trackingState == TrackingState.STOPPED) {
                                anchor.detach()
                                true
                            } else {
                                cubeRenderer.draw(anchor.pose, viewMatrix, projMatrix)
                                false
                            }
                        }
                    }
                })
                renderMode = RENDERMODE_CONTINUOUSLY
            }

            private fun handleTap(frame: Frame, tap: MotionEvent) {
                val hits = frame.hitTest(tap)
                for (hit in hits) {
                    val trackable = hit.trackable

                    if (trackable is Plane &&
                        trackable.isPoseInPolygon(hit.hitPose) &&
                        trackable.trackingState == TrackingState.TRACKING) {

                        val anchor = hit.createAnchor()
                        anchors.add(anchor)
                        Log.d("ARCore", "Cube placed at: ${hit.hitPose.translation.contentToString()}")
                        break
                    }
                }
            }

            override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                if (session == null) {
                    try {
                        session = Session(context).apply {
                            val config = Config(this)
                            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                            configure(config)
                        }
                        session?.setCameraTextureName(textureId)
                    } catch (e: Exception) {
                        Log.e("ARCore", "Failed to start session", e)
                    }
                }
                session?.resume()
            }

            override fun onDetachedFromWindow() {
                super.onDetachedFromWindow()
                session?.pause()
                anchors.forEach { it.detach() }
                anchors.clear()
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP) {
                    synchronized(tapLock) {
                        queuedSingleTap = event
                    }
                }
                return true
            }
        }
        glView
    }, modifier = Modifier.fillMaxSize())
}

class CubeRenderer {
    private var program = 0
    private var positionAttribute = 0
    private var mvpMatrixUniform = 0
    private var colorUniform = 0

    private val cubeVertices = floatArrayOf(
        // Front face
        -0.05f, -0.05f,  0.05f,
        0.05f, -0.05f,  0.05f,
        0.05f,  0.05f,  0.05f,
        -0.05f,  0.05f,  0.05f,

        // Back face
        -0.05f, -0.05f, -0.05f,
        0.05f, -0.05f, -0.05f,
        0.05f,  0.05f, -0.05f,
        -0.05f,  0.05f, -0.05f
    )

    private val cubeIndices = shortArrayOf(
        // Front
        0, 1, 2, 0, 2, 3,
        // Back
        4, 6, 5, 4, 7, 6,
        // Left
        4, 0, 3, 4, 3, 7,
        // Right
        1, 5, 6, 1, 6, 2,
        // Top
        3, 2, 6, 3, 6, 7,
        // Bottom
        4, 5, 1, 4, 1, 0
    )

    private val vertexBuffer = ByteBuffer.allocateDirect(cubeVertices.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(cubeVertices)
            position(0)
        }

    private val indexBuffer = ByteBuffer.allocateDirect(cubeIndices.size * 2)
        .order(ByteOrder.nativeOrder()).asShortBuffer().apply {
            put(cubeIndices)
            position(0)
        }

    fun init() {
        val vertexShader = """
            attribute vec4 a_Position;
            uniform mat4 u_MvpMatrix;
            void main() {
                gl_Position = u_MvpMatrix * a_Position;
            }
        """.trimIndent()

        val fragmentShader = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """.trimIndent()

        program = loadShaderProgram(vertexShader, fragmentShader)
        positionAttribute = GLES20.glGetAttribLocation(program, "a_Position")
        mvpMatrixUniform = GLES20.glGetUniformLocation(program, "u_MvpMatrix")
        colorUniform = GLES20.glGetUniformLocation(program, "u_Color")
    }

    fun draw(pose: Pose, viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        val modelMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)
        val mvMatrix = FloatArray(16)

        pose.toMatrix(modelMatrix, 0)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUseProgram(program)

        GLES20.glUniform4f(colorUniform, 1.0f, 0.5f, 0.0f, 1.0f)

        GLES20.glUniformMatrix4fv(mvpMatrixUniform, 1, false, mvpMatrix, 0)

        GLES20.glEnableVertexAttribArray(positionAttribute)
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cubeIndices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        GLES20.glDisableVertexAttribArray(positionAttribute)
    }

    private fun loadShaderProgram(vertex: String, fragment: String): Int {
        fun compile(type: Int, code: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                Log.e("CubeRenderer", "Shader compilation failed: ${GLES20.glGetShaderInfoLog(shader)}")
                GLES20.glDeleteShader(shader)
                return 0
            }
            return shader
        }

        val vShader = compile(GLES20.GL_VERTEX_SHADER, vertex)
        val fShader = compile(GLES20.GL_FRAGMENT_SHADER, fragment)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vShader)
        GLES20.glAttachShader(program, fShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e("CubeRenderer", "Program linking failed: ${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program)
            return 0
        }

        GLES20.glDeleteShader(vShader)
        GLES20.glDeleteShader(fShader)

        return program
    }
}

class BackgroundRenderer {
    private var quadProgram = 0
    private var quadPositionParam = 0
    private var quadTexCoordParam = 0
    private var quadTexCoordBuffer = ByteBuffer.allocateDirect(8 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f))
            position(0)
        }

    private var quadVertexBuffer = ByteBuffer.allocateDirect(8 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f))
            position(0)
        }

    private var textureId = -1

    fun init(): Int {
        textureId = createExternalTexture()

        val vertexShader = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            void main() {
                gl_Position = a_Position;
                v_TexCoord = a_TexCoord;
            }
        """.trimIndent()

        val fragmentShader = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES u_Texture;
            varying vec2 v_TexCoord;
            void main() {
                gl_FragColor = texture2D(u_Texture, v_TexCoord);
            }
        """.trimIndent()

        quadProgram = loadShaderProgram(vertexShader, fragmentShader)
        quadPositionParam = GLES20.glGetAttribLocation(quadProgram, "a_Position")
        quadTexCoordParam = GLES20.glGetAttribLocation(quadProgram, "a_TexCoord")
        return textureId
    }

    fun draw(textureId: Int) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glUseProgram(quadProgram)

        GLES20.glVertexAttribPointer(quadPositionParam, 2, GLES20.GL_FLOAT, false, 0, quadVertexBuffer)
        GLES20.glEnableVertexAttribArray(quadPositionParam)

        GLES20.glVertexAttribPointer(quadTexCoordParam, 2, GLES20.GL_FLOAT, false, 0, quadTexCoordBuffer)
        GLES20.glEnableVertexAttribArray(quadTexCoordParam)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(quadPositionParam)
        GLES20.glDisableVertexAttribArray(quadTexCoordParam)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(true)
    }

    private fun createExternalTexture(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        return texture[0]
    }

    private fun loadShaderProgram(vertex: String, fragment: String): Int {
        fun compile(type: Int, code: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)
            return shader
        }

        val vShader = compile(GLES20.GL_VERTEX_SHADER, vertex)
        val fShader = compile(GLES20.GL_FRAGMENT_SHADER, fragment)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vShader)
        GLES20.glAttachShader(program, fShader)
        GLES20.glLinkProgram(program)
        return program
    }
}