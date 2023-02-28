import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.io.StringReader
import java.nio.channels.Channels

import javax.xml.parsers.DocumentBuilderFactory


fun main(args: Array<String>) {
    parseLexFridmanRSS()
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}

private fun parseLexFridmanRSS() {
    val playlists = Files.readAllLines(Path.of("MyPlaylists.txt")).toMutableList()
    println(playlists)
    val feedTitleToLink = HashMap<String, String>(playlists.size)
    val s = URL("https://lexfridman.com/feed/podcast/").readText()
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(InputSource(StringReader(s)))
    println("Getting items")
    doc.getElementsByTagName("item").forEachIndexed { item: Node, index: Int ->
        println("Item: $item, index: $index")
        var title: String? = null
        var link: String? = null
        item.childNodes.loopWhile { node, i ->
            if (node is Element) {
                println(node.tagName)
                println(node.nodeValue)
                println(node.textContent)
                println(node.attributes)
                when (node.tagName) {
                    "title" -> title = node.textContent
                    "enclosure" -> link = node.getAttribute("url")
                }
            }
            title == null || link == null
        }
        println(title)
        println(link)
        val inPodcasts = title!! in playlists
        println(inPodcasts)
        if (inPodcasts) feedTitleToLink[title!!] = link!!
    }
    val dir = File(System.getProperty("user.dir"))
    val mp3sIndir =
        dir.walk().mapNotNull { if (it.name.endsWith(".mp3")) it.name.substringBefore("-") else null }.toList().sorted()
    println("MP3s in dir: $mp3sIndir")
    feedTitleToLink.filterNot {/*already downloaded*/ it.key.substring(
        1,
        4
    ) in mp3sIndir
    }.entries.also { println("Entries: $it") }.parallelStream().forEach {
        downloadFile(it.value, it.key + ".mp3", dir)
    }
}

private fun downloadFile(url: String, filename: String, dir: File) {
    val sanitizedFileName = filename
        .replace(
            "[^\\w_]|(files|file|Dateien|fichiers|bestanden|archivos|filer|tiedostot|pliki|soubory|elemei|ficheiros|arquivos|dosyalar|datoteke|fitxers|failid|fails|bylos|fajlovi|fitxategiak)$"
                .toRegex(),
            "-"
        )
        .replace("-{2,}".toRegex(), "-")
        .replace("-mp3", ".mp3")
        .removePrefix("-")
    println("Downloading $url to $dir/$sanitizedFileName")
    FileOutputStream(File(dir, sanitizedFileName))
        .channel
        .transferFrom(
            Channels.newChannel(URL(url).openStream()),
            0,
            Long.MAX_VALUE
        )
    println("Downloaded $url")
}
private fun NodeList.forEachIndexed(function: (Node, Int) -> Unit) {
    repeat(length) { i ->
        function(item(i), i)
    }
}
private fun NodeList.loopWhile(function: (Node, Int) -> Boolean) {
    var i = 0
    var predicate = false
    do {
        item(i)?.let {
            predicate = function(it, i)
        }
        i++
    } while(predicate)
}
