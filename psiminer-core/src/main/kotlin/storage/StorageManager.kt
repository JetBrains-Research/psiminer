package storage

import java.io.File

class StorageManager(
    private val createStorage: (outputDirectory: File) -> Storage,
    private val baseOutputDirectory: File
) {

    private var storageId = 0
    private val storages = mutableListOf<Storage>()

    fun createStorage(): Storage {
        val storagePath = File(baseOutputDirectory, storageId.toString())
        storageId += 1
        val storage = createStorage(storagePath)
        storages.add(storage)
        return storage
    }

    fun printStoragesStatistic() {
        storages.forEachIndexed { _, storage ->
            println("Statistic for Storage-${storage.outputDirectory.name}")
            storage.printStatistic()
        }
    }

    fun closeStorages() {
        storages.forEach { storage ->
            storage.close()
        }
    }
}
