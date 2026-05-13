package com.martins.assignmentschronometer.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.martins.assignmentschronometer.App
import com.martins.assignmentschronometer.ui.screens.chronometer.ChronometerOverlayRoute
import com.martins.assignmentschronometer.ui.theme.AssignmentsChronometerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ChronometerOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val lifecycleOwner = OverlayLifecycleOwner()
    private lateinit var recomposer: Recomposer
    private lateinit var recomposerScope: CoroutineScope

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.start()
        showOverlayView()
    }

    private fun showOverlayView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            (160 * resources.displayMetrics.density).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16
            y = 120
        }

        val sharedViewModel = (application as App).sharedViewModel
        val coroutineContext = AndroidUiDispatcher.CurrentThread

        recomposerScope = CoroutineScope(SupervisorJob() + coroutineContext)
        recomposer = Recomposer(coroutineContext)

        recomposerScope.launch {
            recomposer.runRecomposeAndApplyChanges()
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            compositionContext = recomposer

            setContent {
                AssignmentsChronometerTheme {
                    ChronometerOverlayRoute(
                        sharedViewModel = sharedViewModel,
                        onDrag = { dx, dy ->
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            windowManager.updateViewLayout(this@apply, params)
                        },
                        onClose = { stopSelf() }
                    )
                }
            }
        }

        overlayView = composeView
        windowManager.addView(composeView, params)
        lifecycleOwner.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.destroy()
        recomposer.cancel()
        recomposerScope.cancel()
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }
}