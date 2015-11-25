package org.jetbrains.ktor.launcher

import com.typesafe.config.*
import org.jetbrains.ktor.application.*
import java.io.*
import java.net.*

fun buildDefaultConfig(args: Array<String>): ApplicationConfig {
    val argsMap = args.map { it.splitPair('=') }.filterNotNull().toMap()

    val jar = argsMap["-jar"]?.let { File(it).toURI().toURL() }
    val argConfig = ConfigFactory.parseMap(argsMap.filterKeys { it.startsWith("-P:") }.mapKeys { it.key.removePrefix("-P:") }, "Command-line options")
    val combinedConfig = argConfig.withFallback(ConfigFactory.load())

    val applicationIdPath = "ktor.application.id"
    val applicationId = if (combinedConfig.hasPath(applicationIdPath)) combinedConfig.getString(applicationIdPath) else "Application"
    val log = SLF4JApplicationLog(applicationId)
    val classLoader = jar?.let { URLClassLoader(arrayOf(jar), ApplicationConfig::class.java.classLoader) }
            ?: ApplicationConfig::class.java.classLoader
    val appConfig = ApplicationConfig(combinedConfig, classLoader, log)
    log.info(combinedConfig.getObject("ktor").render())

    return appConfig
}

private fun String.splitPair(ch: Char): Pair<String, String>? = indexOf(ch).let { idx ->
    when (idx) {
        -1 -> null
        else -> Pair(take(idx), drop(idx + 1))
    }
}

