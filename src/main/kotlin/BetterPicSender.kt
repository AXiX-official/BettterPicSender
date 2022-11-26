package org.axix.mirai.plugin.BetterPicSender

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.info
import java.net.URL
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
        version = "0.1.1"
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

    private var tag:String = ""
    private var cmd:String = ""
    //用来储存两段式存图的索引
    private var senderList = mutableMapOf<String,String>()
    private var savepath:String = ""
    private val QuoteReply.originalMessageFromLocal: MessageChain
        get() = MiraiHibernateRecorder[source].firstOrNull()?.toMessageChain() ?: source.originalMessage
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //配置文件目录 "${dataFolder.absolutePath}/"
        Config.reload()
        cmd = Config.commands
        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent>{
            //判断白名单
            if(group.id in Config.whiteGroupList){
                val senderFlag = group.id.toString()+sender.id.toString()
                if(senderFlag in senderList){
                    dowall(message,senderList[senderFlag])
                    senderList.remove(senderFlag)
                    group.sendMessage(Config.successTip)
                }else{
                    val ptext: PlainText? = message.findIsInstance<PlainText>()
                    if(ptext is PlainText){
                        //group.sendMessage(ptext.toString())
                        //判断是否为存图指令
                        if(ptext.content.indexOf(cmd) == 0 ){
                            tag = ptext.content
                            tag = tag.substring(2,tag.length)
                            tag = tag.replace("\\s".toRegex(),"")
                            senderList.put(senderFlag, tag)
                            //如果是引用式存图
                            message[QuoteReply.Key]?.run{
                                //group.sendMessage(originalMessageFromLocal)
                                dowall(originalMessageFromLocal,senderList[senderFlag])
                                senderList.remove(senderFlag)
                                group.sendMessage(Config.successTip)
                            }
                            //如果是一段式存图
                            message[Image.Key]?.run{
                                dowall(message,senderList[senderFlag])
                                senderList.remove(senderFlag)
                                group.sendMessage(Config.successTip)
                            }
                        }
                    }
                }
            }else if(group.id in Config.autosaveList){
                message.forEach{
                    if(it is Image){
                        if(Getext(it.imageId) in Config.dowPicType){
                            downloadImg(URL(it.queryUrl()),it.imageId,savepath+ Config.autofp,5)
                        }
                    }
                }
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
