package org.axix.mirai.plugin.BetterPicSender
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info
import java.io.File
import java.net.URL
import org.axix.mirai.plugin.BetterPicSender.Tool.*

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
        version = "0.1.0"
    ) {
        author("轩晞宇-AXiX")
        info(
            """
            
        """.trimIndent()
        )
        // author 和 info 可以删除.
    }
) {

    private var tag:String = ""
    private var cmd:String = ""
    private var senderList = mutableMapOf<String,String>()
    private var savepath:String = ""

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //配置文件目录 "${dataFolder.absolutePath}/"
        Config.reload()
        //reloadPluginConfig(Config)
        cmd = Config.commands
        savepath =  Config.filePath

        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent>{
            //判断白名单
            if(group.id in Config.whiteGroupList){
                val senderFlag = group.id.toString()+sender.id.toString()
                if(senderFlag in senderList){
                    message.forEach{
                        if(it is Image){
                            val picName = it.imageId
                            val fp = File(savepath+ senderList[senderFlag])
                            //group.sendMessage(picName)
                            if  (!fp.exists()  && !fp.isDirectory())
                            {
                                fp.mkdir()
                            }
                            downloadImg(URL(it.queryUrl()),picName,savepath+ Config.defaultfp,5)
                            if(senderList[senderFlag] != ""){
                                downloadImg(URL(it.queryUrl()),picName,savepath+ senderList[senderFlag],5)
                            }
                            group.sendMessage(Config.successTip)
                        }
                    }
                    senderList.remove(senderFlag)
                }else{
                    message.forEach{
                        if(it is PlainText){
                            //如果发送的是正确的cmd
                            if(it.content.indexOf(cmd) != -1 ){
                                tag = it.content
                                tag = tag.substring(2+tag.indexOf(cmd),tag.length)
                                //group.sendMessage(tag)
                                tag = tag.replace("\\s".toRegex(),"")
                                senderList.put(senderFlag, tag)
                            }
                        }
                    }
                }
            }else if(group.id in Config.autosaveList){
                message.forEach{
                    if(it is Image){
                        downloadImg(URL(it.queryUrl()),it.imageId,savepath+ Config.autofp,5)
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
