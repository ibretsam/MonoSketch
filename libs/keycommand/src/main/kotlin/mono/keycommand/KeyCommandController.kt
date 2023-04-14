/*
 * Copyright (c) 2023, tuanchauict
 */

package mono.keycommand

import mono.common.commandKey
import mono.environment.Build
import mono.livedata.LiveData
import mono.livedata.MutableLiveData
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.KeyboardEvent

/**
 * A controller class to identify command by keyboard.
 */
class KeyCommandController(private val body: HTMLElement) {
    private val keyCommandMutableLiveData: MutableLiveData<KeyCommand> =
        MutableLiveData(KeyCommand.IDLE)
    val keyCommandLiveData: LiveData<KeyCommand> = keyCommandMutableLiveData

    init {
        body.onkeydown = ::updateCommand
        body.onkeyup = { resetKeyCommand() }
    }

    private fun updateCommand(event: KeyboardEvent) {
        val keyCommand = if (event.target == body) {
            KeyCommand.getCommandByKey(event.keyCode, event.commandKey, event.shiftKey)
        } else {
            KeyCommand.IDLE
        }

        if (!keyCommand.isKeyEventPropagationAllowed) {
            event.stopPropagation()
            event.preventDefault()
        }
        keyCommandMutableLiveData.value = keyCommand
        if (Build.DEBUG) {
            println("Key press ${event.code} : ${event.keyCode} cmd ${event.commandKey}")
        }
        if (keyCommand.isRepeatable) {
            keyCommandMutableLiveData.value = KeyCommand.IDLE
        }
    }

    private fun resetKeyCommand() {
        keyCommandMutableLiveData.value = KeyCommand.IDLE
    }
}
