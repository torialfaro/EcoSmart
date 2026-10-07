package com.ecosmart.application.auth

import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk

/**
 * Ayuda a testear código que usa `Task<T>.await()` (kotlinx-coroutines-play-services):
 * si `isComplete == true`, `await()` lee `exception`/`result` directamente sin
 * necesitar un listener real — alcanza con stubear estas propiedades.
 */
internal fun <T> tareaExitosa(resultado: T): Task<T> {
    val tarea = mockk<Task<T>>()
    every { tarea.isComplete } returns true
    every { tarea.isCanceled } returns false
    every { tarea.exception } returns null
    every { tarea.result } returns resultado
    return tarea
}

internal fun <T> tareaFallida(error: Exception): Task<T> {
    val tarea = mockk<Task<T>>()
    every { tarea.isComplete } returns true
    every { tarea.isCanceled } returns false
    every { tarea.exception } returns error
    return tarea
}
