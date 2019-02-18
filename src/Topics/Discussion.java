package Topics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.*;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;

import ConvoBot.Characteristic;
import ConvoBot.ContextGraph;
import ConvoBot.PrintMessage;
import ConvoBot.Synonyms;

public class Discussion extends Topic {

	String[] sampleMessages = { "That�s too bad! Do you like being busy?",
			"Can you do anything to have more free time? Good time management skills can often free up leisure time!",
			"There are many tools to help with procrastination! Have you tried keeping a journal or setting personal deadlines?",
			"That's too bad, did it used to help?",
			"It can be hard to start something, but it only takes an average of 66 days to form a new habit!",
			"Good idea! Procrastination can also be a symptom of anxiety or depression. Do you feel anxious or depressed?",
			"You�re not alone in that feeling *. One in five university students suffer from anxiety or depression. Thankfully there is many resources for students to reach out to on most campuses. Have you looked into your schools student resources?",
			"It never hurts to talk to someone, but if you�re sure there are things you can do for yourself that can help with anxiety. Is your sleep schedule regular?",
			"I see. How about exercise? Do you workout or play any sports." };

	public ArrayList<String> messages;
	public int conversationRounds;
	public static String[] myMessages = { "Let's discuss further why your 0", "Earlier you said your 0",
			"Does that have anything to do with the fact that your 0" };
	public static String[] noMessages = { "Please go on.", "That's very interesting", "I see.",
			"How does that make you feel?", "Could you please elaborate?" };
	ContextGraph contextGraph;

	public Discussion(ContextGraph contextGraph) {
		this.contextGraph = contextGraph;
		conversationRounds = 0;
		messages = new ArrayList<String>();
	}

	public void startTopic() {
		// TODO Auto-generated method stub
		PrintMessage.messageFromBot("******Starting Discussion*****");
		String input;
		for (int i = 0; i < 20; i++) {
			input = PrintMessage.messageFromUser();
			PrintMessage.messageFromBot(discussionRules(input));
		}
		currentTopic++;
	}

	/**
	 * @param input The message from the user
	 * @return The bot response
	 */
	public String discussionRules(String input) {
		extractKeywords(input);
		input = input.toLowerCase();
		String output = null;
		
		ArrayList<String> happyList = new ArrayList<String>();
		happyList.add("happy");
		happyList.add("good");
		happyList.add("well");
		happyList.add("super");
		happyList.add("awesome");
		happyList.add("great");
		happyList.add("okay");

		ArrayList<String> sadList = new ArrayList<String>();
		sadList.add("sad");
		sadList.add("miserable");
		sadList.add("upset");
		sadList.add("down");
		sadList.add("dissapointed");
		sadList.add("depressed");
		
		ArrayList<String> positiveList = new ArrayList<String>();
		positiveList.add("love");
		positiveList.add("adore");
		positiveList.add("like");
		positiveList.add("enjoy");
		
		ArrayList<String> negativeList = new ArrayList<String>();
		negativeList.add("hate");
		negativeList.add("loath");
		negativeList.add("despise");
		negativeList.add("dislike");
		
		
		
		// Case 1: User responds with I
		Pattern p1 = Pattern.compile("(.*)(i[^'m])(.*)");
		Matcher m1 = p1.matcher(input);
		
		// Case 2: User responds with I'm
		Pattern p2 = Pattern.compile("(.*)(i'm)(.*)");
		Matcher m2 = p2.matcher(input);
		
		// Case 3: user responds with my
		Pattern p3 = Pattern.compile("(.*)(my)(.*)");
		Matcher m3 = p3.matcher(input);
		
		// Case 4: User responds with everybody
		Pattern p4 = Pattern.compile("(.*)(everybody)(.*)");
		Matcher m4 = p4.matcher(input);
		
		// Case 5: Single word response
		int inputLength = input.split(" ").length;
		
		if(m1.find()) {
			System.out.println("Case 1");
			//Case 1.1: I am
			Pattern p1_1 = Pattern.compile("(.*)(am)(.*)");
			Matcher m1_1 = p1_1.matcher(input);
			if(m1_1.find()) {
				System.out.println("Case 1.1");
				// Case 1.1.1: I am feeling BLANK
				Pattern p1_1_1 = Pattern.compile("(.*)(feeling)(.*)");
				Matcher m1_1_1 = p1_1_1.matcher(input);
				
				if(m1_1_1.find()) {
					System.out.println("case 1.1.1");
					String keyword = m1_1_1.group(3);
					keyword = keyword.replaceAll("\\s+","");
					
					if(happyList.contains(keyword.toLowerCase())) {
						output = "I'm glad to hear that you're feeling good!";
						//Change graph weight
					}else if(sadList.contains(keyword.toLowerCase())) {
						//Change graph weights
						output = "I'm sorry to hear that.";
					}
					
					// Case 1.1.2 I am BLANK. EX I am worried/sad
				}else {
					System.out.println("case 1.1.2");
					String keyword = m1_1.group(3);
					keyword = keyword.replaceAll("\\s+","");
					System.out.println("Keyword:" + keyword);
					if(happyList.contains(keyword.toLowerCase())) {
						output = "I'm glad to hear that you feel good! Is there anything you would like to talk about?";
						//Change graph weight
					}else if(sadList.contains(keyword.toLowerCase())) {
						//Change graph weights
						output = "I'm sorry to hear that. Why do you feel so " + keyword + "?";
					}else {
						output = noMessages[(int) (Math.random()*noMessages.length)];
					}
				}
			}else { //Case 1.2: I BLANK. EX I hate BLANK, I love BLANK, I procrastinate, ECT
				System.out.println("Case 1.2");
				String[] words = m1.group(3).split(" "); // Splits the String into individual words
				String keyword = words[0];
				String sentence = null;
				if(words.length > 1) {
					words = ArrayUtils.removeElement(words, 0); // Removes the keyword from the sentence
					sentence = Arrays.toString(words); // Recombines into a partial sentence
				}else {
					sentence = null;
				}
				
				if(positiveList.contains(keyword.toLowerCase())) {
					output = "Its good to hear that you like " + sentence + ". " + "How does " + sentence + " make you feel?";
				}else if(negativeList.contains(keyword.toLowerCase())) {
					output = "I'm sorry to hear that " + sentence + " makes you feel like that. Why do you think it makes you feel that way?";
				}else if(keyword.equals("proctastinate")) {
					output = sampleMessages[2];
				}else {
					output = noMessages[(int) (Math.random()*noMessages.length)];
				}
				
			}
			
		}else if(m2.matches()) { // Case 2: User responds with I'm
			System.out.println("Case 2");
			String keyword = null;
			// Case 2.1 I'm feeling BLANK
			Pattern p2_1 = Pattern.compile("(.*)(feeling)(.*)");
			Matcher m2_1 = p2_1.matcher(input);
			// Case 2.2 I'm worried about BLANK
			Pattern p2_2 = Pattern.compile("(.*)(worried about)(.*)");
			Matcher m2_2 = p2_2.matcher(input);
			// Case 2.3 I'm BLANK
			
			if(m2_1.matches()) {
				System.out.println("Case 2.1");
				keyword = m2_1.group(3);
				if(happyList.contains(keyword.toLowerCase())) {
					output = "I'm glad to hear that you're feeling good!";
					//Change graph weight
				}else if(sadList.contains(keyword.toLowerCase())) {
					//Change graph weights
					output = "I'm sorry to hear that.";
				}else{
					output = noMessages[(int) (Math.random()*noMessages.length)];
				}	
			}else if(m2_2.matches()) {
				System.out.println("Case 2.2");
				keyword = m2_2.group(3); // worried implies this will be negative so I dont need to check the synonyms
				output = "Why are you worried about " + keyword + "?";
				//Change graph weights
			}else {
				System.out.println("Case 2.3");
				keyword = m2.group(3);
				if(happyList.contains(keyword.toLowerCase())) {
					output = "I'm glad to hear that you're" + keyword + "! Keep it up by getting into healthy habits. ";
					//Change graph weight
				}else if(sadList.contains(keyword.toLowerCase())) {
					//Change graph weights
					output = "I'm sorry to hear that. " + sampleMessages[7] ;
				}else{
					output = noMessages[(int) (Math.random()*noMessages.length)];
				}
				
			}
		}else {
			output = noMessages[(int) (Math.random()*noMessages.length)];
		}
		
		
		return output;
	
	}

	public void extractKeywords(String input) {
		for (Characteristic c : contextGraph.getCharacteristicsList()) {
			if (input.contains(c.getName())) {
				// What method do we use when we find a keyword or synonym in the patient
				// message?
			}
			for (String s : c.getSynonymns()) {
				if (input.contains(s)) {
					// Same as before
				}
			}
		}
	}

}
