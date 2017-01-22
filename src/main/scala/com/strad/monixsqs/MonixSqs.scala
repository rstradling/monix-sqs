package com.strad.monixsqs

import java.util.concurrent.atomic.AtomicBoolean

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClient}
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient
import com.amazonaws.services.sqs.model._
import monix.eval.Task
import monix.reactive.Observable
import monix.execution.Cancelable
import monix.execution.cancelables.BooleanCancelable


case class SqsListenerResponse(client: AmazonSQSAsync, url: String, result: List[Message])

case class SqsConnection(provider: AWSCredentialsProvider, region: Region,
	maxNumberOfMessages: Int, qName: String) extends BooleanCancelable {
  val isCancelled = new AtomicBoolean(false)
	val sqs = new AmazonSQSAsyncClient(provider)
	sqs.setRegion(region)
  val client = new AmazonSQSBufferedAsyncClient(sqs)
  import scala.collection.JavaConverters._
  val response = client.getQueueAttributes(qName, List("All").asJava)
  private val statusCode = response.getSdkHttpMetadata.getHttpStatusCode
  if (statusCode < 200 || statusCode >= 300) {
    throw new RuntimeException("Could not get the attributes of the queue named $qName")
  }
  override def cancel() : Unit = {
    isCancelled.set(true)
    client.shutdown()
  }

  override def isCanceled() :  Boolean = {
    isCancelled.get
  }
}

object SqsListener {
	def getMessages(con: SqsConnection) : Observable[SqsListenerResponse] = {
    import scala.collection.JavaConverters._
    Observable.unsafeCreate { subscriber =>
      val makeRequest = Task.apply {
        val messageReq = new ReceiveMessageRequest(con.qName).withMaxNumberOfMessages(con.maxNumberOfMessages)
        val ret = {
          val msgRet = con.client.receiveMessage(messageReq)
          val messages = msgRet.getMessages.asScala.toList
          SqsListenerResponse(con.client, con.qName, messages)
        }
        ret
      }
      Observable.repeat(()).mapTask(_ => makeRequest)
      .doOnTerminate(con.cancel())
      .doOnSubscriptionCancel(con.cancel())
      .unsafeSubscribeFn(subscriber)
      con
    }
  }
  def delete(con: SqsConnection)(messages: List[Message]): Task[Unit] = {
    Task.apply {
      messages.map { m =>
        con.client.deleteMessage(con.qName, m.getReceiptHandle)
      }
    }
  }
}