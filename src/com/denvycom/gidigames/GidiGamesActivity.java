package com.denvycom.gidigames;

import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.transitions.CCFadeDownTransition;
import org.cocos2d.transitions.CCSlideInLTransition;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class GidiGamesActivity extends Activity {
	/** Called when the activity is first created. */

	protected CCGLSurfaceView _glSurfaceView;
	private SharedPreferences mPrefs;
	public static float scalefactor ;
	public static GidiGamesActivity  app ;
	public static String ingame = "" ; 
	public static String nextscene = "" ; 
	public static String currentscene = "" ;
	public static String currentlanguage = "" ;
	public static String currentpuzzletype = "" ;
	public static String currentpic= "" ;
	public static String currentpictitle= "" ;
	private static DbAdapter mDbHelper;
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
		app =  this ;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		_glSurfaceView = new CCGLSurfaceView(this);
		CCDirector director = CCDirector.sharedDirector();
		director.attachInView(_glSurfaceView);
		director.setDeviceOrientation(CCDirector.kCCDeviceOrientationLandscapeLeft);
		setContentView( _glSurfaceView);

		scalefactor = CCDirector.sharedDirector().winSize().height / 600 ;
		//Log.v("Gidi Next Scene Value", CCDirector.sharedDirector().winSize().height + "" );
		
		CCDirector.sharedDirector().setAnimationInterval(1.0f / 60.0f);
		
		
	}

	@Override
	public void onStart()
	{
		super.onStart();
		//CCDirector.sharedDirector().setDisplayFPS(true);
		//Log.v("Gidi Next Scene Value",  GidiGamesActivity.nextscene );
		
	}


	@Override
	public void onPause()
	{
		super.onPause();

		CCDirector.sharedDirector().pause();
		SoundEngine.sharedEngine().pauseSound();
		
		SharedPreferences screenDetails = app.getSharedPreferences("screendetails", MODE_PRIVATE);
		Editor edit = screenDetails.edit();
		edit.clear();
		edit.putString("curpic", GidiGamesActivity.currentpic);
		edit.putString("puzzletype", GidiGamesActivity.currentpuzzletype);		
		edit.putString("nextscreen",GidiGamesActivity.currentscene); 
		edit.commit(); 
	}

	@Override
	public void onResume()
	{
		super.onResume();

		CCDirector.sharedDirector().resume();
		SoundEngine.sharedEngine().resumeSound();
		Log.v("Gidi Next Scene Value",  GidiGamesActivity.nextscene );
		

		SharedPreferences screenDetails = app.getSharedPreferences("screendetails", MODE_PRIVATE);
		String nextscreen = screenDetails.getString("nextscreen", "default");
		
		currentpic = screenDetails.getString("curpic", "default");
		currentpuzzletype = screenDetails.getString("puzzletype", "default");
		
		if (nextscreen.equals("MenuLayer")){
			CCScene scene =  MenuLayer.scene(); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else if (nextscreen.equals("PuzzleMenuLayer")){
			CCScene scene =  PuzzleMenuLayer.scene(); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  if (nextscreen.equals("PuzzleNumLayer")){
			CCScene scene =  PuzzleNumLayer.scene(currentpuzzletype); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  if (nextscreen.equals("PuzzleHelpLayer")){
			CCScene scene =  PuzzleHelpLayer.scene(); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  if (nextscreen.equals("PuzzlePicLayer")){
			CCScene scene =  PuzzlePicLayer.scene(currentpuzzletype); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  if (nextscreen.equals("LexisMenuLayer")){
			CCScene scene =  LexisMenuLayer.scene(); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  if (nextscreen.equals("LexisHelpLayer")){
			CCScene scene =  LexisHelpLayer.scene(); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  if (nextscreen.equals("TicTacMenuLayer")){
			CCScene scene =  TicTacMenuLayer.scene(); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  if (nextscreen.equals("TicTacHelpLayer")){
			CCScene scene = TicTacHelpLayer.scene(); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  if (nextscreen.equals("default")){
			CCScene scene = SplashLayer.scene(); // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene);
		}else  {
			CCScene scene =  MenuLayer.scene();  // MenuLayer.scene();
			CCDirector.sharedDirector().runWithScene(scene); 
		}


	}

	@Override
	public void onStop()
	{
		super.onStop();

		CCDirector.sharedDirector().end();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//Ask the user if they want to quit
			SoundEngine.sharedEngine().realesAllSounds();
			clicksound() ;
			if (ingame.equals("")  && GidiGamesActivity.nextscene.equals("MenuLayer") ){

				CCScene next = CCFadeDownTransition.transition(0.2f, MenuLayer.scene());
				CCDirector.sharedDirector().replaceScene(next); 
			} else  if (ingame.equals("")  && GidiGamesActivity.nextscene.equals("PuzzleMenuLayer") ){ 
				CCScene next = CCFadeDownTransition.transition(0.2f, PuzzleMenuLayer.scene());
				CCDirector.sharedDirector().replaceScene(next); 
			} else  if (ingame.equals("")  && GidiGamesActivity.nextscene.equals("PuzzlePicLayer") ){ 
				CCScene next = CCFadeDownTransition.transition(0.2f, PuzzlePicLayer.scene(GidiGamesActivity.currentpuzzletype));
				CCDirector.sharedDirector().replaceScene(next); 
			} else  if (ingame.equals("")  && GidiGamesActivity.nextscene.equals("PuzzleNumLayer") ){ 
				CCScene next = CCFadeDownTransition.transition(0.2f, PuzzleNumLayer.scene(GidiGamesActivity.currentpuzzletype));
				CCDirector.sharedDirector().replaceScene(next); 
			} else  if (ingame.equals("")  && GidiGamesActivity.nextscene.equals("LexisMenuLayer") ){ 
				CCScene next = CCFadeDownTransition.transition(0.2f, LexisMenuLayer.scene());
				CCDirector.sharedDirector().replaceScene(next); 	
			}else  if (ingame.equals("")  && GidiGamesActivity.nextscene.equals("TicTacMenuLayer") ){ 
				CCScene next = CCFadeDownTransition.transition(0.2f, TicTacMenuLayer.scene());
				CCDirector.sharedDirector().replaceScene(next);    
			}else if (GidiGamesActivity.nextscene.equals("")){
				new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Exit")
				.setMessage("Are you sure you want to quit Gidigames" )
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which){
						// Exit the activity
						currentscene = "default" ;
						GidiGamesActivity.this.finish();
					}
				})
				.show();
			}
			/**/

			// Say that we've consumed the event
			return false;
		}

		return true;
	} 

	 

	public static void clicksound() {
		// TODO Auto-generated method stub
		SoundEngine.sharedEngine().playEffect( CCDirector.sharedDirector().getActivity(), R.raw.tileclick);
		//Vibrator vibe = (Vibrator) app.getSystemService(a.VIBRATOR_SERVICE) ;

	}
	
	public static void checkWordBank(){
		//Generate Database
				mDbHelper = new DbAdapter(GidiGamesActivity.app);
				mDbHelper.open();
				Cursor langCursor = mDbHelper.fetchAllWords();
				if(langCursor.getCount() <= 0 ){
					mDbHelper.createLangGameWord("Eze", "ibo", "King", "1");
					mDbHelper.createLangGameWord("Kedu ", "ibo", "Howdy", "1");
					mDbHelper.createLangGameWord("Bia ", "ibo", "Come", "1");
					mDbHelper.createLangGameWord("Ego", "ibo", "Money", "1");
					mDbHelper.createLangGameWord("Ala Beke", "ibo", "Abroad", "1");
					mDbHelper.createLangGameWord("Miri", "ibo", "Water", "1");
					mDbHelper.createLangGameWord("Chukwu", "ibo", "God", "1");
					mDbHelper.createLangGameWord("Biko nu", "ibo", "Please", "1");
					mDbHelper.createLangGameWord("Nkita ", "ibo", "Dog", "1");
					mDbHelper.createLangGameWord("Ulo ", "ibo", "House", "1");
					mDbHelper.createLangGameWord("Gawa ", "ibo", "Go", "1");
					mDbHelper.createLangGameWord("Gini ", "ibo", " What", "1");
					mDbHelper.createLangGameWord("Akpa ", "ibo", "Bag", "1");
					mDbHelper.createLangGameWord("Ofe", "ibo", " Soup", "1");
					mDbHelper.createLangGameWord("Ahia ", "ibo", " Market", "1");
					mDbHelper.createLangGameWord("Eze ", "ibo", " King", "1");
					mDbHelper.createLangGameWord("Nne ", "ibo", " Mother", "1");
					mDbHelper.createLangGameWord("Nna ", "ibo", " Father", "1");
					mDbHelper.createLangGameWord("Isi ", "ibo", " Head", "1");
					mDbHelper.createLangGameWord("Ogbo ", "ibo", " Car", "1");
					mDbHelper.createLangGameWord("Oku ", "ibo", " Fire", "1");
					mDbHelper.createLangGameWord("Uzo ", "ibo", " Road", "1");
					mDbHelper.createLangGameWord("Nko ", "ibo", " Firewood", "1");
					mDbHelper.createLangGameWord("Nwanyi ", "ibo", " Girl ", "1");
					mDbHelper.createLangGameWord("Oge", "ibo", " Time", "1");
					mDbHelper.createLangGameWord("Achicha ", "ibo", " Apple ", "1");
					mDbHelper.createLangGameWord("Afo ", "ibo", " Stomach", "1");
					mDbHelper.createLangGameWord("Ogede ", "ibo", "Banana ", "1");
					mDbHelper.createLangGameWord("Akpu", "ibo", " Cassava", "1");
					mDbHelper.createLangGameWord("Oka ", "ibo", " Maize", "1");
					mDbHelper.createLangGameWord("Oji ", "ibo", " Kolanut", "1");
					mDbHelper.createLangGameWord("Mangoro ", "ibo", " Mango", "1"); 
					mDbHelper.createLangGameWord("Egusi", "ibo", " Melon seed", "1");
					mDbHelper.createLangGameWord("Ero ", "ibo", "Mushroom", "1");
					mDbHelper.createLangGameWord("Oroma", "ibo", " Orange", "1"); 
					mDbHelper.createLangGameWord("Popo ", "ibo", " Pawpaw", "1");
					mDbHelper.createLangGameWord("Usu ", "ibo", " Bat", "1"); 
					mDbHelper.createLangGameWord("Nama ", "ibo", " Cow", "1");
					mDbHelper.createLangGameWord("Akwa ", "ibo", " Egg", "1");
					mDbHelper.createLangGameWord("Enyimba ", "ibo", " Elephant", "1");
					mDbHelper.createLangGameWord("Azu ", "ibo", " Fish", "1");
					mDbHelper.createLangGameWord("Awo", "ibo", " Frog", "1");
					mDbHelper.createLangGameWord("Ewu", "ibo", "Goat", "1");
					mDbHelper.createLangGameWord("Oke", "ibo", " Rat", "1");
					mDbHelper.createLangGameWord("Agu ", "ibo", "Lion ", "1");
					mDbHelper.createLangGameWord("Aturu ", "ibo", " Lamb", "1");
					mDbHelper.createLangGameWord("Popo ", "ibo", " Pawpaw", "1");





					mDbHelper.createLangGameWord("ṣ e-tán", "yoruba", "finish", "1");
					mDbHelper.createLangGameWord("dásílê", "yoruba", "acquit", "1");
					mDbHelper.createLangGameWord("lérí", "yoruba", "above", "1");
					mDbHelper.createLangGameWord("gbé sálo", "yoruba", "abduct", "1");
					mDbHelper.createLangGameWord("olóko", "yoruba", "farmer", "1");
					mDbHelper.createLangGameWord("obìnrin", "yoruba", "female", "1");
					mDbHelper.createLangGameWord("oju", "yoruba", "face", "1");
					mDbHelper.createLangGameWord("Ole ", "yoruba", "Thief", "1");
					mDbHelper.createLangGameWord("Oko ", "yoruba", "Farm", "1");
					mDbHelper.createLangGameWord("Ojo ", "yoruba", "Coward", "1");
					mDbHelper.createLangGameWord("Igba", "yoruba", "Season", "1");
					mDbHelper.createLangGameWord("Orun ", "yoruba", "Sleep", "1");
					mDbHelper.createLangGameWord("Owo", "yoruba", "Money", "1");
					mDbHelper.createLangGameWord("Apa ", "yoruba", "Broom", "1");
					mDbHelper.createLangGameWord("Ewa", "yoruba", "Beans", "1");
					mDbHelper.createLangGameWord("Ayo", "yoruba", "Game", "1");
					mDbHelper.createLangGameWord("Obo", "yoruba", "Monkey", "1");
					mDbHelper.createLangGameWord("Obe", "yoruba", "Soup", "1");
					mDbHelper.createLangGameWord("Ola ", "yoruba", "Wealth", "1");
					mDbHelper.createLangGameWord("Rara", "yoruba", "No", "1");
					mDbHelper.createLangGameWord("Beeni", "yoruba", "Yes", "1");
					mDbHelper.createLangGameWord("Ejo ", "yoruba", "Please", "1");
					mDbHelper.createLangGameWord("Ta-ni ", "yoruba", "Who", "1");
					mDbHelper.createLangGameWord("Ki-ni ", "yoruba", "What", "1");
					mDbHelper.createLangGameWord("Mo-fe ", "yoruba", "I want", "1");
					mDbHelper.createLangGameWord("Nibo ", "yoruba", "Where", "1");
					mDbHelper.createLangGameWord("Okirin", "yoruba", "Man " , "1");
					mDbHelper.createLangGameWord("Obirin ", "yoruba", "Woman", "1");
					mDbHelper.createLangGameWord("Iwe ", "yoruba", "Book", "1");
					mDbHelper.createLangGameWord("Omi ", "yoruba", "Water", "1");
					mDbHelper.createLangGameWord("Motor ", "yoruba", "Car", "1");
					mDbHelper.createLangGameWord("Akoko ", "yoruba", "Time", "1");
					mDbHelper.createLangGameWord("Laipe ", "yoruba", "Later", "1");
					mDbHelper.createLangGameWord("Meji ", "yoruba", "Two", "1");
					mDbHelper.createLangGameWord("Merin", "yoruba", "Four", "1");
					mDbHelper.createLangGameWord("Meta ", "yoruba", "Three", "1");
					mDbHelper.createLangGameWord("Kan ", "yoruba", "One", "1");
					mDbHelper.createLangGameWord("Okere", "yoruba", "Small", "1");
					mDbHelper.createLangGameWord("Dahun", "yoruba", " Answer", "1");
					mDbHelper.createLangGameWord("Bukun", "yoruba", "Bless", "1");
					mDbHelper.createLangGameWord("Jeun ", "yoruba", "Eat", "1");


					mDbHelper.createLangGameWord("Subu", "yoruba", "Fall", "1");
					mDbHelper.createLangGameWord("Tele ", "yoruba", "Follow", "1");


					mDbHelper.createLangGameWord("sadaukarwa ", "hausa", "Dedication ", "1");
					mDbHelper.createLangGameWord("Ilimi ", "hausa","Education","1");
					mDbHelper.createLangGameWord("Rahama ", "hausa", "Mercy", "1");
					mDbHelper.createLangGameWord("Tushe ", "hausa", "Origin", "1");
					mDbHelper.createLangGameWord("Daya ", "hausa", "One", "1");
					mDbHelper.createLangGameWord("Hudu ", "hausa", "Four", "1");
					mDbHelper.createLangGameWord("Byair ", "hausa", "Five", "1");
					mDbHelper.createLangGameWord("Goma ", "hausa", "Ten", "1"); 
					mDbHelper.createLangGameWord("Shidda ", "hausa", "Six", "1");
					mDbHelper.createLangGameWord("Sifili ", "hausa", "Zero", "1"); 
					mDbHelper.createLangGameWord("Talatin ", "hausa", "Thirty", "1"); 
					mDbHelper.createLangGameWord("Talata ", "hausa", "Tuesday", "1");
					mDbHelper.createLangGameWord("Iyaye ", "hausa", "Parents", "1"); 
					mDbHelper.createLangGameWord("litini ", "hausa", "Monday", "1"); 
					mDbHelper.createLangGameWord("Lahadi ", "hausa", "Sunday ", "1"); 
					mDbHelper.createLangGameWord("Yuli ", "hausa", " July", "1"); 
					mDbHelper.createLangGameWord("Yun ", "hausa", " June", "1"); 
					mDbHelper.createLangGameWord("Maris", "hausa", "March", "1"); 
					mDbHelper.createLangGameWord("Augusta", "hausa", " August ", "1");
					mDbHelper.createLangGameWord("Oktoba ", "hausa", " October ", "1"); 
					mDbHelper.createLangGameWord("Barsh ", "hausa", "Sleep", "1"); 
					mDbHelper.createLangGameWord("Mutu ", "hausa", "Die ", "1"); 
					mDbHelper.createLangGameWord("Iwo", "hausa", "Swim", "1"); 
					mDbHelper.createLangGameWord("Zo", "hausa", " Come ", "1");  
					mDbHelper.createLangGameWord("Fadi ", "hausa", "Fail ", "1"); 
					mDbHelper.createLangGameWord("Kuka ", "hausa", "Cry ", "1");
					mDbHelper.createLangGameWord("Ji ", "hausa", "Hear ", "1"); 
					mDbHelper.createLangGameWord("Duba", "hausa", "Look", "1");
					mDbHelper.createLangGameWord("Anmsa ", "hausa", "Answer", "1"); 
					mDbHelper.createLangGameWord("Shi ", "hausa", "Eat", "1");
					mDbHelper.createLangGameWord("Gani ", "hausa", "See", "1"); 
					mDbHelper.createLangGameWord("Surutu ", "hausa", "Shout", "1"); 
					mDbHelper.createLangGameWord("Rawa ", "hausa", "Dance ", "1"); 
					mDbHelper.createLangGameWord("Sayawa ", "hausa", "Stand", "1"); 
					mDbHelper.createLangGameWord("Kiehi", "hausa", "Jealous ", "1"); 
					mDbHelper.createLangGameWord("Zama", "hausa", "Sit", "1"); 
					mDbHelper.createLangGameWord("menta ", "hausa", "Forget ", "1"); 
					mDbHelper.createLangGameWord("Jira", "hausa", "Wait", "1");







				} 


				mDbHelper.close();
				 

				/* emitter = CCParticleFireworks.node();
		          addChild(emitter, 10);

		         emitter.setTexture(CCTextureCache.sharedTextureCache().addImage("stars.png"));
		         emitter.setScale( CCDirector.sharedDirector().winSize().width/480) ;

		         emitter.setAngle(180);
		         emitter.setBlendAdditive(true);*/
				//setEmitterPosition();

	}
	
	 

}