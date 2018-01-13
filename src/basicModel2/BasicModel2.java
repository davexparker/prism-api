package basicModel2;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import parser.State;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import parser.type.Type;
import parser.type.TypeInt;
import prism.DefaultModelGenerator;
import prism.ModelType;
import prism.PrismException;
import prism.PrismLangException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicModel2 extends DefaultModelGenerator {


    private int battery_amount;

    private List<int[]> displacementVectors;


    public int battery_size;
    public int waypointIndex;
    public double battery_threshold;

    public boolean end;

    //current state to be explored.
    private State exploreState;



    private BasicWorkModel2 wm = new BasicWorkModel2();

    public BasicModel2(int battery_amount, double threshold_percent, List<int[]> missionWaypoints){
        this.battery_size = battery_amount;
        this.battery_amount = battery_amount;
        this.waypointIndex = 0;
        this.battery_threshold = threshold_percent*battery_amount;


        this.displacementVectors = new ArrayList<>();
        this.end = false;


        for (int i = 0; i < missionWaypoints.size() - 1; i++) {

            int[] displacement = new int[3];
            for (int j = 0; j < 3; j++) {
                displacement[j] = missionWaypoints.get(i+1)[j] - missionWaypoints.get(i)[j];
            }
            displacementVectors.add(displacement);
        }


        //arbitrary training
        double[][] inputData = new double[4][3];
        inputData[0] = new double[]{10,0,0};
        inputData[1] = new double[]{0,10,0};
        inputData[2] = new double[]{0,0,-10};
        inputData[3] = new double[]{10,10,-10};

        double[] outputData = new double[]{1,2,3,20};
        wm.batchLearn(inputData,outputData);

    }


    public int getBatteryReturningHome(){

        // 2) waypoints met, threshold not met
        // 3) threshold met, waypoints not met

        if(!end){
            int[] returnDisplacement = new int[]{0,0,0};
            for(int i = 0; i < waypointIndex; i++){
                for (int j = 0; j< 3; j++){
                    returnDisplacement[j] += displacementVectors.get(i)[j];
                }
            }
            for (int i = 0; i < 3; i++) {
                returnDisplacement[i] = -returnDisplacement[i];
                System.out.println("return_displ: "+returnDisplacement[i]);
            }


            battery_amount -= wm.getWork(returnDisplacement);

            end = true;
        }
        System.out.println("Final battery amount: "+battery_amount);

        return battery_amount;
    }


    public int getBatteryMovingToWaypoint(){

        // 1) threshold not reached & waypoint not final

        if (battery_amount >= battery_threshold & waypointIndex < displacementVectors.size()){
            battery_amount -= wm.getWork(displacementVectors.get(waypointIndex));
            waypointIndex++;

            System.out.println("battery_amount: "+battery_amount);
        }
        return battery_amount;

    }


    public int getRemainingBattery() {

        for (int[] displacement: displacementVectors) {

            battery_amount -= wm.getWork(displacement);
            waypointIndex++;

            System.out.println("battery_amount: "+ battery_amount);

            if (battery_amount <= battery_threshold) {
                System.out.println("Battery threshold reached:  "+battery_threshold);
                break;
            }
        }

        System.out.println("Returning home.");

        int[] returnDisplacement = new int[]{0,0,0};
        for(int i = 0; i < waypointIndex; i++){
            for (int j = 0; j< 3; j++){
                returnDisplacement[j] += displacementVectors.get(i)[j];
            }
        }
        for (int i = 0; i < 3; i++) {
            returnDisplacement[i] = -returnDisplacement[i];
        }

        battery_amount -= wm.getWork(returnDisplacement);

        System.out.println("waypointIndex: "+ waypointIndex);
        System.out.println("battery_end: "+ battery_amount);

        end = true;

        return battery_amount;
    }



    @Override
    public State getInitialState() throws PrismException {
        //1 variable: index 0 -> battery_amount,
        return new State(1).setValue(0,battery_amount);
    }

    @Override
    public void exploreState(State state) throws PrismException {
        this.exploreState = state;

        battery_amount = ((Integer) exploreState.varValues[0]).intValue();

    }

    @Override
    public State getExploreState() {
        return exploreState;
    }

    @Override
    public int getNumChoices() throws PrismException {
        return 1;
    }

    @Override
    public int getNumTransitions(int i) throws PrismException {
        return 1;
    }

    @Override
    public Object getTransitionAction(int i) throws PrismException {
        return null;
    }

    @Override
    public Object getTransitionAction(int i, int offset) throws PrismException {
        return null;
    }

    @Override
    public double getTransitionProbability(int i, int offset) throws PrismException {
        return 1;
    }

    @Override
    public State computeTransitionTarget(int i, int offset) throws PrismException {
        State target = new State(exploreState);


        if(!end){
            System.out.println("not end");
            if(battery_amount< battery_threshold|| waypointIndex == displacementVectors.size()){
                System.out.println("moving back");
                return target.setValue(0,getBatteryReturningHome());
            }
            else{
                System.out.println("still forward");
                return target.setValue(0,getBatteryMovingToWaypoint());
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
        return Arrays.asList("battery_amount");
    }

    @Override
    public List<Type> getVarTypes() {
        return Arrays.asList(TypeInt.getInstance());
    }

    @Override
    public VarList createVarList() throws PrismException {
        VarList varList = new VarList();
        try {
            varList.addVar(new Declaration("battery_amount", new DeclarationInt(Expression.Int(-5*battery_size), Expression.Int(battery_size))), 0, null);
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
                return (battery_amount >= 0);
        }
        // Should never happen
        return false;
    }

}
