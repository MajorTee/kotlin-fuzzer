package com.github.recognized.dataset

import com.github.recognized.compile.PsiFacade
import com.github.recognized.metrics.Score
import com.github.recognized.service.Metrics
import org.jetbrains.kotlin.psi.KtElement
import org.kodein.di.Kodein
import org.kodein.di.generic.*

interface Corpus {
    fun samples(): List<Sample>
}

interface Sample {
    val tree: KtElement?
    val id: String?
    val metrics: Metrics?
}

class LazySample(private val facade: PsiFacade, private val file: String) : Sample {
    override val tree: KtElement? get() = facade.getPsi(file)
    override val id: String? get() = null
    override val metrics: Metrics? get() = null
}

class IdSample(override val id: String, val sample: Sample) : Sample by sample

class SampleWithMetrics(override val metrics: Metrics, val sample: Sample) : Sample by sample

fun Kodein.MainBuilder.corpuses() {
    bind() from setBinding<Corpus>()
    bind<Corpus>().inSet() with singleton { YouTrackCorpus(instance()) }
    bind<Corpus>().inSet() with singleton { KotlinTestsCorpus(instance()) }
    bind() from singleton { AllCorpuses(instance()) }
}

class AllCorpuses(private val data: Set<Corpus>) : Corpus {
    private val allSamples by lazy { data.flatMap { it.samples() } }

    override fun samples(): List<Sample> = allSamples
}