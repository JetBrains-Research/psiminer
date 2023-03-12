package storage

import java.io.File

class StoragesManager(
    private val createStorage: (outputDirectory: File) -> Storage,
    private val baseOutputDirectory: File
) {

    private var storageId = 0
    private val storages = mutableListOf<Storage>()

    @Synchronized
    fun createStorage(): Storage {
        val storagePath = File(baseOutputDirectory, storageId.toString())
        storageId += 1
        val storage = createStorage(storagePath)
        storages.add(storage)
        return storage
    }

    fun printStoragesStatistic() {
        storages.forEachIndexed { index, storage ->
            println(index)
            storage.printStatistic()
        }
    }

    fun closeStorages() {
        storages.forEach { storage ->
            storage.close()
        }
    }
}