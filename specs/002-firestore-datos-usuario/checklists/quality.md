# Requirements Quality Checklist: Persistencia Remota de Datos de Usuario en Firestore

**Purpose**: Validar completitud, claridad y consistencia de los requisitos de
`002-firestore-datos-usuario/spec.md` (unit tests para los requisitos, no para la
implementación)
**Created**: 2026-10-01
**Feature**: [spec.md](../spec.md)
**Depth**: Standard | **Audience**: Autor/Revisor antes de `/speckit.plan`

## Completitud de Requisitos

- [x] CHK001 - ¿Se especifica qué ocurre con los datos locales de Room/SharedPreferences
      una vez completada la migración de RF-D011 (se eliminan, quedan como caché, o
      permanecen huérfanos)? [Gap, Spec §RF-D011/RF-D006]
- [x] CHK002 - ¿Existe un Acceptance Scenario o Success Criteria que verifique
      específicamente que el cliente Android no puede escribir `puntosHistoricos`,
      `rachaActual` o `nivel` (RF-D014), más allá de la cobertura que US3 da a las
      contraseñas? [Gap, Spec §RF-D014] — Resuelto: SC-D006 agregado.
- [x] CHK003 - ¿Se documenta el riesgo de cold-start del componente de servidor de
      confianza (si se implementa como extensión del backend Render de spec 001, ya
      afectado por este riesgo en RF-074) para el nuevo flujo de otorgamiento de puntos de
      Caminar? [Gap, Spec §RF-D015]
- [x] CHK004 - ¿Se definen los campos concretos del espejo informativo de permisos
      (`usuarios/{uid}/permisosDispositivo`), más allá de describirlo como "espejo
      informativo"? [Gap, Spec Key Entities]
- [x] CHK005 - ¿Se especifica si las consultas de historial (RF-D003) deben soportar el
      orden/paginación que spec 001 ya exige ("últimos 3" + "ver más", RF-041/RF-042)?
      [Gap, Spec §RF-D003] — Resuelto: cláusula agregada a RF-D003.
- [x] CHK006 - ¿Existen Success Criteria o Edge Cases que cubran la sincronización de
      `pasosHoy` y `caminataEnCurso` entre dispositivos, más allá del cambio de
      perfil/actividad aprobada que ya cubre SC-D002? [Gap, Spec §SC-D002/RF-D007/RF-D008]
      — Resuelto: SC-D002 ampliado para cubrirlos explícitamente.
- [x] CHK007 - ¿Se documenta como dependencia/assumption la necesidad de aprovisionar y
      proteger la credencial de Firebase Admin SDK del componente de servidor de confianza
      (de forma análoga a `ECOGPT_SHARED_API_KEY` en spec 001)? [Gap, Dependency] —
      Resuelto: nueva Assumption agregada.
- [x] CHK008 - ¿Se define alguna salvaguarda (p. ej. período de gracia, confirmación
      reforzada) para la eliminación de cuenta (US5), más allá de "confirma la
      eliminación"? [Gap, Spec §US5]

## Claridad de Requisitos

- [x] CHK009 - ¿Está cuantificado "un número acotado de reintentos" en RF-D016 con un
      valor o rango concreto? [Clarity, Spec §RF-D016]
- [x] CHK010 - ¿Está cuantificado "un tiempo acotado y verificable" en SC-D005 con un valor
      concreto (p. ej. minutos u horas)? [Clarity, Spec §SC-D005]
- [x] CHK011 - ¿Se distingue con claridad suficiente "pasos de hoy" (RF-D007, telemetría
      diaria informativa) de la comparación podómetro-vs-meta de una caminata puntual que
      otorga puntos (RF-D015), para que no se confundan como el mismo mecanismo? [Clarity,
      Spec §RF-D007/RF-D015] — Resuelto: RF-D007 aclara explícitamente que no otorga
      puntos por sí solo.
- [x] CHK012 - ¿Especifica RF-D004 explícitamente que los campos que el cliente escribe
      "automáticamente" excluyen los campos restringidos por RF-D014, o queda a
      interpretación del lector? [Clarity, Spec §RF-D004/RF-D014] — Resuelto: exclusión
      explícita agregada a RF-D004.
- [x] CHK013 - ¿Se define "componente de servidor de confianza" con precisión suficiente
      para distinguirlo de las alternativas mencionadas (Cloud Function vs. extensión del
      backend EcoGPT), o se deja deliberadamente abierto para `/speckit.plan`? [Clarity,
      Spec Key Entities] — Resuelto: se aclaró explícitamente que queda abierto a
      `/speckit.plan`.

## Consistencia de Requisitos

- [x] CHK014 - ¿Son consistentes entre sí RF-D010 (rechaza acceso entre usuarios distintos)
      y RF-D014 (rechaza escritura de campos específicos dentro del propio documento), sin
      solaparse ni contradecirse en su redacción? [Consistency, Spec §RF-D010/RF-D014] —
      Resuelto: RF-D010 ahora remite explícitamente a RF-D014 para evitar solape.
- [x] CHK015 - ¿Usa el spec terminología consistente para el componente de servidor de
      confianza a lo largo de todo el documento (Clarifications, Requirements, Key
      Entities), o alterna entre "Cloud Function"/"backend EcoGPT"/"componente de servidor
      de confianza" sin aclarar que son la misma pieza aún no decidida? [Consistency,
      Terminology] — Verificado: el término canónico se usa en todas las secciones, con
      las alternativas siempre entre paréntesis.
- [x] CHK016 - ¿Es RF-D006 ("Room/SharedPreferences nunca como única copia") consistente
      con el tratamiento de la caché offline del SDK de Firestore descrito en
      Clarifications (que sí persiste localmente por diseño del propio SDK)? [Consistency,
      Spec §RF-D006/Clarifications] — Verificado: Clarifications ya distingue
      explícitamente la caché del SDK (permitida) de un almacén propio de la app (no
      permitido como única copia); no hay contradicción.
- [x] CHK017 - ¿Usan RF-D001 a RF-D017 verbos normativos consistentes (DEBE/NO DEBE), sin
      mezclar formas más débiles ("debería", "puede") que debiliten el carácter
      obligatorio de algún requisito? [Consistency, Spec §Requirements] — Verificado: los
      17 requisitos usan DEBE/NO DEBE de forma consistente, sin formas débiles.
- [x] CHK018 - ¿Es consistente el alcance declarado en "Relación con la Especificación 001"
      (excluye explícitamente `Actividad` y `PuntoVerde`) con el resto del documento,
      sin que ningún RF-D0xx reintroduzca esas entidades como dato de usuario? [Consistency,
      Spec §Relación con la Especificación 001] — Verificado tras corregir una
      inconsistencia menor (la tabla de mapeo tenía el esquema antiguo de
      `permisosDispositivo/{tipo}`, ya actualizado a `{dispositivoId}_{tipo}` para
      coincidir con RF-D009).

## Notes

- Checklist generado a partir de una lectura completa de `spec.md` tras la sesión de
  `/speckit.clarify` del 2026-10-01 (RF-D001 a RF-D017).
- Foco solicitado explícitamente: completitud, claridad y consistencia (no se incluyen
  categorías de UX, seguridad o performance como secciones propias, aunque algunos ítems
  las tocan tangencialmente donde afectan la calidad del requisito).
- Ningún ítem de este checklist verifica comportamiento de la app o del backend — todos
  evalúan si el *requisito* está completo, es claro o es consistente con el resto del
  documento.
- **18/18 ítems resueltos** (2026-10-01, segunda ronda): 6 mediante decisiones de negocio
  tomadas en cuestionario (CHK001, CHK003, CHK004, CHK008, CHK009, CHK010 — ver
  Clarifications → "Resolución de hallazgos de `checklists/quality.md`" en spec.md) y 12
  mediante ediciones directas de redacción/completitud sin necesitar una decisión nueva.
  El spec pasó de 17 a 17 RF-D (uno reforzado con cláusulas adicionales) más `SC-D006`
  nuevo, y de 5 a 6 Success Criteria.
