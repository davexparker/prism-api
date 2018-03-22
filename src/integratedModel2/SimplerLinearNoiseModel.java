package integratedModel2;

public class SimplerLinearNoiseModel extends BatterPowerModel {

    private int numParams;
    private double[] modelParams;
    private double[] noiseProbabilities;
    private int noiseSize;
    private double[] noiseDistribution;


    public SimplerLinearNoiseModel(){

        this.numParams = 3;
        this.noiseSize = 2;

        //Random initialization for the parameters
        modelParams = new double[this.numParams];
        for (int i = 0; i < this.numParams; i++) {
            modelParams[i] = 200;
        }

        //Random initialization for noise additions to power usage
        noiseDistribution = new double[noiseSize];
        noiseDistribution[0] = -50;
        noiseDistribution[1] =  50;

        noiseProbabilities = new double[this.noiseSize];
        //Uniform distribution for noise
        for (int i = 0; i < this.noiseSize; i++) {
            noiseProbabilities[i] = 1.0 / noiseSize;
        }

        System.out.println("Printing model parameters...");
        for (int i = 0; i < this.numParams; i++) {
                System.out.print("param_"+i+ ": " +modelParams[i]+". ");
        }
        System.out.println("\nPrinting noise parameters...");
        for (int i = 0; i < this.noiseSize; i++) {
            System.out.print("noise_"+i+ ": " +noiseDistribution[i]+". ");
        }
        System.out.println("\n");
    }

    public void learnBatch(double[][] ENUvelocityData, double[] powerData)  {
        //left blank for now
    }

    public void update(double[][] ENUvelocityData, double[] powerData){
        //left blank for now
    }

    public int getParamSupportSize() {
        return noiseSize;
    }
    public int getNumParams(){
        return numParams;
    }

    //todo check
    public double getPower(double[] ENUvelocity, int[] offsets) {
        //Calculate Power as the scalar product: Force[*]velocity.
        //However, Force here is interpreted as an approximation of the using the
        // classical drag model D = ((1/2)*p*A*C_d*(v^2)), where D is approximated using a 3d linear coefficients.
        double power = 0;
        for (int i = 0; i < numParams; i++) power += Math.abs(modelParams[i]* ENUvelocity[i]);

        //add noise
        return power + noiseDistribution[offsets[0]];
    }

    //todo check
    public double[] getProbabilities(int[] offsets){
        double [] probabilities = new double[numParams];
        for (int i = 0; i < probabilities.length; i++) probabilities[i] = noiseProbabilities[offsets[0]];
        return probabilities;
    }


    public static void main(String[] args){

    }

}
