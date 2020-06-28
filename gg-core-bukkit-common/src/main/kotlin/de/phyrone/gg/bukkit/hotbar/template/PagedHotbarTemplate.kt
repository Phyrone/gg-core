package de.phyrone.gg.bukkit.hotbar.template

import de.phyrone.gg.bukkit.hotbar.PlayerHotbar
import de.phyrone.gg.bukkit.items.InteractiveItem
import org.bukkit.entity.Player

open class PagedHotbarTemplate private constructor(
    private val pages: MutableList<Array<InteractiveItem?>>
) : HotbarTemplate {
    constructor() : this(ArrayList())


    private var currentPage = 0
    private var firstExec = false

    override fun PlayerHotbar.append(player: Player) {
        if (firstExec)
            onPageChange(player, currentPage, currentPage, Direction.INIT)
        pages.getOrNull(currentPage)?.forEachIndexed { slot: Int, interactiveItem: InteractiveItem? ->
            if (interactiveItem != null)
                this.setItem(slot, interactiveItem)
        }
    }

    fun setItem(slot: Int, item: InteractiveItem) {
        if (slot < 0) return
        val page = calculatePageNumber(slot)
        val pagedSlot = calculatePagedSlot(slot, page)
        setItem(page, pagedSlot, item)
    }

    fun setItem(page: Int, slot: Int, item: InteractiveItem) {
        assert(page >= 0)
        assert(slot in (0..8))
        val pageArray = getPageOrCreate(page)
        pageArray[slot] = item
    }

    fun removeItem(slot: Int) {
        if (slot < 0) return
        val page = calculatePageNumber(slot)
        val pagedSlot = calculatePagedSlot(slot, page)
        removeItem(page, pagedSlot)
    }

    fun removeItem(page: Int, slot: Int) {
        assert(page >= 0)
        assert(slot in (0..8))
        pages.getOrNull(slot)?.set(slot, null)
        collectOldPages()
    }

    private fun collectOldPages() {
        var collectSize = 0
        for (page in pages.reversed()) {
            if (page.all { it == null }) collectSize++
            else break
        }
        repeat(collectSize) {
            pages.removeAt(pages.size - 1)
        }
    }

    private fun getPageOrCreate(page: Int): Array<InteractiveItem?> {

        val currentSize = pages.size - 1
        if (currentSize < page) {
            val pagesToCreate = page - currentSize
            repeat(pagesToCreate) {
                pages.add(Array(9) { null })
            }
        }
        return pages[page]
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun calculatePageNumber(slot: Int) = slot / 9

    @Suppress("NOTHING_TO_INLINE")
    private inline fun calculatePagedSlot(slot: Int, page: Int) = slot - page * 9

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isLast() = currentPage >= lastPage()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun lastPage() = (pages.size - 1)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isFirst() = currentPage <= 0


    final override fun PlayerHotbar.next(player: Player): Boolean {
        return if (pages.size > 1) {
            val oldPage = currentPage
            val direction: Direction
            if (isLast()) {
                currentPage = 0
                direction = Direction.FIRST
            } else {
                currentPage++
                direction = Direction.NEXT
            }
            onPageChange(player, oldPage, currentPage, direction)
            true
        } else {
            false
        }
    }

    final override fun PlayerHotbar.prev(player: Player): Boolean {
        return if (pages.size > 1) {
            val oldPage = currentPage
            val direction: Direction
            if (isFirst()) {
                currentPage = lastPage()
                direction = Direction.LAST
            } else {
                currentPage--
                direction = Direction.PREV
            }
            onPageChange(player, oldPage, currentPage, direction)
            true
        } else {
            false
        }
    }

    open fun PlayerHotbar.onPageChange(player: Player, oldPage: Int, newPage: Int, direction: Direction) {

    }

    override fun copy(): HotbarTemplate = PagedHotbarTemplate(pages.map { page -> page.copyOf() }.toMutableList())


    enum class Direction {
        NEXT, PREV, FIRST, LAST, INIT
    }

}