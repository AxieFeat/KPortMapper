package xyz.axie.portmapper

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dosse.upnp.UPnP

@Composable
@Preview
fun App() {
    val isAvailable = remember { UPnP.isUPnPAvailable() }

    var portText by remember { mutableStateOf("") }
    var ports by remember { mutableStateOf(setOf<Int>()) }
    var isValidPorts by remember { mutableStateOf(true) }
    var showPortManager by remember { mutableStateOf(false) }

    val openedPorts = remember { mutableStateListOf<Pair<Int, Port>>() }

    fun openPorts(type: Port, vararg newPorts: Int) {
        openedPorts.addAll(newPorts.map {
            it.also {
                when(type) {
                    Port.TCP -> UPnP.openPortTCP(it)
                    Port.UDP -> UPnP.openPortUDP(it)
                }
            } to type
        }.filterNot { it in openedPorts })
        println("Opened ports in ${type.name}: ${newPorts.joinToString(", ")}")
    }

    fun closePorts(type: Port, vararg toClose: Int) {
        openedPorts.removeAll(toClose.map {
            it.also {
                when (type) {
                    Port.TCP -> UPnP.closePortTCP(it)
                    Port.UDP -> UPnP.closePortUDP(it)
                }
            } to type
        }.toSet())
        println("Closed ports in ${type.name}: ${toClose.joinToString(", ")}")
    }

    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isAvailable) {
                Text(
                    "UPnP is not available on this network.",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.h6
                )
            }

            OutlinedTextField(
                value = portText,
                onValueChange = { newValue ->
                    portText = newValue
                    val split = newValue.split(", ").filter { it.isNotEmpty() }

                    val parsedPorts = split.mapNotNull { it.toIntOrNull() }
                    isValidPorts = split.size == parsedPorts.size && parsedPorts.all { it in 0..65535 }

                    if (isValidPorts) {
                        ports = parsedPorts.toSet()
                    }
                },
                label = { Text("Port (0–65535). Separate with ', '") },
                isError = !isValidPorts,
                modifier = Modifier.fillMaxWidth(),
                enabled = isAvailable
            )

            if (!isValidPorts && isAvailable) {
                Text(
                    "Cannot parse ports",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { openPorts(Port.TCP, *ports.toIntArray()) },
                    enabled = isAvailable && isValidPorts && ports.isNotEmpty()
                ) {
                    Text("Open TCP")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { openPorts(Port.UDP, *ports.toIntArray()) },
                    enabled = isAvailable && isValidPorts && ports.isNotEmpty()
                ) {
                    Text("Open UDP")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { closePorts(Port.TCP, *ports.toIntArray()) },
                    enabled = isAvailable && isValidPorts && ports.isNotEmpty()
                ) {
                    Text("Close TCP")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { closePorts(Port.UDP, *ports.toIntArray()) },
                    enabled = isAvailable && isValidPorts && ports.isNotEmpty()
                ) {
                    Text("Close UDP")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = { showPortManager = true },
                    enabled = isAvailable
                ) {
                    Text("Manage Ports")
                }
            }

            if (showPortManager && isAvailable) {
                Window(
                    onCloseRequest = { showPortManager = false },
                    title = "Port Manager"
                ) {
                    MaterialTheme {
                        Column(
                            modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
                        ) {
                            Text("Opened Ports:", style = MaterialTheme.typography.h6)
                            Spacer(Modifier.height(8.dp))
                            if (openedPorts.isEmpty()) {
                                Text("No ports are currently open.")
                            } else {
                                openedPorts.forEach { (port, type) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$port - ${type.name}")
                                        IconButton(onClick = {
                                            closePorts(type, port)
                                        }) {
                                            Text("×", style = MaterialTheme.typography.h6)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
