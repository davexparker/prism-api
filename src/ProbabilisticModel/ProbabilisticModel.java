package ProbabilisticModel;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
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

public class ProbabilisticModel  extends DefaultModelGenerator {


    private int battery_amount;
    private int battery_size;
    private int waypointIndex;
    private double battery_threshold;
    private List<int[]> displacementVectors;
    private ProbabilisticWorkModel wm = new ProbabilisticWorkModel ();

    //current state to be explored.
    private State exploreState;
    private boolean end;


    public ProbabilisticModel(int battery_amount, double threshold_percent, List<int[]> missionWaypoints){

        this.battery_size = battery_amount;
        this.battery_amount = battery_amount;
        this.waypointIndex = 0;
        this.battery_threshold = threshold_percent*battery_amount;
        this.displacementVectors = new ArrayList<>();
        this.end = false;

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
        //1 variable: index 0 -> battery_amount,
        return new State(3).setValue(0,battery_amount).setValue(1,end).setValue(2,waypointIndex);
    }

    @Override
    public void exploreState(State state) throws PrismException {

        this.exploreState = state;

        battery_amount = (Integer) exploreState.varValues[0];
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
        int temp_offset = offset;

        //change from base 10(offset) to base of the supportSize (assuming equal supportSize for each var.)
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

            if(battery_amount< battery_threshold|| waypointIndex == displacementVectors.size()){
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

                int temp_battery_amount = (int) (battery_amount - wm.getWork(returnDisplacement, offsetHelper(offset)));
                System.out.println("Final battery amountB: "+temp_battery_amount);
                return target.setValue(0, (temp_battery_amount < 0 ? -1 : temp_battery_amount))
                        .setValue(1,true);
            }
            else{
                System.out.println("still forward");

                // threshold not reached & waypoint not final
                int temp_battery_amount =  (int) (battery_amount - wm.getWork(displacementVectors.get(waypointIndex), offsetHelper(offset)));
                System.out.println("battery_amount: "+temp_battery_amount);

                if (temp_battery_amount < 0){System.out.println("Final battery amountA: "+battery_amount); }

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
        return Arrays.asList("battery_amount","end","waypointIndex");
    }

    @Override
    public List<Type> getVarTypes() {
        return Arrays.asList(TypeInt.getInstance(), TypeBool.getInstance());
    }

    @Override
    public VarList createVarList() throws PrismException {
        VarList varList = new VarList();
        try {
            varList.addVar(new Declaration("battery_amount", new DeclarationInt(Expression.Int(-1), Expression.Int(battery_size))), 0, null);
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
    };

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
                return (battery_amount >= 0 & end);
        }
        // Should never happen
        return false;
    }

}
