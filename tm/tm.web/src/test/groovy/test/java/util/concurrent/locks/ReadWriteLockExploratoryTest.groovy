/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package test.java.util.concurrent.locks

import spock.lang.Ignore
import spock.lang.Specification

import java.util.concurrent.locks.ReentrantReadWriteLock

@Ignore
class ReadWriteLockExploratoryTest extends Specification {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    boolean wasRead = false
    boolean tryingToRead = false
    Object pause = new Object()


    def "should not read while writing"() {
        given: "we're writing stuff"
        startWriting()
        Thread.currentThread().sleep(20) // necessary

        and: "a reader thread"
        def readerThread = startReading()

        when: "we try to read while writing"
        // we capture read state
        def tryingToReadWhileLocked = tryingToRead
        def readWhileLocked = wasRead

        endWriting()

        then:
        readWhileLocked == false
        tryingToReadWhileLocked == true
    }

    def startWriting() {
        lock.writeLock().lock()
    }

    def endWriting() {
        lock.writeLock().unlock()
    }

    def startReading() {
        def readerThread = new Thread(createReader())
        startReading readerThread
        return readerThread
    }

    def startReading(def readerThread) {
        readerThread.start()
        waitUntilReadingStarted()
    }

    def waitUntilReadingStarted() {
        try {
            pause.wait()
        } catch (IllegalMonitorStateException e) {
            println e
        }
    }

    def "should read when finished writing"() {
        given:
        startWriting()

        and: "a reader thread"
        def readerThread = startReading()

        when: "we try to read until writing finished"
        // we end writing
        endWriting()

        // and wait for the reader thread to end
        readerThread.join()
        def readWhileUnlocked = wasRead

        then:
        readWhileUnlocked == true
    }

    def createReader() {
        new Runnable() {
            public void run() {
                readingStarted()

                try {
                    ReadWriteLockExploratoryTest.this.lock.readLock().lock()
                    ReadWriteLockExploratoryTest.this.wasRead = true
                } finally {
                    ReadWriteLockExploratoryTest.this.lock.readLock().unlock()
                }
            }

            def readingStarted() {
                ReadWriteLockExploratoryTest.this.tryingToRead = true
                try {
                    ReadWriteLockExploratoryTest.this.pause.notifyAll()
                } catch (IllegalMonitorStateException e) {
                    println e
                }
            }
        }
    }

    def "should read while nothing is writing"() {
        given:
        def readerThread = startReading()

        when:
        readerThread.join()
        def readWithoutReader = wasRead

        then:
        readWithoutReader == true
    }
}
