package sttp.tapir.server.netty.sync.internal.ox

import ox.*
import ox.channels.{Actor, ActorRef}

import scala.util.control.NonFatal
import scala.concurrent.Future
import scala.concurrent.Promise

/** A dispatcher that can start forks, within some "global" scope. Useful when one needs to start an asynchronous task from a thread outside
  * of an Ox scope. Normally Ox doesn't allow to start forks from arbitrary threads, for example in callbacks of external libraries. If you
  * create an `OxDispatcher` inside a scope and pass it for potential handling on another thread, that thread can call
  * {{{
  *   dispatcher.runAsync {
  *     // code to be executed in a fork
  *   } { throwable =>
  *     // error handling if the fork fails with an exception, this will be run on the Ox virtual thread as well
  *   }
  * }}}
  * WARNING! Dispatchers should only be used in special cases, where the proper structure of concurrency scopes cannot be preserved. One
  * such example is integration with callback-based systems like Netty, which runs handler methods on its event loop thread.
  */
private[sync] class OxDispatcher private (actor: ActorRef[OxDispatcherRunner]):
  def runAsync(thunk: Ox ?=> Unit)(onError: Throwable => Unit): Future[CancellableFork[Unit]] =
    val forkPromise = Promise[CancellableFork[Unit]]()
    actor.tell(_.runAsync(thunk, onError, forkPromise))
    forkPromise.future

private trait OxDispatcherRunner:
  def runAsync(thunk: Ox ?=> Unit, onError: Throwable => Unit, forkPromise: Promise[CancellableFork[Unit]]): Unit

object OxDispatcher:
  /** @param ox
    *   concurrency scope where forks will be run, using a nested scope to isolate failures. The dispatcher will only be usable as long as
    *   this scope doesn't complete.
    */
  def create(using ox: Ox): OxDispatcher =
    val actor = Actor.create {
      new OxDispatcherRunner:
        def runAsync(thunk: Ox ?=> Unit, onError: Throwable => Unit, forkPromise: Promise[CancellableFork[Unit]]): Unit =
          val fork = forkCancellable {
            try supervised(thunk)
            catch case NonFatal(e) => onError(e)
          }
          forkPromise.success(fork).discard
    }
    new OxDispatcher(actor)
