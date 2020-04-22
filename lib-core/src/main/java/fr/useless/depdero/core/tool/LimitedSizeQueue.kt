package fr.useless.depdero.core.tool

import java.util.*

class LimitedSizeQueue<T> {

    private val maxSize: Int
    private val storageArea: LinkedList<T>

    constructor(maxSize: Int) {
        this.maxSize = maxSize
        this.storageArea = LinkedList<T>()
    }

    fun addAllSafe(collection: Collection<T>) {
        collection.reversed().take(maxSize).forEach {
            this.addSafe(it)
        }
    }

    fun addSafe(item: T) {
        if ( item is String && item.isEmpty() ) { return }
        if (storageArea.size < maxSize) {
            storageArea.addFirst(item)
        } else {
            storageArea.removeAt( maxSize-1 )
            storageArea.addFirst(item)
        }
    }

    fun lastMaxReached(): T? {
        if ( storageArea.size != maxSize ) { return null }
        return storageArea.last
    }

    override fun toString(): String {
        return storageArea.toString()
    }

    fun toList(): List<T> {
        return storageArea
    }
}