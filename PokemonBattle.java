import java.util.*;
import java.io.*;
/*
PokemonBattle class that takes care of tracking turns and running the bulk of every battle. each fight object has a friendly pokemon, enemy pokemon, list of
other pokemon on your team, a counter for what round your on, and a value to keep track of when you win. the two methods, one used for your turn and one for
the computers turn are called back and forth until either all of your pokemon faint or you defeat the enemy pokemon. they also use other supplimentary methods
used for switching your pokemon out, picking an attack, ending a round or battle, or healing a pokemons energy or health (used with healing methods in the 
pokemon class) 
*/
class PokemonBattle{
	
	static Scanner in = new Scanner(System.in);
	private Pokemon currentPoke; //your pokemon
	private Pokemon enemyPoke; //computer pokemon
	private ArrayList<Pokemon> fightingPokes; //your team
	private int round = 0; //the round, only goes up to 2 then resets, controls when pokemon regain energy
	private boolean won = false; //keeps track of if you win or lose
	
	public PokemonBattle(Pokemon pokePick, Pokemon badPoke, boolean givenTurn, ArrayList<Pokemon> pokeList){ //constructor for the pokemon battle
		fightingPokes = new ArrayList<Pokemon>();
		currentPoke = pokePick;
		enemyPoke = badPoke;
		fightingPokes = pokeList;
	}
	
	public void turnStart(){ //controls the users turn, gives them turn options and checks if their stunned or if they win or lose
		String status = currentPoke.getStatus();
		if(status.equals("stunned")){ //skips your turn if youve been stunned
			System.out.println("Your stunned for this turn!");
			round++;
			currentPoke.clearStat(); //takes away the stun because it only works for one round
		}
		else{ //if not stunned
			System.out.println("\n1. attack\n2. retreat\n3. pass");
			System.out.print("choose an action:  ");
			int turnChoice = in.nextInt();
			if(turnChoice == 1){
				pickAttack(); //allows you to pick which attack to use
				round++;
			}
			else if(turnChoice == 2){ //switch pokemon
				int newPoke = switchOut(); //gets your new choice
				currentPoke = fightingPokes.get(newPoke); //sets the currentPoke to your new pokemon
				currentPoke.clearStat(); //since a pokemon cant be stunned for more than one turn, and a pokemon cant switch in and out in the same turn, the 
										 //stun is removed when they enter
				System.out.println(currentPoke + ", I choose you!");
				round++;
			}
			else if(turnChoice == 3){ //pass, dosent do anything
				System.out.println("You pass your turn"); 
				round++;
			}
		}
		if(won == false && round != 2){ //passes turn to the enemy
			enemyTurn(); 
		}
		else if(won == false && round == 2){ //gives both pokemon 10 energy and then passes to enemy
			endRound();
			enemyTurn();
		}
		else if(won == true && round == 2){ //heals energy and health then gives you the win
			healEnergy();
			healHp();
			roundWin();
		}
		else if(won == true && round != 2){ //only gives you health then gives you the win, energy is dependent on what stage in the round you won
			healHp();
			roundWin();
		}
	}
	
	public void pickAttack(){ //allows you to pick what attack you want to use, checks if you can, and checks if it faints the enemy
		currentPoke.displayAttacks(); //prints out your options
		System.out.print("\nPick an Attack:  ");
		int attackNum = in.nextInt() - 1;
		boolean attackPossible = currentPoke.checkEnergy(attackNum); //checks if the attack is possible
		if(attackPossible == false){ //loops back to the beginning of your turn so you can make another choice
			System.out.println("You cant use that!");
			turnStart();
		}
		else{
			currentPoke.getAttack(attackNum, enemyPoke, currentPoke); //initiates attack if possible
			boolean fainted = enemyPoke.fainted();
			if(fainted){
				won = true;
				currentPoke.clearStat(); //clears all status's when you win
			}
		}
	}
	
	public void enemyTurn(){ //runs the enemies turn, checks for stun, gets a random attack, uses the attack and checks if it faints the users pokemon
		String status = enemyPoke.getStatus();
		if(status.equals("stunned")){ //skips the enemies turn if their stunned
			System.out.println(enemyPoke + " is stunned!");
			round++;
			enemyPoke.clearStat(); //clears stun
		}
		else{
			int randomizer = enemyPoke.getAttacks(); //the randomizer decides the range of numbers you can randomly get and is dependent of the number of attacks
			int attackChoice = (int)(Math.floor(Math.random() * randomizer)); //gets the random number
			boolean attackPossible = enemyPoke.checkEnergy(attackChoice); //checks if the attack can be used
			if(attackPossible == false){ //forces the computer to pass if it cant use the attack
				System.out.println("\n" + enemyPoke + " passes!");
				round++;
			}
			else{ //if the computer can attack
				enemyPoke.getAttack(currentPoke, attackChoice, enemyPoke);
				boolean fainted = currentPoke.fainted();
				if(fainted){ //allows you to pick a new pokemon if your old one faints
					System.out.println(currentPoke + " fainted!\n");
					fightingPokes.remove(currentPoke);
					checkLose(); //checks to make sure you have pokemon to pick from
					round++;
				}
				else{
					round++;
				}
			}
		}
		if(round == 2){ //if 2 turns have passed add energy to each pokemon
			endRound();
		}
		if(won == false){ //only go back to the users turn if no one has won yet
			turnStart();
		}
	}
	
	public int switchOut(){ //lets you swap out your pokemon for another in your team, used for when a pokemon faints or you choose to switch out
		int count = 1;
		for(Pokemon p : fightingPokes){ //displays your options
			String name = p.toString();
			System.out.printf("%d. %s \n", count, name);
			count++;
		}
		System.out.print("\nChoose which pokemon to use:  ");
		int chosenPoke = in.nextInt() - 1;
		return chosenPoke; //returns the index number of the pokemon you want to use
	}
	
	public void endRound(){ //displays end of round text on the screen and gives all team pokes and battling pokes 10 energy
		System.out.println("\nNext round start!");
		for(Pokemon i : fightingPokes){
			i.addEnergy(10);
		}
		enemyPoke.addEnergy(10);
		enemyPoke.updated(); //shows the stats for the two battling pokemon
		currentPoke.updated();
		round = 0;
	}
	
	public void roundWin(){ //sets won to true and ends the battle, which collapses the methods back to the main where the process restarts
		System.out.println(enemyPoke + " fainted!");
		for(Pokemon i : fightingPokes){
			i.updated(); //updated stats for all your pokemon
		}
		System.out.println("\nNext battle start\n");
		won = true;
	}
	
	public void checkLose(){ //checks to see if youve lost the game, used everytime one of your pokemon dies
		if(fightingPokes.size() > 0){ //if you didnt lose, send a new pokemon out
			int newPoke = switchOut();
			currentPoke = fightingPokes.get(newPoke);
			System.out.println(currentPoke + ", I choose you!");
			enemyPoke.clearStat();
		}
		else{ //if you did, won equals true and it ends the game
			System.out.println("Your team has been defeated!");
			won = true;
		}
	}
	
	public void healEnergy(){ //heals your pokemons energy, used after you win a battle at the end of a round
		for(Pokemon i : fightingPokes){
			i.addEnergy(10);
		}
	}
	
	public void healHp(){ //heals your pokemons health, used after you win a battle regardless of round
		for(Pokemon i : fightingPokes){
			i.addHealth(20);
		}
	}
}