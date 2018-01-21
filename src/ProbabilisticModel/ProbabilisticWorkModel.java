package ProbabilisticModel;

//The WorkModel is for learning a linear relationship between (position) displacements and work.
//This one IS probabilistic.

public class ProbabilisticWorkModel {


    private int numParams;
    private int paramSupportSize;
    private double[][] modelParams;
    private double[][] modelParamProbabilities;


    public ProbabilisticWorkModel(){

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


    public double getWork(int[] displacementVector,int[] offsets) {

        double work = 0;

        for (int i = 0; i < numParams; i++) {
            work += Math.abs(modelParams[i][offsets[i]] * displacementVector[i]);
        }
        return work;
    }

    public double[] getProbabilities(int[] offsets){

        double [] probabilities = new double[numParams];

        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = modelParamProbabilities[i][offsets[i]];
        }

        return probabilities;
    }

    public static void main(String[] args){

        ProbabilisticWorkModel pm = new ProbabilisticWorkModel();

        int[] displacements = new int[]{10,100,20};

        System.out.println(pm.getWork(displacements, new int[]{0,0,0}));
        System.out.println(pm.getWork(displacements, new int[]{0,0,1}));
        System.out.println(pm.getWork(displacements, new int[]{1,0,0}));
        System.out.println(pm.getWork(displacements, new int[]{0,1,0}));



//        for (int i = 0; i < pm.getNumParams(); i++) {
//            for (int j = 0; j < pm.getParamSupportSize(); j++) {
//                System.out.println("params vals: "+ pm.modelParams[i][j]);
//                System.out.println("params probs: "+ pm.modelParamProbabilities[i][j]);
//            }
//        }
//

    }



}
