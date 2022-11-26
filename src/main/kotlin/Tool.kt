package org.axix.mirai.plugin.BetterPicSender.Tool

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import org.axix.mirai.plugin.BetterPicSender.Config
import java.io.File
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption


//根据URL下载图片的函数原型
fun downloadImg(url: URL, fileName:String, imagePath: String, tryCount: Int): String {
    return if (tryCount > 0) try {
        //val fileName = "${System.currentTimeMillis()}.jpg"
        // 基于NIO来下载网络上的图片
        FileChannel.open(
            Paths.get("$imagePath/$fileName"),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        ).use {
            it.transferFrom(Channels.newChannel(url.openStream()), 0, Long.MAX_VALUE)
        }
        fileName
    } catch (e: Exception) {
        //puppetmaster.logger.error("${e.javaClass.name} : $e.message")
        // 若发生网络异常，则进行有限次的重试
        downloadImg(url,fileName, imagePath, tryCount - 1)
    } else "err"
}

//一式两份下载
fun dow(picName:String,tag:String?,url:String,Flag:String = "cmd"){
    when(Flag){
        "cmd" -> {
            downloadImg(URL(url),picName, Config.filePath + Config.defaultfp,5)
            if(tag is String && tag != ""){
                val fp = File(Config.filePath + tag)
                if  (!fp.exists()  && !fp.isDirectory())
                {
                    fp.mkdir()
                }
                downloadImg(URL(url),picName, Config.filePath + tag,5)
            }
        }
        "auto" -> {
            downloadImg(URL(url),picName, Config.filePath + Config.autofp,5)
        }
    }
}

//一次性下载整个发言中的图片
suspend fun dowall(msg: MessageChain, tag: String?,Flag: String = "cmd") {
    msg.forEach {
        if (it is Image) {
            if (Getext(it.imageId) in Config.dowPicType) {
                dow(it.imageId, tag, it.queryUrl(),Flag)
            }else if(Getext(it.imageId) == ".mirai"){
                dow(it.imageId+".jpg", tag, it.queryUrl(),Flag)
            }
        }
    }
}
fun Getext(id:String):String{
    return  id.substring(id.indexOf('}')+1,id.length)
}

//获取tag并预处理
fun GetTag(rawcontect:String):String{
    var tag = rawcontect.substring(2,rawcontect.length)
    return tag.replace("\\s".toRegex(),"")
}