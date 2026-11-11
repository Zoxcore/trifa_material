import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap // <--- Required for the AWT bridge
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class VideoStreamProcessor {
    // Compose state that the UI will observe
    var composeBitmap by mutableStateOf<ImageBitmap?>(null)
        private set

    private var currentWidth = 0
    private var currentHeight = 0

    // We use AWT BufferedImage purely as a fast, raw int[] pixel buffer
    private var awtImage: BufferedImage? = null
    private var pixels: IntArray? = null

    // Threading: A single thread that only processes the LATEST frame
    private var latestYuvData: ByteArray? = null
    private var latestWidth = 0
    private var latestHeight = 0
    @Volatile private var hasNewFrame = false
    @Volatile private var isRunning = true

    private val processingThread = thread(name = "VideoConverterThread", start = true) {
        var lastFrameReceivedTime = System.currentTimeMillis()

        while (isRunning) {
            if (hasNewFrame) {
                hasNewFrame = false
                lastFrameReceivedTime = System.currentTimeMillis() // Reset the timer on every new frame

                val data = latestYuvData
                val w = latestWidth
                val h = latestHeight

                if (data != null && w > 0 && h > 0) {
                    updateResolution(w, h)
                    convertYuvToArgbFast(data, w, h)
                    composeBitmap = awtImage!!.toComposeImageBitmap()
                }
            }

            // Smart Sleep Logic:
            // If no frame has arrived in over 500ms, sleep deeply (100ms).
            // Otherwise, sleep briefly (5ms) to maintain low latency for active video.
            val timeSinceLastFrame = System.currentTimeMillis() - lastFrameReceivedTime
            val sleepTime = if (timeSinceLastFrame > 500) 100L else 5L

            try {
                Thread.sleep(sleepTime)
            } catch (_: InterruptedException) {
                // Expected when release() interrupts the thread during app shutdown
                break
            }
        }
    }

    fun processFrame(yuvBuffer: ByteBuffer, w: Int, h: Int) {
        // Rewind and copy immediately so native code can reuse the direct memory safely
        yuvBuffer.rewind()
        val data = ByteArray(yuvBuffer.capacity())
        yuvBuffer.get(data)

        latestYuvData = data
        latestWidth = w
        latestHeight = h
        hasNewFrame = true
    }

    private fun updateResolution(w: Int, h: Int) {
        if (w != currentWidth || h != currentHeight) {
            currentWidth = w
            currentHeight = h
            // TYPE_INT_RGB gives us direct access to an int[] array
            awtImage = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            pixels = (awtImage!!.raster.dataBuffer as DataBufferInt).data
        }
    }

    private fun convertYuvToArgbFast(yuv: ByteArray, w: Int, h: Int) {
        val pixels = this.pixels ?: return
        val frameSize = w * h

        // Direct array access is 10x-50x faster than setRGB()
        for (j in 0 until h) {
            for (i in 0 until w) {
                val yIndex = j * w + i
                val uvIndex = (j / 2) * (w / 2) + (i / 2) + frameSize

                val Y = (yuv[yIndex].toInt() and 0xFF)
                val U = (yuv[uvIndex].toInt() and 0xFF)
                val V = (yuv[uvIndex + frameSize / 4].toInt() and 0xFF)

                var R = (Y + 1.4075 * (V - 128)).toInt()
                var G = (Y - 0.3455 * (U - 128) - (0.7169 * (V - 128))).toInt()
                var B = (Y + 1.7790 * (U - 128)).toInt()

                // Clamping
                if (R > 255) R = 255 else if (R < 0) R = 0
                if (G > 255) G = 255 else if (G < 0) G = 0
                if (B > 255) B = 255 else if (B < 0) B = 0

                // Direct memory write to the backing int[] array
                pixels[yIndex] = (0xFF shl 24) or (R shl 16) or (G shl 8) or B
            }
        }
    }

    fun clear() {
        // 1. Tell Compose to stop drawing the image (reveals the background color)
        composeBitmap = null
        // 2. Wipe the raw memory buffer to 0 (black/transparent) so no old pixels bleed into the next call
        pixels?.fill(0)
    }

    fun release() {
        isRunning = false
        processingThread.interrupt()
    }
}
