package basicModel;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

//The PowerModel is capable of learning a linear relationship between displacements and energy expenditure.
//Note however, it is not probabilistic.

public class BasicWorkModel {


    private RealVector linearCoefficients;


    public double getWork(int[] displacementVector) {

        double[] coefficientArray = linearCoefficients.toArray();

        return Math.abs(coefficientArray[0] * displacementVector[0])
                + Math.abs(coefficientArray[1] * displacementVector[1])
                + Math.abs(coefficientArray[2] * displacementVector[2]);
    }


    public void batchLearn(double[][] x, double[] y){
        //shape of LinerEqnData[] = [dx,dy,dz,E]
        OLSMultipleLinearRegression linearRegression = new OLSMultipleLinearRegression();
        linearRegression.newSampleData(y,x);
        linearCoefficients  = new ArrayRealVector(linearRegression.estimateRegressionParameters());
    }


    public static void main(String[] args){

        double[][] linearEqnCoef = new double[4][3];
        linearEqnCoef[0] = new double[]{10,0,0};
        linearEqnCoef[1] = new double[]{0,10,0};
        linearEqnCoef[2] = new double[]{0,0,-10};
        linearEqnCoef[3] = new double[]{10,10,-10};

        double[] linearConsts = new double[]{1,2,3,20};

        BasicWorkModel pm = new BasicWorkModel();
        pm.batchLearn(linearEqnCoef,linearConsts);

        System.out.println(pm.linearCoefficients);

    }



}
