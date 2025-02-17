/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


@file:Suppress("unused")

package net.mamoe.mirai.internal

import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.utils.BotConfiguration
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal val MockAccount = BotAccount(1, "pwd")

internal val MockConfiguration = BotConfiguration {
    randomDeviceInfo()
}

internal class MockBotBuilder(
    val conf: BotConfiguration = BotConfiguration(),
    val debugConf: BotDebugConfiguration = BotDebugConfiguration(),
) {
    var nhProvider: (QQAndroidBot.(bot: QQAndroidBot) -> NetworkHandler)? = null
    var componentsProvider: (QQAndroidBot.(bot: QQAndroidBot) -> ComponentStorage)? = null

    fun conf(action: BotConfiguration.() -> Unit): MockBotBuilder {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        conf.apply(action)
        return this
    }

    fun debugConf(action: BotDebugConfiguration.() -> Unit): MockBotBuilder {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        debugConf.apply(action)
        return this
    }

    fun networkHandlerProvider(provider: QQAndroidBot.(bot: QQAndroidBot) -> NetworkHandler): MockBotBuilder {
        this.nhProvider = provider
        return this
    }
}

@Suppress("TestFunctionName")
internal fun MockBot(conf: MockBotBuilder.() -> Unit = {}) =
    MockBotBuilder(MockConfiguration.copy()).apply(conf).run {
        object : QQAndroidBot(MockAccount, this.conf, debugConf) {
            override val components: ComponentStorage by lazy {
                componentsProvider?.invoke(this, this) ?: EMPTY_COMPONENT_STORAGE
            }

            override fun createNetworkHandler(): NetworkHandler =
                nhProvider?.invoke(this, this) ?: super.createNetworkHandler()
        }
    }

private val EMPTY_COMPONENT_STORAGE = ConcurrentComponentStorage()
