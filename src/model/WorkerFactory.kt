package model

import model.worker.HttpWorker
import model.worker.ThreadedWorker

import model.worker.Worker
import java.util.*

/**
 * Generate, build, or otherwise present workers to be interacted with the
 * main master
 * Created by stephen on 11/12/15.
 */
object WorkerFactory {

    private const val LOCALHOST: String = "192.168.0.1"

    // scratch: the ThreadFactory integration into an ExecutorService might be a better idea
    fun findWorkers(type: String = "all"): List<Worker> {
        val list = ArrayList<Worker>()

        when (type) {
            "thread" -> {
                list.addAll(buildBaseThreadWorkers(6))
            }
            "http" -> {
                list.addAll(buildLocalHttpWorks(31000, 31001, 31002))
            }
            "all" -> {
                list.addAll(findWorkers("thread"))
                list.addAll(findWorkers("http"))
            }
        }

        return list
    }

    private fun buildLocalHttpWorks(vararg ports: Int): Array<Worker> {
        return Array(ports.size, { int -> HttpWorker(ports[int], LOCALHOST) })
    }

    private fun buildBaseThreadWorkers(count: Int): Array<Worker> {
        return Array(count, { i -> ThreadedWorker(Thread.currentThread(), i) })
    }
}

