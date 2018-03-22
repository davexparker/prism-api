package integratedModel1.legacy

import java.io.File
import java.util

import integratedModel1.BatteryModel
import prism.{Prism, PrismDevNullLog}


class PrismVerifierOld() {

  def main(args: Array[String]): Unit = {

    val prism = new Prism(new PrismDevNullLog())
    prism.initialise()

    // we require a RemusStateEstimator object, for this verification instance
    val initialBatteryWattHrs = 5400

    //cast output to waypoints
    val geodeticWaypoints: util.List[Array[Double]] = new util.ArrayList[Array[Double]]
    geodeticWaypoints.add(Array[Double](0, 0,0))
    geodeticWaypoints.add(Array[Double](1.5857444442518606E-5, 0,0))
    geodeticWaypoints.add(Array[Double](1.5857444442518606E-5, 0.0019999999999999996,0))
    geodeticWaypoints.add(Array[Double](1.5857444442518606E-5, 0.002,0))
    geodeticWaypoints.add(Array[Double](4.757233332755589E-5, 0.002,0))
    geodeticWaypoints.add(Array[Double](4.757233332755589E-5, 2.556022206262406E-19,0))
    geodeticWaypoints.add(Array[Double](7.928722221259316E-5, 0,0))
    geodeticWaypoints.add(Array[Double](7.928722221259316E-55, 0.0019999999999999996,0))
    geodeticWaypoints.add(Array[Double](1.1100211109763038E-4, 0.002,0))
    geodeticWaypoints.add(Array[Double](1.1100211109763038E-4, 2.556022206262406E-19,0))
    geodeticWaypoints.add(Array[Double](1.4271699998266767E-4, 0,0))

    val batteryThreshold: Double = 0.5
    val speed: Int= 1
    val property: String = "P=?[F \"home\"]"

    val bm : BatteryModel = new BatteryModel(initialBatteryWattHrs,batteryThreshold,geodeticWaypoints,speed)
    prism.loadModelGenerator(bm)
    val prob = prism.modelCheck(property).getResult

    println("Prob: = " + prob )
    prism.exportTransToFile(true, Prism.EXPORT_DOT_STATES, new File("dtmc.dot"))
    val result = if (prob.asInstanceOf[Double] > 0.9) true else false
    println(result)
  }
}


