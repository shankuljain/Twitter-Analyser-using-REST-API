
/*
 * File : Analyser.java
 * Created by Shankul Jain on April 20, 2015
 * 
 *_________________________________________________
 *
 *This program uses twitter4j public twitter API .
 */

import twitter4j.*;
import twitter4j.conf.*;
import java.util.*;

class Analyser{
	
	/* final variables */
	
	/* twitter developer keys */
	private static final String CONSUMER_KEY = "consumer key";
    private static final String CONSUMER_SECRET = "consumer secret";
	private static final String ACCESS_KEY= "access key";
	private static final String ACCESS_SECRET = "access secret";
    
	/* user name */
	private static final String USER = "choosetothinq";
    
	/* execution of code starts here */
	public static void main(String args[]) throws Exception{
		
		/* Creating a twitter object that will communicate with twitter server */
		Twitter twitter = getAccessingObject();
		
		/*getting all statuses on user timeline and storing it in a ArrayList*/
		statusList = getStatuses(twitter);
		
		/* if everyting works all right, analysing parts begin here */
		printMostFrequentHashTags();
		printMostRetweetedTweets();
		printMostUsedWords();
	}
	
	/* Method : getStatuses 
	 * This method extracts all the statuses from the twitter server.
	 * If there is some problem with internet connection or rate limit has been exhausted
	 * then it gives a message about the same and program quits the next moment.
	 * 
	 * Wait for some time if rate limit has been exhausted.
	 */
	private static List<Status> getStatuses(Twitter twitter){
		List<Status> mainList = new ArrayList<Status>(900);
		Paging page = new Paging();
		while(true){
			try{
				List<Status> list = twitter.getUserTimeline(USER,page);
				
				if(list.size() == 0) break;
				
				mainList.addAll(list);
				long max_id = list.get(list.size()-1).getId();
				
				page.setMaxId(max_id-1);
			}catch(TwitterException e){
				System.out.println("Exception caught :" +
						" some thing wrong, check internet connction or try after some time");
				System.out.println("error message : "+ e.toString());
				System.out.println("Program will quit now");
				System.exit(0);
			}
		}
		
		return mainList;
	}
	
	/* Method : printMostFrequentHashTags
	 * This method prints top 3 most frequent hashtags appeared on user's timeline. 
	 */
	private static void printMostFrequentHashTags(){
		
		Map<String, Integer> hashtags = getHashTags();
		String max_count_hashtag[] = new String[3];	//stores top 3 most frequent hashtags
		int max_counts[] = new int[3];	//stores their count
		
		for(String hashtag : hashtags.keySet()){
			int count = hashtags.get(hashtag);
			int index = getMinIndex(max_counts);
			if(count > max_counts[index]){
				max_counts[index] = count;
				max_count_hashtag[index] = hashtag;
			}
		}
		
		/* printing top 3 most freqent hashtags */
		System.out.println("top 3 most frequently used hashtags : ");
		for(int i=0; i<3; i++){
			System.out.println("Hashtag : #" + max_count_hashtag[i] + " is used "+ max_counts[i] + " times.");
		}
		System.out.println();
		
	}
	
	/* Method : printMostRetweetedTweets()
	 * This method prints Most Retweeted tweets that are originally updated by User
	 * and contains link.
	 */
	private static void printMostRetweetedTweets() throws Exception{
		
		String max_retweeted_tweet[] = new String[3];	//string containing tweets
		int max_counts[] = new int[3];	//string containing their retweet count
		
		for(Status status : statusList){
			if(!status.isRetweet() && status.getURLEntities().length != 0){
				int count = status.getRetweetCount();
				int index = getMinIndex(max_counts);
				if(count>max_counts[index]){
					max_counts[index] = count;
					max_retweeted_tweet[index] = status.getText();
				}
			}
		}
		
		/* printing top 3 most retweeted tweets with links in them */
		System.out.println("the top 3 most retweeted tweets with links in them : ");
		for(int i=0; i<3; i++){
			System.out.println("Tweet : " + max_retweeted_tweet[i] + " retweeted "+ max_counts[i] + " times"  );
		}
		System.out.println();
		
	}
	
	/* Method : printMostUsedWords 
	 * This methods prints top 3 most used words in timeline.
	 */
	private static void printMostUsedWords(){
		Map<String, Integer> words = getWords();
		
		String max_count_string[] = new String[3];
		int max_counts[] = new int[3];
		
		for(String str : words.keySet()){
			int count = words.get(str);
			int index = getMinIndex(max_counts);
			
			if(count > max_counts[index]){
				max_counts[index] = count;
				max_count_string[index] = str;
			}
		}
		
		/* printing top 3 most frequently used words */
		System.out.println("top 3 most frequently used words : ");
		for(int i=0; i<3; i++){
			System.out.println("Word :  \"" + max_count_string[i] + "\" is used  "+ max_counts[i] +" times" );
		}
		System.out.println();
	}
	
	/* Method : getMinIndex 
	 * this method returns the index of minimum count having in  max count array
	 */
	private static int getMinIndex(int[] maxcounts){
		if(maxcounts[0]>maxcounts[1]){
			if(maxcounts[1]>maxcounts[2]){
				return 2;
			}else{
				return 1;
			}
		}else{
			if(maxcounts[0]>maxcounts[2]){
				return 2;
			}
		}
		return 0;
	}
	
	/* Method : getWords 
	 * this methods extracts the words from all tweets and put them in hashtags with their count.
	 */
	private static Map<String, Integer> getWords(){
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		for(Status status : statusList){
			StringTokenizer tokens = new StringTokenizer(status.getText());
			while(tokens.hasMoreTokens()){
				String token = tokens.nextToken();
				if(!token.equals("RT")){
					if(map.containsKey(token)){
						int count = map.get(token);
						map.put(token, ++count);
					}else{
						map.put(token, 1);
					}
				}		
			}
		}
		
		return map;
	}
	
	/* Method : getHashTags 
	 * This method extracts all the hashtags appeard on user timeline.
	 */
	
	private static Map<String, Integer> getHashTags(){
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		for(Status status : statusList){
			HashtagEntity[] entitles =  status.getHashtagEntities();
			for(HashtagEntity entity : entitles){
				String str = entity.getText();
				
				if(map.containsKey(str)){
					int count = map.get(str);
					map.put(str, ++count);
				}else{
					map.put(str, 1);
				}
				
			}
		}
		return map;
	}
	
	/* Method : getAccessingObject
	 * This method creates an object of class Twitter and configures it with twitter
	 * developer keys.
	 */
	private static Twitter getAccessingObject() throws TwitterException{
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
	    cb.setDebugEnabled(true)
	                .setOAuthConsumerKey(CONSUMER_KEY)
	                .setOAuthConsumerSecret(CONSUMER_SECRET)
	                .setOAuthAccessToken(ACCESS_KEY)
	               	.setOAuthAccessTokenSecret(ACCESS_SECRET);

	    TwitterFactory tf = new TwitterFactory(cb.build());
	    Twitter twitter = tf.getInstance();
		
		return twitter;
	}
	
	private static List<Status> statusList;
}