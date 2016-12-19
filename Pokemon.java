import java.util.*;
import java.io.*;
/*
Class used to create all Pokemon objects. Pokemon objects are used to keep track of multiple stats per pokemon and to keep track of multiple pokemons stats
at once without having to set an overly exessive amount of variables or to avoid over refrencing pokemon stats in method calls. all damage allocation, use off
attacks, addition of health and energy, and displaying of pokemon info onto the game screen is taken care of here. the Attack class within this one allows all
of a pokemons attacks to also be objects. each attack object that is created for a pokemon can only be seen by that specific pokemon, so there is no overlap
in attack fields between pokemon and allows for easier access of attack stats.
*/
class Pokemon{ 
	
	static Scanner in = new Scanner(System.in); 
	//keeps track of all elements of a Pokemon so that you can access each element easily when needed
	private String name; 
	private int hp; //the max hp the pokemon can have, never changes
	private int currentHp; //the hp the pokemon has at the moment
	private int energy = 50; //all pokemon have the same energy
	private int minEnergy; //the smallest amount of energy a pokemon needs to use an attack
	//used for damage calculation
	private String type;
	private String resist;
	private String weak;
	//number and list of all attacks a pokemon can use
	private int attackNum;
	private ArrayList<Attack> attackList;
	//keeps track of changes/effects created by attack specials
	private String status = "";
	
	//constructor for the pokemon objects, takes the name, health, type, resistance, weakness, number of attacks, and the attacks themselves from the txt. file
	public Pokemon(String data){  
		attackList = new ArrayList<Attack>();
		String[] stats = data.split(",");
		name = stats[0];
		hp = Integer.parseInt(stats[1]);
		currentHp = hp;
		type = stats[2];
		resist = stats[3];
		weak = stats[4];
		attackNum = Integer.parseInt(stats[5]);
		Attack firstMove = new Attack(stats[6], Integer.parseInt(stats[7]), Integer.parseInt(stats[8]), stats[9]); //attacks are objects with their own fields
		minEnergy = Integer.parseInt(stats[7]);
		attackList.add(firstMove);
		if(attackNum > 1){ //not all pokemon have 2 or 3 attacks so you only create these attack objects if the number of attacks in the txt. file allow it
			Attack secondMove = new Attack(stats[10], Integer.parseInt(stats[11]), Integer.parseInt(stats[12]), stats[13]);
			if(Integer.parseInt(stats[11]) < minEnergy){
				minEnergy = Integer.parseInt(stats[11]); //attack with the smallest energy cost is the minimum amount of energy a pokemon needs to attack
			}
			attackList.add(secondMove);
		}
		if(attackNum > 2){
			Attack thirdMove = new Attack(stats[14], Integer.parseInt(stats[15]), Integer.parseInt(stats[16]), stats[17]);
			if(Integer.parseInt(stats[15]) < minEnergy){
				minEnergy = Integer.parseInt(stats[15]);
			}
			attackList.add(thirdMove);
		}
	}
	
	public String toString(){ //get the name of the pokemon for mainly display features only
		return name;
	}
	
	public void displayAttacks(){ //shows the user what attacks the pokemon they have selected can use
		int counter = 1;
		for(Attack i : attackList){
			i.attackInfo(counter);
			counter++;
		}
	}
	
	//the first getAttack method that is used by the users pokemon to get the attack they want to use, take energy away from the pokemon using it, get the damage,
	//type, and ability of the attack, and then calls damage to inflict damage on the enemy pokemon. attackNum is the attack picked by the user, and enemyPoke
	//and currentPoke are the pokemon currently fighting.
	public String getAttack(int attackNum, Pokemon enemyPoke, Pokemon currentPoke){ 
		Attack choiceAttack = attackList.get(attackNum);
		choiceAttack.useAttack();
		int damage = choiceAttack.getDamage();
		String special = choiceAttack.getSpecial();
		String status = enemyPoke.damage(damage, type, special, currentPoke);
		return status; //returns whatever status was inflicted by an ability is any for use later in PokemonBattle
	}
	
	//operates the same as the first getAttack but with shuffled parameters for use by the enemy pokemon. currentPoke and enemyPoke are the same as above but
	//attackNum is the number randomly generated to decide what attack the enemy pokemon will use
	public String getAttack(Pokemon currentPoke, int attackNum, Pokemon enemyPoke){
		Attack choiceAttack = attackList.get(attackNum);
		choiceAttack.useAttack();
		int damage = choiceAttack.getDamage();
		String special = choiceAttack.getSpecial();
		String status = currentPoke.damage(damage, type, special, enemyPoke);
		return status; //returns whatever status was inflicted by an ability is any for use later in PokemonBattle
	}
	
	//inflicts damage on a pokemon battling. damage is the amount of damage inflicted before modifiers, attackType is the type of attack being used, special
	//is the ability of the attack being used, special is any special ability the attack has, and the damager is the pokemon that is using the attack
	public String damage(int damage, String attackType, String special, Pokemon damager){
		double chance = Math.random(); //random number is used to choose the odds of the stun, wild card, and wild storm
		boolean storm = false;
		if(special.equals("stun")){ 
			if(chance >= 0.5){
				System.out.println(name + " has been stunned!");
				status = "stunned"; //the effect of stun is added later in PokemonBattle
			}
		}
		else if(special.equals("wild card")){
			if(chance >= 0.5){ //if they miss the blank return kills the method before is damages the pokemon
				System.out.println("The attack missed...");
				return "";
			}
			else{
				System.out.println("The attack hits!");
			}
		}
		else if(special.equals("wild storm")){
			if(chance >= 0.5){ //same principle as wild card but a status is set for use later in the damage method
				System.out.println("The attack hits!");
				storm = true;
			}
			else{
				System.out.println("The attack missed...");
				storm = false;
				return ""; //kills method if attack misses
			}
		}
		else if(special.equals("disable")){
			status = "disable"; //effects of disable used later
		}
		else if(special.equals("recharge")){
			damager.addEnergy(20); //adds 20 energy to the pokemon (energy for the attack is removed in getAttack, so this dosent make it possible to use
		}						   //any moves the poke shouldnt be able too)
		if(attackType.equals(resist)){
			damage = damage / 2; //the the attack type is the currentPokes resistance then you do half damage
			System.out.println("Its not very effective...");
		}
		else if(attackType.equals(weak)){
			damage = damage * 2; //the the attack type is the currentPokes weakness then you do double damage
			System.out.println("Its super effective!!!");
		}
		String damagerStat = damager.getStatus(); //gets the current status of the damager pokemon for disable
		System.out.println(storm);
		if(damagerStat.equals("disable")){
			currentHp = currentHp - (damage - 10); //takes ten damage off any attack used. disable isnt turned off until another battle starts
		}
		else if(storm == true && currentHp > 0){ //recalls damage if the storm variable was set to true at the top of the method. this repeats until false
			System.out.println("The wild storm attack goes again!");
			damage(damage, attackType, special, damager);
		}
		else{ //regular damage
			currentHp = currentHp - damage;
		}
		return "";
	} 
	
	public void updated(){ //displays the updated stats (name, hp, energy) of the pokemon of the screen, used mainly in design of the game interface
		System.out.println("\n" + name);
		System.out.println("hp: " + currentHp);
		System.out.println("energy: " + energy);
	}
	
	public int getAttacks(){ //gets the number of attacks the pokemon has, used to randomly pick an attack for the AI
		return attackNum;
	}
	
	public void addEnergy(int energyAmount){ //adds a specific amount of energy to the pokemon without going over 50
		if(energy <= (50 - energyAmount)){
			energy = energy + energyAmount;
		}
		else{
			energy = 50; //if there is more energy being added than there is room for, cap at 50
		}
	}
	
	public void addHealth(int hpAmount){ //same concept as addEnergy except with the pokemons health
		if(currentHp <= (hp - hpAmount)){
			currentHp = currentHp + hpAmount;
		}
		else{
			currentHp = hp;
		}
	}
	
	public boolean checkEnergy(int attackNum){ //checks to see if the pokemon can currently use the attack they wish to use
		Attack choiceAttack = attackList.get(attackNum); //gets desired attack
		int neededEnergy = choiceAttack.getEnergy(); //gets energy needed for said attack
		if(neededEnergy <= energy){ 
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean fainted(){ //checks to see if the pokemon has any health left
		if(currentHp <= 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public String getStatus(){ //gets pokemon status used in PokemonBattle for stun and in damage for disable
		return status;
	}
	
	public void clearStat(){ //clears any status the pokemon has
		status = "";
	}
	
	//nested attack class for use by the pokemon class specifically. nested class allows for the use of public variable that only the pokemon class can see
	class Attack{ 
		
		//all attack info (name, energy, damage, ability) for refrence at anny point in the pokemon class
		public String attackName;
		public int attackEnergy;
		public int damage;
		public String ability;
		
		public Attack(String Aname, int Aenergy, int Adamage, String Aability){ //created in the constructor for the pokemon class, sets all attack stats
			attackName = Aname;
			attackEnergy = Aenergy;
			damage = Adamage;
			ability = Aability;
		}
		
		public void attackInfo(int listNum){ //prints out all attack info for a specific attack, used in the game interface to show the user usable attacks
			System.out.printf("\n%d. %s, %d, %d, %s", listNum, attackName, attackEnergy, damage, ability);
		}
		
		public void useAttack(){ //takes energy away from the pokemon using the attack
			System.out.printf("\n%s used %s!\n", name, attackName);
			energy = energy - attackEnergy;
		}
		
		public int getDamage(){ //gets the damage of a specific attack for use in damage calculation
			return damage;
		}
		
		public int getEnergy(){ //gets energy of a attack, for use in finding out if an attack is usable or not
			return attackEnergy;
		}
		
		public String getSpecial(){ //gets the special for use in getAttack and damage
			return ability;
		}
	}
}
