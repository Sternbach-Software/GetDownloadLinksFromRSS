import java.io.File
import java.nio.file.Files

fun main() {
    for(file in File(System.getProperty("user.dir")).walk().filter { it.name.endsWith(".mp3") }) {
        println("Name:      ${file.name}")
        val sanitizedFileName = file.name
            .replace(
                "[^\\w_]|(files|file|Dateien|fichiers|bestanden|archivos|filer|tiedostot|pliki|soubory|elemei|ficheiros|arquivos|dosyalar|datoteke|fitxers|failid|fails|bylos|fajlovi|fitxategiak)$"
                    .toRegex(),
                "-"
            )
            .replace("-{2,}".toRegex(), "-")
            .replace("-mp3", ".mp3")
            .removePrefix("-")
        println("Sanitized: $sanitizedFileName")
        file.renameTo(File(file.parentFile, sanitizedFileName))
    }
}