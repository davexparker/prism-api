package integratedModel1;

//This power model assumes and describes a linear relationship between velocity(ENU) and power.

public class LinearProbPowerModel extends BatterPowerModel {

    private int numParams;
    private int paramSupportSize;
    private double[][] modelParams;
    private double[][] modelParamProbabilities;


    public LinearProbPowerModel(){

        this.numParams = 3;
        this.paramSupportSize = 1;   //each random (variable) coefficient has this as the size of it's admissible range.


        modelParams = new double[this.numParams][paramSupportSize];
        modelParamProbabilities = new double[this.numParams][paramSupportSize];

        //Random initialization for the parameters
        for (int i = 0; i < this.numParams; i++) {
            for (int j = 0; j < paramSupportSize; j++) {
                modelParams[i][j] = 200;
                modelParamProbabilities[i][j] = 1.0 / paramSupportSize;
            }
        }

        System.out.println("Printing model parameters...");
        for (int i = 0; i < this.numParams; i++) {
            for (int j = 0; j < paramSupportSize; j++) {
                System.out.print("param_"+i+","+j+" : " +modelParams[i][j]+". ");
            }
        }
        System.out.println("\n");
    }

    //todo
    public void learnBatch(double[][] ENUvelocityData, double[] powerData)  {
        //left blank for now
    }

    //todo
    public void update(double[][] ENUvelocityData, double[] powerData){
        //left blank for now
    }

    public int getParamSupportSize() {
        return paramSupportSize;
    }
    public int getNumParams(){
        return numParams;
    }


    public double getPower(double[] ENUvelocity, int[] offsets) {
        //Calculate Power as the scalar product: Force[*]velocity.
        //However, Force here is interpreted as an approximation of the using the
        // classical drag model D = ((1/2)*p*A*C_d*(v^2)), where D is approximated using a 3d linear coefficients.
        double power = 0;
        for (int i = 0; i < numParams; i++) power += Math.abs(modelParams[i][offsets[i]] * ENUvelocity[i]);
        return power;
    }


    public double[] getProbabilities(int[] offsets){
        double [] probabilities = new double[numParams];
        for (int i = 0; i < probabilities.length; i++) probabilities[i] = modelParamProbabilities[i][offsets[i]];
        return probabilities;
    }


    //Test
    public static void main(String[] args){

        LinearProbPowerModel pm = new LinearProbPowerModel();

        double[] displacements = new double[]{10,100,20};

        System.out.println(pm.getWork(displacements, 5,new int[]{0,0,0}));
        System.out.println(pm.getWork(displacements,5, new int[]{0,0,1}));
        System.out.println(pm.getWork(displacements, 5,new int[]{1,0,0}));
        System.out.println(pm.getWork(displacements, 5,new int[]{0,1,0}));

        for (int i = 0; i < pm.getNumParams(); i++) {
            for (int j = 0; j < pm.getParamSupportSize(); j++) {
                System.out.println("params vals: "+ pm.modelParams[i][j]);
                System.out.println("params probs: "+ pm.modelParamProbabilities[i][j]);
            }
        }
    }



}
