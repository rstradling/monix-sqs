package com.strad.monixsqs

import org.specs2._
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.{Region, Regions}
import monix.execution.Cancelable
import monix.execution.Scheduler.Implicits.global


class MonxiSqsSpec extends Specification { def is = s2"""

 This is my first specification
   it is working                   $e1
 """ 
 def e1 = { 
 	val conn = SqsConnection(new DefaultAWSCredentialsProviderChain(), Region.getRegion(Regions.US_EAST_1), 10, "dev1-globalsearch-contacts-1")
 	val obs  = SqsListener.getMessages(conn)
	val myCancel: Cancelable = obs.map { resp =>
    println(resp.result)
    println("--------------------------------")
    Thread.sleep(5000)
    SqsListener.delete(conn)(resp.result).runAsync
  }.subscribe()
   Thread.sleep(300000)
	"Processing" must have size(11)
 }
}