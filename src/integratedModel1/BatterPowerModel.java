package integratedModel1;

import java.util.Arrays;

abstract class BatterPowerModel {

    abstract int getParamSupportSize();
    abstract int getNumParams();

    abstract double getPower(double[] velocity, int[] offsets);

    public double getWork(double[] EFECdisplacement, int speed, int[] offsets) {

        System.out.println("Printing calculations...");

        for (int i = 0; i < EFECdisplacement.length; i++) {
            System.out.print("- displacement"+i+": " + EFECdisplacement[i]+ "; ");
        }
        System.out.println();


        double[] velocity = speedToENUVelocity(EFECdisplacement, speed);
        for (int i = 0; i < velocity.length; i++) {
            System.out.print("- velocity"+i+": " + velocity[i]+ "; ");

        }
        System.out.println();

        double power = getPower(velocity,offsets);
        System.out.println("- power required: " + power);

        return powerToWork(EFECdisplacement,speed,power);
    }

    abstract double[] getProbabilities(int[] offsets);

    abstract void learnBatch(double[][] ENUvelocityData, double[] powerData) throws ArrayIndexOutOfBoundsException;
    abstract void update(double[][] ENUvelocityData, double[] powerData) throws ArrayIndexOutOfBoundsException;


    private double[] speedToENUVelocity(double[] ECEFdisplacement, int speed){
        double distance = Math.sqrt(Arrays.stream(ECEFdisplacement).map((r) -> Math.pow(r,2)).sum());
        // The sign (direction) on the displacement is required to be preserved.
        return Arrays.stream(ECEFdisplacement).map((r) -> speed * (r/ distance)).toArray();
        }

     private double powerToWork(double[] EFECdisplacement, double speed, double power){
         //Calculate work as: power * time
         double distance  = Math.sqrt(Arrays.stream(EFECdisplacement).map((i) -> Math.pow(i,2)).sum());
         double time = distance/speed;

         System.out.println("- work done: " + (power * time));
         return power * time;
     }

 }

