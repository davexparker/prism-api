package ProbabilisticModel2;

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

//learns power instead of work

public class ProbabilisticModel2 extends DefaultModelGenerator {

    private int batterySize;
    private int speed;   //0 < speed < Inf

    private int batteryAmount;
    private int waypointIndex;
    private double batteryThreshold;
    private List<int[]> displacementVectors;
    private ProbabilisticPowerModel wm = new ProbabilisticPowerModel();

    //current state to be explored.
    private State exploreState;
    private boolean end;


    public ProbabilisticModel2(int batteryAmount, double threshold_percent, List<int[]> missionWaypoints, int speed){

        this.batterySize = batteryAmount;
        this.batteryAmount = batteryAmount;
        this.waypointIndex = 0;
        this.batteryThreshold = threshold_percent*batteryAmount;
        this.displacementVectors = new ArrayList<>();
        this.end = false;
        this.speed = speed;

//        this.exploreState = new State(1);

        for (int i = 0; i < missionWaypoints.size() - 1; i++) {
            int[] displacement = new int[3];
            for (int j = 0; j < 3; j++) {
                displacement[j] = missionWaypoints.get(i+1)[j] - missionWaypoints.get(i)[j];
            }
            displacementVectors.add(displacement);
        }
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
        //When moving we have paramSupportSize^(numParams) different transitions.
        if(end){
            return 1;
        }
        else {
            return (int) Math.pow(wm.getParamSupportSize(),wm.getNumParams());
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
        //When moving, each transition is the joint probability of each param. of the Work model.

        if(end){
            return 1.0;
        }
        else{
            int[] offsets = offsetHelper(offset);

            double jointProb = 1;
            for (double prob: wm.getProbabilities(offsets)) {
                jointProb *= prob;
            }
            return jointProb;
        }
    }

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
            System.out.println("not end");

            if(batteryAmount < batteryThreshold || waypointIndex == displacementVectors.size()){
                System.out.println("moving back");

                    //vector addition of all waypoints reached sofar to attain straight line from home to current position.
                    int[] returnDisplacement = new int[]{0,0,0};
                    for(int i = 0; i < waypointIndex; i++){
                        for (int j = 0; j< 3; j++){
                            returnDisplacement[j] += displacementVectors.get(i)[j];
                        }
                    }

                // moving now in the reverse direction towards home
                for (int i = 0; i < 3; i++) { returnDisplacement[i] = -returnDisplacement[i]; }

                int temp_battery_amount = (int) (batteryAmount - wm.getWork(returnDisplacement, speed, offsetHelper(offset)));
                System.out.println("Final battery amountB: "+temp_battery_amount);
                return target.setValue(0, (temp_battery_amount < 0 ? -1 : temp_battery_amount))
                        .setValue(1,true);
            }
            else{
                System.out.println("still forward");

                // threshold not reached & waypoint not final
                int temp_battery_amount =  (int) (batteryAmount - wm.getWork(displacementVectors.get(waypointIndex), speed, offsetHelper(offset)));
                System.out.println("batteryAmount: "+temp_battery_amount);

                if (temp_battery_amount < 0){System.out.println("Final battery amountA: "+ batteryAmount); }

                return target.setValue(0, (temp_battery_amount < 0 ? -1 : temp_battery_amount))
                        .setValue(1,temp_battery_amount < 0)
                        .setValue(2,waypointIndex + 1);
            }
        }
        else {
            System.out.println("end");
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
            varList.addVar(new Declaration("batteryAmount", new DeclarationInt(Expression.Int(-1), Expression.Int(batterySize))), 0, null);
            varList.addVar(new Declaration("end", new DeclarationBool()), 0, null);
            varList.addVar(new Declaration("waypointIndex", new DeclarationInt(Expression.Int(0), Expression.Int(displacementVectors.size()))), 0, null);
        } catch (PrismLangException e) {
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
