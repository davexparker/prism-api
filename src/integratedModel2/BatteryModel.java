package integratedModel2;

import parser.State;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationBool;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import parser.type.Type;
import parser.type.TypeBool;
import parser.type.TypeInt;
import prism.DefaultModelGenerator;
import prism.ModelType;
import prism.PrismException;
import prism.PrismLangException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BatteryModel extends DefaultModelGenerator {

    private int initialBatteryAmount;
    private int speed;   //0 <= speed <= n

    private int batteryAmount;
    private int waypointIndex;
    private double batteryThreshold;
    private List<double[]> displacementVectors;

    private final int NUM_COODS = 3; // [Lat ,Lon ,h ]
    private SimplerLinearNoiseModel wm = new SimplerLinearNoiseModel();

    //current state to be explored.
    private State exploreState;
    private boolean end;


    public BatteryModel(int batteryAmount, double threshold_percent, List<double[]> GPSwaypoints, int speed) throws Exception {

        if (GPSwaypoints.get(0).length != NUM_COODS){
            throw new Exception("GPSwaypoints requires 3 coods (lat,lon,h) per point");
        }

        this.initialBatteryAmount = batteryAmount;
        this.batteryAmount = batteryAmount;
        this.waypointIndex = 0;
        this.batteryThreshold = threshold_percent*batteryAmount;
        this.displacementVectors = new ArrayList<>();
        this.end = false;
        this.speed = speed;

        // converts from geodetic coods to ECEF
        List<double[]> ECEFwaypoints = new ArrayList<>();
        for(double[] wp: GPSwaypoints) ECEFwaypoints.add(geodeticToECEF(wp[0], wp[1], wp[2]));

        // finds displacements between ECEF waypoints
        for (int i = 0; i < ECEFwaypoints.size() - 1; i++) {
            double[] displacement = new double[NUM_COODS];
            for (int j = 0; j < displacement.length; j++) {
                displacement[j] = ECEFwaypoints.get(i+1)[j] - ECEFwaypoints.get(i)[j];
            }
            displacementVectors.add(displacement);
        }
    }

    private double[] geodeticToECEF(double lat, double lon, double h){
        //formula chosen is referenced below.

        //assumes the WGS84 model
        double a = 6378137.0;           //semi-major axis in metres
        double f = 1 / (298.257223563); //flattening
        double b = a * (1 - f);         //semi-minor axis
        double e = Math.sqrt(2f - Math.pow(f,2));      //eccentricity of ellipsoid
        double N = a / (Math.sqrt(1 - (Math.pow(e,2)*Math.pow(Math.sin(lat),2)))); //Normal

        double x = (N + h)*Math.cos(lat)*Math.cos(lon);
        double y = (N + h)*Math.cos(lat)*Math.sin(lon);
        double z = (((Math.pow(b,2)/Math.pow(a,2))*N) + h)*Math.sin(lat);

        return new double[]{x/1000,y/1000,z/1000};     //in kilometres ?
    }

    @Override
    public State getInitialState() throws PrismException {
        return new State(3).setValue(0, batteryAmount).setValue(1,end).setValue(2,waypointIndex);
    }

    @Override
    public void exploreState(State state) throws PrismException {

        this.exploreState = state;

        batteryAmount = (Integer) exploreState.varValues[0];
        end = (Boolean) exploreState.varValues[1];
        waypointIndex = (Integer) exploreState.varValues[2];
    }

    @Override
    public State getExploreState() {
        return exploreState;
    }

    @Override
    public int getNumChoices() throws PrismException {
        return 1;   //DTMC has only 1 choice
    }

    @Override
    public int getNumTransitions(int i) throws PrismException {

        //When dead, we self-loop.
        //When moving we have paramSupportSize^(NUM_COODS) different transitions.
        if(end){
            return 1;
        }
        else {
            //todo: this is a simple hack for now
            return 2;
        }
    }

    @Override
    public Object getTransitionAction(int i) throws PrismException {
        return null;    //No action labels in this model
    }

    @Override
    public Object getTransitionAction(int i, int offset) throws PrismException {
        return null;    //No action labels in this model
    }

    @Override
    public double getTransitionProbability(int i, int offset) throws PrismException {

        //When dead, the state loops with prob. 1.0
        //When moving, each transition is the joint probability of each param. of the Power model.
        if(end){
            return 1.0;
        }
        else{
            //todo: this is a simple hack for now
            int[] offsets = new int[]{offset};

            double jointProb = 1;
            for (double prob: wm.getProbabilities(offsets)) {
                jointProb *= prob;
            }
            return jointProb;
        }
    }

    //todo: generalize later by allowing admissable range sizes to vary across different R.V.s
    private int[] offsetHelper(int offset){
        int numParams = wm.getNumParams();
        int supportSize = wm.getParamSupportSize();
        int[] offsets = new int[numParams];

        //change from base 10(offset) to base of the supportSize (assuming equal supportSize for each var.)
        int temp_offset = offset;
        for (int j = 0; j < offsets.length; j++) {
            offsets[j] = temp_offset % supportSize;
            temp_offset = (int) ((double) temp_offset / (double) supportSize);
        }
        return offsets;
    }

    @Override
    public State computeTransitionTarget(int k, int offset) throws PrismException {
        State target = new State(exploreState);

        if(!end){

            if(batteryAmount < batteryThreshold || waypointIndex == displacementVectors.size()){
            //vector addition of all waypoints reached sofar to attain straight line from home to current position.
                if(batteryAmount < batteryThreshold){
                    System.out.println("ERROR: Battery threshold reached");
                }
                else {
                    System.out.println("COMPLETED MISSION: UUV at end of trajectory");
                }
            System.out.println("CURRENT STATUS: Returning back to home");

                //todo simplify: rather than summing all traversed displacements, is there an alt?
                    double[] returnDisplacement = new double[NUM_COODS];
                    for(int i = 0; i < waypointIndex; i++){
                        for (int j = 0; j< returnDisplacement.length; j++){
                            returnDisplacement[j] += displacementVectors.get(i)[j];
                        }
                    }
                // moving now in the reverse direction towards home
                for (int i = 0; i < returnDisplacement.length; i++) { returnDisplacement[i] = -returnDisplacement[i]; }

                System.out.println("DEPLETING BATTERY: Current battery level: " + batteryAmount + " wattHrs");
                int temp_battery_amount = (int) (batteryAmount - wm.getWork(returnDisplacement, speed, offsetHelper(offset)));
                System.out.println("DEPLETING BATTERY: Remaining battery level after displacement: " + temp_battery_amount  + " wattHrs \n");


                return target.setValue(0, (temp_battery_amount < 0 ? -1 : temp_battery_amount))
                        .setValue(1,true);
            }
            else{
                System.out.println("CURRENT STATUS: Battery threshold not met or plan not completed");

                // threshold not reached & waypoint not final
                System.out.println("DEPLETING BATTERY: Current battery level: " + batteryAmount  + " wattHrs");
                int temp_battery_amount =  (int) (batteryAmount - wm.getWork(displacementVectors.get(waypointIndex), speed, offsetHelper(offset)));

                System.out.println("DEPLETING BATTERY: Remaining battery level after displacement: " + temp_battery_amount  + " wattHrs \n");

                return target.setValue(0, (temp_battery_amount < 0 ? -1 : temp_battery_amount))
                        .setValue(1,temp_battery_amount < 0)
                        .setValue(2,waypointIndex + 1);
            }
        }
        else {
            System.out.println("CURRENT STATUS: UUV is stationary \n");
            return target;
        }
    }

    @Override
    public ModelType getModelType() {
        return ModelType.DTMC;
    }

    @Override
    public int getNumVars() {
        return 1;
    }

    @Override
    public List<String> getVarNames() {
        return Arrays.asList("batteryAmount","end","waypointIndex");
    }

    @Override
    public List<Type> getVarTypes() {
        return Arrays.asList(TypeInt.getInstance(), TypeBool.getInstance());
    }

    @Override
    public VarList createVarList() throws PrismException {
        VarList varList = new VarList();
        try {
            varList.addVar(new Declaration("batteryAmount", new DeclarationInt(Expression.Int(-1), Expression.Int(initialBatteryAmount))), 0, null);
            varList.addVar(new Declaration("end", new DeclarationBool()), 0, null);
            varList.addVar(new Declaration("waypointIndex", new DeclarationInt(Expression.Int(0), Expression.Int(displacementVectors.size()))), 0, null);
        } catch (PrismLangException e) {
            e.printStackTrace();
        }
        return varList;
    }

    @Override
    public int getNumLabels()
    {
        return 1;
    }

    @Override
    public List<String> getLabelNames()
    {
        return Arrays.asList("home");
    }

    @Override
    public boolean isLabelTrue(int i) throws PrismException
    {
        switch (i) {
            // "home"
            case 0:
                return (batteryAmount >= 0 & end);
        }
        // Should never happen
        return false;
    }

}


// references: geodatic to EFCF formula - Coordinate systems and units in HiSPARC, http://docs.hisparc.nl/coordinates/HiSPARC_coordinates.pdf