package xyz.axie.portmapper

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.dosse.upnp.UPnP
import xyz.axie.portmapper.component.ContentCard
import xyz.axie.portmapper.i18n.I18n
import xyz.axie.portmapper.i18n.Language
import xyz.axie.portmapper.theme.AppTheme

@Composable
@Preview
fun App() {
    var language by remember {
        mutableStateOf(Language.systemLanguage())
    }

    val i18n = remember(language) {
        I18n(language)
    }

    val isAvailable = remember {
        UPnP.isUPnPAvailable()
    }

    var portText by remember {
        mutableStateOf("")
    }

    var ports by remember {
        mutableStateOf(setOf<Int>())
    }

    var isValidPorts by remember {
        mutableStateOf(true)
    }

    val openedPorts = remember {
        mutableStateListOf<Pair<Int, Port>>()
    }

    fun openPorts(type: Port, vararg newPorts: Int) {
        val portsToOpen = newPorts
            .map { it to type }
            .filterNot { it in openedPorts }

        portsToOpen.forEach { (port, portType) ->
            when (portType) {
                Port.TCP -> UPnP.openPortTCP(port)
                Port.UDP -> UPnP.openPortUDP(port)
            }
        }

        openedPorts.addAll(portsToOpen)

        println(
            "Opened ports in ${type.name}: ${
                portsToOpen.joinToString(", ") { it.first.toString() }
            }"
        )
    }

    fun closePorts(type: Port, vararg toClose: Int) {
        toClose.forEach { port ->
            when (type) {
                Port.TCP -> UPnP.closePortTCP(port)
                Port.UDP -> UPnP.closePortUDP(port)
            }
        }

        openedPorts.removeAll(
            toClose.map { it to type }.toSet()
        )

        println(
            "Closed ports in ${type.name}: ${toClose.joinToString(", ")}"
        )
    }

    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Header(
                        language = language,
                        i18n = i18n,
                        onLanguageChange = { newLanguage ->
                            language = newLanguage
                        }
                    )

                    UpnpStatus(
                        isAvailable = isAvailable,
                        i18n = i18n
                    )

                    PortControls(
                        portText = portText,
                        onPortTextChange = { newValue ->
                            portText = newValue

                            val split = newValue
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }

                            val parsedPorts = split.mapNotNull {
                                it.toIntOrNull()
                            }

                            isValidPorts =
                                split.size == parsedPorts.size &&
                                        parsedPorts.all {
                                            it in 0..65535
                                        }

                            if (isValidPorts) {
                                ports = parsedPorts.toSet()
                            }
                        },
                        ports = ports,
                        isValidPorts = isValidPorts,
                        isAvailable = isAvailable,
                        i18n = i18n,
                        onOpenTcp = {
                            openPorts(
                                Port.TCP,
                                *ports.toIntArray()
                            )
                        },
                        onCloseTcp = {
                            closePorts(
                                Port.TCP,
                                *ports.toIntArray()
                            )
                        },
                        onOpenUdp = {
                            openPorts(
                                Port.UDP,
                                *ports.toIntArray()
                            )
                        },
                        onCloseUdp = {
                            closePorts(
                                Port.UDP,
                                *ports.toIntArray()
                            )
                        }
                    )

                    OpenedPortsSection(
                        i18n = i18n,
                        openedPorts = openedPorts,
                        onClosePort = { type, port ->
                            closePorts(type, port)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    language: Language,
    i18n: I18n,
    onLanguageChange: (Language) -> Unit
) {
    var languageMenuExpanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "KPortMapper",
                color = AppTheme.textColor,
                style = MaterialTheme.typography.h4
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = i18n["app.subtitle"],
                style = MaterialTheme.typography.body2,
                color = AppTheme.textColor.copy(
                    alpha = ContentAlpha.medium
                )
            )
        }

        Box {
            TextButton(
                onClick = {
                    languageMenuExpanded = true
                }
            ) {
                Text(
                    text = when (language) {
                        Language.ENGLISH -> "English"
                        Language.RUSSIAN -> "Русский"
                    },
                    color = AppTheme.textColor,
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = "▼",
                    color = AppTheme.accentColor,
                )
            }

            if (languageMenuExpanded) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(0, 44),
                    onDismissRequest = {
                        languageMenuExpanded = false
                    },
                    focusable = true,
                ) {
                    Surface(
                        color = AppTheme.backgroundColor,
                        shape = RoundedCornerShape(10.dp),
                        elevation = 8.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .width(150.dp)
                                .padding(vertical = 6.dp)
                        ) {
                            LanguageMenuItem(
                                text = "English",
                                selected = language == Language.ENGLISH,
                                onClick = {
                                    onLanguageChange(Language.ENGLISH)
                                    languageMenuExpanded = false
                                }
                            )

                            LanguageMenuItem(
                                text = "Русский",
                                selected = language == Language.RUSSIAN,
                                onClick = {
                                    onLanguageChange(Language.RUSSIAN)
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageMenuItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 8.dp,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = AppTheme.textColor,
            )

            if (selected) {
                Text(
                    text = "✓",
                    color = AppTheme.accentColor,
                )
            }
        }
    }
}

@Composable
private fun UpnpStatus(
    isAvailable: Boolean,
    i18n: I18n
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isAvailable) {
            MaterialTheme.colors.primary.copy(
                alpha = 0.10f
            )
        } else {
            MaterialTheme.colors.error.copy(
                alpha = 0.10f
            )
        }
    ) {
        Text(
            text = if (isAvailable) {
                i18n["upnp.available"]
            } else {
                i18n["upnp.unavailable"]
            },
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            color = if (isAvailable) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.error
            },
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun PortControls(
    portText: String,
    onPortTextChange: (String) -> Unit,
    ports: Set<Int>,
    isValidPorts: Boolean,
    isAvailable: Boolean,
    i18n: I18n,
    onOpenTcp: () -> Unit,
    onCloseTcp: () -> Unit,
    onOpenUdp: () -> Unit,
    onCloseUdp: () -> Unit
) {
    val actionsEnabled = isAvailable && isValidPorts && ports.isNotEmpty()

    ContentCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = portText,
                onValueChange = onPortTextChange,
                label = {
                    Text(i18n["port.label"])
                },
                trailingIcon = {
                    if (portText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onPortTextChange("")
                            }
                        ) {
                            Text(
                                text = "×",
                                color = AppTheme.textColor,
                                style = MaterialTheme.typography.h6
                            )
                        }
                    }
                },
                singleLine = true,
                isError = !isValidPorts,
                modifier = Modifier.fillMaxWidth(),
                enabled = isAvailable,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = AppTheme.textColor,
                    backgroundColor = AppTheme.backgroundColor,
                    focusedBorderColor = AppTheme.textColor.copy(alpha = 0.70f),
                    unfocusedLabelColor = AppTheme.textColor.copy(alpha = 0.90f),
                    focusedLabelColor = AppTheme.textColor,
                    cursorColor = AppTheme.textColor,
                )
            )

            if (!isValidPorts && isAvailable) {
                Text(
                    text = i18n["port.invalid"],
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
                )
            }

            Divider()

            ProtocolActions(
                protocol = "TCP",
                openText = i18n["port.open_tcp"],
                closeText = i18n["port.close_tcp"],
                enabled = actionsEnabled,
                onOpen = onOpenTcp,
                onClose = onCloseTcp
            )

            ProtocolActions(
                protocol = "UDP",
                openText = i18n["port.open_udp"],
                closeText = i18n["port.close_udp"],
                enabled = actionsEnabled,
                onOpen = onOpenUdp,
                onClose = onCloseUdp
            )
        }
    }
}

@Composable
private fun ProtocolActions(
    protocol: String,
    openText: String,
    closeText: String,
    enabled: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = protocol,
            color = AppTheme.textColor,
            style = MaterialTheme.typography.subtitle1
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpen,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppTheme.accentColor.copy(alpha = 0.5f),
                )
            ) {
                Text(
                    text = openText,
                )
            }

            OutlinedButton(
                onClick = onClose,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = closeText,
                )
            }
        }
    }
}

@Composable
private fun OpenedPortsSection(
    i18n: I18n,
    openedPorts: List<Pair<Int, Port>>,
    onClosePort: (Port, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = i18n["ports.opened"],
            color = AppTheme.textColor,
            style = MaterialTheme.typography.h6
        )

        ContentCard {
            if (openedPorts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i18n["ports.empty"],
                        color = AppTheme.textColor.copy(
                            alpha = ContentAlpha.medium
                        ),
                        style = MaterialTheme.typography.body2
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    openedPorts.forEachIndexed { index, (port, type) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 8.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = port.toString(),
                                modifier = Modifier.weight(1f),
                                color = AppTheme.textColor,
                                style = MaterialTheme.typography.subtitle1
                            )

                            Text(
                                text = type.name,
                                color = AppTheme.textColor.copy(
                                    alpha = ContentAlpha.medium
                                ),
                                style = MaterialTheme.typography.body2
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            IconButton(
                                onClick = {
                                    onClosePort(type, port)
                                }
                            ) {
                                Text(
                                    text = "×",
                                    color = MaterialTheme.colors.error,
                                    style = MaterialTheme.typography.h6
                                )
                            }
                        }

                        if (index != openedPorts.lastIndex) {
                            Divider()
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
        state = rememberWindowState(
            width = 650.dp,
            height = 800.dp
        )
    ) {
        App()
    }
}