package org.axix.mirai.plugin.BetterPicSender

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.info
import org.axix.mirai.plugin.BetterPicSender.Tool.*
import xyz.cssxsh.mirai.hibernate.*

/**
 * 使用 kotlin 版请把
 * `src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin`
 * 文件内容改成 `org.example.mirai.plugin.PluginMain` 也就是当前主类全类名
 *
 * 使用 kotlin 可以把 java 源集删除不会对项目有影响
 *
 * 在 `settings.gradle.kts` 里改构建的插件名称、依赖库和插件版本
 *
 * 在该示例下的 [JvmPluginDescription] 修改插件名称，id和版本，etc
 *
 * 可以使用 `src/test/kotlin/RunMirai.kt` 在 ide 里直接调试，
 * 不用复制到 mirai-console-loader 或其他启动器中调试
 */

object BetterPicSender : KotlinPlugin(
    JvmPluginDescription(
        id = "org.axix.mirai.plugin.BetterPicSender",
        name = "BetterPicSender",
        version = "0.1.2"
    ) {
        author("轩晞宇-AXiX")
        info(
            """
            
        """.trimIndent()
        )
        dependsOn("xyz.cssxsh.mirai.plugin.mirai-hibernate-plugin", false)
        // author 和 info 可以删除.
    }
) {
    //用来储存两段式存图的索引
    private var senderList = mutableMapOf<String,String>()
    private val QuoteReply.originalMessageFromLocal: MessageChain
        get() = MiraiHibernateRecorder[source].firstOrNull()?.toMessageChain() ?: source.originalMessage

    private suspend fun dowpic(g:Group,m:MessageChain,flag:String,replyFlag:Boolean = true){
        dowall(m,senderList[flag])
        senderList.remove(flag)
        if(replyFlag){
            g.sendMessage(Config.successTip)
        }
    }

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //配置文件目录 "${dataFolder.absolutePath}/"
        Config.reload()
        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent>{
            //判断白名单
            if(group.id in Config.whiteGroupList){
                val senderFlag = group.id.toString()+sender.id.toString()
                if(senderFlag in senderList){
                    message[ForwardMessage.Key]?.run {
                        this.nodeList.forEach { dowpic(group,it.messageChain,senderFlag) }
                        group.sendMessage(Config.successTip)
                    }
                    message[Image.Key]?.run{ dowpic(group,message,senderFlag) }
                }else{
                    val ptext: PlainText? = message.findIsInstance<PlainText>()
                    if(ptext is PlainText){
                        //判断是否为存图指令，并处理得到tag
                        if(ptext.content.indexOf(Config.commands) == 0 ){
                            senderList.put(senderFlag, GetTag(ptext.content))
                            //如果是引用式存图
                            message[QuoteReply.Key]?.run{
                                //对转发消息的支持
                                originalMessageFromLocal[ForwardMessage.Key]?.run {
                                    this.nodeList.forEach { dowpic(group,it.messageChain,senderFlag,false) }
                                    group.sendMessage(Config.successTip)
                                }
                                originalMessageFromLocal[Image.Key]?.run{ dowpic(group,originalMessageFromLocal,senderFlag) }
                            }
                            //如果是一段式存图
                            message[Image.Key]?.run{ dowpic(group,message,senderFlag) }
                        }
                    }
                }
            }
            if(group.id in Config.autosaveList){
                message[ForwardMessage.Key]?.run {
                    this.nodeList.forEach { dowall(it.messageChain,"","auto")}
                }
                message[Image.Key]?.run{ dowall(message,"","auto") }
            }
        }
        eventChannel.subscribeAlways<FriendMessageEvent>{

        }
        eventChannel.subscribeAlways<NewFriendRequestEvent>{

        }
        eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent>{

        }
    }
}
