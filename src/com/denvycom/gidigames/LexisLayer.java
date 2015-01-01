package com.denvycom.gidigames;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.cocos2d.actions.CCScheduler;
import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.instant.CCPlace;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCFadeIn;
import org.cocos2d.actions.interval.CCFadeOut;
import org.cocos2d.actions.interval.CCJumpTo;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCScaleBy;
import org.cocos2d.actions.interval.CCScaleTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.actions.tile.Tile;
import org.cocos2d.config.ccMacros;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.menus.CCMenuItemLabel;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCMotionStreak;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.particlesystem.CCParticleSystem;
import org.cocos2d.particlesystem.CCQuadParticleSystem;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;
import org.cocos2d.types.ccColor4F;
import org.cocos2d.utils.CCFormatter; 
import android.content.Context;
import android.database.Cursor;
import android.view.MotionEvent;





public class LexisLayer extends CCLayer {


	public static  ArrayList<CCNodeExt> spriteList = new ArrayList<CCNodeExt>();
	public static  ArrayList<Integer> spriteCounter = new ArrayList<Integer>();
	private static int numSpritePause = 0 ;  //Stores a counter for number of sprites launched
	private static int MAX_SPRITES_PAUSE = 10 ; //After this number of sprites, we pause
	private static int MAX_SPRITE_POOL = 4  ;
	private static int NUM_WORDS_POOL = 7  ;
	private static int score = 0 ;
	private static CGSize screenSize;
	private int startx,  corrhop = 0, maxcorrhop = 4 ;
	private static int splitfactor = 10 ;
	private static int jumpheight, c_jumpheight, c_endx , endx ,cursel,nextindex ;
	private static boolean correctlanded = true ;
	private static CCMoveTo movenode0,movenode1,movenode2,movenode3;

	private static float time = 90;
	private static Timer timer = new Timer();  

	DbAdapter mDbHelper ;
	private static ArrayList<String>  rootwords = new ArrayList<String>();
	private static ArrayList<String>   englishtrans = new ArrayList<String>();
	private static ArrayList<Integer> englishtransList = new ArrayList<Integer>();
	private static ArrayList<Integer> throwindexes = new ArrayList<Integer>();
	private static String currentword ;
	private static int currentindex = 0 ;
	private static int currentindexholder = 0 ;
	private static int currentthrowindex = 0 ;
	private static boolean throwposition = true ;

	private static int PAUSE_OVERLAY_TAG = 322 ;
	private static int SCROLL_TOP_TAG = 321 ;
	private static int LANG_LAYER_TAG = 349;
	private static int PAUSE_MENU_TAG = 348;
	private static int RESUME_MENU_TAG = 347;
	private static int SCORE_LABEL_TAG = 350 ;
	private static final int GAME_PUASES_LABEL_TAG = 107;
	private static int BEST_SCORE_LABEL_TAG = 450 ;
	private static int MAIN_WORD_NODE_LABEL_TAG = 550 ;
	private static int CANCEL_SPRITE_TAG = 650 ;
	private static int TIMER_LABEL_TAG = 850 ;
	private static int CORRECT_WORD_TAG = 950 ;
	private static final int JUMP_ACTION_TAG = 960;
	private static final int LANG_LABEL_TAG = 960;
	private static CCMotionStreak streak;
	private static int thetime = 0 ;
	private static int moves = 0 ;
	public static boolean gameover = false ;
	private CCBitmapFontAtlas bestScoreLabel;
	private float tilescalefactor = 0.0f;
	private CCQuadParticleSystem emitter;  

	public static CCScene scene(String thelanguage)
	{ 
		GidiGamesActivity.nextscene= "LexisMenuLayer";  
		 GidiGamesActivity.currentscene = "LexisLayer" ;
		CCScene scene = CCScene.node();
		CCLayer layer = new LexisLayer(thelanguage);
		scene.addChild(layer); 

		return scene;
	}

	protected LexisLayer(String selectedlanguage)
	{
		super();
		this.setIsTouchEnabled(true); 
		  GidiGamesActivity.checkWordBank();
		  
		mDbHelper = new DbAdapter(GidiGamesActivity.app);
		mDbHelper.open();
		screenSize = CCDirector.sharedDirector().winSize();
		tilescalefactor  = (screenSize.height/480) ;
		String labelmoves; 

		//Select the most recent best score database
		Cursor ScoreCursor = mDbHelper.fetchLexisBest("lexis", GidiGamesActivity.currentlanguage, "") ;
		if(ScoreCursor.getCount() > 0){
			ScoreCursor.moveToFirst();
			labelmoves = ScoreCursor.getString(ScoreCursor.getColumnIndex(
					DbAdapter.KEY_GAME_MOVES)) ;
		}else{
			labelmoves = "0" ;
		}		


		gameover = false ;
		this.setTag(LANG_LAYER_TAG);
		//Set Up Database

		//Fetch last session id, increment it an update value
		Cursor langCursor = mDbHelper.fetchWordSet(selectedlanguage);
		//Generate the array of words we will be working with

		langCursor.moveToFirst() ;
		rootwords.clear();
		englishtrans.clear();
		englishtransList.clear();
		throwindexes.clear();
		int i = 0 ;
		while (langCursor.moveToNext()){
			rootwords.add(langCursor.getString(1));
			englishtrans.add(langCursor.getString(3));
			englishtransList.add(i);
			throwindexes.add(i);
			i++;

		}


		mDbHelper.close() ;
		Collections.shuffle(englishtransList);
		Collections.shuffle(throwindexes);

		//Add Background Sprite Image
		CCSprite background = CCSprite.sprite("background.jpg");
		background.setScale(screenSize.width / background.getContentSize().width);
		background.setAnchorPoint(CGPoint.ccp(0f,1f)) ;
		background.setPosition(CGPoint.ccp(0, screenSize.height));
		addChild(background,-5);


		//Add Score Label
		String scorestring = CCFormatter.format("%03d", score);
		CCBitmapFontAtlas scoreLabel = CCBitmapFontAtlas.bitmapFontAtlas (scorestring, "bionic.fnt");
		scoreLabel.setScale(1.6f*tilescalefactor);
		//scoreLabel.setColor(ccColor3B.ccc3(255, 242, 75));
		scoreLabel.setAnchorPoint(CGPoint.ccp(0,1));
		scoreLabel.setPosition( CGPoint.ccp( 10*tilescalefactor, screenSize.height - 10*tilescalefactor));
		addChild(scoreLabel,-2, SCORE_LABEL_TAG);  


		// Add Timer Label
		CCBitmapFontAtlas LanguageLabel = CCBitmapFontAtlas.bitmapFontAtlas (selectedlanguage, "greek.fnt");
		LanguageLabel.setScale(1.4f*tilescalefactor);
		LanguageLabel.setAnchorPoint(CGPoint.ccp(0,1));
		LanguageLabel.setColor(ccColor3B.ccc3(50, 205, 50));
		LanguageLabel.setPosition(CGPoint.ccp(10*tilescalefactor , scoreLabel.getPosition().y - scoreLabel.getContentSize().height/2*1.6f*tilescalefactor   -  LanguageLabel.getContentSize().height/1.6f*tilescalefactor));
		addChild(LanguageLabel,-2,LANG_LABEL_TAG);
		
		
		//Add The Main Word
		CCBitmapFontAtlas mainWordLabel = CCBitmapFontAtlas.bitmapFontAtlas ("α�?ιθμό 2", "greek.fnt");
		mainWordLabel.setScale(1.6f*tilescalefactor);
		mainWordLabel.setAnchorPoint(CGPoint.ccp(1f,1f));
		mainWordLabel.setColor(ccColor3B.ccc3(50, 205, 50));

		currentindex = englishtransList.get((currentindexholder) % englishtransList.size());
		currentword = rootwords.get(currentindex)  ;
		mainWordLabel.setString(currentword);

		//Add A NOde to Contain the Main Word
		CCNode mainWordNode = CCNode.node();
		//mainWordNode.setContentSize(mainWordLabel.getContentSize());
		//mainWordNode.setScale(tilescalefactor);
		mainWordNode.setAnchorPoint(1f,1f);
		mainWordNode.setPosition(CGPoint.ccp(screenSize.width - 20*tilescalefactor , screenSize.height ));
		mainWordNode.addChild(mainWordLabel, -2,1);


		addChild(mainWordNode,1, MAIN_WORD_NODE_LABEL_TAG);

		// Add Timer Label
		CCBitmapFontAtlas timerLabel = CCBitmapFontAtlas.bitmapFontAtlas ("00:00", "bionic.fnt");
		timerLabel.setScale(1.0f*tilescalefactor);
		timerLabel.setColor(ccColor3B.ccc3(50, 205, 50));
		timerLabel.setPosition(CGPoint.ccp(screenSize.width - timerLabel.getContentSize().width*tilescalefactor/2 - 30*tilescalefactor, mainWordNode.getPosition().y - mainWordNode.getContentSize().height/2*tilescalefactor - 50*tilescalefactor -  timerLabel.getContentSize().height/1.5f*tilescalefactor));
		addChild(timerLabel,-2,TIMER_LABEL_TAG);

		//Add Best Label

		bestScoreLabel = CCBitmapFontAtlas.bitmapFontAtlas ("BEST: " + labelmoves + " points",  "bionic.fnt");
		bestScoreLabel.setAnchorPoint(1f,0f);
		bestScoreLabel.setScale(0.8f*tilescalefactor);		
		bestScoreLabel.setColor(ccColor3B.ccc3(255, 242, 75));
		bestScoreLabel.setPosition( CGPoint.ccp(screenSize.width - 20*tilescalefactor, timerLabel.getPosition().y - timerLabel.getContentSize().height*tilescalefactor - 10*tilescalefactor  ));
		addChild(bestScoreLabel,-2, BEST_SCORE_LABEL_TAG);



		//CCIntervalAction moveTo = CCMoveTo.action(0.5f, CGPoint.ccp( screenSize.width/2, screenSize.height/2));
		//mainWordLabel.runAction(moveTo);
		CCScaleBy scale = CCScaleBy.action(0.5f, 1.2f);
		CCSequence seq = CCSequence.actions(scale, scale.reverse());
		CCRepeatForever repeat = CCRepeatForever.action(seq);

		mainWordLabel.runAction(repeat);
		spriteCounter.clear() ;
		//Create 4 Menu Items and add to layer
		for (int j = 0 ; j < MAX_SPRITE_POOL; j++){
			//Create a fontatlas, put it in an item, put it in a menu

			//Put the item in a Node and animate the Node.
			CCBitmapFontAtlas eachFontAtlas = CCBitmapFontAtlas.bitmapFontAtlas( "Number : " + j , "bionic.fnt");
			eachFontAtlas.setScale(tilescalefactor);
			CCNodeExt eachNode =  new  CCNodeExt();
			eachNode.setNodeText("Numberrrooo : " + j );
			eachNode.addChild(eachFontAtlas,1,1);
			eachNode.setContentSize(eachFontAtlas.getContentSize());
			eachNode.setScale(tilescalefactor);
			eachNode.setPosition(0, -1 * eachNode.getContentSize().height*tilescalefactor);


			addChild(eachNode,j,j);

			spriteList.add(eachNode);
			spriteCounter.add(j);

		} 





		//Add Overlay with Start button and Instruction
		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 0));
		pauseOverlay.setOpacity(180);
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);

		CCSprite arrow = CCSprite.sprite("arrowtop.png");
		arrow.setScale(tilescalefactor);
		arrow.setPosition(CGPoint.make(screenSize.width - (mainWordLabel.getContentSize().width*tilescalefactor*1.2f /2), screenSize.height -  mainWordLabel.getContentSize().height*tilescalefactor -20*tilescalefactor)) ;
		pauseOverlay.addChild(arrow,3,1) ;
		CCMoveTo move = CCMoveTo.action(0.2f, CGPoint.make(screenSize.width - (mainWordLabel.getContentSize().width*tilescalefactor*1.2f /2), screenSize.height -  mainWordLabel.getContentSize().height*tilescalefactor - 40 *tilescalefactor) ) ;
		CCMoveTo unmove = CCMoveTo.action(0.2f, CGPoint.make(screenSize.width - (mainWordLabel.getContentSize().width*tilescalefactor*1.2f /2),screenSize.height -  mainWordLabel.getContentSize().height*tilescalefactor - 20*tilescalefactor) ) ;

		CCSequence mseq = CCSequence.actions(move, unmove );
		CCRepeatForever mrepeat = CCRepeatForever.action(mseq);
		arrow.runAction(mrepeat);



		String helptext = "1.) Do you know your languages? \n" +
				"2.) You have got 90 seconds! \n"	 +
				"3.) Slash at the flying word that correctly  \ntranslates the green word at top right \n"	+
				"4.) Click Start, Go for it! \n"  ;

		CCBitmapFontAtlas easyFontAtlas = CCBitmapFontAtlas.bitmapFontAtlas( helptext , "dalek.fnt");
		easyFontAtlas.setScale(0.9f*tilescalefactor);
		easyFontAtlas.setPosition(CGPoint.make(screenSize.width/2, screenSize.height/2));
		pauseOverlay.addChild(easyFontAtlas, 3,1);
		//easyFontAtlas.setScale(tilescalefactor);

		CCSprite finger = CCSprite.sprite("finger.png");
		finger.setScale(tilescalefactor);
		finger.setPosition(CGPoint.make(screenSize.width/2 - easyFontAtlas.getContentSize().width*tilescalefactor/4 , screenSize.height/2 + easyFontAtlas.getContentSize().height*tilescalefactor/2)) ;
		pauseOverlay.addChild(finger,3,1) ;


		CCSequence fseq = CCSequence.actions(
				CCFadeIn.action(0.2f),
				//CCMoveTo.action(0.6f, CGPoint.make(screenSize.width/2  - easyFontAtlas.getContentSize().width*tilescalefactor/4, screenSize.height/2 + finger.getContentSize().width*tilescalefactor/2 + easyFontAtlas.getContentSize().height*tilescalefactor/2)), 
				CCMoveTo.action(1.4f, CGPoint.make(screenSize.width/2  + easyFontAtlas.getContentSize().width*tilescalefactor/4, screenSize.height/2 - finger.getContentSize().width*tilescalefactor/2  - easyFontAtlas.getContentSize().height*tilescalefactor/2)),
				CCFadeOut.action(0.8f),
				CCPlace.action( CGPoint.make(screenSize.width/2  - easyFontAtlas.getContentSize().width*tilescalefactor/4, screenSize.height/2 + finger.getContentSize().width*tilescalefactor/2 + easyFontAtlas.getContentSize().height*tilescalefactor/2))

				);
		CCRepeatForever frepeat = CCRepeatForever.action(fseq);
		finger.runAction(frepeat);


		CCMenuItemImage startbtn = CCMenuItemImage.item("startbutton.png", "startbutton.png",this, "startCallback");
		startbtn.setScale(0.8f * tilescalefactor);

		CCMenu startmenu = CCMenu.menu(startbtn); 
		startmenu.setPosition(CGPoint.make(0, 0));
		startbtn.setPosition(CGPoint.make(screenSize.width - startbtn.getContentSize().width/2*tilescalefactor, startbtn.getContentSize().height*tilescalefactor/2));

		pauseOverlay.addChild(startmenu, 3,1);




		gameover = true ;
		score = 0 ;


		streak = new CCMotionStreak(1, 40, "streak.png", 64*tilescalefactor, 64*tilescalefactor, new ccColor4B(255,255,255,255) );
		addChild(streak,400);

		streak.setPosition(screenSize.width/2, screenSize.height/2);

	}

	public void startCallback(Object sender) {
		//SoundEngine.sharedEngine().playEffect(GidiGamesActivity.app, R.raw.cheer);
		time = 90 ; 
		score = 0 ;
		schedule("launchSprite", 0.8f);
		schedule("updateTimeLabel", 1.0f);

		gameover = false ;

		//Add Pause Sprite Button
		CCMenuItemImage pausebtn = CCMenuItemImage.item("pause.png", "pause.png",this, "pauseCallback");
		pausebtn.setScale(0.9f*tilescalefactor);
		CCMenu pausemenu = CCMenu.menu();
		pausemenu.addChild(pausebtn,1,1);
		pausemenu.setContentSize(pausebtn.getContentSize());
		//pausemenu.setScale(tilescalefactor);
		pausemenu.setPosition(CGPoint.make(screenSize.width - pausemenu.getContentSize().width*0.9f*tilescalefactor/2.0f , pausemenu.getContentSize().height*tilescalefactor/2));
		addChild(pausemenu, 100,PAUSE_MENU_TAG);

		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		pauselayer.runAction(CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + pauselayer.getContentSize().height*tilescalefactor)));
		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
	}

	public void pauseCallback(Object sender) {
		unschedule("launchSprite");

		unschedule("updateTimeLabel"); 

		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(false);

		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 255));
		pauseOverlay.setIsTouchEnabled(true);
		pauseOverlay.setOpacity(180);
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);




		//Add scroll to the Layer
		//Add Top Scroll
		CCSprite topscroll = CCSprite.sprite("darktranstop.png");
		float picscale = screenSize.width/topscroll.getContentSize().width ;
		topscroll.setScale(picscale);
		topscroll.setPosition(CGPoint.ccp(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height));
		pauseOverlay.addChild(topscroll,1,SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCDelayTime.action(0.4f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2)), 
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2 + 20 * picscale )) 
				));


		CCBitmapFontAtlas gamepaused = CCBitmapFontAtlas.bitmapFontAtlas ("Game Paused!", "dalek.fnt");
		gamepaused.setColor(ccColor3B.ccc3(105, 75, 41));
		gamepaused.setScale( GidiGamesActivity.scalefactor + 0.4f * GidiGamesActivity.scalefactor);
		gamepaused.setAnchorPoint(1f,1f);
		gamepaused.setPosition(CGPoint.ccp(screenSize.width + gamepaused.getContentSize().width + 40 , screenSize.height -20 ));
		pauseOverlay.addChild(gamepaused,300,GAME_PUASES_LABEL_TAG);
		gamepaused.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width - 20.0f * picscale, screenSize.height - 25 * picscale )) 
				));


		pauseOverlay.addChild(gamepaused,300,GAME_PUASES_LABEL_TAG);

		CCBitmapFontAtlas gamepausedmain = CCBitmapFontAtlas.bitmapFontAtlas ( "Game Paused.", "bionic.fnt");
		gamepausedmain.setAnchorPoint(CGPoint.ccp(0,1));
		//gamemoves.setColor(ccColor3B.ccc3(105, 75, 41));
		gamepausedmain.setScale(tilescalefactor);
		gamepausedmain.setAnchorPoint(0.5f,1f);

		gamepausedmain.setPosition(CGPoint.ccp(screenSize.width / 2.0f , screenSize.height/2.0f ));
		pauseOverlay.addChild(gamepausedmain,300,GAME_PUASES_LABEL_TAG);
		gamepausedmain.runAction(CCSequence.actions(
				CCDelayTime.action(0.1f),
				CCScaleBy.action(0.2f, 2.0f)
				));




		// Add Pause Menu Buttons
		CCMenuItemImage playbtn = CCMenuItemImage.item("play.png", "play.png",this, "playCallback");
		playbtn.setScale(0.9f * tilescalefactor);
		//CCMenuItemImage refreshbtn = CCMenuItemImage.item("refresh.png", "refresh.png",this, "refreshCallback");
		CCMenuItemImage menubtn = CCMenuItemImage.item("menu.png", "menu.png",this, "menuCallback");
		menubtn.setScale(0.9f*tilescalefactor);

		CCMenu resumemenu = CCMenu.menu(playbtn, menubtn); 
		resumemenu.setPosition(CGPoint.make(0, 0));
		menubtn.setPosition(CGPoint.make(screenSize.width - menubtn.getContentSize().width* tilescalefactor /2  , menubtn.getContentSize().height*tilescalefactor/2));
		//refreshbtn.setPosition(CGPoint.make(screenSize.width - refreshbtn.getContentSize().width - menubtn.getContentSize().width - 15, refreshbtn.getContentSize().height));
		playbtn.setPosition(CGPoint.make(screenSize.width -  playbtn.getContentSize().width*tilescalefactor - menubtn.getContentSize().width*tilescalefactor/2.0f -10*tilescalefactor , menubtn.getContentSize().height*tilescalefactor/2));

		//resumemenu.setPosition(CGPoint.make(screenSize.width - resumemenu.getContentSize().width - 50, resumemenu.getContentSize().height));
		pauseOverlay.addChild(resumemenu, 2,RESUME_MENU_TAG) ;

	}



	public void gameOver() {
		unschedule("updateTimeLabel");

		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(false);

		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 255));
		pauseOverlay.setOpacity(200);
		pauseOverlay.setIsTouchEnabled(true); 
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);

		//Log Win in database with moves
		mDbHelper.open();
		mDbHelper.createScoreItem("lexis", GidiGamesActivity.currentlanguage, "", score, thetime) ; //("lexis", "", score, thetime);
		mDbHelper.close() ;

		//Add scroll to the Layer
		//Add Top Scroll
		CCSprite topscroll = CCSprite.sprite("darktranstop.png");
		float picscale = screenSize.width/topscroll.getContentSize().width ;
		topscroll.setScale(picscale);
		topscroll.setPosition(CGPoint.ccp(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height));
		pauseOverlay.addChild(topscroll,1,SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCDelayTime.action(0.4f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2)), 
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2 + 20 * picscale )) 
				));


		String scoretext = "" ;
		if (score > 200){
			scoretext = "Excellent Job! " +score + " Points!!!!";
		}else if (score < 200 && score > 20 ) {
			scoretext = "Fair Try! " +score + " Points!!!!";
		}else if (score < 0 ) {
			scoretext = "Ooooops " +score + " Points! :( ";
			
			emitter = new CCQuadParticleSystem(50);
			emitter.setScale(tilescalefactor);
			emitter.setPosition(CGPoint.ccp(screenSize.width, screenSize.height));
			pauseOverlay.addChild(emitter, 100);
			emitter.setTexture(CCTextureCache.sharedTextureCache().addImage("stars_grayscale.png"));

			// duration
			emitter.setDuration(CCParticleSystem.kCCParticleDurationInfinity);

			// Gravity Mode: gravity
			emitter.setGravity(CGPoint.zero());

			// Set "Gravity" mode (default one)
			emitter.setEmitterMode(CCParticleSystem.kCCParticleModeGravity);

			// Gravity Mode: speed of particles
			emitter.setSpeed(160);
			emitter.setSpeedVar(20);

			// Gravity Mode: radial
			emitter.setRadialAccel(-120);
			emitter.setRadialAccelVar(0);

			// Gravity Mode: tagential
			emitter.setTangentialAccel(30);
			emitter.setTangentialAccelVar(0);

			// angle
			emitter.setAngle(90);
			emitter.setAngleVar(360);

			// emitter position
			emitter.setPosition(CGPoint.ccp(160,240));
			emitter.setPosVar(CGPoint.zero());

			// life of particles
			emitter.setLife(4);
			emitter.setLifeVar(1);

			// spin of particles
			emitter.setStartSpin(0);
			emitter.setStartSpinVar(0);
			emitter.setEndSpin(0);
			emitter.setEndSpinVar(0);

			// color of particles
			ccColor4F startColor = new ccColor4F(0.5f, 0.5f, 0.5f, 1.0f);
			emitter.setStartColor(startColor);

			ccColor4F startColorVar = new ccColor4F(0.5f, 0.5f, 0.5f, 1.0f);
			emitter.setStartColorVar(startColorVar);

			ccColor4F endColor = new ccColor4F(0.1f, 0.1f, 0.1f, 0.2f);
			emitter.setEndColor(endColor);

			ccColor4F endColorVar = new ccColor4F(0.1f, 0.1f, 0.1f, 0.2f);	
			emitter.setEndColorVar(endColorVar);

			// size, in pixels
			emitter.setStartSize(80.0f);
			emitter.setStartSizeVar(40.0f);
			emitter.setEndSize(CCParticleSystem.kCCParticleStartSizeEqualToEndSize);

			// emits per second
			emitter.setEmissionRate( emitter.getTotalParticles()/emitter.getLife());

			// additive
			emitter.setBlendAdditive(true);
		}

		CCBitmapFontAtlas gamepaused = CCBitmapFontAtlas.bitmapFontAtlas (scoretext, "dalek.fnt");
		gamepaused.setColor(ccColor3B.ccc3(105, 75, 41));
		gamepaused.setScale( GidiGamesActivity.scalefactor + 0.4f * GidiGamesActivity.scalefactor);
		gamepaused.setAnchorPoint(1f,1f);
		gamepaused.setPosition(CGPoint.ccp(screenSize.width + gamepaused.getContentSize().width + 40 , screenSize.height -20 ));
		pauseOverlay.addChild(gamepaused,300,GAME_PUASES_LABEL_TAG);
		gamepaused.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width - 20.0f * picscale, screenSize.height - 25 * picscale )) 
				));

		pauseOverlay.addChild(gamepaused,300,GAME_PUASES_LABEL_TAG);



		CCBitmapFontAtlas gamemoves = CCBitmapFontAtlas.bitmapFontAtlas ( CCFormatter.format("%02d", score ) + " Points", "bionic.fnt");
		gamemoves.setAnchorPoint(CGPoint.ccp(0,1));
		//gamemoves.setColor(ccColor3B.ccc3(105, 75, 41));
		gamemoves.setScale(tilescalefactor);
		gamemoves.setAnchorPoint(0.5f,1f);

		gamemoves.setPosition(CGPoint.ccp(screenSize.width / 2.0f , screenSize.height/2.0f ));
		pauseOverlay.addChild(gamemoves,300,GAME_PUASES_LABEL_TAG);
		gamemoves.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f),
				CCScaleBy.action(0.2f, 2.0f)
				));

		CCBitmapFontAtlas instructionFontAtlas = CCBitmapFontAtlas.bitmapFontAtlas( "TAP Menu button below to continue!" , "bionic.fnt");
		instructionFontAtlas.setPosition(gamemoves.getPosition().x, -instructionFontAtlas.getContentSize().height*0.6f*tilescalefactor) ;
		instructionFontAtlas.setScale(0.6f*tilescalefactor) ;
		pauseOverlay.addChild(instructionFontAtlas,301);

		instructionFontAtlas.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f), 
				CCMoveTo.action(0.5f, CGPoint.make(gamemoves.getPosition().x, gamemoves.getPosition().y - 10*tilescalefactor - gamemoves.getContentSize().height *tilescalefactor * 2.0f)) 
				));

		CCBitmapFontAtlas gametime = CCBitmapFontAtlas.bitmapFontAtlas ("Time Up" , "bionic.fnt");
		gametime.setAnchorPoint(CGPoint.ccp(0,1));
		//gamemoves.setColor(ccColor3B.ccc3(105, 75, 41));
		gametime.setScale(tilescalefactor);

		gametime.setAnchorPoint(0.5f,1f);
		gametime.setPosition(CGPoint.ccp(screenSize.width / 2.0f , gamemoves.getPosition().y + gamemoves.getContentSize().height*tilescalefactor/2.0f + 10 ));
		pauseOverlay.addChild(gametime,301);


		thetime = 0 ;

		// Add Pause Menu Buttons
		//CCMenuItemImage playbtn = CCMenuItemImage.item("play.png", "play.png",this, "playCallback");
		//CCMenuItemImage refreshbtn = CCMenuItemImage.item("refresh.png", "refresh.png",this, "refreshCallback");
		CCMenuItemImage menubtn = CCMenuItemImage.item("menu.png", "menu.png",this, "menuCallback");
		menubtn.setScale(0.9f*tilescalefactor);

		CCMenu resumemenu = CCMenu.menu(menubtn); 
		resumemenu.setPosition(CGPoint.make(0, 0));
		menubtn.setPosition(CGPoint.make(screenSize.width - menubtn.getContentSize().width*tilescalefactor/2.0f , menubtn.getContentSize().height*tilescalefactor/2.0f));
		//refreshbtn.setPosition(CGPoint.make(screenSize.width - refreshbtn.getContentSize().width - menubtn.getContentSize().width - 15, refreshbtn.getContentSize().height));
		//cplaybtn.setPosition(CGPoint.make(screenSize.width -  playbtn.getContentSize().width  - 20, playbtn.getContentSize().height));

		//resumemenu.setPosition(CGPoint.make(screenSize.width - resumemenu.getContentSize().width - 50, resumemenu.getContentSize().height));
		pauseOverlay.addChild(resumemenu, 2,RESUME_MENU_TAG) ;

	}

	public void removePauseMenu(Object theSprite) {
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		CCMenu resumemenu = (CCMenu) pauselayer.getChildByTag(RESUME_MENU_TAG);
		resumemenu.setVisible(false);
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(true);

		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
	}
	public void playCallback(Object theSprite){
		schedule("launchSprite", 0.8f);
		schedule("updateTimeLabel", 1.0f); 
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;

		CCBitmapFontAtlas gamepaused = (CCBitmapFontAtlas)  pauselayer.getChildByTag(GAME_PUASES_LABEL_TAG) ;
		gamepaused.runAction(CCMoveTo.action(0.3f, CGPoint.make(screenSize.width + gamepaused.getContentSize().width*tilescalefactor, gamepaused.getPosition().y)));


		CCSprite topscroll = (CCSprite) pauselayer .getChildByTag(SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - topscroll.getContentSize().height*tilescalefactor/2 - 20*tilescalefactor)),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height*tilescalefactor)),
				CCCallFuncN.action(this, "removePauseMenu")
				));


	}

	public void refreshCallback(Object theSprite){
		score = 0 ;
		time = 90 ;
		UpdateScoreLabel (false);
		schedule("launchSprite", 0.8f);
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		CCSprite topscroll = (CCSprite) pauselayer .getChildByTag(SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - topscroll.getContentSize().height/2 - 20)),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height))
				));
		CCMenu resumemenu = (CCMenu) pauselayer.getChildByTag(RESUME_MENU_TAG);
		resumemenu.setVisible(false);
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(true);

		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
	}

	public void menuCallback(Object theSprite){
		thetime = 90 ; 
		CCDirector.sharedDirector().replaceScene(LexisMenuLayer.scene());
	}
	public void updateTimeLabel(float dt) {
		if (time > 0){
			time -= 1;
			String string = CCFormatter.format("%02d:%02d", (int)(time /60) , (int)time  % 60 );
			CCBitmapFontAtlas timerLabel = (CCBitmapFontAtlas) getChildByTag(TIMER_LABEL_TAG) ;

			if(time == 15){
				SoundEngine.sharedEngine().playSound(GidiGamesActivity.app, R.raw.warning,true);
				timerLabel .setColor(ccColor3B.ccRED);
			}
			timerLabel.setString(string);

		}else{
			CCScheduler.sharedScheduler().pause(this);
			SoundEngine.sharedEngine().realesSound(R.raw.warning);
			//unschedule ("spawnSprite");
			CCBitmapFontAtlas timerLabel = (CCBitmapFontAtlas) getChildByTag(TIMER_LABEL_TAG) ;
			timerLabel.stopAllActions();
			timerLabel.setScale(1.0f*tilescalefactor);
			time = 90 ;
			//timerLabel.setColor(ccColor3B.ccc3(50, 205, 50));
			gameOver() ;
		}
	}


	public void wordCallback(CCNodeExt eachNode) {

		//Update Score Label
		//If we have the right word
		if( eachNode.getNodeText().equals(englishtrans.get(currentindex))){

			//Increase Score
			score+= 50 ;
			UpdateScoreLabel(true);

			//Play Word Dedstroyed sound
			SoundEngine.sharedEngine().playEffect(GidiGamesActivity.app, R.raw.word_destroyed);

		}else {

			//Decrease Score
			score-= 10 ;
			UpdateScoreLabel (false);
			//Play Buzz sound
			SoundEngine.sharedEngine().playEffect(GidiGamesActivity.app, R.raw.buzz);

		}

		//eachNode.stopAllActions();
		//CCSequence fallaction = CCSequence.actions(CCMoveTo.action(0.5f, CGPoint.zero()), CCCallFuncN.action(this, "correctWordLanded"));
		//eachNode.runAction(fallaction);
		eachNode.setVisible(false) ;
	}


	public void UpdateScoreLabel (boolean situation){
		CCBitmapFontAtlas scoreLabel = (CCBitmapFontAtlas) getChildByTag(SCORE_LABEL_TAG);
		String scorestring = CCFormatter.format("%03d", score);
		scoreLabel.setString(scorestring);

		if(situation){
			currentindexholder++ ;
			currentindex = englishtransList.get((currentindexholder) % englishtransList.size() );
			currentword = rootwords.get(currentindex)  ;
			CCBitmapFontAtlas mainWordLabel = (CCBitmapFontAtlas) getChildByTag(MAIN_WORD_NODE_LABEL_TAG).getChildByTag(1);
			mainWordLabel.setString(currentword);
		}

	}



	public void launchSprite(float dt) {
		Iterator<Integer> itr = spriteCounter.iterator();
		float splitfactor = 10 * tilescalefactor ;
		screenSize = CCDirector.sharedDirector().winSize();
		String textbuffer ;
		if(itr.hasNext()){
			//Play throwing sound
			SoundEngine.sharedEngine().playEffect(GidiGamesActivity.app, R.raw.throwsprite);

			numSpritePause++ ;
			corrhop ++ ;
			nextindex = itr.next(); 

			spriteCounter.remove(spriteCounter.indexOf(nextindex));
			CCNodeExt eachNode = (CCNodeExt)getChildByTag(nextindex); //spriteList.get(nextindex) ;
			CCBitmapFontAtlas eachItem = (CCBitmapFontAtlas) eachNode.getChildByTag(1) ;

			if (corrhop == maxcorrhop){
				eachItem.setString(englishtrans.get(currentindex));
				eachNode.setNodeText(englishtrans.get(currentindex)) ;
				eachNode.setNodeThrowTag(currentindex);
				corrhop = 0 ;
				maxcorrhop = 3 + (int)(Math.random()* 5)  ;
				currentthrowindex = (currentthrowindex + 1) % throwindexes.size() ;
			}
			else{
				cursel = throwindexes.get(currentthrowindex);
				while (cursel == currentindex){
					currentthrowindex = (currentthrowindex + 1) % throwindexes.size() ;
					cursel = throwindexes.get(currentthrowindex);
				}

				throwindexes.remove(currentthrowindex);
				eachNode.setNodeThrowTag(cursel);
				textbuffer = englishtrans.get(cursel) ;
				eachItem.setString(textbuffer);
				eachNode.setNodeText(textbuffer) ;
			}



			eachNode.setScale(1.5f );

			if(throwposition){
				startx = (int) (eachItem.getContentSize().width * tilescalefactor ) ;
				endx = (int) (screenSize.width - eachItem.getContentSize().width * tilescalefactor - screenSize.width/splitfactor) ;
				throwposition = false ;
			}else{
				startx =(int) (screenSize.width - eachItem.getContentSize().width  * tilescalefactor - screenSize.width/splitfactor) ;
				endx = (int) (eachItem.getContentSize().width * tilescalefactor + screenSize.width/splitfactor) ;
				throwposition = true ;
			}
			jumpheight = (int) (0.8 * screenSize.height  + (ccMacros.CCRANDOM_0_1() * (screenSize.height - (0.8*screenSize.height) - (eachNode.getContentSize().height * tilescalefactor))));

			Random rand =  new Random();


			eachNode.setPosition(CGPoint.make(startx, (eachNode.getContentSize().height * tilescalefactor / 2)* -1 ));
			CCSequence jumpaction = CCSequence.actions(
					CCJumpTo.action(2.5f, CGPoint.make(endx, -1 * eachNode.getContentSize().height ), jumpheight , 1),
					CCDelayTime.action(0.4f + (rand.nextFloat() * 1.6f) ),
					CCCallFuncN.action(this, "destroySprite")
					);
			//jumpaction.setTag(JUMP_ACTION_TAG);
			eachNode.resumeSchedulerAndActions();
			eachNode.runAction(jumpaction);


		}


	}

	public void destroySprite(Object theSprite) {
		CCNodeExt sprite = (CCNodeExt)theSprite;
		sprite.setVisible(true);
		spriteCounter.add(sprite.getTag()) ;
		throwindexes.add(sprite.getNodeThrowTag());

	}


	@Override
	public boolean ccTouchesMoved(MotionEvent event)
	{
		CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
		CGRect spritePos ; 
		for (int i = 0 ; i < MAX_SPRITE_POOL; i++){
			CCNodeExt eachNode = (CCNodeExt)getChildByTag(i);
			spritePos = CGRect.make(eachNode.getPosition().x - (eachNode.getContentSize().width/2.0f),
					eachNode.getPosition().y - (eachNode.getContentSize().height/2.0f),
					eachNode.getContentSize().width   ,
					eachNode.getContentSize().height   );

			if(spritePos.contains(location.x, location.y)){
				ccMacros.CCLOG("Touched Node", "touched : " + eachNode.getNodeText());
				wordCallback(eachNode);
			}
		}
		/*ccMacros.CCLOG("Touch Pos"," " + location.x + "  " + location.y);
		ccMacros.CCLOG("Sprite-1 Pos", "Sprite-1 Pos " + getChildByTag(0).getPosition().x + "  " + getChildByTag(0).getPosition().y);
		ccMacros.CCLOG("Sprite-2 Pos", "Sprite-2 Pos " + getChildByTag(1).getPosition().x + "  " + getChildByTag(1).getPosition().y);
		ccMacros.CCLOG("Sprite-3 Pos", "Sprite-3 Pos " + getChildByTag(2).getPosition().x + "  " + getChildByTag(2).getPosition().y);
		ccMacros.CCLOG("Sprite-4 Pos", "Sprite-4 Pos " + getChildByTag(3).getPosition().x + "  " + getChildByTag(3).getPosition().y);
		 */	
		streak.setPosition(location);
		return true ;
	}

	public void wordTouchedCallback(CCNodeExt touchedNode){

	}
}



