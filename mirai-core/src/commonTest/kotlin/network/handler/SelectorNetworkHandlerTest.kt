/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import io.netty.channel.Channel
import net.mamoe.mirai.internal.network.framework.AbstractRealNetworkHandlerTest
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.impl.netty.AbstractNettyNHTest
import net.mamoe.mirai.internal.network.impl.netty.TestNettyNH
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.cast
import org.junit.jupiter.api.Test
import java.net.SocketAddress
import kotlin.test.assertFails

internal class SelectorNetworkHandlerTest : AbstractRealNetworkHandlerTest<SelectorNetworkHandler>() {
    val channel = AbstractNettyNHTest.NettyNHTestChannel()

    private val selector = TestSelector {
        object : TestNettyNH(bot, createContext(), address) {
            override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel {
                return channel
            }
        }
    }

    override val factory: NetworkHandlerFactory<SelectorNetworkHandler> =
        object : NetworkHandlerFactory<SelectorNetworkHandler> {
            override fun create(context: NetworkHandlerContext, address: SocketAddress): SelectorNetworkHandler {
                return SelectorNetworkHandler(context, selector)
            }
        }

    override val network: SelectorNetworkHandler get() = bot.network.cast()

    @Test
    fun `stop on manual close`() = runBlockingUnit {
        network.resumeConnection()
        network.close(IllegalStateException("Closed by test"))
        assertFails { network.resumeConnection() }
    }
}