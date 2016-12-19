import java.util.*;
import java.io.*;
/*
Main class that initializes the pokemon game. contains a load method to load in the pokemon as well as other set up methods that get the game ready to be played
allows you to pick your team and also controls the beginning of every battle before passing it off to the PokemonBattle class by creating a fight object. fields
are used mainly to keep track of your team of pokemon and the list of pokemon left to defeat, as well as whether you are still battling or not and to check 
after every battle whether you have won the game
*/
public class PokemonArena{ 
	
	static Scanner in = new Scanner(System.in);
	private static ArrayList<Pokemon> allPoke; //arraylist of all the pokemon in the game
	private static ArrayList<Pokemon> teamPoke; //arraylist of the pokemon in your team
	private static boolean battling = true; //makes sure the game runs while there are still pokemon to beat
	private static boolean givenTurn; //hold value of whether you go first or not at the start of each battle
	
	public static void load(){ //takes all the pokemon from the text file and adds them to allPoke as Pokemon objects
		allPoke = new ArrayList<Pokemon>(); 
		try{ 
			Scanner inFile = new Scanner(new BufferedReader(new FileReader("pokemon.txt")));
			inFile.nextLine();
			while(inFile.hasNextLine()){
				allPoke.add(new Pokemon(inFile.nextLine())); //creates the pokemon object from each seperate line in the .txt file
			}
		}
		catch(IOException ex){
			System.out.println("Dude, where is pokemon.txt?");
		}
	}
	
	public static void titleScreen(){ //used to display all the title info outside the main
		int count = 1; //keeps track of what number the user needs to enter to pick the pokemon
		System.out.println("Welcome To Pokemon Arena!");
		System.out.println("----------------------------------\n\n");
		System.out.print("START\n");
		String wait = in.nextLine();
		for(Pokemon i : allPoke){ //prints out all the pokemon for the user to choose from 
			String name = i.toString();
			System.out.printf("%d. %s \n", count, name);
			count++;
		}
		System.out.print("\n");
	}
	
	public static void teamPick(){ //lets the user pick their team
		int pokeNum = 0; //makes sure the user only picks 4 pokemon
		teamPoke = new ArrayList<Pokemon>();
		while(pokeNum != 4){ //allows you to pick until you have four pokemon
			System.out.print("Choose a pokemon:  ");
			int pick = in.nextInt() - 1; //-1 makes sure index of list is in range
			if(teamPoke.contains(allPoke.get(pick)) || pick > 28){ //makes sure the user hasent already chosen that pokemon and that their choice is an actual poke
				System.out.println("Invalid choice!"); 
			}
			else{ 
				teamPoke.add(allPoke.get(pick));
				pokeNum++;
			}
		}
		System.out.print("\n");
		for(int i = 0;i < 4;i++){
			if(allPoke.contains(teamPoke.get(i))){
				allPoke.remove(teamPoke.get(i)); //takes the pokemon you chose out of the allPoke list so you dont fight pokemon you chose
			}
		}
	}
	
	public static void turnChoose(){ //decides whether you or the enemy goes first
		double decide = Math.random();
		if(decide >= 0.5){
			givenTurn = true;
		}
		else if(decide < 0.5){
			givenTurn = false;
		}
	}
	
	public static void pokeChoose(){ //lets you pick what pokemon you want to use for this specific battle
		int count = 1; //keeps track of menu input number just like in titleScreen()
		for(Pokemon p : teamPoke){
			String name = p.toString();
			System.out.printf("%d. %s \n", count, name);
			count++;
		}
		System.out.print("\nChoose which pokemon to use:  "); 
		int chosenPoke = in.nextInt() - 1; //-1 keeps index in range
		Pokemon currentPoke = teamPoke.get(chosenPoke); 
		enemyChoose(currentPoke);
	}
	
	public static void enemyChoose(Pokemon currentPoke){ //decides what your opponent will be for each fight
		int fightChoice = (int)(Math.floor(Math.random() * allPoke.size())); //picks a random number that then gets a random pokemon from allPoke
		Pokemon enemyPoke = allPoke.get(fightChoice);
		allPoke.remove(fightChoice); //once you fight a pokemon you cant fight it again
		PokemonBattle fight = new PokemonBattle(currentPoke, enemyPoke, givenTurn, teamPoke); //creates a fight object
		// *every time you win a battle, this object is called and a new battle object is created
		System.out.println("Battle Start!");
		System.out.println("Vs. " + enemyPoke.toString());
		System.out.println(currentPoke.toString() + ", I choose you!");
		if(givenTurn){ //if else statement that calls the proper method according to whose turn it is first
			System.out.println("You go first");
			fight.turnStart();
		}
		else{
			System.out.println("The enemy goes first");
			fight.enemyTurn();
		}
	}
	
	public static boolean checkWin(){ //checks to make sure there are still pokemon to fight, if not you win
		if(allPoke.size() <= 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public static void main(String[] args){
		load();
		titleScreen();
		teamPick();
		while(battling){ //keeps repeating the process of choosing a pokemon and creating a fight object until allPoke is empty
			turnChoose();
			pokeChoose();
			boolean win = checkWin();
			if(win){
				System.out.println("Congratulations! You are now Trainer Supreme!");
				battling = false; //ends the loop so you cant fight dead pokemon
			}
		}
	}
}