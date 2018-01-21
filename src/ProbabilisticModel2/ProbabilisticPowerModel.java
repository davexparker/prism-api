package ProbabilisticModel2;

//The PowerModel is for learning a linear relationship between velocities and work rate.
//Note however, it is not probabilistic.

import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;

public class ProbabilisticPowerModel {


    private int numParams;
    private int paramSupportSize;
    private double[][] modelParams;
    private double[][] modelParamProbabilities;


    public ProbabilisticPowerModel(){

        numParams = 3;
        paramSupportSize = 3;

        modelParams = new double[numParams][paramSupportSize];
        modelParamProbabilities = new double[numParams][paramSupportSize];

        //Random distribution over parameters
        for (int i = 0; i < numParams; i++) {
            for (int j = 0; j < paramSupportSize; j++) {
                modelParams[i][j] = Math.random()*5;
                modelParamProbabilities[i][j] = 1.0 / paramSupportSize;
            }
        }
    }

    public int getParamSupportSize() {
        return paramSupportSize;
    }

    public int getNumParams(){
        return numParams;
    }


    public double getWork(int[] displacementVector,int speed, int[] offsets) {

        //Find unit vector
        int displacements_squared = 0;
        for (int i = 0; i < displacementVector.length; i++) {
            displacements_squared += Math.pow(displacementVector[i],2);
            System.out.println("dispalcemetns squared: "+ displacements_squared);
        }
        double displacement_magnitude = Math.sqrt(displacements_squared);
        System.out.println("dis_mag :"+displacement_magnitude);

        // Find velocity
        double[] velocityVector = new double[displacementVector.length];
        for (int i = 0; i < displacementVector.length; i++) {

            // The sign (direction) on the displacement is required to be preserved.
            velocityVector[i] =  (speed* ((double)displacementVector[i] /displacement_magnitude));

            System.out.println("velocity :" + velocityVector[i]);
            System.out.println("displacement :" +displacementVector[i] + " velocity :" + velocityVector[i]);

        }


        //Calculate work as power * time
        double[] power = getPower(velocityVector, offsets);
        double work = 0;
        for (int i = 0; i < displacementVector.length; i++) {

            System.out.println("power :"+ power[i]);
            work += power[i] * (displacementVector[i]/ velocityVector[i]);
            System.out.println("work :"+work);
        }

        return work;
    }

    public double[] getPower(double[] velocityVector, int[] offsets) {
        double[] power = new double[velocityVector.length];

        for (int i = 0; i < numParams; i++) {
            power[i] = Math.abs(modelParams[i][offsets[i]] * velocityVector[i]);
        }

        return power;
    }


    public double[] getProbabilities(int[] offsets){

        double [] probabilities = new double[numParams];

        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = modelParamProbabilities[i][offsets[i]];
        }

        return probabilities;
    }

    public static void main(String[] args){

        ProbabilisticPowerModel pm = new ProbabilisticPowerModel();

        int[] displacements = new int[]{10,100,20};

        System.out.println(pm.getWork(displacements, 5,new int[]{0,0,0}));
        System.out.println(pm.getWork(displacements,5, new int[]{0,0,1}));
        System.out.println(pm.getWork(displacements, 5,new int[]{1,0,0}));
        System.out.println(pm.getWork(displacements, 5,new int[]{0,1,0}));



//        for (int i = 0; i < pm.getNumParams(); i++) {
//            for (int j = 0; j < pm.getParamSupportSize(); j++) {
//                System.out.println("params vals: "+ pm.modelParams[i][j]);
//                System.out.println("params probs: "+ pm.modelParamProbabilities[i][j]);
//            }
//        }
    }



}
