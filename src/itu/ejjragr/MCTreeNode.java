package itu.ejjragr;

import java.util.ArrayList;
import java.util.Random;

import competition.cig.robinbaumgarten.astar.LevelScene;
import competition.cig.robinbaumgarten.astar.sprites.Mario;

/**
 * A single Node of the Monte Carlo Tree where you can traverse both
 * up (parent) and down (children) from. Each node contains the state
 * that it covers, the action used to get to the state, the total 
 * reward of itself and its children and finally a number of how many
 * times it has been visited (the number of nodes beneath it).
 * 
 * @author Emil
 *
 */
public class MCTreeNode {
	
	private static final int CHILDREN = 16;
	
	public static Random rand = new Random(1337);
	
	public LevelScene state;
	public boolean[] action;
	public MCTreeNode parent;
	public MCTreeNode[] children = new MCTreeNode[CHILDREN];
	public int numChildren = 0;
	public double reward = 0;
	public int visited = 0;

	/**
	 * Constructor for the MCTreeNode.
	 * 
	 * @param state The state that the node should have.
	 * @param action The action leading to the node's state.
	 * @param parent The parent of the new node or null if it is root.
	 */
	public MCTreeNode(LevelScene state, boolean[] action, MCTreeNode parent){
		this.state = state;
		this.action = action;
		this.parent = parent;
	}
	
	/**
	 * Creates a new child beneath this node in the tree from the given action.
	 * 
	 * @param action The action to be performed on the current state leading to
	 * the new child.
	 * @return The child node containing the state resulting from performing the
	 * action on the current node.
	 */
	public MCTreeNode createChild(boolean[] action){
		MCTreeNode child = new MCTreeNode(advanceStepClone(state, action),action,this);
		children[getChildIndex(action)] = child;
		numChildren++;
		
		return child;
	}
	
	/**
	 * Creates a child beneath this node by applying a random action on this state.
	 * 
	 * @return The new child node.
	 */
	public MCTreeNode createRandomChild(){
		ArrayList<Integer> spaces = getUnexpanded();
		return createChild(this.getActionForIndex(spaces.get(rand.nextInt(spaces.size()))));
	}
	
	/**
	 * Tells if the node has all its possible children created.
	 * 
	 * @return True if no more children can be created, else false.
	 */
	public boolean isExpanded(){
		for(int i = 0; i < CHILDREN; i++){
			if(children[i] == null) return false;
		}
		return true;
	}
	
	/**
	 * Calculates the confidence in a given node depending on the average reward of
	 * this node (exploitation) (and children) and how neglected the node has been
	 * (exploration).
	 * 
	 * @param cp The constant applied to the exploration part of the equation.
	 * @return A value of how attractive the node is to look into.
	 */
	public double calculateConfidence(double cp){ //TODO: FUCKING DYRT
		double exploitation = reward/this.visited;
		double exploration = 2*cp*Math.sqrt((2*Math.log(parent.visited))/this.visited); // Det er SQRT's SKYLD! :(
		//System.out.printf("Exploit: %f Explore: %f\n", exploitation, exploration);
		return exploitation + exploration;
	}
	
	/**
	 * Finds the best direct child by comparing their individual confidence, any ties
	 * are broken randomly.
	 * 
	 * @param cp The constant to use in the exploration part of the confidence equation.
	 * @return The direct child with the highest confidence value.
	 */
	public MCTreeNode bestChild(double cp){
		int best = -1;
		double score = -1;
		for(int i = 0; i < CHILDREN; i++){
			if(children[i] != null){
				double curScore = children[i].calculateConfidence(cp);
				if(curScore > score || (curScore == score && rand.nextBoolean())){
					score = curScore;
					best = i;
				}
			}
		}
		return best > -1 ? children[best] : null;
	}
	
	public double advanceXandReward(int ticks){
		LevelScene copy = advanceStepClone(state,getRandomAction());
		ticks--;
		for(int i = 0; i < ticks; i++) advanceStep(copy, getRandomAction());
		return this.calculateReward(copy);
	}
	
	// --- Private Methods
	
	/**
	 * Calculates the index in the array for a given action.
	 * 
	 * @param action The action that to find a child spot for.
	 * @return The index in the children array that corresponds to the given action.
	 */
	private int getChildIndex(boolean[] action){ //boolean left, boolean right, (boolean down), boolean jump, boolean speed
		//return (action[0] ? 16 : 0) + (action[1] ? 8 : 0) + (action[2] ? 4 : 0) + (action[3] ? 2 : 0) + (action[4] ? 1 : 0);
		return (action[Mario.KEY_LEFT] ? 8 : 0) + (action[Mario.KEY_RIGHT] ? 4 : 0) + (action[Mario.KEY_JUMP] ? 2 : 0) + (action[Mario.KEY_SPEED] ? 1 : 0);
	}
	
	/**
	 * Calculates what action corresponds to a given index in the children array.
	 * 
	 * @param index The index to find the action for.
	 * @return The action that a children and the index should have.
	 */
	private boolean[] getActionForIndex(int index){
		boolean[] result = new boolean[5];
		/*if(index >= 16) { result[0] = true; index -= 16; }
		if(index >= 8) { result[1] = true; index -= 8; }
		if(index >= 4) { result[2] = true; index -= 4; }
		if(index >= 2) { result[3] = true; index -= 2; }
		if(index >= 1) { result[4] = true; index -= 1; }*/
		if(index >= 8) { result[Mario.KEY_LEFT] = true; index -= 8; }
		if(index >= 4) { result[Mario.KEY_RIGHT] = true; index -= 4; }
		if(index >= 2) { result[Mario.KEY_JUMP] = true; index -= 2; }
		if(index >= 1) { result[Mario.KEY_SPEED] = true; index -= 1; }
		return result;
	}
	
	/**
	 * Calculates an ArrayList of the indices for any missing children. This array
	 * will have size = 0 if isExpanded() is true.
	 * 
	 * @return An ArrayList of the indices for any missing children.
	 */
	private ArrayList<Integer> getUnexpanded(){
		ArrayList<Integer> result = new ArrayList<Integer>(CHILDREN);
		for(int i = 0; i < CHILDREN; i++){
			if(children[i] == null) result.add(i);
		}
		return result;
	}
	
	/**
	 * Advances the given state by performing the given action on it.
	 * 
	 * @param state The state to start from.
	 * @param action The action to perform on the given state.
	 * @return The resulting state from performing the action on the given state.
	 */
	private LevelScene advanceStepClone(LevelScene state, boolean[] action){
		try {
			LevelScene result = (LevelScene) state.clone();
			advanceStep(result,action);
			return result;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void advanceStep(LevelScene state, boolean[] action){
		state.mario.setKeys(action);
		state.tick();
	}
	
	private boolean[] getRandomAction(){
		return getActionForIndex(rand.nextInt(CHILDREN));
	}
	
	/**
	 * Calculates the reward for this state. This can be done through different
	 * heuristics but generally it should rely heavily on how far Mario is through
	 * the level, and be very low if Mario dies or loses size (Mode). The value
	 * must be between 0 and 1 for the current calculateConfidence equation to work
	 * as intended.
	 * 
	 * @return A number between 0 and 1 telling how good the current state is, where
	 * 0 is worst and 1 is best.
	 */
	public double calculateReward(LevelScene state){ // TODO: Just some hackup
		if(state.mario.deathTime > 0 || marioShrunk(state) > 1.0) return 0;
		double reward = state.mario.x;
		reward += 10 * (state.enemiesKilled - parent.state.enemiesKilled);
		reward += 1 * (state.coinsCollected - parent.state.coinsCollected);
		
		reward += 10 * (state.mario.x - parent.state.mario.x);
		//reward /= marioShrunk(state);
		return reward;
		//return ((double)state.mario.x) / (state.level.width * marioShrunk(state));
	}
	
	/**
	 * Tells if Mario's Mode has been decreased since last state. Used for dividing
	 * the reward so /2 for losing modes and /1 (nothing) for keeping.
	 * 
	 * @return 2 if it has decreased else 1.
	 */
	private double marioShrunk(LevelScene state){
		int before = marioSize(parent.state.mario);
		int after = marioSize(state.mario);
		if(parent != null && after < before) return 4.0;
		if(parent != null && after > before) return 1/4.0;
		return 1.0;
	}
	
	/**
	 * Finds out what size mario has:
	 * 2 = Fire
	 * 1 = Large
	 * 0 = Small
	 * 
	 * @param mario The Mario object to check size of.
	 * @return An integer telling the current size of Mario.
	 */
	private int marioSize(Mario mario){
		if(mario.fire) return 2;
		if(mario.large) return 1;
		return 0;
	}
}