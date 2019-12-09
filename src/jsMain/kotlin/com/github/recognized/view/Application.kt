package com.github.recognized.view

import Fuzzer
import com.github.recognized.App
import com.github.recognized.horizontal
import com.github.recognized.service.Snippet
import com.github.recognized.service.SortOrder
import com.github.recognized.service.Statistics
import com.github.recognized.spring
import com.github.recognized.vertical
import contrib.ringui.header.ringHeader
import contrib.ringui.ringButton
import kotlinx.coroutines.*
import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.Time
import react.*
import react.dom.*
import styled.*
import kotlin.coroutines.CoroutineContext

object ApplicationStyles : StyleSheet("ApplicationStyles", isStatic = true)

data class ApplicationState(val stat: Statistics, val snippets: List<Snippet>)
class ApplicationProps : RProps

class Timestamp(val value: Int) : RState

class ApplicationComponent : RComponent<ApplicationProps, Timestamp>(), CoroutineScope {
    init {
        state = Timestamp(0)
    }

    private var st: ApplicationState = ApplicationState(Statistics(0, 0, 0.0, "Idle"), emptyList())

    private fun update(action: () -> Unit) {
        setState(transformState = {
            action()
            Timestamp(it.value + 1)
        })
    }

    override fun componentDidMount() {
        updateLoop()
        genLoop()
    }

    override fun componentWillUnmount() {
        coroutineContext.cancel()
    }

    override val coroutineContext: CoroutineContext = Job()

    private var sampleSizes = 20
    private var order = SortOrder.Score

    private fun updateLoop() {
        loop(1_000L) {
            val statNew = App.client.Fuzzer.stat()
            update {
                st = st.copy(stat = statNew)
            }
        }
    }

    private fun loop(delay: Long, action: suspend () -> Unit) {
        launch {
            while (true) {
                val snippets = try {
                    action()
                } catch (ex: Throwable) {
                    console.error(ex)
                    delay(delay)
                }
                delay(delay)
            }
        }
    }

    private fun genLoop() {
        loop(10_000L) {
            val snippets = App.client.Fuzzer.generation(0, sampleSizes, order)
            update {
                st = st.copy(snippets = snippets)
            }
        }
    }

    override fun RBuilder.render() {
        ringHeader {
            horizontal {
                css {
                    flex(1.0)
                    padding(horizontal = 16.px)
                    lineHeight = LineHeight("14px")
                    alignItems = Align.center
                    justifyContent = JustifyContent.center
                }
                h3 {
                    +"Kotlin fuzzer"
                }

                spring()

                ringButton {
                    attrs {
                        onMouseDown = {
                            launch {
                                App.client.Fuzzer.start()
                            }
                        }
                    }

                    +"Start"
                }

                table {
                    thead {
                        tr {
                            td {
                                +"Uptime"
                            }
                            td {

                                +"State"
                            }
                            td {
                                +"Compile rate"
                            }
                            td {
                                +"Iteration"
                            }
                        }
                    }
                    tbody {
                        tr {
                            val uptime = st.stat.uptime
                            td {
                                +"${(uptime / 60 / 60).twoDigit()}:${((uptime / 60) % 60).twoDigit()}:${(uptime % 60).twoDigit()}"
                            }
                            td {
                                +st.stat.state
                            }
                            td {
                                +"${(st.stat.compileSuccessRate * 100).toInt()}%"
                            }
                            td {
                                +st.stat.iterations
                            }
                        }
                    }
                }
            }
        }

        vertical {
            css {
                padding(16.px)
            }

            styledTable {
                css {
                    borderCollapse = BorderCollapse.collapse
                }
                thead {
                    tr {
                        td {
                            +"Snippet"
                        }
                        td {
                            +"Score"
                        }
                        td {
                            +"Jit time, ms"
                        }
                        td {
                            +"Psi count"
                        }
                        td {
                            +"Text length"
                        }
                    }
                }
                tbody {
                    st.snippets.forEach {
                        row(it)
                    }
                }
            }

            ringButton {
                attrs {
                    onMouseDown = {
                        sampleSizes += 20
                    }
                    text = true
                }

                +"Load more"
            }
        }
    }

    private fun RBuilder.row(sample: Snippet) {
        styledTr {
            css {
                padding(top = 4.px, bottom = 4.px)
                borderTop = "1px solid #e0dada"
            }
            key = sample.id
            td {
                +sample.id
            }
            td {
                +sample.value.toString()
            }
            td {
                +sample.metrics.jitTime.toString()
            }
            td {
                +sample.metrics.psiElements.toString()
            }
            td {
                +sample.metrics.symbols.toString()
            }
        }
    }
}


fun Number.twoDigit(): String {
    return toString().let { if (it.length == 1) "0$it" else it }
}