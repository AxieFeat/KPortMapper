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
import xyz.axie.portmapper.i18n.I18n
import xyz.axie.portmapper.i18n.Language

@Composable
@Preview
fun App() {
    var language by remember {
        mutableStateOf(Language.systemLanguage())
    }

    val i18n = remember(language) {
        I18n(language)
    }

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
            var languageMenuExpanded by remember {
                mutableStateOf(false)
            }

            Box {
                Button(
                    onClick = {
                        languageMenuExpanded = true
                    }
                ) {
                    Text(
                        when (language) {
                            Language.ENGLISH -> i18n["language.english"]
                            Language.RUSSIAN -> i18n["language.russian"]
                        }
                    )
                }

                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = {
                        languageMenuExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            language = Language.ENGLISH
                            languageMenuExpanded = false
                        }
                    ) {
                        Text(i18n["language.english"])
                    }

                    DropdownMenuItem(
                        onClick = {
                            language = Language.RUSSIAN
                            languageMenuExpanded = false
                        }
                    ) {
                        Text(i18n["language.russian"])
                    }
                }
            }

            if (!isAvailable) {
                Text(
                    i18n["upnp.unavailable"],
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
                    isValidPorts =
                        split.size == parsedPorts.size &&
                                parsedPorts.all { it in 0..65535 }

                    if (isValidPorts) {
                        ports = parsedPorts.toSet()
                    }
                },
                label = { Text(i18n["port.label"]) },
                isError = !isValidPorts,
                modifier = Modifier.fillMaxWidth(),
                enabled = isAvailable
            )

            if (!isValidPorts && isAvailable) {
                Text(
                    i18n["port.invalid"],
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
                    Text(i18n["port.open_tcp"])
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { openPorts(Port.UDP, *ports.toIntArray()) },
                    enabled = isAvailable && isValidPorts && ports.isNotEmpty()
                ) {
                    Text(i18n["port.open_udp"])
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
                    Text(i18n["port.close_tcp"])
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { closePorts(Port.UDP, *ports.toIntArray()) },
                    enabled = isAvailable && isValidPorts && ports.isNotEmpty()
                ) {
                    Text(i18n["port.close_udp"])
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
                    Text(i18n["ports.manage"])
                }
            }

            if (showPortManager && isAvailable) {
                Window(
                    onCloseRequest = { showPortManager = false },
                    title = i18n["ports.manager_title"]
                ) {
                    MaterialTheme {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                i18n["ports.opened"],
                                style = MaterialTheme.typography.h6
                            )

                            Spacer(Modifier.height(8.dp))

                            if (openedPorts.isEmpty()) {
                                Text(i18n["ports.empty"])
                            } else {
                                openedPorts.forEach { (port, type) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$port - ${type.name}")

                                        IconButton(
                                            onClick = {
                                                closePorts(type, port)
                                            }
                                        ) {
                                            Text(
                                                "×",
                                                style = MaterialTheme.typography.h6
                                            )
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
    Window(
        onCloseRequest = ::exitApplication,
        title = "KPortMapper",
    ) {
        App()
    }
}
