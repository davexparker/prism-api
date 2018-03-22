package integratedModel1;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class PrismVerifier {


    public static void main(String[] args){
    try {
        PrismLog mainLog = new PrismDevNullLog();
        Prism prism = new Prism(mainLog);
        prism.initialise();

        List<double[]> missionWaypoints = new ArrayList<>();
        missionWaypoints.add(new double[]{0,0,0});
        missionWaypoints.add(new double[]{1.5857444442518606E-5, 0,0});
        missionWaypoints.add(new double[]{1.5857444442518606E-5, 0.0019999999999999996,0});
        missionWaypoints.add(new double[]{1.5857444442518606E-5, 0.002,0});
        missionWaypoints.add(new double[]{4.757233332755589E-5, 0.002,0});
        missionWaypoints.add(new double[]{4.757233332755589E-5, 2.556022206262406E-19,0});
        missionWaypoints.add(new double[]{7.928722221259316E-5, 0,0});
        missionWaypoints.add(new double[]{7.928722221259316E-55, 0.0019999999999999996,0});
        missionWaypoints.add(new double[]{1.1100211109763038E-4, 0.002,0});
        missionWaypoints.add(new double[]{1.1100211109763038E-4, 2.556022206262406E-19,0});
        missionWaypoints.add(new double[]{1.4271699998266767E-4, 0,0});


        int initialBatteryWattHrs = 5400;
        int speed = 1;
        double batteryThreshold = 0.5;
        String property = "P=?[F \"home\"]";

        BatteryModel bm = new BatteryModel(initialBatteryWattHrs,batteryThreshold,missionWaypoints,speed);
        prism.loadModelGenerator(bm);

        Object prob = prism.modelCheck(property).getResult();

        System.out.println(property + " = " + prob);

        prism.exportTransToFile(true, Prism.EXPORT_DOT_STATES, new File("dtmc.dot"));


        // Close down PRISM
        prism.closeDown();

    } catch (FileNotFoundException | PrismException e) {
        System.out.println("Error: " + e.getMessage());
        System.exit(1);
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

}
