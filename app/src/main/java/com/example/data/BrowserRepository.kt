package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val dao: BrowserDao) {

    // Bookmarks
    val allBookmarks: Flow<List<BookmarkItem>> = dao.getAllBookmarks()
    val bookmarksSortedByName: Flow<List<BookmarkItem>> = dao.getBookmarksSortedByName()

    suspend fun addBookmark(bookmark: BookmarkItem) = dao.insertBookmark(bookmark)
    suspend fun removeBookmark(bookmark: BookmarkItem) = dao.deleteBookmark(bookmark)
    suspend fun removeBookmarkByUrl(url: String) = dao.deleteBookmarkByUrl(url)
    fun isBookmarked(url: String): Flow<Boolean> = dao.isBookmarked(url)

    // History
    val allHistory: Flow<List<HistoryItem>> = dao.getAllHistory()
    fun searchHistory(query: String): Flow<List<HistoryItem>> = dao.searchHistory(query)
    suspend fun addHistory(history: HistoryItem) = dao.insertHistory(history)
    suspend fun removeHistory(history: HistoryItem) = dao.deleteHistory(history)
    suspend fun clearHistory() = dao.clearAllHistory()

    // Downloads
    val allDownloads: Flow<List<DownloadItem>> = dao.getAllDownloads()
    fun getDownloadsByCategory(category: String): Flow<List<DownloadItem>> = dao.getDownloadsByCategory(category)
    suspend fun addDownload(download: DownloadItem) = dao.insertDownload(download)
    suspend fun updateDownload(download: DownloadItem) = dao.updateDownload(download)
    suspend fun removeDownload(download: DownloadItem) = dao.deleteDownload(download)
    suspend fun clearDownloads() = dao.clearDownloads()

    // Memos
    val allMemos: Flow<List<MemoItem>> = dao.getAllMemos()
    suspend fun addMemo(memo: MemoItem) = dao.insertMemo(memo)
    suspend fun removeMemo(memo: MemoItem) = dao.deleteMemo(memo)
}
