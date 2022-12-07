import java.lang.IllegalArgumentException

// AOC Day 7
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()

    val root = Directory("root", null)
    var currentDirectory = root

    val lineIter = lines.listIterator()
    while (lineIter.hasNext()) {
        val line = lineIter.next()

        if (line.startsWith("$ cd")) {
            currentDirectory = changeDirectory(line, root, currentDirectory)
        } else if (line == "$ ls") {
            populateDirectoryWithListInfo(lineIter, currentDirectory)
        } else {
            throw IllegalArgumentException("Unsupported command: $line")
        }
    }

    val totalSize = 70_000_000L
    val sizeInUse = root.totalSize()
    val sizeAvailable = totalSize - sizeInUse
    println("Filesystem: $sizeInUse / $totalSize, available: $sizeAvailable")

    val smallDirs = findSmallDirectories(root)
    val totalSizeSmallDirs = smallDirs.map(Directory::totalSize).sum()
    println("Small directories summed up size are: $totalSizeSmallDirs")

    val sizeRequired = 30_000_000L
    val sizeToDelete = sizeRequired - sizeAvailable
    val dirToDelete = smallestDirectoryBiggerThan(root, sizeToDelete)
    println("Directory to delete ${dirToDelete.absolutePath()}, size ${dirToDelete.totalSize()}")
}

private fun findSmallDirectories(root: Directory) =
    getAllDirectories(root).filter { it.totalSize() <= 100_000 }.toList()

private fun smallestDirectoryBiggerThan(root: Directory, sizeThreshold: Long) =
    getAllDirectories(root).filter { it.totalSize() >= sizeThreshold }.minBy(Directory::totalSize)

fun changeDirectory(line: String, root: Directory, currentDirectory: Directory): Directory =
    when (val targetRaw = CD_PATTERN.matchEntire(line)!!.groupValues[1]) {
        "/" -> root
        ".." -> currentDirectory.parent
            ?: throw IllegalArgumentException("Can not 'cd ..' beyond root")

        else -> currentDirectory.getDirectoryByName(targetRaw)
            ?: throw IllegalArgumentException(
                "Can not 'cd $targetRaw', directory is unknown." +
                        " Currently at ${currentDirectory.absolutePath()}, known sub-dirs are ${
                            currentDirectory.getDirectories().map(Directory::name)
                        }"
            )
    }

private fun populateDirectoryWithListInfo(
    lineIter: ListIterator<String>,
    currentDirectory: Directory
) {
    while (lineIter.hasNext()) {
        val line = lineIter.next()
        if (line.first() == '$') {
            break
        }

        val (sizeOrType, name) = line.split(' ', limit = 2)

        if (sizeOrType == "dir") {
            currentDirectory.addDirectoryIfUnknown(Directory(name, currentDirectory))
        } else {
            currentDirectory.addFileIfUnknown(
                FileEntry(
                    name,
                    sizeOrType.toLong(),
                    currentDirectory
                )
            )
        }
    }

    if (lineIter.hasNext()) {
        lineIter.previous()
    }
}

private val CD_PATTERN = Regex("\\$ cd (.+)")

class FileEntry(val name: String, val size: Long, val directory: Directory) {
    override fun toString(): String {
        return "FileEntry(name='$name', size=$size)"
    }
}

class Directory(
    val name: String,
    val parent: Directory?,
    private val nameToDirectory: MutableMap<String, Directory> = HashMap(),
    private val nameToFile: MutableMap<String, FileEntry> = HashMap()
) {
    private var totalSizeCache: Long? = null

    fun absolutePath(): String {
        val dirs = ArrayList<Directory>()

        var currentDir = this
        while (currentDir.parent != null) {
            dirs.add(currentDir)
            currentDir = currentDir.parent!!
        }
        dirs.add(currentDir)

        return dirs.asReversed().map(Directory::name).joinToString("/")
    }

    fun getDirectoryByName(name: String) = nameToDirectory[name]

    fun getDirectories() = nameToDirectory.values.toList()

    fun addDirectoryIfUnknown(directory: Directory) {
        require(directory.parent == this)
        nameToDirectory.putIfAbsent(directory.name, directory)
        totalSizeCache = null
    }

    fun addFileIfUnknown(file: FileEntry) {
        require(file.directory == this)
        nameToFile.putIfAbsent(file.name, file)
        totalSizeCache = null
    }

    fun totalSize(): Long {
        if (totalSizeCache == null) {
            totalSizeCache =
                nameToFile.values.map(FileEntry::size).sum() + nameToDirectory.values.map(
                    Directory::totalSize
                ).sum()
        }
        return totalSizeCache!!
    }

    override fun toString(): String {
        return "Directory(name='$name', subDirs=${nameToDirectory.values.map(Directory::name)}, files=${nameToFile.values})"
    }
}

private fun getAllDirectories(root: Directory): Set<Directory> {
    val allDirectories = HashSet<Directory>()

    val dirsToExplore = ArrayDeque<Directory>()
    dirsToExplore.add(root)

    while (dirsToExplore.isNotEmpty()) {
        val currentDir = dirsToExplore.removeFirst()
        allDirectories.add(currentDir)

        dirsToExplore.addAll(currentDir.getDirectories())
    }

    return allDirectories
}
