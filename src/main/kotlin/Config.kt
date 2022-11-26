package org.axix.mirai.plugin.BetterPicSender

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("config") {
    val whiteGroupList: MutableList<Long> by value(mutableListOf(615686876))
    val autosaveList: MutableList<Long> by value(mutableListOf(818035927))
    val adminQQ: Long by value<Long>(2879710747)
    val commands: String by value<String>("/s")
    val filePath: String by value<String>("""D:\pic\""")
    val defaultfp: String by value<String>("""main""")
    val autofp: String by value<String>("""autosave""")
    val successTip: String by value<String>("已保存")
    // 当网络连接出现故障时，重试的次数
    //val retryCount: Int by value(R.DEFAULT_RETRY_COUNT)

    // 冷却时间ms
    //val cd: Int by value(R.DEFAULT_CD)
}