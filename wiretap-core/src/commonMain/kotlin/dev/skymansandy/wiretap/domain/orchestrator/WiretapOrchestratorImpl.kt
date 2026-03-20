package dev.skymansandy.wiretap.domain.orchestrator

internal class WiretapOrchestratorImpl(
    httpOrchestrator: HttpOrchestrator,
    socketOrchestrator: SocketOrchestrator,
) : WiretapOrchestrator,
    HttpOrchestrator by httpOrchestrator,
    SocketOrchestrator by socketOrchestrator
