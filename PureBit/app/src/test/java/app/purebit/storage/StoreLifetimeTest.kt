package app.purebit.storage

import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

class StoreLifetimeTest {
    @Test fun closeWaitsForAnAlreadyCommittingWriteAndPreventsLaterWrites() {
        val gate=StoreLifetime(Any());val entered=CountDownLatch(1);val release=CountDownLatch(1)
        val closing=CountDownLatch(1);val pool=Executors.newFixedThreadPool(2)
        val file=Files.createTempFile("bitwire-store-", ".txt").toFile()
        try {
            val write=pool.submit {gate.access {entered.countDown();check(release.await(2,TimeUnit.SECONDS));file.writeText("committed")}}
            assertTrue(entered.await(2,TimeUnit.SECONDS))
            val close=pool.submit {closing.countDown();gate.close {assertEquals("committed",file.readText())}}
            assertTrue(closing.await(2,TimeUnit.SECONDS));assertFalse(close.isDone)
            release.countDown();write.get(2,TimeUnit.SECONDS);close.get(2,TimeUnit.SECONDS)
            assertTrue(gate.closed)
            assertThrows(IllegalStateException::class.java){gate.access {file.writeText("late")}}
            assertFalse(gate.offer {fail("Cannot enqueue after close")})
            assertEquals("committed",file.readText())
        }finally{release.countDown();pool.shutdownNow();file.delete()}
    }
    @Test fun blockedProviderReadCannotCommitAfterOldOwnerCloses() {
        val lock=Any();val old=StoreLifetime(lock);val replacement=StoreLifetime(lock)
        val entered=CountDownLatch(1);val release=CountDownLatch(1);val pool=Executors.newSingleThreadExecutor()
        val file=Files.createTempFile("bitwire-owner-", ".txt").toFile()
        try {
            val read=pool.submit<Boolean> {
                entered.countDown();check(release.await(2,TimeUnit.SECONDS)) // Provider IO is outside access.
                runCatching {old.access {file.writeText("stale")}}.isFailure
            }
            assertTrue(entered.await(2,TimeUnit.SECONDS))
            old.close {} // Does not wait for provider IO.
            replacement.access {file.writeText("new owner")}
            release.countDown();assertTrue(read.get(2,TimeUnit.SECONDS))
            assertEquals("new owner",file.readText())
            assertFalse(replacement.closed)
        }finally{release.countDown();pool.shutdownNow();file.delete()}
    }
    @Test fun separateOwnersSerializeFileTransactionsAndCloseIndependently() {
        val lock=Any();val a=StoreLifetime(lock);val b=StoreLifetime(lock)
        val entered=CountDownLatch(1);val release=CountDownLatch(1);val second=CountDownLatch(1)
        val pool=Executors.newFixedThreadPool(2);val events=mutableListOf<String>()
        try {
            val first=pool.submit {a.access {events+="first begin";entered.countDown();check(release.await(2,TimeUnit.SECONDS));events+="first end"}}
            assertTrue(entered.await(2,TimeUnit.SECONDS))
            val next=pool.submit {second.countDown();b.access {events+="second"}}
            assertTrue(second.await(2,TimeUnit.SECONDS));assertFalse(next.isDone)
            release.countDown();first.get(2,TimeUnit.SECONDS);next.get(2,TimeUnit.SECONDS)
            assertEquals(listOf("first begin","first end","second"),events)
            a.close {};assertTrue(b.offer {events+="still open"})
        }finally{release.countDown();pool.shutdownNow()}
    }
}
