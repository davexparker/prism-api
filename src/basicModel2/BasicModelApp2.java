package basicModel2;

import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class BasicModelApp2 {

    public static void main(String[] args){

            try {
                PrismLog mainLog = new PrismDevNullLog();
                Prism prism = new Prism(mainLog);
                prism.initialise();

                List<int[]> missionWaypoints = new ArrayList<>();
                missionWaypoints.add(new int[]{10,10,-10});
                missionWaypoints.add(new int[]{20,20,-25});
                missionWaypoints.add(new int[]{36,36,-40});
                missionWaypoints.add(new int[]{100,60,-100});


                int battery_amount = 1000;

                System.out.println("Initial battery amount: " + battery_amount);

                BasicModel2 prismModel = new BasicModel2(battery_amount,0.1, missionWaypoints);

                prism.loadModelGenerator(prismModel);
                prism.exportTransToFile(true, Prism.EXPORT_DOT_STATES, new File("dtmc.dot"));

                String[] props = new String[] {
                        "P=?[F \"home\"]",
                };
                for (String prop : props) {
                    System.out.println(prop + ":");
                    System.out.println(prism.modelCheck(prop).getResult());
                }

                // Close down PRISM
                prism.closeDown();

            } catch (FileNotFoundException | PrismException e) {
                System.out.println("Error: " + e.getMessage());
                System.exit(1);
            }
    }
}
