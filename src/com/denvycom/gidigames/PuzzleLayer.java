package com.denvycom.gidigames;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList; 
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCScaleBy;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.config.ccMacros;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.menus.CCMenuItemLabel;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.opengl.CCTexture2D;
import org.cocos2d.opengl.CCTextureAtlas;
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
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.MotionEvent;





public class PuzzleLayer extends CCLayer {

	CCTextureAtlas textureAtlas;
	private int toppoint = 0 ;
	private CCBitmapFontAtlas bestScoreLabel;
	private int topleft = 0  ;


	public static boolean gameover = false ;
	public static  ArrayList<CCNodeExt> spriteList = new ArrayList<CCNodeExt>();
	public static  int[] tileNumbers ;  
	public static  int  tileIndex ;  
	private static CGPoint emptyPosition  ;
	private static CGSize screenSize;
	private static int TILE_SQUARE_SIZE = 150  ;
	private static int NUM_ROWS = 0 ;
	private static int NUM_COLUMNS = 0 ;
	private static Context appcontext ;
	private static int thetime = 0 ;
	private static int moves = 0 ;
	private static String PUZZLE_TYPE = "";
	float scalefactor ;

	private static int BEST_SCORE_LABEL_TAG = 102 ;
	private static int TIMER_LABEL_TAG = 103 ;
	private static int TILE_NODE_TAG = 105 ;
	private static int MOVES_LABEL_TAG = 106 ;
	private static final int GAME_PUASES_LABEL_TAG = 107;
	private static int PAUSE_OVERLAY_TAG = 322 ;
	private static int SCROLL_TOP_TAG = 321 ;
	private static int PAUSE_MENU_TAG = 348;
	private static int HINT_MENU_TAG = 338;
	private static int RESUME_MENU_TAG = 358;
	String[] letterbuffer = {"", "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	DbAdapter mDbHelper ;
	private float tilescalefactor = 0.0f;
	private int useableheight = 0;
	private CCBitmapFontAtlas movesLabel;
	private int completed = 0;
	private CCBitmapFontAtlas primerLabel;
	private CCQuadParticleSystem emitter;
	Bitmap mybit ;
	private BitmapFactory myfact;

	public static CCScene scene(int numrows, int numcolumns, String type)
	{ 
		GidiGamesActivity.nextscene= "PuzzleNumLayer"; 
		GidiGamesActivity.currentscene = "PuzzleLayer" ;
		if (PUZZLE_TYPE.equals("picture")){
			GidiGamesActivity.nextscene= "PuzzlePicLayer"; 
		}
		
		NUM_ROWS = numrows ;
		PUZZLE_TYPE = type ;
		NUM_COLUMNS = numcolumns ;
		CCScene scene = CCScene.node();
		CCLayer layer = new PuzzleLayer();
		scene.addChild(layer);

		return scene;
	}

	public PuzzleLayer() {
		super();
		this.setIsTouchEnabled(true); 

		appcontext = CCDirector.sharedDirector().getActivity();

       GidiGamesActivity.checkWordBank();

		mDbHelper = new DbAdapter(appcontext);
		mDbHelper.open();
		String labelmoves; 

		Cursor ScoreCursor =   mDbHelper.fetchPuzzleBest("puzzlemania", GidiGamesActivity.currentpuzzletype,String.valueOf(NUM_ROWS)); // mDbHelper.fetchBestScore("puzzlemania", "");
		if(ScoreCursor.getCount() > 0){
			ScoreCursor.moveToFirst();
			labelmoves = ScoreCursor.getString(ScoreCursor.getColumnIndex(
					DbAdapter.KEY_GAME_MOVES)) ;
		}else{
			labelmoves = "0" ;
		}		

		mDbHelper.close();

		gameover = false ;

		screenSize = CCDirector.sharedDirector().winSize();
		tilescalefactor   = (screenSize.height/480) ;
		TILE_SQUARE_SIZE = (int) (TILE_SQUARE_SIZE * tilescalefactor) ;

		//Add Background Sprite Image
		CCSprite background = CCSprite.sprite("background.jpg");
		background.setScale(screenSize.width / background.getContentSize().width);
		background.setAnchorPoint(CGPoint.ccp(0f,1f)) ;
		background.setPosition(CGPoint.ccp(0, screenSize.height));
		addChild(background,-5);


		// Add Timer Label
		CCBitmapFontAtlas timerLabel = CCBitmapFontAtlas.bitmapFontAtlas ("00:00", "bionic.fnt");
		timerLabel.setScale(1.5f*tilescalefactor);
		timerLabel.setAnchorPoint(1f,1f);
		timerLabel.setColor(ccColor3B.ccc3(50, 205, 50));
		timerLabel.setPosition(CGPoint.ccp(screenSize.width - 25*tilescalefactor , screenSize.height - 10*tilescalefactor ));
		addChild(timerLabel,-2,TIMER_LABEL_TAG);

		// Add Moves Label
		movesLabel = CCBitmapFontAtlas.bitmapFontAtlas ("Moves : 000", "bionic.fnt");
		movesLabel.setScale(0.8f*tilescalefactor);
		movesLabel.setAnchorPoint(1f,0f);
		movesLabel.setColor(ccColor3B.ccc3(50, 205, 50));
		movesLabel.setPosition(CGPoint.ccp(screenSize.width - 25*tilescalefactor, timerLabel.getPosition().y - timerLabel.getContentSize().height*tilescalefactor - 10*tilescalefactor -  timerLabel.getContentSize().height*tilescalefactor));
		addChild(movesLabel,-2,MOVES_LABEL_TAG);

		//Add Best Label

		bestScoreLabel = CCBitmapFontAtlas.bitmapFontAtlas ("BEST : " + labelmoves  ,  "bionic.fnt");
		bestScoreLabel.setAnchorPoint(1f,0f);
		bestScoreLabel.setScale(0.8f*tilescalefactor);		
		bestScoreLabel.setColor(ccColor3B.ccc3(255, 242, 75));
		bestScoreLabel.setPosition( CGPoint.ccp(screenSize.width - 25*tilescalefactor, movesLabel.getPosition().y - movesLabel.getContentSize().height*tilescalefactor  ));
		addChild(bestScoreLabel,-2, BEST_SCORE_LABEL_TAG);


		primerLabel = CCBitmapFontAtlas.bitmapFontAtlas ("    Great job!    \nRow " + completed + " Completed!", "bionic.fnt");
		primerLabel.setScale(0.9f*tilescalefactor); 
		primerLabel.setAnchorPoint(1f,1f);
		primerLabel.setColor(ccColor3B.ccc3(255, 242, 75));
		primerLabel.setPosition(CGPoint.ccp(screenSize.width + 2* primerLabel.getContentSize().width * tilescalefactor , bestScoreLabel.getPosition().y  - bestScoreLabel.getContentSize().height*tilescalefactor/2  ));
		addChild(primerLabel,25,900);




		//Add Overlay with Start button and Instruction
		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 0));
		pauseOverlay.setOpacity(180);
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);

		String helptext = "1.) Tap OR drag each tile to move it \n"	 +
				"2.) Rearrange the tiles in increasing numeric order  \n     left to right (e.g Row 1 = 123, Row 2 = 456 \n"	+
				"3.) Win by arranging all numbers correctly!! \n" +
				"4.) Empty space should be at bottom right! Go!"	;

		CCBitmapFontAtlas easyFontAtlas = CCBitmapFontAtlas.bitmapFontAtlas( helptext , "dalek.fnt");
		easyFontAtlas.setScale(0.9f*tilescalefactor);
		easyFontAtlas.setPosition(CGPoint.make(screenSize.width/2, screenSize.height/2));
		pauseOverlay.addChild(easyFontAtlas, 3,1);



		CCMenuItemImage startbtn = CCMenuItemImage.item("startbutton.png", "startbutton.png",this, "startCallback");
		startbtn.setScale(0.8f * tilescalefactor);

		CCMenu startmenu = CCMenu.menu(startbtn); 
		startmenu.setPosition(CGPoint.make(0, 0));
		startbtn.setPosition(CGPoint.make(screenSize.width - startbtn.getContentSize().width/2*tilescalefactor, startbtn.getContentSize().height*tilescalefactor/2));

		pauseOverlay.addChild(startmenu, 3,1);

		/*CCBitmapFontAtlas instructionFontAtlas = CCBitmapFontAtlas.bitmapFontAtlas( "TAP tiles (NOT DRAG) to arrange them! \n     HAVE FUN and MIND THE TIME!" , "dalek.fnt");
		instructionFontAtlas.setPosition(menu.getPosition().x, menu.getPosition().y -25 - menu.getContentSize().height) ;
		instructionFontAtlas.setScale(0.6f) ;
		pauseOverlay.addChild(instructionFontAtlas,3,1);*/

		gameover = true ;



		generateTiles();
		appcontext = CCDirector.sharedDirector().getActivity();


		/*timer.scheduleAtFixedRate( new TimerTask(){
			public void run() {
				updateTimeLabel();
			}
		}, 0, 1000 );*/


	}

	public void startCallback(Object sender) {
		//SoundEngine.sharedEngine().playEffect(appcontext, R.raw.cheer);
		thetime = 0 ;
		moves = 0 ;
		schedule("updateTimeLabel", 1.0f);
		gameover = false ;

		//Add Pause Sprite Button
		CCMenuItemImage pausebtn = CCMenuItemImage.item("pause.png", "pause.png",this, "pauseCallback");
		pausebtn.setScale(0.9f*tilescalefactor);
		CCMenu pausemenu = CCMenu.menu();
		pausemenu.addChild(pausebtn,1,1);
		pausemenu.setContentSize(pausebtn.getContentSize()); 
		pausemenu.setPosition(CGPoint.make(screenSize.width - pausemenu.getContentSize().width*0.9f*tilescalefactor/2.0f , pausemenu.getContentSize().height*tilescalefactor/2));
		addChild(pausemenu, 100,PAUSE_MENU_TAG);

		//Add Hint button if Picuter puzxle
		if (PUZZLE_TYPE.equals("picture")){
			CCMenuItemImage hintbtn = CCMenuItemImage.item("hint.png", "pause.png",this, "hintCallback");
			hintbtn.setScale(0.9f*tilescalefactor);
			CCMenu hintmenu = CCMenu.menu();
			hintmenu.addChild(hintbtn,1,1);
			hintmenu.setContentSize(hintbtn.getContentSize()); 
			hintmenu.setPosition(CGPoint.make(screenSize.width - hintmenu.getContentSize().width*0.9f*tilescalefactor/2.0f - 10*tilescalefactor - hintmenu.getContentSize().width*0.9f*tilescalefactor , hintmenu.getContentSize().height*tilescalefactor/2));
			addChild(hintmenu, 100,HINT_MENU_TAG);
			
		}
		
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		pauselayer.runAction(CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + pauselayer.getContentSize().height*tilescalefactor)));
		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
	}

	public void generateTiles(){
		//tileNumbers ;
		tileIndex = 0 ;
		//Generate the random but solvable number series
		tileNumbers = Utility.byteArrayToIntArray(Utility.getRandomArray((NUM_ROWS * NUM_COLUMNS), false));			//Create 12 Boxes and arrange ont the screenMenu Items and add to layer
		int nextval ;

		CCNode tilesNode = CCNode.node();
		tilesNode.setTag(TILE_NODE_TAG);
		addChild(tilesNode);


		//TILE_SQUARE_SIZE = (int) ((screenSize.height  *tilescalefactor)/NUM_ROWS) ;
		int useablewidth = (int) (screenSize.width - movesLabel.getContentSize().width*tilescalefactor ) ;
		useableheight =  (int) (screenSize.height  - 30*tilescalefactor) ;

		TILE_SQUARE_SIZE = (int) Math.min((useableheight/NUM_ROWS) , (useablewidth/NUM_COLUMNS)) ;

		toppoint = (int) (useableheight  - (TILE_SQUARE_SIZE / 2) + 15*tilescalefactor)   ;
		scalefactor = TILE_SQUARE_SIZE / 150.0f ;

		topleft = (int) ((TILE_SQUARE_SIZE / 2) + 15*tilescalefactor) ;

		CCSprite tile = CCSprite.sprite("tictile.png");
		CCSprite tilebox = CCSprite.sprite("tilebox.png");
		
		for (int j = toppoint ; j > toppoint - (TILE_SQUARE_SIZE * NUM_ROWS); j-= TILE_SQUARE_SIZE){
			for (int i = topleft ; i < (topleft - 5*tilescalefactor) + (TILE_SQUARE_SIZE * NUM_COLUMNS); i+= TILE_SQUARE_SIZE){

				if (tileIndex >= (NUM_ROWS * NUM_COLUMNS)) {
					break ;
				}
				nextval = tileNumbers[tileIndex ];
				CCNodeExt eachNode =  new  CCNodeExt(); 
				eachNode.setContentSize(tile.getContentSize());
				//
				
				eachNode.setPosition(i, j);
				eachNode.setNodeText(nextval + "");



				//Add Tile number
				CCBitmapFontAtlas tileNumber = CCBitmapFontAtlas.bitmapFontAtlas ("00", "bionic.fnt");
				tileNumber.setScale(1.4f);
				tileNumber.setColor(ccColor3B.ccc3(102, 85, 46));

				if (PUZZLE_TYPE.equals("number")){
					eachNode.setScale(scalefactor);
					eachNode.addChild(tile,1,1);
					tileNumber.setString(nextval + ""); 
					eachNode.addChild(tileNumber,2 );
				}else if (PUZZLE_TYPE.equals("letter")){
					eachNode.setScale(scalefactor);
					eachNode.addChild(tile,1,1);
					tileNumber.setString(letterbuffer[nextval]);  ; 
					eachNode.addChild(tileNumber,2 );
				}else if (PUZZLE_TYPE.equals("picture")){ 
					tileNumber.setScale(TILE_SQUARE_SIZE * tilescalefactor/tilebox.getContentSize().height);
					tilebox.setScale(TILE_SQUARE_SIZE/tilebox.getContentSize().height);
					tileNumber.setString(nextval + ""); 
					//eachNode.addChild(tileNumber,2 );
					eachNode.addChild(tilebox,1,1);
				}


				if( nextval != 0){
					tilesNode.addChild(eachNode,1,nextval);
				}else {
					emptyPosition = CGPoint.ccp(i, j);
				}

				//Add each Node to a HashMap to note its location
				tileIndex++;
			}
		} 

		//Add Picture Spites if picture puzzle
		if (PUZZLE_TYPE.equals("picture")){

			CCNode tileNode = (CCNode) getChildByTag(TILE_NODE_TAG);
			int nodeindex = 1 ;
			boolean result = false;


			CCTexture2D metexture = new CCTexture2D();
			Bitmap mybit = null;
			try {
				mybit = getBitmapFromAsset(GidiGamesActivity.currentpic);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			 
			metexture.initWithImage(mybit);


			int rowindex = 0 ;
			for (float j = 0 ; j < NUM_ROWS ; j++){
				for (float i = 0 ; i <NUM_COLUMNS; i++){

					float theblock = Math.min( mybit.getHeight()/NUM_ROWS, mybit.getWidth()/NUM_COLUMNS) ;

					CCSpriteFrame myframe = CCSpriteFrame.frame(metexture, CGRect.make(i*theblock, j*theblock, theblock, theblock), CGPoint.make(0, 0));
					tile = CCSprite.sprite(myframe);
					tile.setScale(TILE_SQUARE_SIZE/theblock);

					tileNode.getChildByTag(nodeindex).addChild(tile,-1,1);
					tileNode.setContentSize(tile.getContentSize());

					if(nodeindex == (NUM_ROWS * NUM_COLUMNS) - 1){
						break ;
					} 
					nodeindex++ ;
				}



			}
		}

	}
	

	 /**
    * Helper Functions
    * @throws IOException 
    */
   private Bitmap getBitmapFromAsset(String strName) throws IOException
   {
       AssetManager assetManager = CCDirector.sharedDirector().getActivity().getAssets();

       InputStream istr = assetManager.open(strName);
       Bitmap bitmap = BitmapFactory.decodeStream(istr);

       return bitmap;
   }

   
	public void updateTimeLabel(float dt) {
		thetime += 1;
		String string = CCFormatter.format("%02d:%02d", (int)(thetime /60) , (int)thetime % 60 );
		CCBitmapFontAtlas timerLabel = (CCBitmapFontAtlas) getChildByTag(TIMER_LABEL_TAG) ;
		timerLabel.setString(string);

	}

	/*Class Slides the tile. 
	 * Params : theNode to be slidded
	 * Boolean to move tile or not.
	 * */
	public void slideTile(String direction, CCNodeExt thenode, boolean move){ 
		CCBitmapFontAtlas moveslabel = (CCBitmapFontAtlas) getChildByTag(MOVES_LABEL_TAG);

		if(move && !gameover){ // Without this if .. the game becomes crazy .. tiles can be lifted from board .. possible cheatmode
			moves++ ;
			moveslabel.setString("Moves : " + CCFormatter.format("%03d", moves ));
			CGPoint nodePosition = thenode.getPosition();
			CGPoint tempPosition = emptyPosition ;
			CCMoveTo movetile = CCMoveTo.action(0.4f, tempPosition); 
			CCSequence movetileSeq = CCSequence.actions(movetile, CCCallFuncN.action(this, "handleWin"));
			thenode.runAction(movetileSeq);
			emptyPosition = nodePosition ;
			SoundEngine.sharedEngine().playEffect(appcontext, R.raw.tileclick);
			thenode.runAction(movetileSeq); 
		}else{ 
		}




	}



	public void slideCallback(CCNodeExt thenode) {

		CGPoint nodePosition = thenode.getPosition();

		if((nodePosition.x - TILE_SQUARE_SIZE)== emptyPosition.x && nodePosition.y == emptyPosition.y){
			slideTile("Left", thenode,true);
		}else if((nodePosition.x + TILE_SQUARE_SIZE) == emptyPosition.x && nodePosition.y == emptyPosition.y){
			slideTile("Right", thenode,true);
		}else if((nodePosition.x)== emptyPosition.x && nodePosition.y == (emptyPosition.y  + TILE_SQUARE_SIZE )){
			slideTile("Down", thenode,true);
		}else if((nodePosition.x )== emptyPosition.x && nodePosition.y == (emptyPosition.y  - TILE_SQUARE_SIZE)){ 
			slideTile("Up", thenode,true);
		}else{
			slideTile("Unmovable", thenode,false);
		}

	}

	public void handleWin(Object sender){
		if(checkCorrect()){
			gameover = true ;

			SoundEngine.sharedEngine().playEffect(appcontext, R.raw.cheer);

			WinCallback(sender);
		}
	}
	public boolean checkCorrect(){
		CCNode tileNode = (CCNode) getChildByTag(TILE_NODE_TAG);
		int nodeindex = 1 ;
		boolean result = false;

		int rowindex = 0 ;
		for (float j = toppoint ; j > toppoint - (TILE_SQUARE_SIZE * NUM_ROWS); j-= TILE_SQUARE_SIZE){
			for (float i = topleft ; i < (topleft - 5) + (TILE_SQUARE_SIZE * NUM_COLUMNS); i+= TILE_SQUARE_SIZE){
				if(tileNode.getChildByTag(nodeindex).getPosition().x == i && tileNode.getChildByTag(nodeindex).getPosition().y == j ){
					result = true ; 
				}else{ 
					return false ;
				}
				nodeindex++ ;
				if(nodeindex == (NUM_ROWS * NUM_COLUMNS)){
					return result ;
				}
			}

			rowindex++ ;
			if (result && rowindex == (completed + 1) && rowindex != NUM_ROWS){
				completed  =  rowindex ;
				/*Log.w("Level up ", " ----- Row   Complete! - " + rowindex + " Completed " + completed );
				ccMacros.CCLOG("Level up ", " ----- Row   Complete! - " + rowindex  );
				 */

				CCSequence theseq = CCSequence.actions(CCMoveTo.action(0.2f, CGPoint.ccp(screenSize.width - 10*tilescalefactor , bestScoreLabel.getPosition().y  - bestScoreLabel.getContentSize().height*tilescalefactor/2)), 
						CCDelayTime.action(4f),
						CCMoveTo.action(0.2f, CGPoint.ccp(screenSize.width  + 2 * primerLabel.getContentSize().width * tilescalefactor  , bestScoreLabel.getPosition().y  - bestScoreLabel.getContentSize().height*tilescalefactor/2)));


				/*primerLabel.setPosition(CGPoint.ccp(screenSize.width/2 , screenSize.height/2 ));

				CCSequence theseq = CCSequence.actions(CCScaleBy.action(0.15f, 0.4f*tilescalefactor ), 
						CCDelayTime.action(2f),
						CCScaleBy.action(0.15f, 3.0f*tilescalefactor ),
						CCFadeOut.action(0.2f));*/

				primerLabel.setString("Excellent!   \nRow " + completed + " Completed!" ) ;
				primerLabel.runAction(theseq) ;





			}
		}

		rowindex = 0 ;
		return result ;
	}

	public void hintCallback(Object sender) {

		unschedule("updateTimeLabel"); 
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(false);
		
		CCMenu hintmenu = (CCMenu) getChildByTag(HINT_MENU_TAG);
		hintmenu.setVisible(false);

		 
		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 255));
		pauseOverlay.setIsTouchEnabled(true);
		pauseOverlay.setOpacity(180);
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);
  
		Bitmap mybit = null;
		try {
			mybit = getBitmapFromAsset(GidiGamesActivity.currentpic);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		 

		CCSprite tilebox = CCSprite.sprite("tilebox.png");
		
		
		CCSprite hintsprite = CCSprite.sprite(mybit, "bit");
		float hintspritescalefact = (screenSize.height - (30 * tilescalefactor))/ hintsprite.getContentSize().getHeight() ;
		hintsprite.setScale(hintspritescalefact) ;
		hintsprite.setAnchorPoint(0f,1f);
		hintsprite.setPosition(CGPoint.ccp(15*tilescalefactor, screenSize.height - 15*tilescalefactor)) ;
		
		tilebox.setScale(hintsprite.getContentSize().height * hintspritescalefact/tilebox.getContentSize().height) ;
		//tilebox.setScale((screenSize.height - 10 * tilescalefactor)/ hintsprite.getContentSize().getHeight()) ;
		tilebox.setPosition(CGPoint.ccp(15*tilescalefactor, screenSize.height - 15*tilescalefactor)) ;
		tilebox.setAnchorPoint(0f,1f);
		
		pauseOverlay.addChild(tilebox,2) ;
		pauseOverlay.addChild(hintsprite,1);

 
		// Add Pause Menu Buttons
		CCMenuItemImage playbtn = CCMenuItemImage.item("play.png", "play.png",this, "hintplayCallback");
		playbtn.setScale(0.9f * tilescalefactor);
	 

		CCMenu resumemenu = CCMenu.menu(playbtn); 
		resumemenu.setPosition(CGPoint.make(0, 0)); 
		playbtn.setPosition(CGPoint.make(screenSize.width - playbtn.getContentSize().width* tilescalefactor /2  , playbtn.getContentSize().height*tilescalefactor/2));
		pauseOverlay.addChild(resumemenu, 2,RESUME_MENU_TAG) ;

	}

	public void pauseCallback(Object sender) {

		unschedule("updateTimeLabel"); 

		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(false);

		if (PUZZLE_TYPE.equals("picture")){
			CCMenu hintmenu = (CCMenu) getChildByTag(HINT_MENU_TAG);
			hintmenu.setVisible(false);
		}
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


	public void WinCallback(Object sender) {
		unschedule("updateTimeLabel");

		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(false);
		
		if (PUZZLE_TYPE.equals("picture")){
			CCMenu hintmenu = (CCMenu) getChildByTag(HINT_MENU_TAG);
			hintmenu.setVisible(false);
		}

		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 255));
		pauseOverlay.setOpacity(200);
		pauseOverlay.setIsTouchEnabled(true); 
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);

		//Log Win in database with moves
		//mDbHelper.createItem("puzzlemania", "", String.valueOf(moves), thetime);
		mDbHelper.open();
		mDbHelper.createScoreItem("puzzlemania", GidiGamesActivity.currentpuzzletype, String.valueOf(NUM_ROWS), moves, thetime) ;
        mDbHelper.close();
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

		CCBitmapFontAtlas gamepaused = CCBitmapFontAtlas.bitmapFontAtlas ("Congratulations! You WIN", "dalek.fnt");
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



		CCBitmapFontAtlas gamemoves = CCBitmapFontAtlas.bitmapFontAtlas ( CCFormatter.format("%02d", moves ) + " Moves", "bionic.fnt");
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


		CCBitmapFontAtlas gametime = CCBitmapFontAtlas.bitmapFontAtlas (CCFormatter.format("%02d:%02d", (int)(thetime /60) , (int)thetime % 60 ), "bionic.fnt");
		gametime.setAnchorPoint(CGPoint.ccp(0,1));
		//gamemoves.setColor(ccColor3B.ccc3(105, 75, 41));
		gametime.setScale(tilescalefactor);

		gametime.setAnchorPoint(0.5f,1f);
		gametime.setPosition(CGPoint.ccp(screenSize.width / 2.0f , gamemoves.getPosition().y + gamemoves.getContentSize().height*tilescalefactor/2.0f + 10 ));
		pauseOverlay.addChild(gametime,301);


		thetime = 0 ;

		CCMenuItemImage menubtn = CCMenuItemImage.item("menu.png", "menu.png",this, "menuCallback");
		menubtn.setScale(0.9f*tilescalefactor);

		CCMenu resumemenu = CCMenu.menu(menubtn); 
		resumemenu.setPosition(CGPoint.make(0, 0));
		menubtn.setPosition(CGPoint.make(screenSize.width - menubtn.getContentSize().width*tilescalefactor/2.0f , menubtn.getContentSize().height*tilescalefactor/2.0f));
		//refreshbtn.setPosition(CGPoint.make(screenSize.width - refreshbtn.getContentSize().width - menubtn.getContentSize().width - 15, refreshbtn.getContentSize().height));
		//cplaybtn.setPosition(CGPoint.make(screenSize.width -  playbtn.getContentSize().width  - 20, playbtn.getContentSize().height));

		//resumemenu.setPosition(CGPoint.make(screenSize.width - resumemenu.getContentSize().width - 50, resumemenu.getContentSize().height));
		pauseOverlay.addChild(resumemenu, 2,RESUME_MENU_TAG) ;

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


	public void removePauseMenu(Object theSprite) {
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		CCMenu resumemenu = (CCMenu) pauselayer.getChildByTag(RESUME_MENU_TAG);
		resumemenu.setVisible(false);
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(true);

		pauselayer.runAction(CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + pauselayer.getContentSize().height)));

		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
	}
	
	public void playCallback(Object theSprite){
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
	
	public void hintplayCallback(Object theSprite){
		schedule("updateTimeLabel", 1.0f); 
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		CCMenu resumemenu = (CCMenu) pauselayer.getChildByTag(RESUME_MENU_TAG);
		resumemenu.setVisible(false);
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(true);

		CCMenu hintmenu = (CCMenu) getChildByTag(HINT_MENU_TAG);
		hintmenu.setVisible(true);

		pauselayer.runAction(CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + pauselayer.getContentSize().height)));

		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;

	}

	public void loadPuzzleMenu(Object theSprite) {
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		CCMenu resumemenu = (CCMenu) pauselayer.getChildByTag(RESUME_MENU_TAG);
		resumemenu.setVisible(false);
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(true);

		pauselayer.runAction(CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + pauselayer.getContentSize().height*tilescalefactor)));

		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
		CCDirector.sharedDirector().replaceScene(PuzzleMenuLayer.scene());
	}
	public void menuCallback(Object theSprite){
		unschedule("updateTimeLabel");
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;

		CCBitmapFontAtlas gamepaused = (CCBitmapFontAtlas)  pauselayer.getChildByTag(GAME_PUASES_LABEL_TAG) ;
		gamepaused.runAction(CCMoveTo.action(0.3f, CGPoint.make(screenSize.width + gamepaused.getContentSize().width*tilescalefactor, gamepaused.getPosition().y)));


		CCSprite topscroll = (CCSprite) pauselayer .getChildByTag(SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - topscroll.getContentSize().height*tilescalefactor/2 - 20*tilescalefactor)),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height*tilescalefactor)),
				CCCallFuncN.action(this, "loadPuzzleMenu")
				));


	}

	public void refreshCallback(Object theSprite){
		thetime = 0 ;
		//UpdateScoreLabel (false);
		//schedule("launchSprite", 0.8f);
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		CCSprite topscroll = (CCSprite) pauselayer .getChildByTag(SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - topscroll.getContentSize().height*tilescalefactor/2 - 20*tilescalefactor)),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height*tilescalefactor))
				));
		CCMenu resumemenu = (CCMenu) pauselayer.getChildByTag(RESUME_MENU_TAG);
		resumemenu.setVisible(false);
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(true);

		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
	}

	/*@Override
	 public boolean ccTouchesBegan(MotionEvent event) {
		CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
		CGRect spritePos ;

		CCNode tilesNode = (CCNode) getChildByTag(TILE_NODE_TAG) ;


		for (int i = 1 ; i < (NUM_COLUMNS * NUM_ROWS); i++){
			CCNodeExt eachNode = (CCNodeExt) tilesNode.getChildByTag(i) ;
			spritePos = CGRect.make(
					eachNode.getPosition().x - (eachNode.getContentSize().width/2.0f),
                    eachNode.getPosition().y - (eachNode.getContentSize().height/2.0f),
                    eachNode.getContentSize().width   ,
                    eachNode.getContentSize().height   );

			//ccMacros.CCLOG("Touched Node", "touched : " + location.x + " :  " + eachNode.getPosition().x);

			if(spritePos.contains(location.x, location.y)){
				ccMacros.CCLOG("Touched Node", "touched : " + eachNode.getNodeText());
				 slideCallback(eachNode);
			}
		}

       return true ;  // TODO Auto-generated method stub
   }*/

	@Override
	public boolean ccTouchesBegan(MotionEvent event)
	{
		CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
		CGRect spritePos ;

		CCNode tilesNode = (CCNode) getChildByTag(TILE_NODE_TAG) ;
		//ccMacros.CCLOG("Began", "Began : " + location.x + " :  "  );

		for (int i = 1 ; i < (NUM_ROWS * NUM_COLUMNS); i++){
			CCNodeExt eachNode = (CCNodeExt) tilesNode.getChildByTag(i) ;
			spritePos = CGRect.make(
					eachNode.getPosition().x - (eachNode.getContentSize().width*scalefactor/2.0f),
					eachNode.getPosition().y - (eachNode.getContentSize().height*scalefactor/2.0f),
					eachNode.getContentSize().width*scalefactor   ,
					eachNode.getContentSize().height*scalefactor   );



			if(spritePos.contains(location.x, location.y)){
				//ccMacros.CCLOG("Began Touched Node", "Began touched : " + eachNode.getNodeText());
				slideCallback(eachNode);

			}
		}

		return true ;
	}

	@Override
	public boolean ccTouchesEnded(MotionEvent event)
	{
		CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
		CGRect spritePos ; 

		ccMacros.CCLOG("Ended"," " + location.x + "  " + location.y);


		return true ;
	}




}



