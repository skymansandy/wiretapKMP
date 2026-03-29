package dev.skymansandy.wiretapsample.model

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.StateFlow

data class SampleAction(
    val label: String,
    val color: Color,
    val category: ActionCategory,
)

data class SampleMessage(
    val type: MessageType,
    val text: String,
) {

    enum class MessageType { Sent, Received, System }
}

interface HttpSampleActions {

    val statusLog: StateFlow<String>
    val actions: List<SampleAction>
    fun executeAction(index: Int)
}

interface WsSampleActions {

    val isConnected: StateFlow<Boolean>
    val isConnecting: StateFlow<Boolean>
    val servers: List<Pair<String, String>>
    val selectedServerIndex: StateFlow<Int>
    val messageLog: SnapshotStateList<SampleMessage>
    fun selectServer(index: Int)
    fun toggleConnection()
    fun sendMessage(text: String)
}
