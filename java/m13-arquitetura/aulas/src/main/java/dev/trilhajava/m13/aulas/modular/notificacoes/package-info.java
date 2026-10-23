/** Módulo notificacoes: avisa o paciente. Da agenda, só pode usar os eventos. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Notificações",
        allowedDependencies = "agenda :: eventos")
package dev.trilhajava.m13.aulas.modular.notificacoes;
