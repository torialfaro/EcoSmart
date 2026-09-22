package com.ecosmart.infrastructure.sensors

import android.content.Context
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Captura una foto con CameraX (research.md §1.3, RF-025). El binding de
 * la preview a un `LifecycleOwner`/`PreviewView` vive en
 * `VerificacionFotoScreen` (T079) — a esta clase le corresponde ejecutar
 * la captura sobre un [ImageCapture] ya vinculado y guardar el resultado,
 * y copiar a un archivo local lo elegido desde la Galería (Photo Picker).
 */
@Singleton
class CamaraProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun crearArchivoTemporalParaFoto(): File =
        File.createTempFile("ecosmart_foto_", ".jpg", context.cacheDir)

    suspend fun capturarFoto(imageCapture: ImageCapture, archivoDestino: File): Uri =
        suspendCancellableCoroutine { continuation ->
            val opciones = ImageCapture.OutputFileOptions.Builder(archivoDestino).build()
            imageCapture.takePicture(
                opciones,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        continuation.resume(Uri.fromFile(archivoDestino))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                },
            )
        }

    /** Copia la imagen elegida en el Photo Picker (un `content://` Uri) a un archivo local. */
    fun copiarDesdeGaleria(uri: Uri): File {
        val destino = crearArchivoTemporalParaFoto()
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        return destino
    }
}

/** RF-025 — selector de imágenes del sistema (Photo Picker); no requiere permiso de Galería en Android 13+. */
object SelectorGaleria {
    val solicitudSoloImagenes = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
}
